package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}

import scala.concurrent.duration._
import play.api.Logger
import play.api.mvc.{Action, AnyContent, Request, Result}

import scala.concurrent.{Await, ExecutionContext, Future}
import uk.gov.ons.addressIndex.model.db.index.{HybridAddress, HybridAddresses}
import play.api.libs.json.Json
import uk.gov.ons.addressIndex.model.{BulkBody, BulkBodyDebug}
import uk.gov.ons.addressIndex.model.config.QueryParamsConfig
import uk.gov.ons.addressIndex.model.db.{BulkAddress, BulkAddressRequestData, BulkAddresses}
import uk.gov.ons.addressIndex.server.modules._
import uk.gov.ons.addressIndex.model.server.response._
import uk.gov.ons.addressIndex.parsers.Tokens
import uk.gov.ons.addressIndex.server.utils.{HopperScoreHelper, Splunk}

import scala.annotation.tailrec
import scala.util.Try
import scala.util.control.NonFatal

@Singleton
class AddressController @Inject()(
  esRepo: ElasticsearchRepository,
  parser: ParserModule,
  conf: ConfigModule,
  versionProvider: VersionModule
)(implicit ec: ExecutionContext) extends PlayHelperController with AddressIndexCannedResponse {

  val logger = Logger("address-index-server:AddressController")

  override val apiVersion: String = versionProvider.apiVersion
  override val dataVersion: String = versionProvider.dataVersion

  /**
    * Address query API
    *
    * @param input the address query
    * @return Json response with addresses information
    */
  def addressQuery(input: String, offset: Option[String] = None, limit: Option[String] = None): Action[AnyContent] = Action async { implicit req =>
    logger.info(s"#addressQuery:\ninput $input, offset: ${offset.getOrElse("default")}, limit: ${limit.getOrElse("default")}")
    val startingTime = System.currentTimeMillis()

    // get the defaults and maxima for the paging parameters from the config
    val defLimit = conf.config.elasticSearch.defaultLimit
    val defOffset = conf.config.elasticSearch.defaultOffset
    val maxLimit = conf.config.elasticSearch.maximumLimit
    val maxOffset = conf.config.elasticSearch.maximumOffset

    val limval = limit.getOrElse(defLimit.toString)
    val offval = offset.getOrElse(defOffset.toString)

    def writeSplunkLogs(doResponseTime: Boolean = true, badRequestErrorMessage: String = "", formattedOutput: String = "", numOfResults: String = "", score: String = ""): Unit = {
      val responseTime = if (doResponseTime) (System.currentTimeMillis() - startingTime).toString else ""
      val networkid = req.headers.get("authorization").getOrElse("Anon").split("_")(0)
      Splunk.log(IP = req.remoteAddress, url = req.uri, responseTimeMillis = responseTime,
        isInput = true, input = input, offset = offval,
        limit = limval, badRequestMessage = badRequestErrorMessage, formattedOutput = formattedOutput,
        numOfResults = numOfResults, score = score, networkid = networkid)
    }

    val limitInvalid = Try(limval.toInt).isFailure
    val offsetInvalid = Try(offval.toInt).isFailure
    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)
    val offsetInt = Try(offval.toInt).toOption.getOrElse(defOffset)

    // Check the offset and limit parameters before proceeding with the request
    if (limitInvalid) {
      writeSplunkLogs(badRequestErrorMessage = LimitNotNumericAddressResponseError.message)
      futureJsonBadRequest(LimitNotNumeric)
    } else if (limitInt < 1) {
      writeSplunkLogs(badRequestErrorMessage = LimitTooSmallAddressResponseError.message)
      futureJsonBadRequest(LimitTooSmall)
    } else if (limitInt > maxLimit) {
      writeSplunkLogs(badRequestErrorMessage = LimitTooLargeAddressResponseError.message)
      futureJsonBadRequest(LimitTooLarge)
    } else if (offsetInvalid) {
      writeSplunkLogs(badRequestErrorMessage = OffsetNotNumericAddressResponseError.message)
      futureJsonBadRequest(OffsetNotNumeric)
    } else if (offsetInt < 0) {
      writeSplunkLogs(badRequestErrorMessage = OffsetTooSmallAddressResponseError.message)
      futureJsonBadRequest(OffsetTooSmall)
    } else if (offsetInt > maxOffset) {
      writeSplunkLogs(badRequestErrorMessage = OffsetTooLargeAddressResponseError.message)
      futureJsonBadRequest(OffsetTooLarge)
    } else if (input.isEmpty) {
      writeSplunkLogs(badRequestErrorMessage = EmptyQueryAddressResponseError.message)
      futureJsonBadRequest(EmptySearch)
    } else {
      val tokens = parser.parse(input)

      logger.info(s"#addressQuery parsed:\n${tokens.map{case (label, token) => s"label: $label , value:$token"}.mkString("\n")}")

      val request: Future[HybridAddresses] = esRepo.queryAddresses(tokens, offsetInt, limitInt)

      request.map { case HybridAddresses(hybridAddresses, maxScore, total) =>


        val addresses: Seq[AddressResponseAddress] = hybridAddresses.map(AddressResponseAddress.fromHybridAddress)

        val scoredAdresses = HopperScoreHelper.getScoresForAddresses(addresses, tokens)

        addresses.foreach{ address =>
          writeSplunkLogs(formattedOutput = address.formattedAddressNag, numOfResults = total.toString, score = address.underlyingScore.toString)
        }

        writeSplunkLogs()

        jsonOk(
          AddressBySearchResponseContainer(
            apiVersion = apiVersion,
            dataVersion = dataVersion,
            response = AddressBySearchResponse(
              tokens = tokens,
              addresses = scoredAdresses,
              limit = limitInt,
              offset = offsetInt,
              total = total,
              maxScore = maxScore
            ),
            status = OkAddressResponseStatus
          )
        )
      }.recover{
        case NonFatal(exception) =>

          writeSplunkLogs(badRequestErrorMessage = FailedRequestToEsError.message)

          logger.warn(s"Could not handle individual request (address input), problem with ES ${exception.getMessage}")
          InternalServerError(Json.toJson(FailedRequestToEs))
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

    val startingTime = System.currentTimeMillis()
    def writeSplunkLogs(badRequestErrorMessage: String = "", notFound: Boolean = false, formattedOutput: String = "", numOfResults: String = "", score: String = ""): Unit = {
      val responseTime = System.currentTimeMillis() - startingTime
      val networkid = req.headers.get("authorization").getOrElse("Anon").split("_")(0)
      Splunk.log(IP = req.remoteAddress, url = req.uri, responseTimeMillis = responseTime.toString,
        isUprn = true, uprn = uprn, isNotFound = notFound, formattedOutput = formattedOutput,
        numOfResults = numOfResults, score = score, networkid = networkid)
    }

    val request: Future[Option[HybridAddress]] = esRepo.queryUprn(uprn)
    request.map {
      case Some(hybridAddress) =>

        val address = AddressResponseAddress.fromHybridAddress(hybridAddress)

        writeSplunkLogs(formattedOutput = address.formattedAddressNag, numOfResults = "1", score = hybridAddress.score.toString)

        jsonOk(
          AddressByUprnResponseContainer(
            apiVersion = apiVersion,
            dataVersion = dataVersion,
            response = AddressByUprnResponse(
              address = Some(address)
            ),
            status = OkAddressResponseStatus
          )
        )

      case None =>
        writeSplunkLogs(notFound = true)
        jsonNotFound(NoAddressFoundUprn)

    }.recover {
      case NonFatal(exception) =>

        writeSplunkLogs(badRequestErrorMessage = FailedRequestToEsError.message)

        logger.warn(s"Could not handle individual request (uprn), problem with ES ${exception.getMessage}")
        InternalServerError(Json.toJson(FailedRequestToEs))
    }
  }



  /**
    * a POST route which will process all `BulkQuery` items in the `BulkBody`
    * @return reduced information on found addresses (uprn, formatted address)
    */
  def bulk(limitPerAddress: Option[Int]): Action[BulkBody] = Action(parse.json[BulkBody]) { implicit request =>
    logger.info(s"#bulkQuery with ${request.body.addresses.size} items")

    val requestsData: Stream[BulkAddressRequestData] = requestDataFromRequest(request)

    val configOverwrite: Option[QueryParamsConfig] = request.body.config

    bulkQuery(requestsData, configOverwrite, limitPerAddress)
  }

  private def requestDataFromRequest(request: Request[BulkBody]): Stream[BulkAddressRequestData] = request.body.addresses.toStream.map {
    row => BulkAddressRequestData(row.id, row.address, parser.parse(row.address))
  }

  /**
    * a POST route which will process all `BulkQuery` items in the `BulkBody`
    * this version is slower and more memory-consuming
    * @return all the information on found addresses (uprn, formatted address, found address json object)
    */
  def bulkFull(limitPerAddress: Option[Int]): Action[BulkBody] = Action(parse.json[BulkBody]) { implicit request =>
    logger.info(s"#bulkFullQuery with ${request.body.addresses.size} items")

    val requestsData: Stream[BulkAddressRequestData] = requestDataFromRequest(request)

    val configOverwrite: Option[QueryParamsConfig] = request.body.config

    bulkQuery(requestsData, configOverwrite, limitPerAddress, includeFullAddress = true)
  }

  /**
    * Bulk endpoint that accepts tokens instead of input texts for each address
    * @return reduced info on found addresses
    */
  def bulkDebug(limitPerAddress: Option[Int]): Action[BulkBodyDebug] = Action(parse.json[BulkBodyDebug]) { implicit request =>
    logger.info(s"#bulkDebugQuery with ${request.body.addresses.size} items")

    val requestsData: Stream[BulkAddressRequestData] = request.body.addresses.toStream.map {
      row => BulkAddressRequestData(row.id, row.tokens.values.mkString(" "), row.tokens)
    }
    val configOverwrite: Option[QueryParamsConfig] = request.body.config

    bulkQuery(requestsData, configOverwrite, limitPerAddress)
  }


  private def bulkQuery(
    requestData: Stream[BulkAddressRequestData],
    configOverwrite: Option[QueryParamsConfig],
    limitPerAddress: Option[Int],
    includeFullAddress: Boolean = false
  )(implicit request: Request[_]): Result = {

    val startingTime = System.currentTimeMillis()

    val defaultBatchSize = conf.config.bulk.batch.perBatch

    val results: Seq[BulkAddress] = iterateOverRequestsWithBackPressure(requestData, defaultBatchSize, limitPerAddress, configOverwrite)

    logger.info(s"#bulkQuery processed")

    // Used to distinguish individual bulk logs
    val uuid = java.util.UUID.randomUUID.toString

    val bulkItems = results.map { bulkAddress =>

        val addressBulkResponseAddress = AddressBulkResponseAddress.fromBulkAddress(bulkAddress, includeFullAddress)
        val networkid = request.headers.get("authorization").getOrElse("Anon").split("_")(0)
        // Side effects
        Splunk.log(IP = request.remoteAddress, url = request.uri, input = addressBulkResponseAddress.inputAddress, isBulk = true,
          formattedOutput = addressBulkResponseAddress.matchedFormattedAddress,
          score = addressBulkResponseAddress.score.toString, uuid = uuid, networkid = networkid)

        addressBulkResponseAddress
      }

    val response =
      jsonOk(
        AddressBulkResponseContainer(
          apiVersion = apiVersion,
          dataVersion = dataVersion,
          bulkAddresses = bulkItems,
          status = OkAddressResponseStatus
        )
      )

    val responseTime = System.currentTimeMillis() - startingTime
    Splunk.log(IP = request.remoteAddress, url = request.uri, responseTimeMillis = responseTime.toString, isBulk = true, bulkSize = requestData.size.toString)

    response
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
    * @param configOverwrite optional configuration that will overwrite current queryParam
    * @param canUpScale wether or not this particular iteration can upscale the mini-batch size
    * @param successfulResults accumulator of successfull results
    * @return Queried addresses
    */
  @tailrec
  final def iterateOverRequestsWithBackPressure(
    requests: Stream[BulkAddressRequestData],
    miniBatchSize: Int,
    limitPerAddress: Option[Int] = None,
    configOverwrite: Option[QueryParamsConfig] = None,
    canUpScale: Boolean = true,
    successfulResults: Seq[BulkAddress] = Seq.empty
  ): Seq[BulkAddress] = {

    Splunk.log(isBulk = true, batchSize = miniBatchSize.toString)

    val defaultBatchSize = conf.config.bulk.batch.perBatch
    val bulkSizeWarningThreshold = conf.config.bulk.batch.warningThreshold

    if (miniBatchSize < defaultBatchSize * bulkSizeWarningThreshold)
      logger.warn(s"#bulkQuery mini-bulk size it less than a ${defaultBatchSize * bulkSizeWarningThreshold}: size = $miniBatchSize , check if everything is fine with ES")
    else logger.info(s"#bulkQuery sending a mini-batch of the size $miniBatchSize")

    val miniBatch = requests.take(miniBatchSize)
    val requestsAfterMiniBatch = requests.drop(miniBatchSize)
    val addressesPerAddress = limitPerAddress.getOrElse(conf.config.bulk.limitPerAddress)
    val result: BulkAddresses = Await.result(queryBulkAddresses(miniBatch, addressesPerAddress, configOverwrite), Duration.Inf)

    val requestsLeft = requestsAfterMiniBatch ++ result.failedRequests

    if (requestsLeft.isEmpty) successfulResults ++ result.successfulBulkAddresses
    else if (miniBatchSize == 1 && result.failedRequests.nonEmpty)
      throw new Exception(s"""
           Bulk query request: mini-bulk was scaled down to the size of 1 and it still fails, something's wrong with ES.
           Last request failure message: ${result.failedRequests.head.lastFailExceptionMessage}
        """)
    else {
      val miniBatchUpscale = conf.config.bulk.batch.upscale
      val miniBatchDownscale = conf.config.bulk.batch.downscale
      val newMiniBatchSize =
        if (result.failedRequests.isEmpty && canUpScale) math.ceil(miniBatchSize * miniBatchUpscale).toInt
        else if (result.failedRequests.isEmpty) miniBatchSize
        else math.floor(miniBatchSize * miniBatchDownscale).toInt

      val nextCanUpScale = canUpScale && result.failedRequests.isEmpty

      iterateOverRequestsWithBackPressure(requestsLeft, newMiniBatchSize, limitPerAddress, configOverwrite, nextCanUpScale, successfulResults ++ result.successfulBulkAddresses)
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
  def queryBulkAddresses(
    inputs: Stream[BulkAddressRequestData],
    limitPerAddress: Int,
    configOverwrite: Option[QueryParamsConfig] = None
  ): Future[BulkAddresses] = {

    val bulkAddresses: Future[Stream[Either[BulkAddressRequestData, Seq[BulkAddress]]]] = esRepo.queryBulk(inputs, limitPerAddress, configOverwrite)

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
