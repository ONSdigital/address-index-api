package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc._
import retry.Success
import uk.gov.ons.addressIndex.model.db.index.HybridAddress
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseAddress, FailedRequestToEsError, OkAddressResponseStatus}
import uk.gov.ons.addressIndex.model.server.response.eq.{AddressByEQUprnResponse, AddressByEQUprnResponseContainer}
import uk.gov.ons.addressIndex.server.model.dao.QueryValues
import uk.gov.ons.addressIndex.server.modules.response.UPRNControllerResponse
import uk.gov.ons.addressIndex.server.modules.validation.UPRNControllerValidation
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, ElasticsearchRepository, VersionModule, _}
import uk.gov.ons.addressIndex.server.utils.{APIThrottle, AddressAPILogger}
import scala.concurrent.duration.DurationInt
import odelay.Timer.default
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

@Singleton
class EQUPRNController @Inject()(val controllerComponents: ControllerComponents,
                                 esRepo: ElasticsearchRepository,
                                 conf: ConfigModule,
                                 versionProvider: VersionModule,
                                 overloadProtection: APIThrottle,
                                 uprnValidation: UPRNControllerValidation
                                )(implicit ec: ExecutionContext)
  extends PlayHelperController(versionProvider) with UPRNControllerResponse {

  lazy val logger = new AddressAPILogger("address-index-server:EQUPRNController")

  /**
    * UPRN query API
    *
    * @param uprn uprn of the address to be fetched
    * @return
    */
  def uprnQueryEQ(uprn: String,
                addresstype: Option[String] = None,
                epoch: Option[String] = None
               ): Action[AnyContent] = Action async { implicit req =>

    val clusterid = conf.config.elasticSearch.clusterPolicies.uprn

    val endpointType = "equprn"

    val hist = true
    val verb = false

    val bestMatchAddressType: String = addresstype.getOrElse("paf")

    val addressType = bestMatchAddressType match {
      case "paf" => AddressResponseAddress.AddressTypes.paf
      case "welshpaf" => AddressResponseAddress.AddressTypes.welshPaf
      case "nag" => AddressResponseAddress.AddressTypes.nag
      case "welshnag" => AddressResponseAddress.AddressTypes.welshNag
      case "nisra" => AddressResponseAddress.AddressTypes.nisra
      case _ => bestMatchAddressType
    }

    val epochVal = epoch.getOrElse("")
    val tocLink = conf.config.termsAndConditionsLink
    val startingTime = System.currentTimeMillis()

    def writeLog(badRequestErrorMessage: String = "", notFound: Boolean = false, formattedOutput: String = "", numOfResults: String = "", score: String = "", activity: String = ""): Unit = {
      val responseTime = System.currentTimeMillis() - startingTime
      // Set the networkId field to the username supplied in the user header
      // if this is not present, extract the user and organisation from the api key
      val authVal = req.headers.get("authorization").getOrElse("Anon")
      val authHasPlus = authVal.indexOf("+") > 0
      val keyNetworkId = Try(if (authHasPlus) authVal.split("\\+")(0) else authVal.split("_")(0)).getOrElse("")
      val organisation = Try(if (authHasPlus) keyNetworkId.split("_")(1) else "not set").getOrElse("")
      val networkId = req.headers.get("user").getOrElse(keyNetworkId)

      logger.systemLog(ip = req.remoteAddress, url = req.uri, responseTimeMillis = responseTime.toString,
        uprn = uprn, isNotFound = notFound, formattedOutput = formattedOutput,
        numOfResults = numOfResults, score = score, networkid = networkId, organisation = organisation,
        historical = hist, epoch = epochVal, verbose = verb, badRequestMessage = badRequestErrorMessage,
        endpoint = endpointType, activity = activity, clusterid = clusterid
      )
    }

    val queryValues = QueryValues(
      uprn = Some(uprn),
      addressType = Some(bestMatchAddressType),
      epoch = Some(epochVal),
      historical = Some(hist),
      verbose = Some(verb),
    )

    val result: Option[Future[Result]] =
      uprnValidation.validateUprn(uprn, queryValues)
        .orElse(uprnValidation.validateAddressType(bestMatchAddressType, queryValues))
        .orElse(uprnValidation.validateSource(queryValues))
        .orElse(uprnValidation.validateKeyStatus(queryValues))
        .orElse(uprnValidation.validateEpoch(queryValues))
        .orElse(None)

    result match {

      case Some(res) =>
        res // a validation error

      case _ =>
        val args = UPRNArgs(
          uprn = uprn,
          uprns = null,
          historical = hist,
          epoch = epochVal,
        )

        implicit val success = Success[Option[HybridAddress]](_ != null)

        val request: Future[Option[HybridAddress]] =
          retry.Pause(3, 1.seconds).apply { ()  =>
            overloadProtection.breaker.withCircuitBreaker(
              esRepo.runUPRNQuery(args)
            )
          }

        request.map {
          case Some(hybridAddress) =>

            val address = AddressByEQUprnResponse.fromHybridAddress(hybridAddress, addressType)

            writeLog(
              formattedOutput = AddressResponseAddress.fromHybridAddress(hybridAddress, verb).formattedAddressNag, numOfResults = "1",
              score = hybridAddress.score.toString, activity = "address_request"
            )

            jsonOk(
              AddressByEQUprnResponseContainer(
                apiVersion = apiVersion,
                dataVersion = dataVersion,
                termsAndConditions = tocLink,
                response = AddressByEQUprnResponse(
                  address = Some(address),
                  addressType = addressType,
                  epoch = epochVal
                ),
                status = OkAddressResponseStatus
              )
            )

          case None =>
            writeLog(notFound = true)
            jsonNotFound(NoAddressFoundUprn(queryValues))

        }.recover {
          case NonFatal(exception) =>
            if (overloadProtection.breaker.isHalfOpen) {
              logger.warn(s"Elasticsearch is overloaded or down (uprn input). Circuit breaker is Half Open: ${exception.getMessage}")
              TooManyRequests(Json.toJson(FailedRequestToEsTooBusyUprn(exception.getMessage, queryValues)))
            }else if (overloadProtection.breaker.isOpen) {
              logger.warn(s"Elasticsearch is overloaded or down (uprn input). Circuit breaker is open: ${exception.getMessage}")
              TooManyRequests(Json.toJson(FailedRequestToEsTooBusyUprn(exception.getMessage, queryValues)))
            } else {
              // Circuit Breaker is closed. Some other problem
              writeLog(badRequestErrorMessage = FailedRequestToEsError.message)
              logger.warn(s"Could not handle individual request (uprn), problem with ES ${exception.getMessage}")
              InternalServerError(Json.toJson(FailedRequestToEsUprn(exception.getMessage, queryValues)))
            }
        }
    }
  }
}