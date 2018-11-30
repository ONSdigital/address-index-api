package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import uk.gov.ons.addressIndex.model.db.index.HybridAddressesPartial
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseAddress, FailedRequestToEsPartialAddressError, OkAddressResponseStatus}
import uk.gov.ons.addressIndex.model.server.response.partialaddress.{AddressByPartialAddressResponse, AddressByPartialAddressResponseContainer}
import uk.gov.ons.addressIndex.server.modules.response.PartialAddressControllerResponse
import uk.gov.ons.addressIndex.server.modules.validation.PartialAddressControllerValidation
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, ElasticsearchRepository, ParserModule, VersionModule}
import uk.gov.ons.addressIndex.server.utils.{APIThrottler, AddressAPILogger, ThrottlerStatus}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

@Singleton
class PartialAddressController @Inject()(val controllerComponents: ControllerComponents,
  esRepo: ElasticsearchRepository,
  parser: ParserModule,
  conf: ConfigModule,
  versionProvider: VersionModule,
  overloadProtection: APIThrottler,
  partialAddressValidation: PartialAddressControllerValidation
)(implicit ec: ExecutionContext)
  extends PlayHelperController(versionProvider) with PartialAddressControllerResponse {

  lazy val logger = AddressAPILogger("address-index-server:PartialAddressController")

  /**
    * PartialAddress query API
    *
    * @param input input for the address to be fetched
    * @return Json response with addresses information
    */

  def partialAddressQuery(input: String, offset: Option[String] = None, limit: Option[String] = None,
    classificationfilter: Option[String] = None,
    // startDate: Option[String], endDate: Option[String],
    historical: Option[String] = None, verbose: Option[String] = None,
    startboost: Option[String] = None
  ): Action[AnyContent] = Action async { implicit req =>
    val startingTime = System.currentTimeMillis()

    val clusterid = conf.config.elasticSearch.clusterPolicies.partial

    // get the defaults and maxima for the paging parameters from the config
    val defLimit = conf.config.elasticSearch.defaultLimitPartial
    val defOffset = conf.config.elasticSearch.defaultOffset

    val limval = limit.getOrElse(defLimit.toString)
    val offval = offset.getOrElse(defOffset.toString)

    val filterString = classificationfilter.getOrElse("").replaceAll("\\s+","")
    val endpointType = "partial"

    //  val startDateVal = startDate.getOrElse("")
    //  val endDateVal = endDate.getOrElse("")
    val startDateVal = ""
    val endDateVal = ""

    val hist = historical match {
      case Some(x) => Try(x.toBoolean).getOrElse(true)
      case None => true
    }

    val verb = verbose match {
      case Some(x) => Try(x.toBoolean).getOrElse(false)
      case None => false
    }

    val defStartBoost = conf.config.elasticSearch.defaultStartBoost

    // query string param for testing only
    val sboost = startboost match {
      case Some(x) => Try(x.toInt).getOrElse(defStartBoost)
      case None => defStartBoost
    }

    def boostAtStart(inAddresses: Seq[AddressResponseAddress]): Seq[AddressResponseAddress] = {
      val boostedAddresses: Seq[AddressResponseAddress] = inAddresses.map {add => boostAddress(add)}
      boostedAddresses.sortBy(_.underlyingScore)(Ordering[Float].reverse)
    }

    def boostAddress(add: AddressResponseAddress): AddressResponseAddress =  {
   //   logger.warn("input =  " + input.toUpperCase())
   //   logger.warn("formatted address = " + add.formattedAddress.toUpperCase())
    //  logger.warn("underlying score = " + add.underlyingScore)
      if (add.formattedAddress.toUpperCase().replaceAll("[,]", "").startsWith(input.toUpperCase().replaceAll("[,]", ""))){
    //  logger.warn("uprating " + input.toUpperCase())
      add.copy(underlyingScore = add.underlyingScore + sboost)
    } else add.copy(underlyingScore = add.underlyingScore)
    }

    def writeLog(doResponseTime: Boolean = true, badRequestErrorMessage: String = "", notFound: Boolean = false, formattedOutput: String = "", numOfResults: String = "", score: String = "", activity: String = ""): Unit = {
      val responseTime = if (doResponseTime) (System.currentTimeMillis() - startingTime).toString else ""
      val networkid = if (req.headers.get("authorization").getOrElse("Anon").indexOf("+") > 0) req.headers.get("authorization").getOrElse("Anon").split("\\+")(0) else req.headers.get("authorization").getOrElse("Anon").split("_")(0)
      val organisation =  if (req.headers.get("authorization").getOrElse("Anon").indexOf("+") > 0) req.headers.get("authorization").getOrElse("Anon").split("\\+")(0).split("_")(1) else "not set"

      logger.systemLog(
        ip = req.remoteAddress, url = req.uri, responseTimeMillis = responseTime,
        partialAddress = input, isNotFound = notFound, offset = offval,
        limit = limval, filter = filterString, badRequestMessage = badRequestErrorMessage,
        formattedOutput = formattedOutput,
        numOfResults = numOfResults, score = score, networkid = networkid, organisation = organisation,
        startDate = startDateVal, endDate = endDateVal,
        historical = hist, verbose = verb, endpoint = endpointType, activity = activity, clusterid = clusterid
      )
    }

    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)
    val offsetInt = Try(offval.toInt).toOption.getOrElse(defOffset)

    val result: Option[Future[Result]] =
      partialAddressValidation.validatePartialLimit(limit)
        .orElse(partialAddressValidation.validatePartialOffset(offset))
  //      .orElse(partialAddressValidation.validateStartDate(startDateVal))
  //      .orElse(partialAddressValidation.validateEndDate(endDateVal))
        .orElse(partialAddressValidation.validateSource)
        .orElse(partialAddressValidation.validateKeyStatus)
        .orElse(partialAddressValidation.validateInput(input))
        .orElse(partialAddressValidation.validateAddressFilter(classificationfilter))
        .orElse(None)

    result match {

      case Some(res) =>
        res // a validation error

      case _ =>

        val request: Future[HybridAddressesPartial] =
          overloadProtection.breaker.withCircuitBreaker(
            esRepo.queryPartialAddress(input, offsetInt, limitInt, filterString, startDateVal, endDateVal, None, hist, verb)
          )

        request.map {
          case HybridAddressesPartial(hybridAddressesPartial, maxScore, total) =>
            val addresses: Seq[AddressResponseAddress] = hybridAddressesPartial.map(
              AddressResponseAddress.fromHybridAddressPartial(_,verb)
            )

            val sortAddresses = if (sboost > 0) boostAtStart(addresses) else addresses

//            addresses.foreach { address =>
//            writeLog(
//              formattedOutput = address.formattedAddressNag, numOfResults = total.toString,
//              score = address.underlyingScore.toString, activity = "address_response"
//            )
//          }

            writeLog(activity = "address_request")

            jsonOk(
              AddressByPartialAddressResponseContainer(
                apiVersion = apiVersion,
                dataVersion = dataVersion,
                response = AddressByPartialAddressResponse(
                  input = input,
                  addresses = sortAddresses,
                  filter = filterString,
                  historical = hist,
                  limit = limitInt,
                  offset = offsetInt,
                  total = total,
                  maxScore = maxScore,
                  startDate = startDateVal,
                  endDate = endDateVal,
                  verbose  = verb
                ),
                status = OkAddressResponseStatus
              )
            )

        }.recover {
          case NonFatal(exception) =>

            overloadProtection.currentStatus match {
              case ThrottlerStatus.HalfOpen =>
                logger.warn(s"Elasticsearch is overloaded or down (address input). Circuit breaker is Half Open: ${exception.getMessage}")
                TooManyRequests(Json.toJson(FailedRequestToEsTooBusyPartialAddress(exception.getMessage)))
              case ThrottlerStatus.Open =>
                logger.warn(s"Elasticsearch is overloaded or down (address input). Circuit breaker is open: ${exception.getMessage}")
                TooManyRequests(Json.toJson(FailedRequestToEsTooBusyPartialAddress(exception.getMessage)))
              case _ =>
                // Circuit Breaker is closed. Some other problem
                writeLog(badRequestErrorMessage = FailedRequestToEsPartialAddressError.message)
                logger.warn(s"Could not handle individual request (partialAddress input), problem with ES ${exception.getMessage}")
                InternalServerError(Json.toJson(FailedRequestToEsPartialAddress(exception.getMessage)))
            }
        }
    }
  }
}
