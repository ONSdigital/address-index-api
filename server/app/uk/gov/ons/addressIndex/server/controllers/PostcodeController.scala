package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc._
import retry.Success
import uk.gov.ons.addressIndex.model.db.index.HybridAddressCollection
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseAddress, FailedRequestToEsPostcodeError, OkAddressResponseStatus}
import uk.gov.ons.addressIndex.model.server.response.postcode.{AddressByPostcodeResponse, AddressByPostcodeResponseContainer}
import uk.gov.ons.addressIndex.server.model.dao.QueryValues
import uk.gov.ons.addressIndex.server.modules.response.PostcodeControllerResponse
import uk.gov.ons.addressIndex.server.modules.validation.PostcodeControllerValidation
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, ElasticsearchRepository, VersionModule, _}
import uk.gov.ons.addressIndex.server.utils.{APIThrottle, AddressAPILogger}
import odelay.Timer.default

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.math.Ordering
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
                    includeauxiliarysearch: Option[String] = None,
                    eboost: Option[String] = None,
                    nboost: Option[String] = None,
                    sboost: Option[String] = None,
                    wboost: Option[String] = None
                   ): Action[AnyContent] = Action async { implicit req =>
    val startingTime = System.currentTimeMillis()

    val clusterId = conf.config.elasticSearch.clusterPolicies.postcode

    // get the defaults and maxima for the paging parameters from the config
    val defLimit = conf.config.elasticSearch.defaultLimitPostcode
    val defOffset = conf.config.elasticSearch.defaultOffset

    val limVal = limit.getOrElse(defLimit.toString)
    val offVal = offset.getOrElse(defOffset.toString)

    val filterString = classificationfilter.getOrElse("").replaceAll("\\s+", "")
    val endpointType = "postcode"

    val hist = historical.flatMap(x => Try(x.toBoolean).toOption).getOrElse(false)
    val verb = verbose.flatMap(x => Try(x.toBoolean).toOption).getOrElse(false)
    val auxiliary = includeauxiliarysearch.flatMap(x => Try(x.toBoolean).toOption).getOrElse(false)

    val epochVal = epoch.getOrElse("")

    val eboostVal = {if (eboost.getOrElse("1.0").isEmpty) "1.0" else eboost.getOrElse("1.0")}
    val nboostVal = {if (nboost.getOrElse("1.0").isEmpty) "1.0" else nboost.getOrElse("1.0")}
    val sboostVal = {if (sboost.getOrElse("1.0").isEmpty) "1.0" else sboost.getOrElse("1.0")}
    val wboostVal = {if (wboost.getOrElse("1.0").isEmpty) "1.0" else wboost.getOrElse("1.0")}

    val eboostDouble = Try(eboostVal.toDouble).toOption.getOrElse(1.0D)
    val nboostDouble = Try(nboostVal.toDouble).toOption.getOrElse(1.0D)
    val sboostDouble = Try(sboostVal.toDouble).toOption.getOrElse(1.0D)
    val wboostDouble = Try(wboostVal.toDouble).toOption.getOrElse(1.0D)

    def writeLog(doResponseTime: Boolean = true, badRequestErrorMessage: String = "", notFound: Boolean = false, formattedOutput: String = "", numOfResults: String = "", score: String = "", activity: String = ""): Unit = {
      val responseTime = if (doResponseTime) (System.currentTimeMillis() - startingTime).toString else ""
      val networkId = Try(if (req.headers.get("authorization").getOrElse("Anon").indexOf("+") > 0) req.headers.get("authorization").getOrElse("Anon").split("\\+")(0) else req.headers.get("authorization").getOrElse("Anon").split("_")(0)).getOrElse("")
      val organisation = Try(if (req.headers.get("authorization").getOrElse("Anon").indexOf("+") > 0) req.headers.get("authorization").getOrElse("Anon").split("\\+")(0).split("_")(1) else "not set").getOrElse("")
      logger.systemLog(
        ip = req.remoteAddress, url = req.uri, responseTimeMillis = responseTime,
        postcode = postcode, isNotFound = notFound, offset = offVal,
        limit = limVal, filter = filterString, badRequestMessage = badRequestErrorMessage,
        formattedOutput = formattedOutput,
        numOfResults = numOfResults, score = score, networkid = networkId, organisation = organisation,
        historical = hist, epoch = epochVal, verbose = verb,
        endpoint = endpointType, activity = activity, clusterid = clusterId,
        includeAuxiliary = auxiliary
      )
    }

    val limitInt = Try(limVal.toInt).toOption.getOrElse(defLimit)
    val offsetInt = Try(offVal.toInt).toOption.getOrElse(defOffset)

    val queryValues = QueryValues(
      postcode = Some(postcode),
      epoch = Some(epochVal),
      filter = Some(filterString),
      historical = Some(hist),
      limit = Some(limitInt),
      offset = Some(offsetInt),
      verbose = Some(verb),
      includeAuxiliarySearch = Some(auxiliary),
      eboost = Some(eboostDouble),
      nboost = Some(nboostDouble),
      sboost = Some(sboostDouble),
      wboost = Some(wboostDouble)
    )

    val result: Option[Future[Result]] =
      postcodeValidation.validatePostcodeLimit(limit, queryValues)
        .orElse(postcodeValidation.validatePostcodeOffset(offset, queryValues))
        .orElse(postcodeValidation.validateSource(queryValues))
        .orElse(postcodeValidation.validateKeyStatus(queryValues))
        .orElse(postcodeValidation.validatePostcodeFilter(classificationfilter, queryValues))
        .orElse(postcodeValidation.validatePostcode(postcode, queryValues))
        .orElse(postcodeValidation.validateEpoch(queryValues))
        .orElse(None)

    result match {
      case Some(res) =>
        res // a validation error

      case _ =>
        val args = PostcodeArgs(
          postcode = postcode,
          start = (if (auxiliary) 0 else offsetInt),
          limit = (if (auxiliary) 5000 else limitInt),
          filters = filterString,
          historical = hist,
          verbose = verb,
          epoch = epochVal,
          skinny = !verb,
          includeAuxiliarySearch = auxiliary,
          eboost = eboostDouble,
          nboost = nboostDouble,
          sboost = sboostDouble,
          wboost = wboostDouble
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

            val addresses1: Seq[AddressResponseAddress] = hybridAddresses.map(
              AddressResponseAddress.fromHybridAddress(_, verb)
            )

            val hits = addresses1.size

            val addresses2: Seq[AddressResponseAddress] = if (auxiliary) addresses1.zipWithIndex.map{pair =>
              //        addresstype=CE and not (estabtype = 'Household' or 'NA' or 'Other' or 'Residential Caravaner' or 'Residential Boat' or 'Sheltered Accommodation') 1000000
              //        E or W countrycode 100000
              //        900x uprn 10000
              //        Ranking from ES query 0-9999
              val uprn = pair._1.uprn
              val estabtype = pair._1.census.estabType
              val addresstype = pair._1.census.addressType
              val countrycode = pair._1.census.countryCode
              val ceboost: Int = if (addresstype.equalsIgnoreCase("CE") &&
                !(estabtype.equalsIgnoreCase("Household")
                  || estabtype.equalsIgnoreCase("NA")
                  || estabtype.equalsIgnoreCase("Other")
                  || estabtype.equalsIgnoreCase("Residential Caravaner")
                  || estabtype.equalsIgnoreCase("Residential Boat")
                  || estabtype.equalsIgnoreCase("Sheltered Accommodation")
                  )) 1000000 else 0
              val countboost: Int = if(countrycode.equals("E") || countrycode.equals("W")) 100000 else 0
              val uprnboost: Int = if (uprn.mkString.take(3).equals("900")) 10000 else 0
              val newscore: Double = ceboost + countboost + uprnboost + hits - pair._2
              pair._1.copy(confidenceScore=newscore)
            } else Seq.empty

            val addresses3 = if (auxiliary) addresses2.sortBy(_.confidenceScore)(Ordering[Double].reverse) else Seq.empty

            val addresses = if (auxiliary) addresses3.slice(offsetInt, offsetInt + limitInt) else addresses1
    //        val addresses = addresses3.map(add => add.copy(confidenceScore = 100))

            writeLog(activity = "postcode_request")

            jsonOk(
              AddressByPostcodeResponseContainer(
                apiVersion = apiVersion,
                dataVersion = dataVersion,
                response = AddressByPostcodeResponse(
                  postcode = postcode,
                  addresses = addresses,
                  filter = filterString,
                  historical = hist,
                  epoch = epochVal,
                  limit = limitInt,
                  offset = offsetInt,
                  total = total,
                  maxScore = maxScore,
                  verbose = verb,
                  includeauxiliarysearch = auxiliary
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
              writeLog(badRequestErrorMessage = FailedRequestToEsPostcodeError.message)
              logger.warn(s"Could not handle individual request (postcode input), problem with ES ${exception.getMessage}")
              InternalServerError(Json.toJson(FailedRequestToEsPostcode(exception.getMessage, queryValues)))
            }
        }
    }
  }
}
