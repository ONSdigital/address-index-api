package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc._
import retry.Success
import uk.gov.ons.addressIndex.model.db.index.HybridAddressCollection
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseAddress, FailedRequestToEsPostcodeError, OkAddressResponseStatus}
import uk.gov.ons.addressIndex.model.server.response.address.AddressResponseAddressNonIDS.addressesToNonIDS
import uk.gov.ons.addressIndex.model.server.response.postcode.{AddressByPostcodeResponse, AddressByPostcodeResponseContainer}
import uk.gov.ons.addressIndex.server.model.dao.{QueryValues, RequestValues}
import uk.gov.ons.addressIndex.server.modules.response.PostcodeControllerResponse
import uk.gov.ons.addressIndex.server.modules.validation.PostcodeControllerValidation
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, ElasticsearchRepository, VersionModule, _}
import uk.gov.ons.addressIndex.server.utils.{APIThrottle, AddressAPILogger}
import odelay.Timer.default

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

@Singleton
class PostcodeController @Inject()(val controllerComponents: ControllerComponents,
                                   esRepo: ElasticsearchRepository,
                                   conf: ConfigModule,
                                   versionProvider: VersionModule,
                                   overloadProtection: APIThrottle,
                                   postcodeValidation: PostcodeControllerValidation
                                  )(implicit ec: ExecutionContext)
  extends PlayHelperController(versionProvider) with PostcodeControllerResponse {

  lazy val logger: AddressAPILogger = AddressAPILogger("address-index-server:PostcodeController")

  /**
    * POSTCODE query API
    *
    * @param postcode postcode of the address to be fetched
    * @return Json response with addresses information
    */
  def postcodeQuery(postcode: String,
                    offset: Option[String] = None,
                    limit: Option[String] = None,
                    classificationfilter: Option[String] = None,
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

    val clusterId = conf.config.elasticSearch.clusterPolicies.postcode

    val pafDefault = pafdefault.flatMap(x => Try(x.toBoolean).toOption).getOrElse(false)

    // get the defaults and maxima for the paging parameters from the config
    val defLimit = conf.config.elasticSearch.defaultLimitPostcode
    val defOffset = conf.config.elasticSearch.defaultOffset

    val limVal = limit.getOrElse(defLimit.toString)
    val offVal = offset.getOrElse(defOffset.toString)

    val filterString = classificationfilter.getOrElse("").replaceAll("\\s+", "")
    val endpointType = "postcode"

    val hist = historical.flatMap(x => Try(x.toBoolean).toOption).getOrElse(false)
    val verb = verbose.flatMap(x => Try(x.toBoolean).toOption).getOrElse(false)

    val epochVal = epoch.getOrElse("")

    val eboostVal = if (eboost.getOrElse("1.0").isEmpty) "1.0" else eboost.getOrElse("1.0")
    val nboostVal = if (nboost.getOrElse("1.0").isEmpty) "1.0" else nboost.getOrElse("1.0")
    val sboostVal = if (sboost.getOrElse("1.0").isEmpty) "1.0" else sboost.getOrElse("1.0")
    val wboostVal = if (wboost.getOrElse("1.0").isEmpty) "1.0" else wboost.getOrElse("1.0")
    val lboostVal = if (lboost.getOrElse("1.0").isEmpty) "1.0" else lboost.getOrElse("1.0")
    val mboostVal = if (mboost.getOrElse("1.0").isEmpty) "1.0" else mboost.getOrElse("1.0")
    val jboostVal = if (jboost.getOrElse("1.0").isEmpty) "1.0" else jboost.getOrElse("1.0")

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
        postcode = postcode, isNotFound = notFound, offset = offVal,
        limit = limVal, filter = filterString, badRequestMessage = badRequestErrorMessage,
        formattedOutput = formattedOutput,
        numOfResults = numOfResults, score = score, networkid = networkId, organisation = organisation,
        historical = hist, epoch = epochVal, verbose = verb,
        endpoint = endpointType, activity = activity, clusterid = clusterId,
        pafDefault = pafDefault
      )
    }

    val limitInt = Try(limVal.toInt).toOption.getOrElse(defLimit)
    val offsetInt = Try(offVal.toInt).toOption.getOrElse(defOffset)

    val requestValues = RequestValues(ip=req.remoteAddress,url=req.uri,networkid=req.headers.get("user").getOrElse(""),endpoint=endpointType)

    val queryValues = QueryValues(
      postcode = Some(postcode),
      epoch = Some(epochVal),
      filter = Some(filterString),
      historical = Some(hist),
      limit = Some(limitInt),
      offset = Some(offsetInt),
      verbose = Some(verb),
      eboost = Some(eboostDouble),
      wboost = Some(wboostDouble),
      nboost = Some(nboostDouble),
      sboost = Some(sboostDouble),
      lboost = Some(lboostDouble),
      mboost = Some(mboostDouble),
      jboost = Some(jboostDouble),
      pafDefault = Some(pafDefault)
    )

    val result: Option[Future[Result]] =
      postcodeValidation.validatePostcodeLimit(limit,queryValues,requestValues)
        .orElse(postcodeValidation.validatePostcodeOffset(offset,queryValues,requestValues))
        .orElse(postcodeValidation.validateSource(queryValues,requestValues))
        .orElse(postcodeValidation.validateKeyStatus(queryValues,requestValues))
        .orElse(postcodeValidation.validatePostcodeFilter(classificationfilter, queryValues,requestValues))
        .orElse(postcodeValidation.validatePostcode(postcode, queryValues,requestValues))
        .orElse(postcodeValidation.validateEpoch(queryValues,requestValues))
        .orElse(None)

    result match {
      case Some(res) =>
        res // a validation error

      case _ =>
        val args = PostcodeArgs(
          postcode = postcode,
          start = offsetInt,
          limit = limitInt,
          filters = filterString,
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

        implicit val success = Success[HybridAddressCollection](_ != null)

        val request: Future[HybridAddressCollection] =
          retry.Pause(3, 1.seconds).apply { ()  =>
            overloadProtection.breaker.withCircuitBreaker(
              esRepo.runMultiResultQuery(args)
            )
          }

        request.map {
          case HybridAddressCollection(hybridAddresses, aggregations@_, maxScore, total) =>

            val addresses: Seq[AddressResponseAddress] = hybridAddresses.map(
              AddressResponseAddress.fromHybridAddress(_, verb, pafDefault)
            )

            writeLog(activity = "postcode_request")

            jsonOk(
              AddressByPostcodeResponseContainer(
                apiVersion = apiVersion,
                dataVersion = dataVersion,
                response = AddressByPostcodeResponse(
                  postcode = postcode,
                  addresses = addressesToNonIDS(addresses),
                  filter = filterString,
                  historical = hist,
                  epoch = epochVal,
                  limit = limitInt,
                  offset = offsetInt,
                  total = total,
                  maxScore = maxScore,
                  verbose = verb,
                  pafdefault = pafDefault
                ),
                status = OkAddressResponseStatus
              )
            )
        }.recover {
          case NonFatal(exception) =>
            if (overloadProtection.breaker.isHalfOpen) {
              logger.warn(s"Elasticsearch is overloaded or down (postcode input). Circuit breaker is Half Open: ${exception.getMessage}")
              TooManyRequests(Json.toJson(FailedRequestToEsTooBusyPostCode(exception.getMessage, queryValues)))
            }else if (overloadProtection.breaker.isOpen) {
              logger.warn(s"Elasticsearch is overloaded or down (postcode input). Circuit breaker is open: ${exception.getMessage}")
              TooManyRequests(Json.toJson(FailedRequestToEsTooBusyPostCode(exception.getMessage, queryValues)))
            } else {
              // Circuit Breaker is closed. Some other problem
              writeLog(responseCode = "500",badRequestErrorMessage = FailedRequestToEsPostcodeError.message)
              logger.warn(s"Could not handle individual request (postcode input), problem with ES ${exception.getMessage}")
              InternalServerError(Json.toJson(FailedRequestToEsPostcode(exception.getMessage, queryValues)))
            }
        }
    }
  }
}
