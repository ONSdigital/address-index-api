package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc._
import uk.gov.ons.addressIndex.model.db.index.HybridAddressCollection
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseAddressPostcodeEQ, FailedRequestToEsPostcodeError, OkAddressResponseStatus}
import uk.gov.ons.addressIndex.model.server.response.eq.{AddressByEQPostcodeResponse, AddressByEQPostcodeResponseContainer}
import uk.gov.ons.addressIndex.server.model.dao.QueryValues
import uk.gov.ons.addressIndex.server.modules.response.PostcodeControllerResponse
import uk.gov.ons.addressIndex.server.modules.validation.PostcodeControllerValidation
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, ElasticsearchRepository, VersionModule, _}
import uk.gov.ons.addressIndex.server.utils.{APIThrottle, AddressAPILogger}
import uk.gov.ons.addressIndex.server.utils.GroupOptions

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

@Singleton
class EQPostcodeController @Inject()(val controllerComponents: ControllerComponents,
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
  def postcodeQuery(postcode: String,
                    offset: Option[String] = None,
                    limit: Option[String] = None,
                    classificationfilter: Option[String] = None,
                    historical: Option[String] = None,
                    verbose: Option[String] = None,
                    favourpaf: Option[String] = None,
                    favourwelsh: Option[String] = None,
                    epoch: Option[String] = None,
                    groupfullpostcodes: Option[String] = None,
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
    val favourPaf = favourpaf.flatMap(x => Try(x.toBoolean).toOption).getOrElse(true)
    val favourWelsh = favourwelsh.flatMap(x => Try(x.toBoolean).toOption).getOrElse(false)

    val epochVal = epoch.getOrElse("")

    val groupFullPostcodes: String = groupfullpostcodes.getOrElse(GroupOptions.NO.toString)

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
        endpoint = endpointType, activity = activity, clusterid = clusterId
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
      favourpaf = Some(favourPaf),
      favourwelsh = Some(favourWelsh),
      groupFullPostcodes = Some(groupFullPostcodes),
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
          start = offsetInt,
          limit = limitInt,
          filters = filterString,
          historical = hist,
          verbose = verb,
          epoch = epochVal,
          skinny = !verb,
          favourpaf = favourPaf,
          favourwelsh = favourWelsh,
          groupfullpostcodes = groupFullPostcodes,
          eboost = eboostDouble,
          nboost = nboostDouble,
          sboost = sboostDouble,
          wboost = wboostDouble
        )

        val request: Future[HybridAddressCollection] =
          overloadProtection.breaker.withCircuitBreaker(
            esRepo.runMultiResultQuery(args)
          )

        request.map {
          case HybridAddressCollection(hybridAddresses, aggregations, maxScore, total) =>

            val addresses: Seq[AddressResponseAddressPostcodeEQ] = hybridAddresses.map(
              AddressResponseAddressPostcodeEQ.fromHybridAddress(_, favourPaf, favourWelsh)
            )
            val aggsOrEmpty = if (aggregations.isEmpty) None else Some(aggregations)

            writeLog(activity = "eq_postcode_request")

            jsonOk(
              AddressByEQPostcodeResponseContainer(
                apiVersion = apiVersion,
                dataVersion = dataVersion,
                response = AddressByEQPostcodeResponse(
                  postcode = postcode,
                  postcodes = aggsOrEmpty,
                  addresses = addresses,
                  filter = filterString,
                  historical = hist,
                  epoch = epochVal,
                  limit = limitInt,
                  offset = offsetInt,
                  total = total,
                  maxScore = maxScore,
                  verbose = verb,
                  groupfullpostcodes = groupFullPostcodes
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
