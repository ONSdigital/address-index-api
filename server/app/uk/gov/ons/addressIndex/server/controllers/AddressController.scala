package uk.gov.ons.addressIndex.server.controllers

import java.text.SimpleDateFormat
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.ons.addressIndex.model.db.index.HybridAddresses
import uk.gov.ons.addressIndex.model.server.response._
import uk.gov.ons.addressIndex.parsers.Tokens
import uk.gov.ons.addressIndex.server.modules.response.AddressIndexResponse
import uk.gov.ons.addressIndex.server.modules.validation.AddressValidation
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, ElasticsearchRepository, ParserModule, VersionModule}
import uk.gov.ons.addressIndex.server.utils.{AddressAPILogger, _}
import scala.math._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

@Singleton
class AddressController @Inject()(val controllerComponents: ControllerComponents,
                                   esRepo: ElasticsearchRepository,
                                   parser: ParserModule,
                                   conf: ConfigModule,
                                   versionProvider: VersionModule,
                                   overloadProtection: APIThrottler,
                                   addressValidation: AddressValidation
                                 )(implicit ec: ExecutionContext)
  extends PlayHelperController(versionProvider) with AddressIndexResponse {

  lazy val logger = AddressAPILogger("address-index-server:AddressController")

  val missing: String = "missing"
  val invalid: String = "invalid"
  val valid: String = "valid"
  val notRequired: String = "not required"
  val DATE_FORMAT = "yyyy-MM-dd"

  /**
    * Codelist List API
    *
    * @return Json response with codelist
    */
  def codeList(): Action[AnyContent] = Action async { implicit req =>
    val codList = Tokens.codeList.map { clval =>

      new AddressResponseCodelist(name = clval.split("=").headOption.getOrElse(""),
        description = clval.split("=").lastOption.getOrElse(""))
    }

    val codeListContainer = new AddressResponseCodelistListContainer(codList)

    Future(Ok(Json.toJson(codeListContainer)))
  }

  /**
    * Classification List API
    *
    * @return Json response with codelist
    */
  def codeListClassification(): Action[AnyContent] = Action async { implicit req =>
    val classList = Tokens.classList.map { classval =>

      new AddressResponseClassification(code = classval.split("=").headOption.getOrElse(""),
        label = classval.split("=").lastOption.getOrElse(""))
    }

    val codListContainer = new AddressResponseClassificationListContainer(classList)
    Future(Ok(Json.toJson(codListContainer)))
  }

  /**
    * Custodian List API
    *
    * @return Json response with codelist
    */
  def codeListCustodian(): Action[AnyContent] = Action async { implicit req =>
    val custList = Tokens.custodianList.map { custval =>

      new AddressResponseCustodian(
        custval.split(",").lift(0).getOrElse(""),
        custval.split(",").lift(1).getOrElse(""),
        custval.split(",").lift(2).getOrElse(""),
        custval.split(",").lift(3).getOrElse(""),
        custval.split(",").lift(4).getOrElse(""),
        custval.split(",").lift(5).getOrElse("")
      )
    }

    val custListContainer = new AddressResponseCustodianListContainer(custList)
    Future(Ok(Json.toJson(custListContainer)))
  }

  /**
    * Source List API
    *
    * @return Json response with codelist
    */
  def codeListSource(): Action[AnyContent] = Action async { implicit req =>

    val sourceList = Tokens.sourceList.map { sourceval =>
      new AddressResponseSource(sourceval.split("=").headOption.getOrElse(""),
        sourceval.split("=").lastOption.getOrElse(""))
    }

    val sourceListContainer = new AddressResponseSourceListContainer(sourceList)
    Future(Ok(Json.toJson(sourceListContainer)))
  }

  /**
    * Logical Status List API
    *
    * @return Json response with codelist
    */
  def codeListLogicalStatus(): Action[AnyContent] = Action async { implicit req =>

    val logicalList = Tokens.logicalStatusList.map { logstatval =>
      new AddressResponseLogicalStatus(logstatval.split("=").headOption.getOrElse(""),
        logstatval.split("=").lastOption.getOrElse(""))
    }

    val logicalListContainer = new AddressResponseLogicalStatusListContainer(logicalList)
    Future(Ok(Json.toJson(logicalListContainer)))
  }


  /**
    * Address query API
    *
    * @param input the address query
    * @return Json response with addresses information
    */
  def addressQuery(implicit input: String, offset: Option[String] = None, limit: Option[String] = None,
                   classificationfilter: Option[String] = None, rangekm: Option[String] = None,
                   lat: Option[String] = None, lon: Option[String] = None,
                   startDate: Option[String] = None, endDate: Option[String] = None,
                   historical: Option[String] = None,
                   matchthreshold: Option[String] = None): Action[AnyContent] = Action async { implicit req =>

    val startingTime: Long = System.currentTimeMillis()
    val IP: String = req.remoteAddress
    val URL: String = req.uri

    val startDateVal = startDate.getOrElse("")
    val endDateVal = endDate.getOrElse("")

    // get the defaults and maxima for the paging parameters from the config
    val defLimit = conf.config.elasticSearch.defaultLimit
    val defOffset = conf.config.elasticSearch.defaultOffset
    val defThreshold = conf.config.elasticSearch.matchThreshold

    val limval = limit.getOrElse(defLimit.toString)
    val offval = offset.getOrElse(defOffset.toString)
    val threshval = matchthreshold.getOrElse(defThreshold.toString)

    val filterString = classificationfilter.getOrElse("")

    val hist = historical match {
      case Some(x) => Try(x.toBoolean).getOrElse(true)
      case None => true
    }

    // validate radius parameters
    val rangeVal = rangekm.getOrElse("")
    val latVal = lat.getOrElse("")
    val lonVal = lon.getOrElse("")

    def writeLog(badRequestErrorMessage: String = "", formattedOutput: String = "", numOfResults: String = "", score: String = ""): Unit = {

      val networkid = req.headers.get("authorization").getOrElse("Anon").split("_")(0)

      logger.systemLog(ip = IP, url = URL, responseTimeMillis = (System.currentTimeMillis() - startingTime).toString,
        input = input, offset = offval, limit = limval, filter = filterString,
        endDate=endDateVal, startDate = startDateVal, historical = hist, rangekm = rangeVal, lat = latVal, lon = lonVal,
        badRequestMessage = badRequestErrorMessage, formattedOutput = formattedOutput,
        numOfResults = numOfResults, score = score, networkid = networkid)
    }

    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)
    val offsetInt = Try(offval.toInt).toOption.getOrElse(defOffset)
    val thresholdFloat = Try(threshval.toFloat).toOption.getOrElse(defThreshold)

    val result: Option[Future[Result]] =
      addressValidation.validateAddressFilter(classificationfilter)
        .orElse(addressValidation.validateStartDate(startDateVal))
        .orElse(addressValidation.validateEndDate(endDateVal))
        .orElse(addressValidation.validateThreshold(matchthreshold))
        .orElse(addressValidation.validateRange(rangekm))
        .orElse(addressValidation.validateSource)
        .orElse(addressValidation.validateKeyStatus)
        .orElse(addressValidation.validateAddressLimit(limit))
        .orElse(addressValidation.validateAddressOffset(offset))
        .orElse(addressValidation.validateInput(input))
        .orElse(addressValidation.validateLocation(lat, lon, rangekm))
        .orElse(None)

    result match {

      case Some(res) =>
        res // a validation error

      case _ =>
        val tokens = parser.parse(input)

        // try to get enough results to accurately calculate the hybrid score (may need to be more sophisticated)
        val minimumSample = conf.config.elasticSearch.minimumSample
        val limitExpanded = max(offsetInt + (limitInt * 2), minimumSample)

        val request: Future[HybridAddresses] =
          overloadProtection.breaker.withCircuitBreaker(esRepo.queryAddresses(
            tokens, 0, limitExpanded, filterString,
            rangeVal, latVal, lonVal, startDateVal, endDateVal, None, hist)
          )

        request.map {
          case HybridAddresses(hybridAddresses, maxScore, total) =>

            val addresses: Seq[AddressResponseAddress] = hybridAddresses.map(
              AddressResponseAddress.fromHybridAddress
            )
            //  calculate the elastic denominator value which will be used when scoring each address
            val elasticDenominator = Try(
              ConfidenceScoreHelper.calculateElasticDenominator(addresses.map(_.underlyingScore))
            ).getOrElse(1D)
            // calculate the Hopper and hybrid scores for each  address

            val scoredAddresses = HopperScoreHelper.getScoresForAddresses(
              addresses, tokens, elasticDenominator)

            // work out the threshold for accepting matches (default 5% -> 0.05)
            val threshold = Try((thresholdFloat / 100).toDouble).getOrElse(0.05D)

            // filter out scores below threshold, sort the resultant collection, highest score first
            val sortedAddresses = scoredAddresses.filter(_.confidenceScore > threshold).sortBy(
              _.confidenceScore)(Ordering[Double].reverse)
            // capture the number of matches before applying offset and limit

            val newTotal = sortedAddresses.length

            // trim the result list according to offset and limit paramters
            val limitedSortedAddresses = sortedAddresses.drop(offsetInt).take(limitInt)

            addresses.foreach { address =>
              writeLog(
                formattedOutput = address.formattedAddressNag, numOfResults = total.toString,
                score = address.underlyingScore.toString
              )
            }

            writeLog()

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
                  startDate=startDateVal,
                  endDate=endDateVal,
                  limit = limitInt,
                  offset = offsetInt,
                  total = newTotal,
                  maxScore = maxScore,
                  sampleSize = limitExpanded,
                  matchthreshold = thresholdFloat
                ),
                status = OkAddressResponseStatus
              )
            )
        }.recover {
          case NonFatal(exception) =>

            overloadProtection.currentStatus match {
              case ThrottlerStatus.HalfOpen => {
                logger.warn(s"Elasticsearch is overloaded or down (address input). Circuit breaker is Half Open: ${exception.getMessage}")
                TooManyRequests(Json.toJson(FailedRequestToEsTooBusy))
              }
              case ThrottlerStatus.Open => {
                logger.warn(s"Elasticsearch is overloaded or down (address input). Circuit breaker is open: ${exception.getMessage}")
                TooManyRequests(Json.toJson(FailedRequestToEsTooBusy))
              }
              case _ =>
                // Circuit Breaker is closed. Some other problem
                writeLog(badRequestErrorMessage = FailedRequestToEsError.message)
                logger.warn(s"Could not handle individual request (address input), problem with ES ${exception.getMessage}")
                InternalServerError(Json.toJson(FailedRequestToEs))
            }
        }
    }
  }

  /**
    * PartialAddress query API
    *
    * @param input input for the address to be fetched
    * @return Json response with addresses information
    */
    /*TODO: Add validation for 
    
        } else if (startDateInvalid) {
      writeSplunkLogs(badRequestErrorMessage = StartDateInvalidResponseError.message)
      futureJsonBadRequest(StartDateInvalid)
    } else if (endDateInvalid) {
      writeSplunkLogs(badRequestErrorMessage = EndDateInvalidResponseError.message)
      futureJsonBadRequest(EndDateInvalid)
    */
  def partialAddressQuery(input: String, offset: Option[String] = None, limit: Option[String] = None,
                          classificationfilter: Option[String] = None, startDate: Option[String], endDate: Option[String],
			  historical: Option[String] = None): Action[AnyContent] = Action async { implicit req =>
    val startingTime = System.currentTimeMillis()

    // get the defaults and maxima for the paging parameters from the config
    val defLimit = conf.config.elasticSearch.defaultLimitPartial
    val defOffset = conf.config.elasticSearch.defaultOffset

    val limval = limit.getOrElse(defLimit.toString)
    val offval = offset.getOrElse(defOffset.toString)

    val filterString = classificationfilter.getOrElse("")

    val startDateVal = startDate.getOrElse("")
    val endDateVal = endDate.getOrElse("")

    val hist = historical match {
      case Some(x) => Try(x.toBoolean).getOrElse(true)
      case None => true
    }

    def writeLog(doResponseTime: Boolean = true, badRequestErrorMessage: String = "", notFound: Boolean = false, formattedOutput: String = "", numOfResults: String = "", score: String = ""): Unit = {
      val responseTime = if (doResponseTime) (System.currentTimeMillis() - startingTime).toString else ""
      val networkid = req.headers.get("authorization").getOrElse("Anon").split("_")(0)
      logger.systemLog(
        ip = req.remoteAddress, url = req.uri, responseTimeMillis = responseTime,
        partialAddress = input, isNotFound = notFound, offset = offval,
        limit = limval, filter = filterString, badRequestMessage = badRequestErrorMessage,
        formattedOutput = formattedOutput,
        numOfResults = numOfResults, score = score, networkid = networkid, startDate = startDateVal, endDate = endDateVal,
        historical = hist
      )
    }

    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)
    val offsetInt = Try(offval.toInt).toOption.getOrElse(defOffset)

    val result: Option[Future[Result]] =
      addressValidation.validateAddressLimit(limit)
        .orElse(addressValidation.validateAddressOffset(offset))
        .orElse(addressValidation.validateStartDate(startDateVal))
        .orElse(addressValidation.validateEndDate(endDateVal))
        .orElse(addressValidation.validateSource)
        .orElse(addressValidation.validateKeyStatus)
        .orElse(addressValidation.validateInput(input))
        .orElse(None)

    result match {

      case Some(res) =>
        res // a validation error

      case _ =>

        val request: Future[HybridAddresses] =
          overloadProtection.breaker.withCircuitBreaker(
            esRepo.queryPartialAddress(input, offsetInt, limitInt, filterString, startDateVal, endDateVal, None, hist)
          )

        request.map {
          case HybridAddresses(hybridAddresses, maxScore, total) =>
            //TODO: Verify AddressResponsePartialAddress is correct after the merge
            val addresses: Seq[AddressResponsePartialAddress] = hybridAddresses.map(
              AddressResponsePartialAddress.fromHybridAddress
            )

            addresses.foreach { address =>
              writeLog(
                formattedOutput = address.formattedAddressNag, numOfResults = total.toString,
                score = address.underlyingScore.toString
              )
            }

            writeLog()

            jsonOk(
              AddressByPartialAddressResponseContainer(
                apiVersion = apiVersion,
                dataVersion = dataVersion,
                response = AddressByPartialAddressResponse(
                  input = input,
                  addresses = addresses,
                  filter = filterString,
                  historical = hist,
                  limit = limitInt,
                  offset = offsetInt,
                  total = total,
                  maxScore = maxScore,
                  startDate = startDateVal,
                  endDate = endDateVal
                ),
                status = OkAddressResponseStatus
              )
            )

        }.recover {
          case NonFatal(exception) =>

            overloadProtection.currentStatus match {
              case ThrottlerStatus.HalfOpen =>
                logger.warn(s"Elasticsearch is overloaded or down (address input). Circuit breaker is Half Open: ${exception.getMessage}")
                TooManyRequests(Json.toJson(FailedRequestToEsTooBusy))
              case ThrottlerStatus.Open =>
                logger.warn(s"Elasticsearch is overloaded or down (address input). Circuit breaker is open: ${exception.getMessage}")
                TooManyRequests(Json.toJson(FailedRequestToEsTooBusy))
              case _ =>
                // Circuit Breaker is closed. Some other problem
                writeLog(badRequestErrorMessage = FailedRequestToEsPartialAddressError.message)
                logger.warn(s"Could not handle individual request (partialAddress input), problem with ES ${exception.getMessage}")
                InternalServerError(Json.toJson(FailedRequestToEsPartialAddress))
            }
        }
    }
  }
}
