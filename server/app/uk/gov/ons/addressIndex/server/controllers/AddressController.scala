package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.server.model.dao.QueryValues
import uk.gov.ons.addressIndex.server.modules.response.AddressControllerResponse
import uk.gov.ons.addressIndex.server.modules.validation.AddressControllerValidation
import uk.gov.ons.addressIndex.server.modules.{AddressArgs, ConfigModule, ElasticsearchRepository, ParserModule, Region, VersionModule}
import uk.gov.ons.addressIndex.server.utils.{AddressAPILogger, _}

import scala.concurrent.{ExecutionContext, Future}
import scala.math._
import scala.util.Try
import scala.util.control.NonFatal
import uk.gov.ons.addressIndex.model.db.index.HybridAddressCollection

@Singleton
class AddressController @Inject()(val controllerComponents: ControllerComponents,
                                  esRepo: ElasticsearchRepository,
                                  parser: ParserModule,
                                  conf: ConfigModule,
                                  versionProvider: VersionModule,
                                  overloadProtection: APIThrottler,
                                  addressValidation: AddressControllerValidation
                                 )(implicit ec: ExecutionContext)
  extends PlayHelperController(versionProvider) with AddressControllerResponse {

  lazy val logger = AddressAPILogger("address-index-server:AddressController")

  val missing: String = "missing"
  val invalid: String = "invalid"
  val valid: String = "valid"
  val notRequired: String = "not required"

  /**
    * Address query API
    *
    * @param input the address query
    * @return Json response with addresses information
    */
  def addressQuery(implicit input: String,
                   offset: Option[String] = None,
                   limit: Option[String] = None,
                   classificationfilter: Option[String] = None,
                   rangekm: Option[String] = None,
                   lat: Option[String] = None,
                   lon: Option[String] = None,
                   historical: Option[String] = None,
                   matchthreshold: Option[String] = None,
                   verbose: Option[String] = None,
                   epoch: Option[String] = None
                  ): Action[AnyContent] = Action async { implicit req =>

    val clusterId = conf.config.elasticSearch.clusterPolicies.address

    val startingTime: Long = System.currentTimeMillis()
    val ip: String = req.remoteAddress
    val url: String = req.uri

    // get the defaults and maxima for the paging parameters from the config
    val defLimit = conf.config.elasticSearch.defaultLimit
    val defOffset = conf.config.elasticSearch.defaultOffset
    val defThreshold = conf.config.elasticSearch.matchThreshold

    val limVal = limit.getOrElse(defLimit.toString)
    val offVal = offset.getOrElse(defOffset.toString)
    val threshVal = matchthreshold.getOrElse(defThreshold.toString)

    val filterString = classificationfilter.getOrElse("").replaceAll("\\s+", "")
    val endpointType = "address"

    val hist = historical.flatMap(x => Try(x.toBoolean).toOption).getOrElse(true)
    val verb = verbose.flatMap(x => Try(x.toBoolean).toOption).getOrElse(false)

    // validate radius parameters
    val rangeVal = rangekm.getOrElse("")
    val latVal = lat.getOrElse("")
    val lonVal = lon.getOrElse("")

    val epochVal = epoch.getOrElse("")

    def writeLog(badRequestErrorMessage: String = "", formattedOutput: String = "", numOfResults: String = "", score: String = "", activity: String = ""): Unit = {
      val authVal = req.headers.get("authorization").getOrElse("Anon")
      val authHasPlus = authVal.indexOf("+") > 0
      val networkId = if (authHasPlus) authVal.split("\\+")(0) else authVal.split("_")(0)
      val organisation = if (authHasPlus) networkId.split("_")(1) else "not set"

      logger.systemLog(ip = ip, url = url, responseTimeMillis = (System.currentTimeMillis() - startingTime).toString,
        input = input, offset = offVal, limit = limVal, filter = filterString,
        historical = hist, epoch = epochVal, rangekm = rangeVal, lat = latVal, lon = lonVal,
        badRequestMessage = badRequestErrorMessage, formattedOutput = formattedOutput,
        numOfResults = numOfResults, score = score, networkid = networkId, organisation = organisation,
        verbose = verb, endpoint = endpointType, activity = activity, clusterid = clusterId)
    }

    def trimAddresses(fullAddresses: Seq[AddressResponseAddress]): Seq[AddressResponseAddress] = {
      fullAddresses.map { address => address.copy(nag = None, paf = None, relatives = None, crossRefs = None) }
    }

    val limitInt = Try(limVal.toInt).toOption.getOrElse(defLimit)
    val offsetInt = Try(offVal.toInt).toOption.getOrElse(defOffset)
    val thresholdFloat = Try(threshVal.toFloat).toOption.getOrElse(defThreshold)

    val queryValues = QueryValues(
      epoch = Some(epochVal),
      filter = Some(filterString),
      historical = Some(hist),
      limit = Some(limitInt),
      offset = Some(offsetInt),
      verbose = Some(verb),
      rangeKM = Some(rangeVal),
      latitude = Some(latVal),
      longitude = Some(lonVal),
      matchThreshold = Some(thresholdFloat)
    )

    val args = AddressArgs(
      input = input,
      tokens = Map.empty, // temporary, filled later
      region = Region.fromStrings(rangeVal, latVal, lonVal),
      epoch = epochVal,
      historical = hist,
      verbose = verb,
      filters = filterString,
      start = offsetInt, // temporary, but zeroed later?
      limit = limitInt, // temporary, expanded later
      queryParamsConfig = None,
    )

    val result: Option[Future[Result]] =
      addressValidation.validateAddressFilter(classificationfilter, queryValues)
        .orElse(addressValidation.validateThreshold(matchthreshold, queryValues))
        .orElse(addressValidation.validateRange(rangekm, queryValues))
        .orElse(addressValidation.validateSource(queryValues))
        .orElse(addressValidation.validateKeyStatus(queryValues))
        .orElse(addressValidation.validateLimit(limit, queryValues))
        .orElse(addressValidation.validateOffset(offset, queryValues))
        .orElse(addressValidation.validateInput(input, queryValues))
        .orElse(addressValidation.validateLocation(lat, lon, rangekm, queryValues))
        .orElse(addressValidation.validateEpoch(queryValues))
        .orElse(None)

    result match {
      case Some(res) =>
        res // a validation error

      case _ =>
        val tokens = if (input.isEmpty && rangeVal != "" && latVal != "" && lonVal != "" && filterString != "") {
          parser.parse("*")
        } else {
          parser.parse(input)
        }

        // try to get enough results to accurately calculate the hybrid score (may need to be more sophisticated)
        val minimumSample = conf.config.elasticSearch.minimumSample
        val limitExpanded = max(offsetInt + (limitInt * 2), minimumSample)

        val finalArgs = args.copy(
          tokens = tokens,
          start = 0,
          limit = limitExpanded,
        )

        val request: Future[HybridAddressCollection] =
          overloadProtection.breaker.withCircuitBreaker(esRepo.runMultiResultQuery(finalArgs))

        request.map {
          case HybridAddressCollection(hybridAddresses, maxScore, total@_) =>
            val addresses: Seq[AddressResponseAddress] = hybridAddresses.map(
              AddressResponseAddress.fromHybridAddress(_, verbose = true)
            )

            //  calculate the elastic denominator value which will be used when scoring each address
            val elasticDenominator =
              Try(ConfidenceScoreHelper.calculateElasticDenominator(addresses.map(_.underlyingScore))).getOrElse(1D)

            // calculate the Hopper and hybrid scores for each  address
            val scoredAddresses = HopperScoreHelper.getScoresForAddresses(addresses, tokens, elasticDenominator)

            // work out the threshold for accepting matches (default 5% -> 0.05)
            val threshold = Try((thresholdFloat / 100).toDouble).getOrElse(0.05D)

            // filter out scores below threshold, sort the resultant collection, highest score first
            val sortedAddresses =
              scoredAddresses.filter(_.confidenceScore >= threshold).sortBy(_.confidenceScore)(Ordering[Double].reverse)

            // capture the number of matches before applying offset and limit
            val newTotal = sortedAddresses.length

            // trim the result list according to offset and limit paramters
            val limitedSortedAddresses = sortedAddresses.slice(offsetInt, offsetInt + limitInt)

            // if verbose is false, strip out full address details (these are needed for score so must be
            // removed retrospectively)
            val finalAddresses = if (verb) limitedSortedAddresses else trimAddresses(limitedSortedAddresses)

            writeLog(activity = "address_request")
            if (overloadProtection.currentStatus == ThrottlerStatus.HalfOpen)
            overloadProtection.setStatus(ThrottlerStatus.Closed)

            jsonOk(
              AddressBySearchResponseContainer(
                apiVersion = apiVersion,
                dataVersion = dataVersion,
                response = AddressBySearchResponse(
                  tokens = tokens,
                  addresses = finalAddresses,
                  filter = filterString,
                  historical = hist,
                  epoch = epochVal,
                  rangekm = rangeVal,
                  latitude = latVal,
                  longitude = lonVal,
                  limit = limitInt,
                  offset = offsetInt,
                  total = newTotal,
                  maxScore = maxScore,
                  sampleSize = limitExpanded,
                  matchthreshold = thresholdFloat,
                  verbose = verb
                ),
                status = OkAddressResponseStatus
              )
            )
        }.recover {
          case NonFatal(exception) =>
            overloadProtection.currentStatus match {
              case ThrottlerStatus.HalfOpen =>
                logger.warn(s"Elasticsearch is overloaded or down (address input). Circuit breaker is Half Open: ${exception.getMessage}")
                TooManyRequests(Json.toJson(FailedRequestToEsTooBusy(exception.getMessage, queryValues)))
              case ThrottlerStatus.Open =>
                logger.warn(s"Elasticsearch is overloaded or down (address input). Circuit breaker is open: ${exception.getMessage}")
                TooManyRequests(Json.toJson(FailedRequestToEsTooBusy(exception.getMessage, queryValues)))
              case _ =>
                // Circuit Breaker is closed. Some other problem
                writeLog(badRequestErrorMessage = FailedRequestToEsError.message)
                logger.warn(s"Could not handle individual request (address input), problem with ES ${exception.getMessage}")
                InternalServerError(Json.toJson(FailedRequestToEs(exception.getMessage, queryValues)))
            }
        }
    }
  }
}
