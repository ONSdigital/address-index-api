package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}

import com.sksamuel.elastic4s.ElasticDsl._
import play.api.Logger
import play.api.mvc.{Action, AnyContent}

import scala.concurrent.{Await, ExecutionContext, Future}
import uk.gov.ons.addressIndex.model.db.index.{HybridAddress, HybridAddresses}
import com.sksamuel.elastic4s.ElasticDsl._
import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.{BulkBody, BulkItem, BulkResp}
import uk.gov.ons.addressIndex.model.db.{BulkAddress, BulkAddresses, RejectedRequest}
import uk.gov.ons.addressIndex.server.modules._
import uk.gov.ons.addressIndex.model.server.response._
import uk.gov.ons.addressIndex.parsers.Implicits._
import uk.gov.ons.addressIndex.parsers.Tokens

import scala.concurrent.duration.Duration
import scala.io.Source
import scala.util.Try

@Singleton
class AddressController @Inject()(
  esRepo: ElasticsearchRepository,
  parser: ParserModule,
  conf: AddressIndexConfigModule
)(implicit ec: ExecutionContext) extends AddressIndexController {

  val logger = Logger("address-index-server:AddressController")

  /**
    * Test elastic is connected
    *
    * @return
    */
  def elasticTest(): Action[AnyContent] = Action async { implicit req =>
    esRepo.client execute {
      get cluster health
    } map { resp =>
      Ok(resp.toString)
    }
  }

  /**
    * Address query API
    *
    * @param input the address query
    * @return Json response with addresses information
    */
  def addressQuery(input: String, offset: Option[String] = None, limit: Option[String] = None): Action[AnyContent] = Action async { implicit req =>
    logger.info(s"#addressQuery:\ninput $input, offset: ${offset.getOrElse("default")}, limit: ${limit.getOrElse("default")}")
    // get the defaults and maxima for the paging parameters from the config
    val defLimit = conf.config.elasticSearch.defaultLimit
    val defOffset = conf.config.elasticSearch.defaultOffset
    val maxLimit = conf.config.elasticSearch.maximumLimit
    val maxOffset = conf.config.elasticSearch.maximumOffset
    // TODO Look at refactoring to use types
    val limval = limit.getOrElse(defLimit.toString())
    val offval = offset.getOrElse(defOffset.toString())
    val limitInvalid = Try(limval.toInt).isFailure
    val offsetInvalid = Try(offval.toInt).isFailure
    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)
    val offsetInt = Try(offval.toInt).toOption.getOrElse(defOffset)
    // Check the offset and limit parameters before proceeding with the request
    if (limitInvalid) {
      futureJsonBadRequest(LimitNotNumeric)
    } else if (limitInt < 1) {
      futureJsonBadRequest(LimitTooSmall)
    } else if (limitInt > maxLimit) {
      futureJsonBadRequest(LimitTooLarge)
    } else if (offsetInvalid) {
      futureJsonBadRequest(OffsetNotNumeric)
    } else if (offsetInt < 0) {
      futureJsonBadRequest(OffsetTooSmall)
    } else if (offsetInt > maxOffset) {
      futureJsonBadRequest(OffsetTooLarge)
    } else if (input.isEmpty) {
      futureJsonBadRequest(EmptySearch)
    } else {
      val tokens = parser.tag(input)

      logger.info(s"#addressQuery parsed:\n${tokens.map(token => s"value: ${token.value} , label:${token.label}").mkString("\n")}")

      val request: Future[HybridAddresses] = esRepo.queryAddresses(offsetInt, limitInt, tokens)

      request.map { case HybridAddresses(hybridAddresses, maxScore, total) =>
        jsonOk(
          AddressBySearchResponseContainer(
            response = AddressBySearchResponse(
              tokens = tokens,
              addresses = hybridAddresses.map(AddressResponseAddress.fromHybridAddress),
              limit = limitInt,
              offset = offsetInt,
              total = total,
              maxScore = maxScore
            ),
            status = OkAddressResponseStatus
          )
        )
      }
    }
  }

  /**
    * UPRN query API
    *
    * @param uprn uprn of the address to be fetched
    * @return
    */
  def uprnQuery(uprn: String): Action[AnyContent] = Action async { implicit req =>
    logger.info(s"#uprnQuery: uprn: $uprn")

    val request: Future[Option[HybridAddress]] = esRepo.queryUprn(uprn)

    request.map {

      case Some(hybridAddress) => jsonOk(
        AddressByUprnResponseContainer(
          response = AddressByUprnResponse(
            address = Some(AddressResponseAddress.fromHybridAddress(hybridAddress))
          ),
          status = OkAddressResponseStatus
        )
      )

      case None => jsonNotFound(NoAddressFoundUprn)

    }
  }



  /**
    * a POST route which will process all `BulkQuery` items in the `BulkBody`.
    * @return
    */
  def bulkQuery(): Action[BulkBody] = Action.async(parse.json[BulkBody]) { implicit req =>
    logger.info(s"#bulkQuery with ${req.body.addresses.size} items")
    val tokenizedAddresses: Iterator[(String, Seq[CrfTokenResult])] = req.body.addresses.toIterator.map(a => (a.id, parser.tag(a.address)))
    val bulkRequestsPerBatch = conf.config.elasticSearch.bulkRequestsPerBatch
    val chunkedTokenizedAddresses = tokenizedAddresses.grouped(bulkRequestsPerBatch).toList
    val results = chunkedTokenizedAddresses.map(tokens => Await.result(queryBulkAddresses(tokens.toIterator, conf.config.bulkLimit), Duration.Inf))
    logger.info(s"#bulkQuery processed")
    logger.info(s"#bulkQuery converting to response")
    futureJsonOk(
      BulkResp(
        resp = results flatMap { result =>
          val successes = result.successfulBulkAddresses map { addr =>
            BulkItem(
              id = addr.id,
              organisationName = addr.tokens.getOrElse(Tokens.organisationName, ""),
              departmentName = addr.tokens.getOrElse(Tokens.departmentName, ""),
              subBuildingName = addr.tokens.getOrElse(Tokens.subBuildingName, ""),
              buildingName = addr.tokens.getOrElse(Tokens.buildingName, ""),
              buildingNumber = addr.tokens.getOrElse(Tokens.buildingNumber, ""),
              streetName = addr.tokens.getOrElse(Tokens.streetName, ""),
              locality = addr.tokens.getOrElse(Tokens.locality, ""),
              townName = addr.tokens.getOrElse(Tokens.townName, ""),
              postcode = addr.tokens.getOrElse(Tokens.postcode, ""),
              uprn = addr.hybridAddress.uprn,
              score = addr.hybridAddress.score,
              exceptionMessage = ""
            )
          }
          val failures = result.failedBulkAddresses map { failures =>
            val tokenMap = Tokens.tokensToMap(failures.tokens)
            BulkItem(
              id = failures.id,
              organisationName = tokenMap.getOrElse(Tokens.organisationName, ""),
              departmentName = tokenMap.getOrElse(Tokens.departmentName, ""),
              subBuildingName = tokenMap.getOrElse(Tokens.subBuildingName, ""),
              buildingName = tokenMap.getOrElse(Tokens.buildingName, ""),
              buildingNumber = tokenMap.getOrElse(Tokens.buildingNumber, ""),
              streetName = tokenMap.getOrElse(Tokens.streetName, ""),
              locality = tokenMap.getOrElse(Tokens.locality, ""),
              townName = tokenMap.getOrElse(Tokens.townName, ""),
              postcode = tokenMap.getOrElse(Tokens.postcode, ""),
              uprn = "",
              score = 0f,
              exceptionMessage = failures.exception.getMessage
            )
          }
          successes ++ failures
        }
      )
    )
  }


  /**
    * Requests addresses for each tokens sequence supplied.
    * This method should not be in `Repository` because it uses `queryAddress`
    * that needs to be mocked through dependency injection
    * @param inputs an iterator containing a collection of tokens per each lines,
    *               typically a result of a parser applied to `Source.fromFile("/path").getLines`
    * @return BulkAddresses containing successful addresses and other information
    */
  def queryBulkAddresses(inputs: Iterator[(String, Seq[CrfTokenResult])], limitPerAddress: Int): Future[BulkAddresses] = {

    val addressesRequests: Iterator[Future[Either[RejectedRequest, Seq[BulkAddress]]]] =
      inputs.map { case (id, tokens) =>

        val bulkAddressRequest: Future[Seq[BulkAddress]] =
          esRepo.queryAddresses(0, limitPerAddress, tokens).map { case HybridAddresses(hybridAddresses, _, _) =>
            hybridAddresses.map(hybridAddress => BulkAddress(
                id = id,
                tokens = Tokens.tokensToMap(tokens),
                hybridAddress = hybridAddress
              )
            )
          }

        // Successful requests are stored in the `Right`
        // Failed requests will be stored in the `Left`
        bulkAddressRequest.map(Right(_)).recover {
          case exception: Throwable => Left(RejectedRequest(id, tokens, exception))
        }

      }

    // This also transforms lazy `Iterator` into an in-memory sequence
    val bulkAddresses: Future[Seq[Either[RejectedRequest, Seq[BulkAddress]]]] = Future.sequence(addressesRequests.toList)

    val successfulAddresses: Future[Seq[BulkAddress]] = bulkAddresses.map(collectSuccessfulAddresses)

    val failedAddresses: Future[Seq[RejectedRequest]] = bulkAddresses.map(collectFailedAddresses)

    // transform (Future(X), Future[Y]) into Future(X, Y)
    for {
      successful <- successfulAddresses
      failed <- failedAddresses
    } yield BulkAddresses(successful, failed)
  }


  private def collectSuccessfulAddresses(addresses: Seq[Either[RejectedRequest, Seq[BulkAddress]]]): Seq[BulkAddress] =
    addresses.collect {
      case Right(bulkAddresses) => bulkAddresses
    }.flatten

  private def collectFailedAddresses(addresses: Seq[Either[RejectedRequest, Seq[BulkAddress]]]): Seq[RejectedRequest] =
    addresses.collect {
      case Left(address) => address
    }

}
