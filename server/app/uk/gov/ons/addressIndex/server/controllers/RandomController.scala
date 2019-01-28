package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.ons.addressIndex.model.db.index.{HybridAddresses, HybridAddressesSkinny}
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseAddress, FailedRequestToEsRandomError, OkAddressResponseStatus}
import uk.gov.ons.addressIndex.model.server.response.random.{AddressByRandomResponse, AddressByRandomResponseContainer}
import uk.gov.ons.addressIndex.server.modules._
import uk.gov.ons.addressIndex.server.modules.response.RandomControllerResponse
import uk.gov.ons.addressIndex.server.modules.validation.RandomControllerValidation
import uk.gov.ons.addressIndex.server.utils.{APIThrottler, AddressAPILogger, ThrottlerStatus}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

@Singleton
class RandomController @Inject()(val controllerComponents: ControllerComponents,
                                   esRepo: ElasticsearchRepository,
                                   conf: ConfigModule,
                                   versionProvider: VersionModule,
                                   overloadProtection: APIThrottler,
                                   randomValidation: RandomControllerValidation
                                  )(implicit ec: ExecutionContext)
  extends PlayHelperController(versionProvider) with RandomControllerResponse {

  lazy val logger = AddressAPILogger("address-index-server:RandomController")

  /**
    * Random query API
    *
    * @return Json response with addresses information
    */
  def randomQuery(classificationfilter: Option[String] = None, limit: Option[String] = None, historical: Option[String] = None, verbose: Option[String] = None, epoch: Option[String] = None): Action[AnyContent] = Action async { implicit req =>
    val startingTime = System.currentTimeMillis()

    val clusterid = conf.config.elasticSearch.clusterPolicies.random

    val defLimit = conf.config.elasticSearch.defaultLimitRandom

    val limval = limit.getOrElse(defLimit.toString)

    val filterString = classificationfilter.getOrElse("").replaceAll("\\s+","")
    val endpointType = "random"

    val hist = historical match {
      case Some(x) => Try(x.toBoolean).getOrElse(true)
      case None => true
    }

    val verb = verbose match {
      case Some(x) => Try(x.toBoolean).getOrElse(false)
      case None => false
    }

    val epochVal = epoch.getOrElse("")

    def writeLog(doResponseTime: Boolean = true, badRequestErrorMessage: String = "", notFound: Boolean = false, formattedOutput: String = "", numOfResults: String = "", score: String = "", activity: String = ""): Unit = {
      val responseTime = if (doResponseTime) (System.currentTimeMillis() - startingTime).toString else ""
      val networkid = if (req.headers.get("authorization").getOrElse("Anon").indexOf("+") > 0) req.headers.get("authorization").getOrElse("Anon").split("\\+")(0) else req.headers.get("authorization").getOrElse("Anon").split("_")(0)
      val organisation =  if (req.headers.get("authorization").getOrElse("Anon").indexOf("+") > 0) req.headers.get("authorization").getOrElse("Anon").split("\\+")(0).split("_")(1) else "not set"

      logger.systemLog(
        ip = req.remoteAddress, url = req.uri, responseTimeMillis = responseTime,
        isNotFound = notFound, filter = filterString, badRequestMessage = badRequestErrorMessage,
        limit = limval, formattedOutput = formattedOutput,
        numOfResults = numOfResults, score = score, networkid = networkid, organisation = organisation,
        historical = hist, epoch = epochVal, verbose = verb,
        endpoint = endpointType, activity = activity, clusterid = clusterid
      )
    }

    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)

    val result: Option[Future[Result]] =
      randomValidation.validateSource
          .orElse(randomValidation.validateRandomLimit(limit))
        .orElse(randomValidation.validateKeyStatus)
        .orElse(randomValidation.validateRandomFilter(classificationfilter))
        .orElse(randomValidation.validateEpoch(epoch))
        .orElse(None)

    result match {

      case Some(res) =>
        res // a validation error

      case _ =>

        if (verb==false) {
          val request: Future[HybridAddressesSkinny] =
            overloadProtection.breaker.withCircuitBreaker(
              esRepo.queryRandomSkinny(filterString, limitInt, hist, verb, epochVal)
            )

          request.map {
            case HybridAddressesSkinny(hybridAddresses, maxScore@_, total@_) =>

              val addresses: Seq[AddressResponseAddress] = hybridAddresses.map(
                AddressResponseAddress.fromHybridAddressSkinny(_,verb)
              )

              writeLog(activity = "random_address_request")

              jsonOk(
                AddressByRandomResponseContainer(
                  apiVersion = apiVersion,
                  dataVersion = dataVersion,
                  response = AddressByRandomResponse(
                    addresses = addresses,
                    filter = filterString,
                    historical = hist,
                    epoch = epochVal,
                    limit = limitInt,
                    verbose = verb
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
                  TooManyRequests(Json.toJson(FailedRequestToEsTooBusyRandom(exception.getMessage)))
                case ThrottlerStatus.Open =>
                  logger.warn(
                    s"Elasticsearch is overloaded or down (address input). Circuit breaker is open: ${exception.getMessage}"
                  )
                  TooManyRequests(Json.toJson(FailedRequestToEsTooBusyRandom(exception.getMessage)))
                case _ =>
                  // Circuit Breaker is closed. Some other problem
                  writeLog(badRequestErrorMessage = FailedRequestToEsRandomError.message)
                  logger.warn(
                    s"Could not handle individual request (random input), problem with ES ${exception.getMessage}"
                  )
                  InternalServerError(Json.toJson(FailedRequestToEsRandom(exception.getMessage)))
              }
          }
        }else{
          val request: Future[HybridAddresses] =
            overloadProtection.breaker.withCircuitBreaker(
              esRepo.queryRandom(filterString, limitInt, hist, verb, epochVal)
            )

          request.map {
            case HybridAddresses(hybridAddresses, maxScore@_, total@_) =>

              val addresses: Seq[AddressResponseAddress] = hybridAddresses.map(
                AddressResponseAddress.fromHybridAddress(_,verb)
              )

              writeLog(activity = "random_address_request")

              jsonOk(
                AddressByRandomResponseContainer(
                  apiVersion = apiVersion,
                  dataVersion = dataVersion,
                  response = AddressByRandomResponse(
                    addresses = addresses,
                    filter = filterString,
                    historical = hist,
                    epoch = epochVal,
                    limit = limitInt,
                    verbose = verb
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
                  TooManyRequests(Json.toJson(FailedRequestToEsTooBusyRandom(exception.getMessage)))
                case ThrottlerStatus.Open =>
                  logger.warn(
                    s"Elasticsearch is overloaded or down (address input). Circuit breaker is open: ${exception.getMessage}"
                  )
                  TooManyRequests(Json.toJson(FailedRequestToEsTooBusyRandom(exception.getMessage)))
                case _ =>
                  // Circuit Breaker is closed. Some other problem
                  writeLog(badRequestErrorMessage = FailedRequestToEsRandomError.message)
                  logger.warn(
                    s"Could not handle individual request (random input), problem with ES ${exception.getMessage}"
                  )
                  InternalServerError(Json.toJson(FailedRequestToEsRandom(exception.getMessage)))
              }
          }
        }




    }
  }
}
