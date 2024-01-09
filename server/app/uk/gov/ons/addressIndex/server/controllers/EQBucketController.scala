package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.ons.addressIndex.model.db.index.HybridAddressCollection
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseAddressBucketEQ, FailedRequestToEsPostcodeError, OkAddressResponseStatus}
import uk.gov.ons.addressIndex.model.server.response.eq.{AddressByEQBucketResponse, AddressByEQBucketResponseContainer}
import uk.gov.ons.addressIndex.server.model.dao.{QueryValues, RequestValues}
import uk.gov.ons.addressIndex.server.modules.response.PostcodeControllerResponse
import uk.gov.ons.addressIndex.server.modules.validation.PostcodeControllerValidation
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, ElasticsearchRepository, VersionModule, _}
import uk.gov.ons.addressIndex.server.utils.{APIThrottle, AddressAPILogger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

@Singleton
class EQBucketController @Inject()(val controllerComponents: ControllerComponents,
                                   esRepo: ElasticsearchRepository,
                                   conf: ConfigModule,
                                   versionProvider: VersionModule,
                                   overloadProtection: APIThrottle,
                                   postcodeValidation: PostcodeControllerValidation
                                  )(implicit ec: ExecutionContext)
  extends PlayHelperController(versionProvider) with PostcodeControllerResponse {

  lazy val logger: AddressAPILogger = AddressAPILogger("address-index-server:EQPostcodeController")

  /**
    * EQ POSTCODE query API
    *
    * @param postcode postcode of the address to be fetched
    * @return Json response with addresses information
    */
  def bucketQueryEQ(postcode: Option[String] = None,
                    streetname: Option[String] = None,
                    townname: Option[String] = None,
                    offset: Option[String] = None,
                    limit: Option[String] = None,
                    classificationfilter: Option[String] = None,
                    favourpaf: Option[String] = None,
                    favourwelsh: Option[String] = None,
                    epoch: Option[String] = None
                   ): Action[AnyContent] = Action async { implicit req =>
    val startingTime = System.currentTimeMillis()
    val tocLink = conf.config.termsAndConditionsLink
    val clusterId = conf.config.elasticSearch.clusterPolicies.postcode
    val circuitBreakerDisabled = conf.config.elasticSearch.circuitBreakerDisabled

    // get the defaults and maxima for the paging parameters from the config
    val defLimit = conf.config.elasticSearch.defaultLimitPostcode
    val defOffset = conf.config.elasticSearch.defaultOffset

    val limVal = limit.getOrElse(defLimit.toString)
    val offVal = offset.getOrElse(defOffset.toString)

    val filterString = classificationfilter.getOrElse("").replaceAll("\\s+", "")
    val endpointType = "postcode"

    val postcodeVal = postcode.getOrElse("*")
    val streetNameVal = streetname.getOrElse("*")
    val townNameVal = townname.getOrElse("*")
    val bucketPattern = postcodeVal + "_" + streetNameVal + "_" + townNameVal

    val hist = false
    val verb = false
    val favourPaf = favourpaf.flatMap(x => Try(x.toBoolean).toOption).getOrElse(true)
    val favourWelsh = favourwelsh.flatMap(x => Try(x.toBoolean).toOption).getOrElse(false)

    val epochVal = epoch.getOrElse("")

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
        postcode = bucketPattern, isNotFound = notFound, offset = offVal,
        limit = limVal, filter = filterString, badRequestMessage = badRequestErrorMessage,
        formattedOutput = formattedOutput,
        numOfResults = numOfResults, score = score, networkid = networkId, organisation = organisation,
        historical = hist, epoch = epochVal, verbose = verb,
        endpoint = endpointType, activity = activity, clusterid = clusterId
      )
    }

    val limitInt = Try(limVal.toInt).toOption.getOrElse(defLimit)
    val offsetInt = Try(offVal.toInt).toOption.getOrElse(defOffset)

    val requestValues = RequestValues(ip=req.remoteAddress,url=req.uri,networkid=req.headers.get("user").getOrElse(""),endpoint=endpointType)

    val queryValues = QueryValues(
      postcode = Some(postcodeVal),
      streetname = Some(streetNameVal),
      townname = Some(townNameVal),
      epoch = Some(epochVal),
      filter = Some(filterString),
      historical = Some(hist),
      limit = Some(limitInt),
      offset = Some(offsetInt),
      verbose = Some(verb),
      favourpaf = Some(favourPaf),
      favourwelsh = Some(favourWelsh)
    )

    val result: Option[Future[Result]] =
      postcodeValidation.validatePostcodeLimit(limit, queryValues,requestValues)
        .orElse(postcodeValidation.validatePostcodeOffset(offset, queryValues,requestValues))
        .orElse(postcodeValidation.validateSource(queryValues,requestValues))
        .orElse(postcodeValidation.validateKeyStatus(queryValues,requestValues))
        .orElse(postcodeValidation.validatePostcodeFilter(classificationfilter, queryValues,requestValues))
        .orElse(postcodeValidation.validateBucketPattern(bucketPattern, queryValues,requestValues))
        .orElse(postcodeValidation.validateEpoch(queryValues,requestValues))
        .orElse(None)

    result match {
      case Some(res) =>
        res // a validation error

      case _ =>
        val args = BucketArgs(
          bucketpattern = bucketPattern,
          start = offsetInt,
          limit = limitInt,
          filters = filterString,
          historical = hist,
          verbose = verb,
          epoch = epochVal,
          skinny = !verb,
          favourpaf = favourPaf,
          favourwelsh = favourWelsh
        )

        val request: Future[HybridAddressCollection] =
          if (circuitBreakerDisabled) esRepo.runMultiResultQuery(args) else
          overloadProtection.breaker.withCircuitBreaker(
            esRepo.runMultiResultQuery(args)
          )

        request.map {
          case HybridAddressCollection(hybridAddresses, aggregations@_, maxScore, total) =>

            val addresses: Seq[AddressResponseAddressBucketEQ] = hybridAddresses.map(
              AddressResponseAddressBucketEQ.fromHybridAddress(_, favourPaf, favourWelsh)
            )

            writeLog(activity = "eq_bucket_request")

            jsonOk(
              AddressByEQBucketResponseContainer(
                apiVersion = apiVersion,
                dataVersion = dataVersion,
                termsAndConditions = tocLink,
                response = AddressByEQBucketResponse(
                  postcode = postcodeVal,
                  streetname = streetNameVal,
                  townname = townNameVal,
                  addresses = addresses,
                  filter = filterString,
                  epoch = epochVal,
                  limit = limitInt,
                  offset = offsetInt,
                  total = total,
                  maxScore = maxScore),
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
