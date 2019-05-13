package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import uk.gov.ons.addressIndex.model.db.index.HybridAddressCollection
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseAddress, FailedRequestToEsPartialAddressError, OkAddressResponseStatus}
import uk.gov.ons.addressIndex.model.server.response.partialaddress.{AddressByPartialAddressResponse, AddressByPartialAddressResponseContainer}
import uk.gov.ons.addressIndex.server.model.dao.QueryValues
import uk.gov.ons.addressIndex.server.modules._
import uk.gov.ons.addressIndex.server.modules.response.PartialAddressControllerResponse
import uk.gov.ons.addressIndex.server.modules.validation.PartialAddressControllerValidation
import uk.gov.ons.addressIndex.server.utils.{APIThrottler, AddressAPILogger, ThrottlerStatus}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

@Singleton
class PartialAddressController @Inject()(val controllerComponents: ControllerComponents,
                                         esRepo: ElasticsearchRepository,
                                         conf: ConfigModule,
                                         versionProvider: VersionModule,
                                         overloadProtection: APIThrottler,
                                         partialAddressValidation: PartialAddressControllerValidation
                                        )(implicit ec: ExecutionContext)
  extends PlayHelperController(versionProvider) with PartialAddressControllerResponse {

  lazy val logger = AddressAPILogger("address-index-server:PartialAddressController")

  /**
    * PartialAddress query API
    *
    * @param input input for the address to be fetched
    * @return Json response with addresses information
    */

  def partialAddressQuery(input: String,
                          fallback: Option[String] = None,
                          offset: Option[String] = None,
                          limit: Option[String] = None,
                          classificationFilter: Option[String] = None,
                          historical: Option[String] = None,
                          verbose: Option[String] = None,
                          epoch: Option[String] = None,
                          startBoost: Option[String] = None
                         ): Action[AnyContent] = Action async { implicit req =>

    val startingTime = System.currentTimeMillis()

    val clusterid = conf.config.elasticSearch.clusterPolicies.partial

    // get the defaults and maxima for the paging parameters from the config
    val defLimit = conf.config.elasticSearch.defaultLimitPartial
    val defOffset = conf.config.elasticSearch.defaultOffset

    val limval = limit.getOrElse(defLimit.toString)
    val offval = offset.getOrElse(defOffset.toString)

    val filterString = classificationFilter.getOrElse("").replaceAll("\\s+", "")
    val endpointType = "partial"

    //  val startDateVal = startDate.getOrElse("")
    //  val endDateVal = endDate.getOrElse("")
    val startDateVal = ""
    val endDateVal = ""

    val fall = fallback.flatMap(x => Try(x.toBoolean).toOption).getOrElse(true)
    val hist = historical.flatMap(x => Try(x.toBoolean).toOption).getOrElse(true)
    val verb = verbose.flatMap(x => Try(x.toBoolean).toOption).getOrElse(false)

    val epochVal = epoch.getOrElse("")

    val defStartBoost = conf.config.elasticSearch.defaultStartBoost
    // query string param for testing, will probably be removed
    val sboost = startBoost.flatMap(x => Try(x.toInt).toOption).getOrElse(defStartBoost)

    def boostAtStart(inAddresses: Seq[AddressResponseAddress]): Seq[AddressResponseAddress] = {
      val boostedAddresses: Seq[AddressResponseAddress] = inAddresses.map { add => boostAddress(add) }
      boostedAddresses.sortBy(_.underlyingScore)(Ordering[Float].reverse)
    }

    def boostAddress(add: AddressResponseAddress): AddressResponseAddress = {
      if (add.formattedAddress.toUpperCase().replaceAll("[,]", "").startsWith(input.toUpperCase().replaceAll("[,]", ""))) {
        add.copy(underlyingScore = add.underlyingScore + sboost)
      } else add.copy(underlyingScore = add.underlyingScore)
    }

    def writeLog(doResponseTime: Boolean = true, badRequestErrorMessage: String = "", notFound: Boolean = false, formattedOutput: String = "", numOfResults: String = "", score: String = "", activity: String = ""): Unit = {
      val responseTime = if (doResponseTime) (System.currentTimeMillis() - startingTime).toString else ""
      val networkId = if (req.headers.get("authorization").getOrElse("Anon").indexOf("+") > 0) req.headers.get("authorization").getOrElse("Anon").split("\\+")(0) else req.headers.get("authorization").getOrElse("Anon").split("_")(0)
      val organisation = if (req.headers.get("authorization").getOrElse("Anon").indexOf("+") > 0) req.headers.get("authorization").getOrElse("Anon").split("\\+")(0).split("_")(1) else "not set"

      logger.systemLog(
        ip = req.remoteAddress, url = req.uri, responseTimeMillis = responseTime,
        partialAddress = input, isNotFound = notFound, offset = offval,
        limit = limval, filter = filterString, badRequestMessage = badRequestErrorMessage,
        formattedOutput = formattedOutput,
        numOfResults = numOfResults, score = score, networkid = networkId, organisation = organisation,
        historical = hist, epoch = epochVal, verbose = verb, endpoint = endpointType, activity = activity, clusterid = clusterid
      )
    }

    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)
    val offsetInt = Try(offval.toInt).toOption.getOrElse(defOffset)

    val queryValues = QueryValues(
      input = Some(input),
      epoch = Some(epochVal),
      filter = Some(filterString),
      historical = Some(hist),
      limit = Some(limitInt),
      offset = Some(offsetInt),
      startDate = Some(startDateVal),
      endDate = Some(endDateVal),
      verbose = Some(verb)
    )

    val result: Option[Future[Result]] =
      partialAddressValidation.validatePartialLimit(limit, queryValues)
        .orElse(partialAddressValidation.validatePartialOffset(offset, queryValues))
        //      .orElse(partialAddressValidation.validateStartDate(startDateVal))
        //      .orElse(partialAddressValidation.validateEndDate(endDateVal))
        .orElse(partialAddressValidation.validateSource(queryValues))
        .orElse(partialAddressValidation.validateKeyStatus(queryValues))
        .orElse(partialAddressValidation.validateInput(input, queryValues))
        .orElse(partialAddressValidation.validateAddressFilter(classificationFilter, queryValues))
        .orElse(partialAddressValidation.validateEpoch(queryValues))
        .orElse(None)

    result match {
      case Some(res) =>
        res // a validation error

      case _ =>
        val args = PartialArgs(
          input = input,
          fallback = fall,
          start = offsetInt,
          limit = limitInt,
          filters = filterString,
          filterDateRange = DateRange(startDateVal, endDateVal),
          historical = hist,
          verbose = verb,
          epoch = epochVal,
          skinny = !verb,
        )

        val request: Future[HybridAddressCollection] =
          overloadProtection.breaker.withCircuitBreaker(
            esRepo.runMultiResultQuery(args)
          )

        request.map {
          case HybridAddressCollection(hybridAddresses, maxScore, total) =>
            val addresses: Seq[AddressResponseAddress] = hybridAddresses.map(
              AddressResponseAddress.fromHybridAddress(_, verb)
            )

            val sortAddresses = if (sboost > 0) boostAtStart(addresses) else addresses

            writeLog(activity = "partial_request")
            if (overloadProtection.currentStatus == ThrottlerStatus.HalfOpen)
              overloadProtection.setStatus(ThrottlerStatus.Closed)

            jsonOk(
              AddressByPartialAddressResponseContainer(
                apiVersion = apiVersion,
                dataVersion = dataVersion,
                response = AddressByPartialAddressResponse(
                  input = input,
                  addresses = sortAddresses,
                  filter = filterString,
                  historical = hist,
                  epoch = epochVal,
                  limit = limitInt,
                  offset = offsetInt,
                  total = total,
                  maxScore = maxScore,
                  startDate = startDateVal,
                  endDate = endDateVal,
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
                TooManyRequests(Json.toJson(FailedRequestToEsTooBusyPartialAddress(exception.getMessage, queryValues)))
              case ThrottlerStatus.Open =>
                logger.warn(s"Elasticsearch is overloaded or down (address input). Circuit breaker is open: ${exception.getMessage}")
                TooManyRequests(Json.toJson(FailedRequestToEsTooBusyPartialAddress(exception.getMessage, queryValues)))
              case _ =>
                // Circuit Breaker is closed. Some other problem
                writeLog(badRequestErrorMessage = FailedRequestToEsPartialAddressError.message)
                logger.warn(s"Could not handle individual request (partialAddress input), problem with ES ${exception.getMessage}")
                InternalServerError(Json.toJson(FailedRequestToEsPartialAddress(exception.getMessage, queryValues)))
            }
        }

    }
  }
}
