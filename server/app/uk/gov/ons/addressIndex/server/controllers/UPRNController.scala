package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc._
import retry.Success
import uk.gov.ons.addressIndex.model.db.index.HybridAddress
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseAddress, FailedRequestToEsError, OkAddressResponseStatus}
import uk.gov.ons.addressIndex.model.server.response.uprn.{AddressByUprnResponse, AddressByUprnResponseContainer}
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
class UPRNController @Inject()(val controllerComponents: ControllerComponents,
                               esRepo: ElasticsearchRepository,
                               conf: ConfigModule,
                               versionProvider: VersionModule,
                               overloadProtection: APIThrottle,
                               uprnValidation: UPRNControllerValidation
                              )(implicit ec: ExecutionContext)
  extends PlayHelperController(versionProvider) with UPRNControllerResponse {

  lazy val logger = new AddressAPILogger("address-index-server:UPRNController")

  /**
    * UPRN query API
    *
    * @param uprn uprn of the address to be fetched
    * @return
    */
  def uprnQuery(uprn: String,
                historical: Option[String] = None,
                verbose: Option[String] = None,
                epoch: Option[String] = None,
                pafdefault: Option[String] = None
               ): Action[AnyContent] = Action async { implicit req =>

    val clusterid = conf.config.elasticSearch.clusterPolicies.uprn

    val pafDefault = pafdefault.flatMap(x => Try(x.toBoolean).toOption).getOrElse(false)

    val endpointType = "uprn"

    val hist = historical.flatMap(x => Try(x.toBoolean).toOption).getOrElse(true)
    val verb = verbose.flatMap(x => Try(x.toBoolean).toOption).getOrElse(false)

    val epochVal = epoch.getOrElse("")

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
        endpoint = endpointType, activity = activity, clusterid = clusterid,
        pafDefault = pafDefault
      )
    }

    val queryValues = QueryValues(
      uprn = Some(uprn),
      epoch = Some(epochVal),
      historical = Some(hist),
      verbose = Some(verb),
      pafDefault = Some(pafDefault)
    )

    val result: Option[Future[Result]] =
      uprnValidation.validateUprn(uprn, queryValues)
        .orElse(uprnValidation.validateSource(queryValues))
        .orElse(uprnValidation.validateKeyStatus(queryValues))
        .orElse(uprnValidation.validateEpoch(queryValues))
        .orElse(None)

    result match {

      case Some(res) =>
        res // a validation error

      case _ =>
        // TODO do we even need `verbose` any more? Is it still used?
        val args = UPRNArgs(
          uprn = uprn,
          uprns = null,
          historical = hist,
          epoch = epochVal,
          auth = req.headers.get("authorization").getOrElse("Anon"),
          pafDefault = pafDefault
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

            val address = AddressResponseAddress.fromHybridAddress(hybridAddress, verb, pafdefault=pafDefault)

            writeLog(
              formattedOutput = address.formattedAddressNag, numOfResults = "1",
              score = hybridAddress.score.toString, activity = "address_request"
            )

            jsonOk(
              AddressByUprnResponseContainer(
                apiVersion = apiVersion,
                dataVersion = dataVersion,
                response = AddressByUprnResponse(
                  address = Some(address),
                  historical = hist,
                  epoch = epochVal,
                  verbose = verb,
                  pafdefault = pafDefault
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
