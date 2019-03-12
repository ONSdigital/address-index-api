package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.ons.addressIndex.model.db.index.HybridAddresses
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.server.model.dao.QueryValues
import uk.gov.ons.addressIndex.server.modules.response.AddressControllerResponse
import uk.gov.ons.addressIndex.server.modules.validation.AddressControllerValidation
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, ElasticsearchRepository, ParserModule, VersionModule}
import uk.gov.ons.addressIndex.server.utils.{AddressAPILogger, _}

import scala.concurrent.{ExecutionContext, Future}
import scala.math._
import scala.util.Try
import scala.util.control.NonFatal

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
  def addressQuery(implicit input: String, offset: Option[String] = None, limit: Option[String] = None,
                   classificationFilter: Option[String] = None, rangekm: Option[String] = None,
                   lat: Option[String] = None, lon: Option[String] = None,
                   // startDate: Option[String] = None, endDate: Option[String] = None,
                   historical: Option[String] = None,
                   matchThreshold: Option[String] = None,
                   verbose: Option[String] = None, epoch: Option[String] = None): Action[AnyContent] = Action async { implicit req =>

    val clusterId = conf.config.elasticSearch.clusterPolicies.address

    val startingTime: Long = System.currentTimeMillis()
    val ip: String = req.remoteAddress
    val url: String = req.uri

    //   val startDateVal = startDate.getOrElse("")
    //   val endDateVal = endDate.getOrElse("")
    val startDateVal = ""
    val endDateVal = ""

    // get the defaults and maxima for the paging parameters from the config
    val defLimit = conf.config.elasticSearch.defaultLimit
    val defOffset = conf.config.elasticSearch.defaultOffset
    val defThreshold = conf.config.elasticSearch.matchThreshold

    val limVal = limit.getOrElse(defLimit.toString)
    val offVal = offset.getOrElse(defOffset.toString)
    val threshVal = matchThreshold.getOrElse(defThreshold.toString)

    val filterString = classificationFilter.getOrElse("").replaceAll("\\s+", "")
    val endpointType = "address"

    val hist = historical match {
      case Some(x) => Try(x.toBoolean).getOrElse(true)
      case None => true
    }

    val verb = verbose match {
      case Some(x) => Try(x.toBoolean).getOrElse(false)
      case None => false
    }

    // validate radius parameters
    val rangeVal = rangekm.getOrElse("")
    val latVal = lat.getOrElse("")
    val lonVal = lon.getOrElse("")

    val epochVal = epoch.getOrElse("")

    def writeLog(badRequestErrorMessage: String = "", formattedOutput: String = "", numOfResults: String = "", score: String = "", activity: String = ""): Unit = {
      val networkId = if (req.headers.get("authorization").getOrElse("Anon").indexOf("+") > 0) req.headers.get("authorization").getOrElse("Anon").split("\\+")(0) else req.headers.get("authorization").getOrElse("Anon").split("_")(0)
      val organisation = if (req.headers.get("authorization").getOrElse("Anon").indexOf("+") > 0) req.headers.get("authorization").getOrElse("Anon").split("\\+")(0).split("_")(1) else "not set"

      logger.systemLog(ip = ip, url = url, responseTimeMillis = (System.currentTimeMillis() - startingTime).toString,
        input = input, offset = offVal, limit = limVal, filter = filterString,
        //  endDate=endDateVal, startDate = startDateVal,
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
      startDate = Some(startDateVal),
      endDate = Some(endDateVal),
      verbose = Some(verb),
      rangeKM = Some(rangeVal),
      latitude = Some(latVal),
      longitude = Some(lonVal),
      matchThreshold = Some(thresholdFloat)
    )

    val result: Option[Future[Result]] =
      addressValidation.validateAddressFilter(classificationFilter, queryValues)
        //     .orElse(addressValidation.validateStartDate(startDateVal))
        //    .orElse(addressValidation.validateEndDate(endDateVal))
        .orElse(addressValidation.validateThreshold(matchThreshold, queryValues))
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
        val tokens = parser.parse(input)

        // try to get enough results to accurately calculate the hybrid score (may need to be more sophisticated)
        val minimumSample = conf.config.elasticSearch.minimumSample
        val limitExpanded = max(offsetInt + (limitInt * 2), minimumSample)

        val request: Future[HybridAddresses] =
          overloadProtection.breaker.withCircuitBreaker(esRepo.queryAddresses(
            tokens, 0, limitExpanded, filterString,
            rangeVal, latVal, lonVal, startDateVal, endDateVal, None, hist, isBulk = false, epochVal)
          )

        request.map {
          case HybridAddresses(hybridAddresses, maxScore, total@_) =>
            val addresses: Seq[AddressResponseAddress] = hybridAddresses.map(
              AddressResponseAddress.fromHybridAddress(_, verbose = true)
            )

            //  calculate the elastic denominator value which will be used when scoring each address
            val elasticDenominator = Try(
              ConfidenceScoreHelper.calculateElasticDenominator(addresses.map(_.underlyingScore))
            ).getOrElse(1D)

            // calculate the Hopper and hybrid scores for each  address
            val scoredAddresses = HopperScoreHelper.getScoresForAddresses(
              addresses, tokens, elasticDenominator)

            // work out the threshold for accepting matches (default 5% -> 0.05)
            val threshold = Try((thresholdFloat / 100).toDouble).getOrElse(0.05D)

            // filter out scores below threshold, sort the resultant collection, highest score first
            val sortedAddresses = scoredAddresses.filter(_.confidenceScore > threshold).sortBy(
              _.confidenceScore)(Ordering[Double].reverse)

            // capture the number of matches before applying offset and limit
            val newTotal = sortedAddresses.length

            // trim the result list according to offset and limit paramters
            val limitedSortedAddresses = sortedAddresses.slice(offsetInt, offsetInt + limitInt)

            // if verbose is false, strip out full address details (these are needed for score so must be
            // removed retrospectively)
            val finalAddresses = if (verb) limitedSortedAddresses else trimAddresses(limitedSortedAddresses)

            //            addresses.foreach { address =>
            //              writeLog(
            //                formattedOutput = address.formattedAddressNag, numOfResults = total.toString,
            //                score = address.underlyingScore.toString, activity = "address_response"
            //              )
            //            }

            writeLog(activity = "address_request")

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
                  startDate = startDateVal,
                  endDate = endDateVal,
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
