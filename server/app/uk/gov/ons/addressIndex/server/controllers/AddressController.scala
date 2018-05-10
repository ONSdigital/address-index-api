package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}

import scala.concurrent.duration._
import play.api.Logger
import play.api.mvc._

import scala.concurrent.{Await, ExecutionContext, Future}
import uk.gov.ons.addressIndex.model.db.index.{HybridAddress, HybridAddresses}
import play.api.libs.json.Json
import uk.gov.ons.addressIndex.model.{BulkBody, BulkBodyDebug}
import uk.gov.ons.addressIndex.model.config.QueryParamsConfig
import uk.gov.ons.addressIndex.model.db.{BulkAddress, BulkAddressRequestData, BulkAddresses}
import uk.gov.ons.addressIndex.server.modules._
import uk.gov.ons.addressIndex.model.server.response._
import uk.gov.ons.addressIndex.server.utils.{ConfidenceScoreHelper, HopperScoreHelper, Splunk}

import scala.annotation.tailrec
import scala.util.Try
import scala.util.control.NonFatal
import scala.math._

@Singleton
class AddressController @Inject()(
  val controllerComponents: ControllerComponents,
  esRepo: ElasticsearchRepository,
  parser: ParserModule,
  conf: ConfigModule,
  versionProvider: VersionModule
)(implicit ec: ExecutionContext) extends PlayHelperController with AddressIndexCannedResponse {

  val logger = Logger("address-index-server:AddressController")

  override val apiVersion: String = versionProvider.apiVersion
  override val dataVersion: String = versionProvider.dataVersion

  val missing: String = "missing"
  val invalid: String = "invalid"
  val valid: String = "valid"
  val notRequired: String = "not required"


  def codelists(): Action[AnyContent] = Action async { implicit req =>
      val message = "{\"message\":\"codelist functions only available via the API gateway\"}"
      Future(Ok(message))
  }

  /**
    * Address query API
    *
    * @param input the address query
    * @return Json response with addresses information
    */
  def addressQuery(input: String, offset: Option[String] = None, limit: Option[String] = None, classificationfilter: Option[String] = None, rangekm: Option[String] = None, lat: Option[String] = None, lon: Option[String] = None, historical: Option[String] = None, matchthreshold: Option[String] = None): Action[AnyContent] = Action async { implicit req =>
   // logger.info(s"#addressQuery:\ninput $input, offset: ${offset.getOrElse("default")}, limit: ${limit.getOrElse("default")}")
    val startingTime = System.currentTimeMillis()

    // check API key
    val apiKey = req.headers.get("authorization").getOrElse(missing)
    val keyStatus = checkAPIkey(apiKey)

    // check source
    val source = req.headers.get("Source").getOrElse(missing)
    val sourceStatus = checkSource(source)

    // get the defaults and maxima for the paging parameters from the config
    val defLimit = conf.config.elasticSearch.defaultLimit
    val defOffset = conf.config.elasticSearch.defaultOffset
    val maxLimit = conf.config.elasticSearch.maximumLimit
    val maxOffset = conf.config.elasticSearch.maximumOffset
    val defThreshold = conf.config.elasticSearch.matchThreshold

    val limval = limit.getOrElse(defLimit.toString)
    val offval = offset.getOrElse(defOffset.toString)
    val threshval = matchthreshold.getOrElse(defThreshold.toString)

    val filterString = classificationfilter.getOrElse("")

    val hist = historical match {
      case Some(x) => Try(x.toBoolean).getOrElse(true)
      case None => true
    }

    // validate radius paramas
    val rangeVal = rangekm.getOrElse("")
    val latVal = lat.getOrElse("")
    val lonVal = lon.getOrElse("")
    val rangeInvalid = if (rangeVal.equals("")) false else Try(rangeVal.toDouble).isFailure
    val latInvalid = if (rangeVal.equals("")) false else Try(latVal.toDouble).isFailure
    val lonInvalid = if (rangeVal.equals("")) false else Try(lonVal.toDouble).isFailure

    val latTooFarNorth = if (rangeVal.equals("")) false else {
      (Try(latVal.toDouble).getOrElse(50D) > 60.9)
    }
    val latTooFarSouth = if (rangeVal.equals("")) false else {
      (Try(latVal.toDouble).getOrElse(50D) < 49.8)
    }
    val lonTooFarEast = if (rangeVal.equals("")) false else {
      (Try(lonVal.toDouble).getOrElse(0D) > 1.8)
    }
    val lonTooFarWest = if (rangeVal.equals("")) false else {
      (Try(lonVal.toDouble).getOrElse(0D) < -8.6)
    }

    def writeSplunkLogs(doResponseTime: Boolean = true, badRequestErrorMessage: String = "", formattedOutput: String = "", numOfResults: String = "", score: String = ""): Unit = {
      val responseTime = if (doResponseTime) (System.currentTimeMillis() - startingTime).toString else ""
      val networkid = req.headers.get("authorization").getOrElse("Anon").split("_")(0)
      Splunk.log(IP = req.remoteAddress, url = req.uri, responseTimeMillis = responseTime,
        isInput = true, input = input, offset = offval, limit = limval, filter = filterString, historical = hist,
        rangekm = rangeVal, lat = latVal, lon = lonVal,
        badRequestMessage = badRequestErrorMessage, formattedOutput = formattedOutput,
        numOfResults = numOfResults, score = score, networkid = networkid)
    }

    val limitInvalid = Try(limval.toInt).isFailure
    val offsetInvalid = Try(offval.toInt).isFailure
    val thresholdInvalid = Try(threshval.toFloat).isFailure
    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)
    val offsetInt = Try(offval.toInt).toOption.getOrElse(defOffset)
    val thresholdFloat = Try(threshval.toFloat).toOption.getOrElse(defThreshold)
    val thresholdNotInRange = !(thresholdFloat >= 0 && thresholdFloat <= 100)

    // Check the api key, offset and limit parameters before proceeding with the request
    if (sourceStatus == missing) {
      writeSplunkLogs(badRequestErrorMessage = SourceMissingError.message)
      futureJsonUnauthorized(SourceMissing)
    } else if (sourceStatus == invalid) {
      writeSplunkLogs(badRequestErrorMessage = SourceInvalidError.message)
      futureJsonUnauthorized(SourceInvalid)
    } else if (keyStatus == missing) {
      writeSplunkLogs(badRequestErrorMessage = ApiKeyMissingError.message)
      futureJsonUnauthorized(KeyMissing)
    } else if (keyStatus == invalid) {
      writeSplunkLogs(badRequestErrorMessage = ApiKeyInvalidError.message)
      futureJsonUnauthorized(KeyInvalid)
    } else if (limitInvalid) {
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
    } else if (thresholdInvalid) {
      writeSplunkLogs(badRequestErrorMessage = ThresholdNotNumericAddressResponseError.message)
      futureJsonBadRequest(ThresholdNotNumeric)
    } else if (thresholdNotInRange) {
      writeSplunkLogs(badRequestErrorMessage = ThresholdNotInRangeAddressResponseError.message)
      futureJsonBadRequest(ThresholdNotInRange)
    } else if (input.isEmpty) {
      writeSplunkLogs(badRequestErrorMessage = EmptyQueryAddressResponseError.message)
      futureJsonBadRequest(EmptySearch)
    } else if (!filterString.isEmpty && !filterString.matches("""\b(residential|commercial|C|c|C\w+|c\w+|L|l|L\w+|l\w+|M|m|M\w+|m\w+|O|o|O\w+|o\w+|P|p|P\w+|p\w+|R|r|R\w+|r\w+|U|u|U\w+|u\w+|X|x|X\w+|x\w+|Z|z|Z\w+|z\w+)\b.*""") ) {
      writeSplunkLogs(badRequestErrorMessage = FilterInvalidError.message)
      futureJsonBadRequest(FilterInvalid)
    } else if (rangeInvalid) {
      writeSplunkLogs(badRequestErrorMessage = RangeNotNumericAddressResponseError.message)
      futureJsonBadRequest(RangeNotNumeric)
    } else if (latInvalid) {
      writeSplunkLogs(badRequestErrorMessage = LatitudeNotNumericAddressResponseError.message)
      futureJsonBadRequest(LatitiudeNotNumeric)
    } else if (lonInvalid) {
      writeSplunkLogs(badRequestErrorMessage = LongitudeNotNumericAddressResponseError.message)
      futureJsonBadRequest(LongitudeNotNumeric)
    } else if (latTooFarNorth) {
      writeSplunkLogs(badRequestErrorMessage = LatitudeTooFarNorthAddressResponseError.message)
      futureJsonBadRequest(LatitudeTooFarNorth)
    } else if (latTooFarSouth) {
      writeSplunkLogs(badRequestErrorMessage = LatitudeTooFarSouthAddressResponseError.message)
      futureJsonBadRequest(LatitudeTooFarSouth)
    } else if (lonTooFarEast) {
      writeSplunkLogs(badRequestErrorMessage = LongitudeTooFarEastAddressResponseError.message)
      futureJsonBadRequest(LongitudeTooFarEast)
    } else if (lonTooFarWest) {
      writeSplunkLogs(badRequestErrorMessage = LongitudeTooFarWestAddressResponseError.message)
      futureJsonBadRequest(LongitudeTooFarWest)
    } else {
      val tokens = parser.parse(input)

    //  logger.info(s"#addressQuery parsed:\n${tokens.map{case (label, token) => s"label: $label , value:$token"}.mkString("\n")}")

      // try to get enough results to accurately calcuate the hybrid score (may need to be more sophisticated)
      val minimumSample = conf.config.elasticSearch.minimumSample
      val limitExpanded = max(offsetInt + (limitInt * 2),minimumSample)
      val request: Future[HybridAddresses] = esRepo.queryAddresses(tokens, 0, limitExpanded, filterString, rangeVal, latVal, lonVal, None, hist)

      request.map { case HybridAddresses(hybridAddresses, maxScore, total) =>

        val addresses: Seq[AddressResponseAddress] = hybridAddresses.map(AddressResponseAddress.fromHybridAddress)
        //  calculate the elastic denominator value which will be used when scoring each address
        val elasticDenominator = Try(ConfidenceScoreHelper.calculateElasticDenominator(addresses.map(_.underlyingScore))).getOrElse(1D)
        // calculate the Hopper and hybrid scores for each  address
        val scoredAddresses = HopperScoreHelper.getScoresForAddresses(addresses, tokens, elasticDenominator)
        // work out the threshold for accepting matches (default 5% -> 0.05)
        val threshold = Try((thresholdFloat / 100).toDouble).getOrElse(0.05D)

        // filter out scores below threshold, sort the resultant collection, highest score first
        val sortedAddresses = scoredAddresses.filter(_.confidenceScore > threshold).sortBy(_.confidenceScore)(Ordering[Double].reverse)
        // capture the number of matches before applying offset and limit
        val newTotal = sortedAddresses.length
        // trim the result list according to offset and limit paramters
        val limitedSortedAddresses = sortedAddresses.drop(offsetInt).take(limitInt)

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
              addresses = limitedSortedAddresses,
              filter = filterString,
              historical = hist,
              rangekm = rangeVal,
              latitude = latVal,
              longitude = lonVal,
              limit = limitInt,
              offset = offsetInt,
              total = newTotal,
              maxScore = maxScore,
              sampleSize=limitExpanded,
              matchthreshold = thresholdFloat
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
  def uprnQuery(uprn: String, historical: Option[String] = None): Action[AnyContent] = Action async { implicit req =>
   // logger.info(s"#uprnQuery: uprn: $uprn")

    // check API key
    val apiKey = req.headers.get("authorization").getOrElse(missing)
    val keyStatus = checkAPIkey(apiKey)

    // check source
    val source = req.headers.get("Source").getOrElse(missing)
    val sourceStatus = checkSource(source)

    val uprnInvalid = Try(uprn.toLong).isFailure

    val hist = historical match {
      case Some(x) => Try(x.toBoolean).getOrElse(true)
      case None => true
    }

    val startingTime = System.currentTimeMillis()
    def writeSplunkLogs(badRequestErrorMessage: String = "", notFound: Boolean = false, formattedOutput: String = "", numOfResults: String = "", score: String = ""): Unit = {
      val responseTime = System.currentTimeMillis() - startingTime
      val networkid = req.headers.get("authorization").getOrElse("Anon").split("_")(0)
      Splunk.log(IP = req.remoteAddress, url = req.uri, responseTimeMillis = responseTime.toString,
        isUprn = true, uprn = uprn, isNotFound = notFound, formattedOutput = formattedOutput,
        numOfResults = numOfResults, score = score, networkid = networkid, historical = hist)
    }

    if (sourceStatus == missing) {
      writeSplunkLogs(badRequestErrorMessage = SourceMissingError.message)
      futureJsonUnauthorized(SourceMissing)
    } else if (sourceStatus == invalid) {
      writeSplunkLogs(badRequestErrorMessage = SourceInvalidError.message)
      futureJsonUnauthorized(SourceInvalid)
    } else if (keyStatus == missing) {
      writeSplunkLogs(badRequestErrorMessage = ApiKeyMissingError.message)
      futureJsonUnauthorized(KeyMissing)
    } else if (keyStatus == invalid) {
      writeSplunkLogs(badRequestErrorMessage = ApiKeyInvalidError.message)
      futureJsonUnauthorized(KeyInvalid)
    } else if (uprnInvalid) {
      writeSplunkLogs(badRequestErrorMessage = UprnNotNumericAddressResponseError.message)
      futureJsonBadRequest(UprnNotNumeric)
    } else {
      val request: Future[Option[HybridAddress]] = esRepo.queryUprn(uprn, hist)
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
  }


  /**
    * POSTCODE query API
    *
    * @param postcode postcode of the address to be fetched
    * @return Json response with addresses information
    */
  def postcodeQuery(postcode: String, offset: Option[String] = None, limit: Option[String] = None, classificationfilter: Option[String] = None, historical: Option[String] = None): Action[AnyContent] = Action async { implicit req =>
    // logger.info(s"#addressQuery:\ninput $input, offset: ${offset.getOrElse("default")}, limit: ${limit.getOrElse("default")}")
    val startingTime = System.currentTimeMillis()

    // check API key
    val apiKey = req.headers.get("authorization").getOrElse(missing)
    val keyStatus = checkAPIkey(apiKey)

    // check source
    val source = req.headers.get("Source").getOrElse(missing)
    val sourceStatus = checkSource(source)

    // get the defaults and maxima for the paging parameters from the config
    val defLimit = conf.config.elasticSearch.defaultLimitPostcode
    val defOffset = conf.config.elasticSearch.defaultOffset
    val maxLimit = conf.config.elasticSearch.maximumLimit
    val maxOffset = conf.config.elasticSearch.maximumOffset

    val limval = limit.getOrElse(defLimit.toString)
    //val limval = "100"
    val offval = offset.getOrElse(defOffset.toString)

    val filterString = classificationfilter.getOrElse("")

    val hist = historical match {
      case Some(x) => Try(x.toBoolean).getOrElse(true)
      case None => true
    }

    def writeSplunkLogs(doResponseTime: Boolean = true, badRequestErrorMessage: String = "", notFound: Boolean = false, formattedOutput: String = "", numOfResults: String = "", score: String = ""): Unit = {
      val responseTime = if (doResponseTime) (System.currentTimeMillis() - startingTime).toString else ""
      val networkid = req.headers.get("authorization").getOrElse("Anon").split("_")(0)
      Splunk.log(IP = req.remoteAddress, url = req.uri, responseTimeMillis = responseTime,
        isPostcode = true, postcode = postcode, isNotFound = notFound, offset = offval,
        limit = limval, filter = filterString, badRequestMessage = badRequestErrorMessage, formattedOutput = formattedOutput,
        numOfResults = numOfResults, score = score, networkid = networkid, historical = hist)
    }

    val limitInvalid = Try(limval.toInt).isFailure
    val offsetInvalid = Try(offval.toInt).isFailure
    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)
    val offsetInt = Try(offval.toInt).toOption.getOrElse(defOffset)

    // Check the api key, offset and limit parameters before proceeding with the request
    if (sourceStatus == missing) {
      writeSplunkLogs(badRequestErrorMessage = SourceMissingError.message)
      futureJsonUnauthorized(SourceMissing)
    } else if (sourceStatus == invalid) {
      writeSplunkLogs(badRequestErrorMessage = SourceInvalidError.message)
      futureJsonUnauthorized(SourceInvalid)
    } else if (keyStatus == missing) {
      writeSplunkLogs(badRequestErrorMessage = ApiKeyMissingError.message)
      futureJsonUnauthorized(KeyMissing)
    } else if (keyStatus == invalid) {
      writeSplunkLogs(badRequestErrorMessage = ApiKeyInvalidError.message)
      futureJsonUnauthorized(KeyInvalid)
    } else if (limitInvalid) {
      writeSplunkLogs(badRequestErrorMessage = LimitNotNumericPostcodeAddressResponseError.message)
      futureJsonBadRequest(LimitNotNumericPostcode)
    } else if (limitInt < 1) {
      writeSplunkLogs(badRequestErrorMessage = LimitTooSmallPostcodeAddressResponseError.message)
      futureJsonBadRequest(LimitTooSmallPostcode)
    } else if (limitInt > maxLimit) {
      writeSplunkLogs(badRequestErrorMessage = LimitTooLargePostcodeAddressResponseError.message)
      futureJsonBadRequest(LimitTooLargePostcode)
    } else if (offsetInvalid) {
      writeSplunkLogs(badRequestErrorMessage = OffsetNotNumericPostcodeAddressResponseError.message)
      futureJsonBadRequest(OffsetNotNumericPostcode)
    } else if (offsetInt < 0) {
      writeSplunkLogs(badRequestErrorMessage = OffsetTooSmallPostcodeAddressResponseError.message)
      futureJsonBadRequest(OffsetTooSmallPostcode)
    } else if (offsetInt > maxOffset) {
      writeSplunkLogs(badRequestErrorMessage = OffsetTooLargePostcodeAddressResponseError.message)
      futureJsonBadRequest(OffsetTooLargePostcode)
    } else if (postcode.isEmpty) {
      writeSplunkLogs(badRequestErrorMessage = EmptyQueryPostcodeAddressResponseError.message)
      futureJsonBadRequest(EmptySearchPostcode)
    } else if (!filterString.isEmpty && !filterString.matches("""\b(residential|commercial|C|c|C\w+|c\w+|L|l|L\w+|l\w+|M|m|M\w+|m\w+|O|o|O\w+|o\w+|P|p|P\w+|p\w+|R|r|R\w+|r\w+|U|u|U\w+|u\w+|X|x|X\w+|x\w+|Z|z|Z\w+|z\w+)\b.*""") ) {
      writeSplunkLogs(badRequestErrorMessage = FilterInvalidPostcodeError.message)
      futureJsonBadRequest(FilterInvalidPostcode)
    } else {
      val tokens = parser.parse(postcode)

      //  logger.info(s"#addressQuery parsed:\n${tokens.map{case (label, token) => s"label: $label , value:$token"}.mkString("\n")}")

      val request: Future[HybridAddresses] = esRepo.queryPostcode(postcode, offsetInt, limitInt, filterString, None, hist)

      request.map {
        case HybridAddresses(hybridAddresses, maxScore, total) =>

        val addresses: Seq[AddressResponseAddress] = hybridAddresses.map(AddressResponseAddress.fromHybridAddress)

        val scoredAdresses = HopperScoreHelper.getScoresForAddresses(addresses, tokens,1D)

        addresses.foreach{ address =>
          writeSplunkLogs(formattedOutput = address.formattedAddressNag, numOfResults = total.toString, score = address.underlyingScore.toString)
        }

        writeSplunkLogs()

        jsonOk(
          AddressByPostcodeResponseContainer(
            apiVersion = apiVersion,
            dataVersion = dataVersion,
            response = AddressByPostcodeResponse(
              postcode = postcode,
              addresses = scoredAdresses,
              filter = filterString,
              historical = hist,
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

          writeSplunkLogs(badRequestErrorMessage = FailedRequestToEsPostcodeError.message)

          logger.warn(s"Could not handle individual request (postcode input), problem with ES ${exception.getMessage}")
          InternalServerError(Json.toJson(FailedRequestToEsPostcode))
      }

    }
  }




  /**
    * a POST route which will process all `BulkQuery` items in the `BulkBody`
    * @return reduced information on found addresses (uprn, formatted address)
    */
  def bulk(limitperaddress: Option[String], historical: Option[String] = None, matchthreshold: Option[String] = None): Action[BulkBody] = Action(parse.json[BulkBody]) { implicit request =>
    logger.info(s"#bulkQuery with ${request.body.addresses.size} items")
    // check API key
    val apiKey = request.headers.get("authorization").getOrElse(missing)
    val keyStatus = checkAPIkey(apiKey)

    // check source
    val source = request.headers.get("Source").getOrElse(missing)
    val sourceStatus = checkSource(source)

    val hist = historical match {
      case Some(x) => Try(x.toBoolean).getOrElse(true)
      case None => true
    }

    // get the defaults and maxima for the paging parameters from the config
    val defLimit = conf.config.bulk.limitperaddress
    val maxLimit = conf.config.bulk.maxLimitperaddress
    val limval = limitperaddress.getOrElse(defLimit.toString)
    val limitInvalid = Try(limval.toInt).isFailure
    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)

    val defThreshold = conf.config.bulk.matchThreshold
    val threshval = matchthreshold.getOrElse(defThreshold.toString)
    val thresholdInvalid = Try(threshval.toFloat).isFailure
    val thresholdFloat = Try(threshval.toFloat).toOption.getOrElse(defThreshold)
    val thresholdNotInRange = !(thresholdFloat >= 0 && thresholdFloat <= 100)

    if (sourceStatus == missing) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = SourceMissingError.message)
      jsonUnauthorized(SourceMissing)
    } else if (sourceStatus == invalid) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = SourceInvalidError.message)
      jsonUnauthorized(SourceInvalid)
    } else if (keyStatus == missing) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = ApiKeyMissingError.message)
      jsonUnauthorized(KeyMissing)
    } else if (keyStatus == invalid) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = ApiKeyInvalidError.message)
      jsonUnauthorized(KeyInvalid)
    } else if (limitInvalid) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = LimitNotNumericAddressResponseError.message)
      jsonBadRequest(LimitNotNumeric)
    } else if (limitInt < 1) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = LimitTooSmallAddressResponseError.message)
      jsonBadRequest(LimitTooSmall)
    } else if (limitInt > maxLimit) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = LimitTooLargeAddressResponseError.message)
      jsonBadRequest(LimitTooLarge)
    } else if (thresholdInvalid) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = ThresholdNotNumericAddressResponseError.message)
      jsonBadRequest(ThresholdNotNumeric)
    } else if (thresholdNotInRange) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = ThresholdNotInRangeAddressResponseError.message)
      jsonBadRequest(ThresholdNotInRange)
    } else {
      val requestsData: Stream[BulkAddressRequestData] = requestDataFromRequest(request)

      val configOverwrite: Option[QueryParamsConfig] = request.body.config

      bulkQuery(requestsData, configOverwrite, Some(limitInt), false, hist, thresholdFloat)
    }
  }

  private def requestDataFromRequest(request: Request[BulkBody]): Stream[BulkAddressRequestData] = request.body.addresses.toStream.map {
    row => BulkAddressRequestData(row.id, row.address, parser.parse(row.address))
  }

  /**
    * a POST route which will process all `BulkQuery` items in the `BulkBody`
    * this version is slower and more memory-consuming
    * @return all the information on found addresses (uprn, formatted address, found address json object)
    */
  def bulkFull(limitperaddress: Option[String], historical: Option[String] = None, matchthreshold: Option[String] = None): Action[BulkBody] = Action(parse.json[BulkBody]) { implicit request =>
    logger.info(s"#bulkFullQuery with ${request.body.addresses.size} items")
    // check API key
    val apiKey = request.headers.get("authorization").getOrElse(missing)
    val keyStatus = checkAPIkey(apiKey)

    // check source
    val source = request.headers.get("Source").getOrElse(missing)
    val sourceStatus = checkSource(source)

    val hist = historical match {
      case Some(x) => Try(x.toBoolean).getOrElse(true)
      case None => true
    }

    val defLimit = conf.config.bulk.limitperaddress
    val maxLimit = conf.config.bulk.maxLimitperaddress
    val limval = limitperaddress.getOrElse(defLimit.toString)
    val limitInvalid = Try(limval.toInt).isFailure
    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)

    val defThreshold = conf.config.bulk.matchThreshold
    val threshval = matchthreshold.getOrElse(defThreshold.toString)
    val thresholdInvalid = Try(threshval.toFloat).isFailure
    val thresholdFloat = Try(threshval.toFloat).toOption.getOrElse(defThreshold)
    logger.info("threshold = " + thresholdFloat)
    val thresholdNotInRange = !(thresholdFloat >= 0 && thresholdFloat <= 100)

    if (sourceStatus == missing) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = SourceMissingError.message)
      jsonUnauthorized(SourceMissing)
    } else if (sourceStatus == invalid) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = SourceInvalidError.message)
      jsonUnauthorized(SourceInvalid)
    } else if (keyStatus == missing) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = ApiKeyMissingError.message)
      jsonUnauthorized(KeyMissing)
    } else if (keyStatus == invalid) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = ApiKeyInvalidError.message)
      jsonUnauthorized(KeyInvalid)
    } else if (limitInvalid) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = LimitNotNumericAddressResponseError.message)
      jsonBadRequest(LimitNotNumeric)
    } else if (limitInt < 1) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = LimitTooSmallAddressResponseError.message)
      jsonBadRequest(LimitTooSmall)
    } else if (limitInt > maxLimit) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = LimitTooLargeAddressResponseError.message)
      jsonBadRequest(LimitTooLarge)
    } else if (thresholdInvalid) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = ThresholdNotNumericAddressResponseError.message)
      jsonBadRequest(ThresholdNotNumeric)
    } else if (thresholdNotInRange) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = ThresholdNotInRangeAddressResponseError.message)
      jsonBadRequest(ThresholdNotInRange)
    } else {
      val requestsData: Stream[BulkAddressRequestData] = requestDataFromRequest(request)

      val configOverwrite: Option[QueryParamsConfig] = request.body.config

      bulkQuery(requestsData, configOverwrite, Some(limitInt), includeFullAddress = true, hist, thresholdFloat)
    }
  }

  /**
    * Bulk endpoint that accepts tokens instead of input texts for each address
    * @return reduced info on found addresses
    */
  def bulkDebug(limitperaddress: Option[String], historical: Option[String] = None, matchthreshold: Option[String] = None): Action[BulkBodyDebug] = Action(parse.json[BulkBodyDebug]) { implicit request =>
    logger.info(s"#bulkDebugQuery with ${request.body.addresses.size} items")
    // check API key
    val apiKey = request.headers.get("authorization").getOrElse(missing)
    val keyStatus = checkAPIkey(apiKey)

    // check source
    val source = request.headers.get("Source").getOrElse(missing)
    val sourceStatus = checkSource(source)

    val hist = historical match {
      case Some(x) => Try(x.toBoolean).getOrElse(true)
      case None => true
    }
    val defLimit = conf.config.bulk.limitperaddress
    val maxLimit = conf.config.bulk.maxLimitperaddress
    val limval = limitperaddress.getOrElse(defLimit.toString)
    val limitInvalid = Try(limval.toInt).isFailure
    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)

    val defThreshold = conf.config.bulk.matchThreshold
    val threshval = matchthreshold.getOrElse(defThreshold.toString)
    val thresholdInvalid = Try(threshval.toFloat).isFailure
    val thresholdFloat = Try(threshval.toFloat).toOption.getOrElse(defThreshold)
    logger.info("threshold = " + thresholdFloat)
    val thresholdNotInRange = !(thresholdFloat >= 0 && thresholdFloat <= 100)

    if (sourceStatus == missing) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = SourceMissingError.message)
      jsonUnauthorized(SourceMissing)
    } else if (sourceStatus == invalid) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = SourceInvalidError.message)
      jsonUnauthorized(SourceInvalid)
    } else if (keyStatus == missing) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = ApiKeyMissingError.message)
      jsonUnauthorized(KeyMissing)
    } else if (keyStatus == invalid) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = ApiKeyInvalidError.message)
      jsonUnauthorized(KeyInvalid)
    } else if (limitInvalid) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = LimitNotNumericAddressResponseError.message)
      jsonBadRequest(LimitNotNumeric)
    } else if (limitInt < 1) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = LimitTooSmallAddressResponseError.message)
      jsonBadRequest(LimitTooSmall)
    } else if (limitInt > maxLimit) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = LimitTooLargeAddressResponseError.message)
      jsonBadRequest(LimitTooLarge)
    } else if (thresholdInvalid) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = ThresholdNotNumericAddressResponseError.message)
      jsonBadRequest(ThresholdNotNumeric)
    } else if (thresholdNotInRange) {
      Splunk.log(IP = request.remoteAddress, url = request.uri, isBulk = true, badRequestMessage = ThresholdNotInRangeAddressResponseError.message)
      jsonBadRequest(ThresholdNotInRange)
    } else {
      val requestsData: Stream[BulkAddressRequestData] = request.body.addresses.toStream.map {
        row => BulkAddressRequestData(row.id, row.tokens.values.mkString(" "), row.tokens)
      }
      val configOverwrite: Option[QueryParamsConfig] = request.body.config

      bulkQuery(requestsData, configOverwrite, Some(limitInt), false, hist, thresholdFloat)
    }
  }


  private def bulkQuery(
    requestData: Stream[BulkAddressRequestData],
    configOverwrite: Option[QueryParamsConfig],
    limitperaddress: Option[Int],
    includeFullAddress: Boolean = false,
    historical: Boolean,
    matchThreshold: Float
  )(implicit request: Request[_]): Result = {

    val networkid = request.headers.get("authorization").getOrElse("Anon").split("_").headOption.getOrElse("")

    val startingTime = System.currentTimeMillis()

    val defaultBatchSize = conf.config.bulk.batch.perBatch
    val resultLimit = limitperaddress.getOrElse(conf.config.bulk.limitperaddress)
    val results: Stream[Seq[AddressBulkResponseAddress]] = iterateOverRequestsWithBackPressure(requestData, defaultBatchSize, Some(resultLimit), configOverwrite, historical, matchThreshold)

    logger.info(s"#bulkQuery processed")

    // Used to distinguish individual bulk logs
    val uuid = java.util.UUID.randomUUID.toString

    val bulkItems = results.flatMap{ addresses =>
      addresses
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
    limitperaddress: Option[Int] = None,
    configOverwrite: Option[QueryParamsConfig] = None,
    historical: Boolean,
    matchThreshold: Float,
    canUpScale: Boolean = true,
    successfulResults: Stream[Seq[AddressBulkResponseAddress]] = Stream.empty
  ): Stream[Seq[AddressBulkResponseAddress]] = {

    Splunk.log(isBulk = true, batchSize = miniBatchSize.toString)

    val defaultBatchSize = conf.config.bulk.batch.perBatch
    val bulkSizeWarningThreshold = conf.config.bulk.batch.warningThreshold

    if (miniBatchSize < defaultBatchSize * bulkSizeWarningThreshold)
      logger.warn(s"#bulkQuery mini-bulk size it less than a ${defaultBatchSize * bulkSizeWarningThreshold}: size = $miniBatchSize , check if everything is fine with ES")
    else logger.info(s"#bulkQuery sending a mini-batch of the size $miniBatchSize")

    val miniBatch = requests.take(miniBatchSize)
    val requestsAfterMiniBatch = requests.drop(miniBatchSize)
    val addressesPerAddress = limitperaddress.getOrElse(conf.config.bulk.limitperaddress)
    val result: BulkAddresses = Await.result(queryBulkAddresses(miniBatch, addressesPerAddress, configOverwrite, historical, matchThreshold), Duration.Inf)

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

      iterateOverRequestsWithBackPressure(requestsLeft, newMiniBatchSize, limitperaddress, configOverwrite, historical, matchThreshold, nextCanUpScale, successfulResults ++ result.successfulBulkAddresses)
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
    limitperaddress: Int,
    configOverwrite: Option[QueryParamsConfig] = None,
    historical: Boolean,
    matchThreshold: Float
  ): Future[BulkAddresses] = {

    val bulkAddresses: Future[Stream[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]] = esRepo.queryBulk(inputs, limitperaddress, configOverwrite, historical, matchThreshold)

    val successfulAddresses: Future[Stream[Seq[AddressBulkResponseAddress]]] = bulkAddresses.map(collectSuccessfulAddresses)

    val failedAddresses: Future[Stream[BulkAddressRequestData]] = bulkAddresses.map(collectFailedAddresses)

    // transform (Future[X], Future[Y]) into Future[Z[X, Y]]
    for {
      successful <- successfulAddresses
      failed <- failedAddresses
    } yield BulkAddresses(successful, failed)
  }


  private def collectSuccessfulAddresses(addresses: Stream[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]): Stream[Seq[AddressBulkResponseAddress]] =
    addresses.collect {
      case Right(bulkAddresses) => bulkAddresses
    }

  private def collectFailedAddresses(addresses: Stream[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]): Stream[BulkAddressRequestData] =
    addresses.collect {
      case Left(address) => address
    }

  /**
    * Method to validate api key
    * @param apiKey
    * @return not required, valid, invalid or missing
    */
  def checkAPIkey(apiKey: String): String = {
    val keyRequired = conf.config.apiKeyRequired
    if (keyRequired) {
      val masterKey = conf.config.masterKey
      val apiKeyTest = apiKey.drop(apiKey.indexOf("_")+1)
      apiKeyTest match {
        case key if key == missing => missing
        case key if key == masterKey => valid
        case _ => invalid
      }
    } else {
      notRequired
    }
  }

  /**
    * Method to check source of query
    * @param source
    * @return not required, valid, invalid or missing
    */
  def checkSource(source: String): String = {
    val sourceRequired = conf.config.sourceRequired
    if (sourceRequired) {
      val sourceName = conf.config.sourceKey
      source match {
        case key if key == missing => missing
        case key if key == sourceName => valid
        case _ => invalid
      }
    } else {
      notRequired
    }
  }

}
