package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.ons.addressIndex.model.db.index.HybridAddresses
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseAddress, FailedRequestToEsPostcodeError, OkAddressResponseStatus}
import uk.gov.ons.addressIndex.model.server.response.postcode.{AddressByPostcodeResponse, AddressByPostcodeResponseContainer}
import uk.gov.ons.addressIndex.server.modules._
import uk.gov.ons.addressIndex.server.modules.response.PostcodeControllerResponse
import uk.gov.ons.addressIndex.server.modules.validation.PostcodeControllerValidation
import uk.gov.ons.addressIndex.server.utils.{APIThrottler, AddressAPILogger, ThrottlerStatus}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

@Singleton
class PostcodeController @Inject()(val controllerComponents: ControllerComponents,
  esRepo: ElasticsearchRepository,
  parser: ParserModule,
  conf: ConfigModule,
  versionProvider: VersionModule,
  overloadProtection: APIThrottler,
  postcodeValidation: PostcodeControllerValidation
)(implicit ec: ExecutionContext)
  extends PlayHelperController(versionProvider) with PostcodeControllerResponse {

  lazy val logger = AddressAPILogger("address-index-server:PostcodeController")

  /**
    * POSTCODE query API
    *
    * @param postcode postcode of the address to be fetched
    * @return Json response with addresses information
    */
  def postcodeQuery(postcode: String, offset: Option[String] = None, limit: Option[String] = None, classificationfilter: Option[String] = None,
                    startDate: Option[String] = None, endDate: Option[String] = None,
                    historical: Option[String] = None, verbose: Option[String] = None): Action[AnyContent] = Action async { implicit req =>
    val startingTime = System.currentTimeMillis()

    // get the defaults and maxima for the paging parameters from the config
    val defLimit = conf.config.elasticSearch.defaultLimitPostcode
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

    val verb = verbose match {
      case Some(x) => Try(x.toBoolean).getOrElse(true)
      case None => true
    }

    def writeLog(doResponseTime: Boolean = true, badRequestErrorMessage: String = "", notFound: Boolean = false, formattedOutput: String = "", numOfResults: String = "", score: String = ""): Unit = {
      val responseTime = if (doResponseTime) (System.currentTimeMillis() - startingTime).toString else ""
      val networkid = req.headers.get("authorization").getOrElse("Anon").split("_")(0)
      logger.systemLog(
        ip = req.remoteAddress, url = req.uri, responseTimeMillis = responseTime,
        postcode = postcode, isNotFound = notFound, offset = offval,
        limit = limval, filter = filterString, badRequestMessage = badRequestErrorMessage,
        formattedOutput = formattedOutput,
        numOfResults = numOfResults, score = score, networkid = networkid,
        startDate = startDateVal, endDate = endDateVal, historical = hist
      )
    }

    def trimAddresses (fullAddresses: Seq[AddressResponseAddress]): Seq[AddressResponseAddress] = {
      fullAddresses.map{address => address.copy(nag=None,paf=None,relatives=None,crossRefs=None)}
    }

    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)
    val offsetInt = Try(offval.toInt).toOption.getOrElse(defOffset)

    val result: Option[Future[Result]] =
      postcodeValidation.validatePostcodeLimit(limit)
        .orElse(postcodeValidation.validateStartDate(startDateVal))
        .orElse(postcodeValidation.validateEndDate(endDateVal))
        .orElse(postcodeValidation.validatePostcodeOffset(offset))
        .orElse(postcodeValidation.validateSource)
        .orElse(postcodeValidation.validateKeyStatus)
        .orElse(postcodeValidation.validatePostcodeFilter(classificationfilter))
        .orElse(postcodeValidation.validatePostcode(postcode))
        .orElse(None)

    result match {

      case Some(res) =>
        res // a validation error

      case _ =>

        val request: Future[HybridAddresses] =
          overloadProtection.breaker.withCircuitBreaker(
            esRepo.queryPostcode(postcode, offsetInt, limitInt, filterString, startDateVal, endDateVal, None, hist)
          )

        request.map {
          case HybridAddresses(hybridAddresses, maxScore, total) =>

            val addresses: Seq[AddressResponseAddress] = hybridAddresses.map(
              AddressResponseAddress.fromHybridAddress
            )

            addresses.foreach { address =>
              writeLog(
                formattedOutput = address.formattedAddressNag, numOfResults = total.toString,
                score = address.underlyingScore.toString
              )
            }

            writeLog()

            // if verbose is false, strip out full address details (these are needed for score so must be
            // removed retrospectively)
            val finalAddresses = if (verb) addresses else trimAddresses(addresses)

            jsonOk(
              AddressByPostcodeResponseContainer(
                apiVersion = apiVersion,
                dataVersion = dataVersion,
                response = AddressByPostcodeResponse(
                  postcode = postcode,
                  addresses = finalAddresses,
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
                logger.warn(
                  s"Elasticsearch is overloaded or down (address input). Circuit breaker is Half Open: ${exception.getMessage}"
                )
                TooManyRequests(Json.toJson(FailedRequestToEsTooBusyPostCode))
              case ThrottlerStatus.Open =>
                logger.warn(
                  s"Elasticsearch is overloaded or down (address input). Circuit breaker is open: ${exception.getMessage}"
                )
                TooManyRequests(Json.toJson(FailedRequestToEsTooBusyPostCode))
              case _ =>
                // Circuit Breaker is closed. Some other problem
                writeLog(badRequestErrorMessage = FailedRequestToEsPostcodeError.message)
                logger.warn(
                  s"Could not handle individual request (postcode input), problem with ES ${exception.getMessage}"
                )
                InternalServerError(Json.toJson(FailedRequestToEsPostcode))
            }
        }
    }
  }
}
