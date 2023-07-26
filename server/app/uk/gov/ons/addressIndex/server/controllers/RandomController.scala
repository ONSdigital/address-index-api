package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.ons.addressIndex.model.db.index.HybridAddressCollection
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseAddress, FailedRequestToEsRandomError, OkAddressResponseStatus}
import uk.gov.ons.addressIndex.model.server.response.random.{AddressByRandomResponse, AddressByRandomResponseContainer}
import uk.gov.ons.addressIndex.server.model.dao.{QueryValues, RequestValues}
import uk.gov.ons.addressIndex.server.modules._
import uk.gov.ons.addressIndex.server.modules.response.RandomControllerResponse
import uk.gov.ons.addressIndex.server.modules.validation.RandomControllerValidation
import uk.gov.ons.addressIndex.server.utils.{APIThrottle, AddressAPILogger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

@Singleton
class RandomController @Inject()(val controllerComponents: ControllerComponents,
                                 esRepo: ElasticsearchRepository,
                                 conf: ConfigModule,
                                 versionProvider: VersionModule,
                                 overloadProtection: APIThrottle,
                                 randomValidation: RandomControllerValidation
                                )(implicit ec: ExecutionContext)
  extends PlayHelperController(versionProvider) with RandomControllerResponse {

  lazy val logger: AddressAPILogger = AddressAPILogger("address-index-server:RandomController")

  /**
    * Random query API
    *
    * @return Json response with addresses information
    */
  def randomQuery(classificationfilter: Option[String] = None,
                  limit: Option[String] = None,
                  historical: Option[String] = None,
                  verbose: Option[String] = None,
                  epoch: Option[String] = None,
                  eboost: Option[String] = None,
                  nboost: Option[String] = None,
                  sboost: Option[String] = None,
                  wboost: Option[String] = None,
                  lboost: Option[String] = None,
                  mboost: Option[String] = None,
                  jboost: Option[String] = None,
                  pafdefault: Option[String] = None
                 ): Action[AnyContent] = Action async { implicit req =>
    val startingTime = System.currentTimeMillis()

    val clusterid = conf.config.elasticSearch.clusterPolicies.random

    val pafDefault = pafdefault.flatMap(x => Try(x.toBoolean).toOption).getOrElse(false)

    val defLimit = conf.config.elasticSearch.defaultLimitRandom
    val limval = limit.getOrElse(defLimit.toString)

    val filterString = classificationfilter.getOrElse("").replaceAll("\\s+", "")
    val endpointType = "random"

    val hist = historical.flatMap(x => Try(x.toBoolean).toOption).getOrElse(true)
    val verb = verbose.flatMap(x => Try(x.toBoolean).toOption).getOrElse(false)

    val epochVal = epoch.getOrElse("")

    val eboostVal = if (eboost.getOrElse("1.0").isEmpty) "1.0" else eboost.getOrElse("1.0")
    val nboostVal = if (nboost.getOrElse("1.0").isEmpty) "1.0" else nboost.getOrElse("1.0")
    val sboostVal = if (sboost.getOrElse("1.0").isEmpty) "1.0" else sboost.getOrElse("1.0")
    val wboostVal = if (wboost.getOrElse("1.0").isEmpty) "1.0" else wboost.getOrElse("1.0")
    val lboostVal = if (lboost.getOrElse("1.0").isEmpty) "1.0" else nboost.getOrElse("1.0")
    val mboostVal = if (mboost.getOrElse("1.0").isEmpty) "1.0" else sboost.getOrElse("1.0")
    val jboostVal = if (jboost.getOrElse("1.0").isEmpty) "1.0" else wboost.getOrElse("1.0")

    val eboostDouble = Try(eboostVal.toDouble).toOption.getOrElse(1.0D)
    val nboostDouble = Try(nboostVal.toDouble).toOption.getOrElse(1.0D)
    val sboostDouble = Try(sboostVal.toDouble).toOption.getOrElse(1.0D)
    val wboostDouble = Try(wboostVal.toDouble).toOption.getOrElse(1.0D)
    val lboostDouble = Try(lboostVal.toDouble).toOption.getOrElse(1.0D)
    val mboostDouble = Try(mboostVal.toDouble).toOption.getOrElse(1.0D)
    val jboostDouble = Try(jboostVal.toDouble).toOption.getOrElse(1.0D)

    def writeLog(responseCode: String = "200", doResponseTime: Boolean = true, badRequestErrorMessage: String = "", notFound: Boolean = false, formattedOutput: String = "", numOfResults: String = "", score: String = "", activity: String = ""): Unit = {
      val responseTime = if (doResponseTime) (System.currentTimeMillis() - startingTime).toString else ""
      // Set the networkId field to the username supplied in the user header
      // if this is not present, extract the user and organisation from the api key
      val authVal = req.headers.get("authorization").getOrElse("Anon")
      val authHasPlus = authVal.indexOf("+") > 0
      val keyNetworkId = Try(if (authHasPlus) authVal.split("\\+")(0) else authVal.split("_")(0)).getOrElse("")
      val organisation = Try(if (authHasPlus) keyNetworkId.split("_")(1) else "not set").getOrElse("")
      val networkId = req.headers.get("user").getOrElse(keyNetworkId)
      logger.systemLog(responsecode = responseCode,
        ip = req.remoteAddress, url = req.uri, responseTimeMillis = responseTime,
        isNotFound = notFound, filter = filterString, badRequestMessage = badRequestErrorMessage,
        limit = limval, formattedOutput = formattedOutput,
        numOfResults = numOfResults, score = score, networkid = networkId, organisation = organisation,
        historical = hist, epoch = epochVal, verbose = verb,
        eboost = eboostVal, nboost = nboostVal, sboost = sboostVal, wboost = wboostVal,
        endpoint = endpointType, activity = activity, clusterid = clusterid, pafDefault = pafDefault
      )
    }

    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)

    val requestValues = RequestValues(ip=req.remoteAddress,url=req.uri,networkid=req.headers.get("user").getOrElse(""),endpoint=endpointType)

    val queryValues = QueryValues(
      epoch = Some(epochVal),
      filter = Some(filterString),
      historical = Some(hist),
      limit = Some(limitInt),
      verbose = Some(verb),
      eboost = Some(eboostDouble),
      nboost = Some(nboostDouble),
      sboost = Some(sboostDouble),
      wboost = Some(wboostDouble),
      lboost = Some(lboostDouble),
      mboost = Some(mboostDouble),
      jboost = Some(jboostDouble),
      pafDefault = Some(pafDefault)
    )

    val result: Option[Future[Result]] =
      randomValidation.validateSource(queryValues,requestValues)
        .orElse(randomValidation.validateRandomLimit(limit, queryValues,requestValues))
        .orElse(randomValidation.validateKeyStatus(queryValues,requestValues))
        .orElse(randomValidation.validateRandomFilter(classificationfilter, queryValues,requestValues))
        .orElse(randomValidation.validateEpoch(queryValues,requestValues))
        .orElse(randomValidation.validateBoosts(eboost,nboost,sboost,wboost,queryValues,requestValues))
        .orElse(None)

    result match {

      case Some(res) =>
        res // a validation error

      case _ =>
        val args = RandomArgs(
          filters = filterString,
          limit = limitInt,
          historical = hist,
          verbose = verb,
          epoch = epochVal,
          skinny = !verb,
          eboost = eboostDouble,
          nboost = nboostDouble,
          sboost = sboostDouble,
          wboost = wboostDouble,
          lboost = lboostDouble,
          mboost = mboostDouble,
          jboost = jboostDouble,
          pafDefault = pafDefault
        )

        val request: Future[HybridAddressCollection] =
          overloadProtection.breaker.withCircuitBreaker(
            esRepo.runMultiResultQuery(args)
          )

        request.map {
          case HybridAddressCollection(hybridAddresses, _, _, _) =>

            val addresses: Seq[AddressResponseAddress] = hybridAddresses.map(
              AddressResponseAddress.fromHybridAddress(_, verb, pafdefault=pafDefault)
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
                  verbose = verb,
                  eboost = eboostDouble,
                  nboost = nboostDouble,
                  sboost = sboostDouble,
                  wboost = wboostDouble,
                  lboost = lboostDouble,
                  mboost = mboostDouble,
                  jboost = jboostDouble,
                  pafdefault = pafDefault
                ),
                status = OkAddressResponseStatus
              )
            )

        }.recover {
          case NonFatal(exception) =>
            if (overloadProtection.breaker.isHalfOpen) {
              logger.warn(s"Elasticsearch is overloaded or down (random input). Circuit breaker is Half Open: ${exception.getMessage}")
              TooManyRequests(Json.toJson(FailedRequestToEsTooBusyRandom(exception.getMessage, queryValues)))
            }else if (overloadProtection.breaker.isOpen) {
              logger.warn(s"Elasticsearch is overloaded or down (random input). Circuit breaker is open: ${exception.getMessage}")
              TooManyRequests(Json.toJson(FailedRequestToEsTooBusyRandom(exception.getMessage, queryValues)))
            } else {
              // Circuit Breaker is closed. Some other problem
              writeLog(responseCode = "500",badRequestErrorMessage = FailedRequestToEsRandomError.message)
              logger.warn(s"Could not handle individual request (random input), problem with ES ${exception.getMessage}")
              InternalServerError(Json.toJson(FailedRequestToEsRandom(exception.getMessage, queryValues)))
            }
        }

    }
  }
}
