package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.ons.addressIndex.model.db.index.HybridAddress
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseAddress, FailedRequestToEsError, OkAddressResponseStatus}
import uk.gov.ons.addressIndex.model.server.response.eq.{AddressByEQUprnResponse, AddressByEQUprnResponseContainer}
import uk.gov.ons.addressIndex.server.model.dao.QueryValues
import uk.gov.ons.addressIndex.server.modules.response.UPRNControllerResponse
import uk.gov.ons.addressIndex.server.modules.validation.UPRNControllerValidation
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, ElasticsearchRepository, VersionModule, _}
import uk.gov.ons.addressIndex.server.utils.{APIThrottle, AddressAPILogger}

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
  def uprnQuery(uprn: String,
                bestMatchAddressType: String,
                historical: Option[String] = None,
                verbose: Option[String] = None,
                epoch: Option[String] = None
               ): Action[AnyContent] = Action async { implicit req =>

    val clusterid = conf.config.elasticSearch.clusterPolicies.uprn

    val endpointType = "equprn"

    val hist = historical.flatMap(x => Try(x.toBoolean).toOption).getOrElse(true)
    val verb = verbose.flatMap(x => Try(x.toBoolean).toOption).getOrElse(false)

    val addressType = bestMatchAddressType match {
      case "paf" => AddressResponseAddress.AddressTypes.paf
      case "welshpaf" => AddressResponseAddress.AddressTypes.welshPaf
      case "nag" => AddressResponseAddress.AddressTypes.nag
      case "welshnag" => AddressResponseAddress.AddressTypes.welshNag
      case "nisra" => AddressResponseAddress.AddressTypes.nisra
      case _ => bestMatchAddressType
    }

    val epochVal = epoch.getOrElse("")

    val startingTime = System.currentTimeMillis()

    def writeLog(badRequestErrorMessage: String = "", notFound: Boolean = false, formattedOutput: String = "", numOfResults: String = "", score: String = "", activity: String = ""): Unit = {
      val responseTime = System.currentTimeMillis() - startingTime
      val networkid = Try(if (req.headers.get("authorization").getOrElse("Anon").indexOf("+") > 0) req.headers.get("authorization").getOrElse("Anon").split("\\+")(0) else req.headers.get("authorization").getOrElse("Anon").split("_")(0)).getOrElse("")
      val organisation = Try(if (req.headers.get("authorization").getOrElse("Anon").indexOf("+") > 0) req.headers.get("authorization").getOrElse("Anon").split("\\+")(0).split("_")(1) else "not set").getOrElse("")


      logger.systemLog(ip = req.remoteAddress, url = req.uri, responseTimeMillis = responseTime.toString,
        uprn = uprn, isNotFound = notFound, formattedOutput = formattedOutput,
        numOfResults = numOfResults, score = score, networkid = networkid, organisation = organisation,
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
        // TODO do we even need `verbose` any more? Is it still used?
        val args = UPRNArgs(
          uprn = uprn,
          historical = hist,
          epoch = epochVal,
        )

        val request: Future[Option[HybridAddress]] = overloadProtection.breaker.withCircuitBreaker(
          esRepo.runUPRNQuery(args)
        )

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
                response = AddressByEQUprnResponse(
                  address = Some(address),
                  addressType = addressType,
                  historical = hist,
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