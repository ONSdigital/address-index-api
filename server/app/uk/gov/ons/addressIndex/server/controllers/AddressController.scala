package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import scala.concurrent.duration._
import play.api.Logger
import play.api.mvc.{Action, AnyContent}
import scala.concurrent.{Await, ExecutionContext, Future}
import uk.gov.ons.addressIndex.model.db.index.{HybridAddress, HybridAddresses}
import com.sksamuel.elastic4s.ElasticDsl._
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.{BulkBody, BulkItem, BulkResp}
import uk.gov.ons.addressIndex.model.db.{BulkAddress, BulkAddressRequestData, BulkAddresses}
import uk.gov.ons.addressIndex.server.modules._
import uk.gov.ons.addressIndex.model.server.response._
import uk.gov.ons.addressIndex.parsers.Tokens

import scala.annotation.tailrec
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
    * a POST route which will process all `BulkQuery` items in the `BulkBody`
    *
    * @return
    */
  def bulkQuery(): Action[BulkBody] = Action(parse.json[BulkBody]) { implicit req =>
    logger.info(s"#bulkQuery with ${req.body.addresses.size} items")

    val requestsData: Stream[BulkAddressRequestData] = req.body.addresses.toStream.map{
      row => BulkAddressRequestData(row.id, row.address, parser.tag(row.address))
    }

    val defaultBatchSize = conf.config.bulkRequestsPerBatch

    val results: Seq[BulkAddress] = iterateOverRequestsWithBackPressure(requestsData, defaultBatchSize, Seq.empty)

    logger.info(s"#bulkQuery processed")
    logger.info(s"#bulkQuery converting to response")

    jsonOk(
      BulkResp(
        resp = results.map { address =>
          BulkItem(
            maxScorePossible = address.maxPossibleScore,
            inputAddress = address.inputAddress,
            matchedFormattedAddress = address.matchedFormattedAddress,
            id = address.id,
            organisationName = address.tokens.getOrElse(Tokens.organisationName, ""),
            departmentName = address.tokens.getOrElse(Tokens.departmentName, ""),
            subBuildingName = address.tokens.getOrElse(Tokens.subBuildingName, ""),
            buildingName = address.tokens.getOrElse(Tokens.buildingName, ""),
            buildingNumber = address.tokens.getOrElse(Tokens.buildingNumber, ""),
            streetName = address.tokens.getOrElse(Tokens.streetName, ""),
            locality = address.tokens.getOrElse(Tokens.locality, ""),
            townName = address.tokens.getOrElse(Tokens.townName, ""),
            postcode = address.tokens.getOrElse(Tokens.postcode, ""),
            uprn = address.hybridAddress.uprn,
            score = address.hybridAddress.score
          )
        }
      )
    )
  }

  /**
    * Iterates over requests data and adapts the size of the bulk-chunks using back-pressure.
    * ES rejects requests when it cannot handle them (because of the consumed resources)
    * in this case we reduce the bulk size so that we could process the data successfully.
    * Otherwise we increase the size of the bulk so that we could do more in one bulk
    *
    * It should throw an exception if the situation is desperate (we only do one request at
    * a time and this request fails)
    * @param requests Stream of data that will be used to query ES
    * @param miniBatchSize the size of the bulk to use
    * @param successfulResults accumulator of successfull results
    * @return Queried addresses
    */
  @tailrec
  final def iterateOverRequestsWithBackPressure(requests: Stream[BulkAddressRequestData], miniBatchSize: Int,
    successfulResults: Seq[BulkAddress]): Seq[BulkAddress] = {
    logger.info(s"#bulkQuery sending a mini-batch of the size $miniBatchSize")

    val miniBatch = requests.take(miniBatchSize)
    val requestsAfterMiniBatch = requests.drop(miniBatchSize)
    val result: BulkAddresses = Await.result(queryBulkAddresses(miniBatch, conf.config.bulkLimit), Duration.Inf)

    val requestsLeft = result.failedRequests ++ requestsAfterMiniBatch

    if (requestsLeft.isEmpty) successfulResults ++ result.successfulBulkAddresses
    else if (miniBatchSize == 1 && result.failedRequests.nonEmpty) throw new Exception("Bulk query request: mini-bulk was scaled down to the size of 1 and it still fails, something's wrong with ES.")
    else {
      val miniBatchUpscale = conf.config.bulkMiniBatchUpscale
      val miniBatchDownscale = conf.config.bulkMiniBatchDownscale
      val newMiniBatchSize =
        if (result.failedRequests.isEmpty) math.ceil(miniBatchSize * miniBatchUpscale).toInt
        else math.floor(miniBatchSize * miniBatchDownscale).toInt

      iterateOverRequestsWithBackPressure(requestsLeft, newMiniBatchSize, successfulResults ++ result.successfulBulkAddresses)
    }
  }

  /**
    * Requests addresses for each tokens sequence supplied.
    * This method should not be in `Repository` because it uses `queryAddress`
    * that needs to be mocked through dependency injection
    * @param inputs an iterator containing a collection of tokens per each lines,
    *               typically a result of a parser applied to `Source.fromFile("/path").getLines`
    * @return BulkAddresses containing successful addresses and other information
    */
  def queryBulkAddresses(inputs: Stream[BulkAddressRequestData], limitPerAddress: Int): Future[BulkAddresses] = {

    val addressesRequests: Stream[Future[Either[BulkAddressRequestData, Seq[BulkAddress]]]] =
      inputs.map { case BulkAddressRequestData(id, originalInput, tokens) =>

        val bulkAddressRequest: Future[Seq[BulkAddress]] =
          esRepo.queryAddresses(0, limitPerAddress, tokens).map { case HybridAddresses(hybridAddresses, maxScore, _) =>
            val resp = hybridAddresses.map(hybridAddress =>
              BulkAddress(
                maxPossibleScore = maxScore,
                matchedFormattedAddress = AddressResponseAddress.fromHybridAddress(hybridAddress).formattedAddress,
                inputAddress = originalInput,
                id = id,
                tokens = Tokens.postTokenizeTreatment(tokens),
                hybridAddress = hybridAddress
              )
            )
            if(resp.isEmpty) {
              Seq(
                BulkAddress(
                  maxPossibleScore = maxScore,
                  matchedFormattedAddress = "",
                  inputAddress = originalInput,
                  id = id,
                  tokens = Tokens.postTokenizeTreatment(tokens),
                  hybridAddress = HybridAddress(
                    uprn = "",
                    lpi = Seq.empty,
                    paf = Seq.empty,
                    score = 0
                  )
                )
              )
            } else {
              resp
            }
          }

        // Successful requests are stored in the `Right`
        // Failed requests will be stored in the `Left`
        bulkAddressRequest.map(Right(_)).recover {
          case exception: Exception =>
            logger.info(s"#bulk query: rejected future (this is, most likely, normal) ${exception.getMessage}")
            Left(BulkAddressRequestData(id, originalInput, tokens))
        }
      }

    val bulkAddresses: Future[Stream[Either[BulkAddressRequestData, Seq[BulkAddress]]]] = Future.sequence(addressesRequests)

    val successfulAddresses: Future[Stream[BulkAddress]] = bulkAddresses.map(collectSuccessfulAddresses)

    val failedAddresses: Future[Stream[BulkAddressRequestData]] = bulkAddresses.map(collectFailedAddresses)

    // transform (Future[X], Future[Y]) into Future[Z[X, Y]]
    for {
      successful <- successfulAddresses
      failed <- failedAddresses
    } yield BulkAddresses(successful, failed)
  }


  private def collectSuccessfulAddresses(addresses: Stream[Either[BulkAddressRequestData, Seq[BulkAddress]]]): Stream[BulkAddress] =
    addresses.collect {
      case Right(bulkAddresses) => bulkAddresses
    }.flatten

  private def collectFailedAddresses(addresses: Stream[Either[BulkAddressRequestData, Seq[BulkAddress]]]): Stream[BulkAddressRequestData] =
    addresses.collect {
      case Left(address) => address
    }

}
