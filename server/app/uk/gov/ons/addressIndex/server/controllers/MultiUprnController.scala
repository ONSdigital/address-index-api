package uk.gov.ons.addressIndex.server.controllers

import play.api.libs.json.Json
import play.api.mvc._
import retry.Success
import uk.gov.ons.addressIndex.model.db.index.HybridAddressCollection
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseAddress, FailedRequestToEsError, OkAddressResponseStatus}
import uk.gov.ons.addressIndex.model.server.response.address.AddressResponseAddressNonIDS.addressesToNonIDS
import uk.gov.ons.addressIndex.model.server.response.uprn.{AddressByMultiUprnResponse, AddressByMultiUprnResponseContainer}
import uk.gov.ons.addressIndex.model.MultiUprnBody
import uk.gov.ons.addressIndex.server.model.dao.{QueryValues, RequestValues}
import uk.gov.ons.addressIndex.server.modules._
import uk.gov.ons.addressIndex.server.modules.response.UPRNControllerResponse
import uk.gov.ons.addressIndex.server.modules.validation.UPRNControllerValidation
import uk.gov.ons.addressIndex.server.utils.{APIThrottle, AddressAPILogger}
import javax.inject.{Inject, Singleton}

import scala.concurrent.duration.{Duration, DurationInt}
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

@Singleton
class MultiUprnController @Inject()(val controllerComponents: ControllerComponents,
                                    esRepo: ElasticsearchRepository,
                                    conf: ConfigModule,
                                    versionProvider: VersionModule,
                                    overloadProtection: APIThrottle,
                                    uprnValidation: UPRNControllerValidation
                                   )(implicit ec: ExecutionContext)
  extends PlayHelperController(versionProvider) with UPRNControllerResponse {

  lazy val logger = new AddressAPILogger("address-index-server:MultiUPRNController")
  val circuitBreakerDisabled = conf.config.elasticSearch.circuitBreakerDisabled

  /**
    * a POST route which will process all `BulkQuery` items in the `BulkBody`
    *
    * @return reduced information on found addresses (uprn, formatted address)
    */
  def multiUprn(historical: Option[String] = None,
                verbose: Option[String] = None,
                epoch: Option[String] = None,
                pafdefault: Option[String] = None
          ): Action[MultiUprnBody] = Action(parse.json[MultiUprnBody]) async { implicit req =>

    logger.info(s"#multi UPRN query with ${req.body.uprns.size} items")
    val uprnString = req.body.uprns.toString()
    val uprns = req.body.uprns.toList
    val uprn = uprns.head

    val clusterid = conf.config.elasticSearch.clusterPolicies.uprn

    val pafDefault = pafdefault.flatMap(x => Try(x.toBoolean).toOption).getOrElse(false)

    val endpointType = "uprn"

    val hist = historical.flatMap(x => Try(x.toBoolean).toOption).getOrElse(true)
    val verb = verbose.flatMap(x => Try(x.toBoolean).toOption).getOrElse(false)

    val epochVal = epoch.getOrElse("")

    val startingTime = System.currentTimeMillis()

    def writeLog(responseCode: String = "200", badRequestErrorMessage: String = "", notFound: Boolean = false, formattedOutput: String = "", numOfResults: String = "", score: String = "", activity: String = ""): Unit = {
      val responseTime = System.currentTimeMillis() - startingTime
      // Set the networkId field to the username supplied in the user header
      // if this is not present, extract the user and organisation from the api key
      val authVal = req.headers.get("authorization").getOrElse("Anon")
      val authHasPlus = authVal.indexOf("+") > 0
      val keyNetworkId = Try(if (authHasPlus) authVal.split("\\+")(0) else authVal.split("_")(0)).getOrElse("")
      val organisation = Try(if (authHasPlus) keyNetworkId.split("_")(1) else "not set").getOrElse("")
      val networkId = req.headers.get("user").getOrElse(keyNetworkId)

      logger.systemLog(responsecode = responseCode, ip = req.remoteAddress, url = req.uri, responseTimeMillis = responseTime.toString,
        uprn = uprnString, isNotFound = notFound, formattedOutput = formattedOutput,
        numOfResults = numOfResults, score = score, networkid = networkId, organisation = organisation,
        historical = hist, epoch = epochVal, verbose = verb, badRequestMessage = badRequestErrorMessage,
        endpoint = endpointType, activity = activity, clusterid = clusterid, pafDefault = pafDefault
      )
    }

    val requestValues = RequestValues(ip=req.remoteAddress,url=req.uri,networkid=req.headers.get("user").getOrElse(""),endpoint=endpointType)

    val queryValues = QueryValues(
      uprns = Some(uprns),
      epoch = Some(epochVal),
      historical = Some(hist),
      verbose = Some(verb),
      pafDefault = Some(pafDefault)
    )

    val result: Option[Future[Result]] =
      uprnValidation.validateUprns(uprns,queryValues,requestValues)
        .orElse(uprnValidation.validateSource(queryValues,requestValues))
        .orElse(uprnValidation.validateKeyStatus(queryValues,requestValues))
        .orElse(uprnValidation.validateEpoch(queryValues,requestValues))
        .orElse(None)

    result match {

      case Some(res) =>
        res // a validation error

      case _ =>
        val args = UPRNArgs(
          uprn = "",
          uprns = uprns,
          historical = hist,
          epoch = epochVal,
          auth = req.headers.get("authorization").getOrElse("Anon"),
          pafDefault = pafDefault
        )

        implicit val success = Success[HybridAddressCollection](_ != null)

        val request: Future[HybridAddressCollection] =
          retry.Pause(3, 1.seconds).apply { ()  =>
            if (circuitBreakerDisabled) esRepo.runMultiUPRNQuery(args) else
            overloadProtection.breaker.withCircuitBreaker(
              esRepo.runMultiUPRNQuery(args)
            )
          }

        request.map {
          case HybridAddressCollection(hybridAddresses,_,_,_) =>

            val addresses: Seq[AddressResponseAddress] = hybridAddresses.map(
              AddressResponseAddress.fromHybridAddress(_, verbose = verb, pafdefault = pafDefault)
            )

            writeLog(
              formattedOutput = "Mutiple UPRN results", numOfResults = addresses.size.toString,
              score = "100", activity = "address_request"
            )

            jsonOk(
              AddressByMultiUprnResponseContainer(
                apiVersion = apiVersion,
                dataVersion = dataVersion,
                response = AddressByMultiUprnResponse(
                  addresses = addressesToNonIDS(addresses, "I"),
                  historical = hist,
                  epoch = epochVal,
                  verbose = verb,
                  pafdefault = pafDefault
                ),
                status = OkAddressResponseStatus
              )
            )

          case _ =>
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
              writeLog(responseCode = "500",badRequestErrorMessage = FailedRequestToEsError.message)
              logger.warn(s"Could not handle individual request (uprn), problem with ES ${exception.getMessage}")
              InternalServerError(Json.toJson(FailedRequestToEsUprn(exception.getMessage, queryValues)))
            }
        }
    }
  }
}
