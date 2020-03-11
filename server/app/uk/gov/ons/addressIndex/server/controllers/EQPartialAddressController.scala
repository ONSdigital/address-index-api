package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import uk.gov.ons.addressIndex.model.db.index.HybridAddressCollection
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.eq.{AddressByEQPartialAddressResponse, AddressByEQPartialAddressResponseContainer}
import uk.gov.ons.addressIndex.server.model.dao.QueryValues
import uk.gov.ons.addressIndex.server.modules._
import uk.gov.ons.addressIndex.server.modules.response.PartialAddressControllerResponse
import uk.gov.ons.addressIndex.server.modules.validation.PartialAddressControllerValidation
import uk.gov.ons.addressIndex.server.utils.{APIThrottle, AddressAPILogger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

@Singleton
class EQPartialAddressController @Inject()(val controllerComponents: ControllerComponents,
                                         esRepo: ElasticsearchRepository,
                                         conf: ConfigModule,
                                         versionProvider: VersionModule,
                                         overloadProtection: APIThrottle,
                                         partialAddressValidation: PartialAddressControllerValidation
                                        )(implicit ec: ExecutionContext)
  extends PlayHelperController(versionProvider) with PartialAddressControllerResponse {

  lazy val logger: AddressAPILogger = AddressAPILogger("address-index-server:EQPartialAddressController")

  val sboost: Int = conf.config.elasticSearch.defaultStartBoost

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
                          classificationfilter: Option[String] = None,
                          historical: Option[String] = None,
                          verbose: Option[String] = None,
                          epoch: Option[String] = None,
                          fromsource: Option[String] = None,
                          highlight: Option[String] = None,
                          favourpaf: Option[String] = None,
                          favourwelsh: Option[String] = None
                         ): Action[AnyContent] = Action async { implicit req =>

    val startingTime = System.currentTimeMillis()

    val clusterid = conf.config.elasticSearch.clusterPolicies.partial

    // get the defaults and maxima for the paging parameters from the config
    val defLimit = conf.config.elasticSearch.defaultLimitPartial
    val defOffset = conf.config.elasticSearch.defaultOffset

    val limval = limit.getOrElse(defLimit.toString)
    val offval = offset.getOrElse(defOffset.toString)

    val filterString = classificationfilter.getOrElse("").replaceAll("\\s+", "")
    val endpointType = "partial"

    val fall = fallback.flatMap(x => Try(x.toBoolean).toOption).getOrElse(true)
    val hist = historical.flatMap(x => Try(x.toBoolean).toOption).getOrElse(true)
    val verb = verbose.flatMap(x => Try(x.toBoolean).toOption).getOrElse(false)
    val favourPaf = favourpaf.flatMap(x => Try(x.toBoolean).toOption).getOrElse(true)
    val favourWelsh = favourwelsh.flatMap(x => Try(x.toBoolean).toOption).getOrElse(true)
    val highVal = highlight.getOrElse("on")
    val highVerbose: Boolean = highVal == "debug"

    val epochVal = epoch.getOrElse("")
    val fromsourceVal = {if (fromsource.getOrElse("all").isEmpty) "all" else fromsource.getOrElse("all")}

    def writeLog(doResponseTime: Boolean = true, badRequestErrorMessage: String = "", notFound: Boolean = false, formattedOutput: String = "", numOfResults: String = "", score: String = "", activity: String = ""): Unit = {
      val responseTime = if (doResponseTime) (System.currentTimeMillis() - startingTime).toString else ""
      val networkId = if (req.headers.get("authorization").getOrElse("Anon").indexOf("+") > 0) req.headers.get("authorization").getOrElse("Anon").split("\\+")(0) else req.headers.get("authorization").getOrElse("Anon").split("_")(0)
      val organisation = if (req.headers.get("authorization").getOrElse("Anon").indexOf("+") > 0) req.headers.get("authorization").getOrElse("Anon").split("\\+")(0).split("_")(1) else "not set"

      logger.systemLog(
        ip = req.remoteAddress, url = req.uri, responseTimeMillis = responseTime,
        partialAddress = input, isNotFound = notFound, offset = offval,
        fallback = fall,
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
      fallback = Some(fall),
      epoch = Some(epochVal),
      filter = Some(filterString),
      historical = Some(hist),
      limit = Some(limitInt),
      offset = Some(offsetInt),
      verbose = Some(verb),
      fromsource = Some(fromsourceVal),
      highlight = Some(highVal),
      favourpaf = Some(favourPaf),
      favourwelsh = Some(favourWelsh)
    )

    val result: Option[Future[Result]] =
      partialAddressValidation.validatePartialLimit(limit, queryValues)
        .orElse(partialAddressValidation.validatePartialOffset(offset, queryValues))
        .orElse(partialAddressValidation.validateSource(queryValues))
        .orElse(partialAddressValidation.validateKeyStatus(queryValues))
        .orElse(partialAddressValidation.validateInput(input, queryValues))
        .orElse(partialAddressValidation.validateAddressFilter(classificationfilter, queryValues))
        .orElse(partialAddressValidation.validateEpoch(queryValues))
        .orElse(partialAddressValidation.validateFromSource(queryValues))
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
          historical = hist,
          verbose = verb,
          epoch = epochVal,
          skinny = !verb,
          fromsource = fromsourceVal,
          highlight = highVal,
          favourpaf = favourPaf,
          favourwelsh = favourWelsh
        )

        val request: Future[HybridAddressCollection] =
          overloadProtection.breaker.withCircuitBreaker(
            esRepo.runMultiResultQuery(args)
          )

        request.map {
          case HybridAddressCollection(hybridAddresses, maxScore, total) =>
            val addresses: Seq[AddressResponseAddressEQ] = hybridAddresses.map(
              AddressResponseAddressEQ.fromHybridAddress(_, verb)
            )

            val sortAddresses = if (sboost > 0) boostAtStart(addresses, input, favourPaf, favourWelsh, highVerbose) else addresses

            writeLog(activity = "eq_partial_request")

            jsonOk(
              AddressByEQPartialAddressResponseContainer(
                apiVersion = apiVersion,
                dataVersion = dataVersion,
                response = AddressByEQPartialAddressResponse(
                  input = input,
                  addresses = AddressByEQPartialAddressResponse.toEQAddressByPartialResponse(sortAddresses),
                  filter = filterString,
                  fallback = fall,
                  historical = hist,
                  epoch = epochVal,
                  limit = limitInt,
                  offset = offsetInt,
                  total = total,
                  maxScore = maxScore,
                  verbose = verb,
                  fromsource = fromsourceVal,
                  highlight = highVal,
                  favourpaf = favourPaf,
                  favourwelsh = favourWelsh
                ),
                status = OkAddressResponseStatus
              )
            )
        }.recover {
          case NonFatal(exception) =>
            if (overloadProtection.breaker.isHalfOpen) {
              logger.warn(s"Elasticsearch is overloaded or down (partialAddress input). Circuit breaker is Half Open: ${exception.getMessage}")
              TooManyRequests(Json.toJson(FailedRequestToEsTooBusyPartialAddress(exception.getMessage, queryValues)))
            }else if (overloadProtection.breaker.isOpen) {
              logger.warn(s"Elasticsearch is overloaded or down (partialAddress input). Circuit breaker is Open: ${exception.getMessage}")
              TooManyRequests(Json.toJson(FailedRequestToEsTooBusyPartialAddress(exception.getMessage, queryValues)))
            } else {
              // Circuit Breaker is closed. Some other problem
              writeLog(badRequestErrorMessage = FailedRequestToEsPartialAddressError.message)
              logger.warn(s"Could not handle individual request (partialAddress input), problem with ES ${exception.getMessage}")
              InternalServerError(Json.toJson(FailedRequestToEsPartialAddress(exception.getMessage, queryValues)))
            }
        }
    }
  }

  def getBestMatchAddress(highlights: Option[AddressResponseHighlight], favourPaf: Boolean = true, favourWelsh: Boolean = false): String =
  {

    highlights match {
      case Some(value) => AddressResponseAddressEQ.removeConcatenatedPostcode(AddressResponseAddress.removeEms(determineBestMatchAddress(value, favourPaf, favourWelsh)))
      case None => ""
    }
  }

  def getSource(highlights: Option[AddressResponseHighlight], favourPaf: Boolean = true, favourWelsh: Boolean = false): String =
  {

    highlights match {
      case Some(value) => AddressResponseAddressEQ.removeConcatenatedPostcode(AddressResponseAddressEQ.removeEms(determineSource(value, favourPaf, favourWelsh)))
      case None => ""
    }
  }

  def getLang(highlights: Option[AddressResponseHighlight], favourPaf: Boolean = true, favourWelsh: Boolean = false): String =
  {

    highlights match {
      case Some(value) => AddressResponseAddressEQ.removeConcatenatedPostcode(AddressResponseAddressEQ.removeEms(determineLang(value, favourPaf, favourWelsh)))
      case None => ""
    }
  }

  def determineBestMatchAddress(highlight: AddressResponseHighlight, favourPaf: Boolean, favourWelsh: Boolean): String =
  {
    val highs = sortHighs(highlight.hits.getOrElse(Seq()), favourPaf, favourWelsh)
    highs.headOption.map(_.highLightedText).getOrElse("")
  }

  def determineSource(highlight: AddressResponseHighlight, favourPaf: Boolean, favourWelsh: Boolean): String =
  {
    val highs = sortHighs(highlight.hits.getOrElse(Seq()), favourPaf, favourWelsh)
    highs.headOption.map(_.source).getOrElse("")
  }

  def determineLang(highlight: AddressResponseHighlight, favourPaf: Boolean, favourWelsh: Boolean): String =
  {
    val highs = sortHighs(highlight.hits.getOrElse(Seq()), favourPaf, favourWelsh)
    highs.headOption.map(_.lang).getOrElse("")
  }

  def sortHighs(hits: Seq[AddressResponseHighlightHit], favourPaf: Boolean, favourWelsh: Boolean): Seq[AddressResponseHighlightHit] =
  {
    if (favourPaf) {
      if (favourWelsh)
        hits.sortBy(_.source)(Ordering[String].reverse).sortBy(_.lang)(Ordering[String].reverse).sortBy(_.distinctHitCount)(Ordering[Int].reverse)
      else
        hits.sortBy(_.source)(Ordering[String].reverse).sortBy(_.lang).sortBy(_.distinctHitCount)(Ordering[Int].reverse)
    } else {
      if (favourWelsh)
        hits.sortBy(_.source).sortBy(_.lang)(Ordering[String].reverse).sortBy(_.distinctHitCount)(Ordering[Int].reverse)
      else
        hits.sortBy(_.source).sortBy(_.lang).sortBy(_.distinctHitCount)(Ordering[Int].reverse)
    }
  }

  def boostAtStart(inAddresses: Seq[AddressResponseAddressEQ], input: String, favourPaf: Boolean, favourWelsh: Boolean, highVerbose: Boolean): Seq[AddressResponseAddressEQ] = {
    inAddresses.map { add => boostAddress(add, input, favourPaf, favourWelsh, highVerbose) }
  }

  def boostAddress(add: AddressResponseAddressEQ, input: String, favourPaf: Boolean, favourWelsh: Boolean, highVerbose: Boolean): AddressResponseAddressEQ = {
    if (add.formattedAddress.toUpperCase().replaceAll("[,]", "").startsWith(input.toUpperCase().replaceAll("[,]", ""))) {
      add.copy(
        confidenceScore = (math.round(add.underlyingScore)*5).min(100),
        underlyingScore = add.underlyingScore,
        highlights = if (add.highlights.isEmpty) None else Option(add.highlights.get.copy(
          bestMatchAddress = getBestMatchAddress(add.highlights, favourPaf, favourWelsh),
          source = getSource(add.highlights, favourPaf, favourWelsh),
          lang = getLang(add.highlights, favourPaf, favourWelsh),
          hits = if (highVerbose) Option(sortHighs(add.highlights.get.hits.getOrElse(Seq()), favourPaf, favourWelsh)) else None)))
    } else add.copy(
      confidenceScore = (math.round(add.underlyingScore)*5).min(100),
      underlyingScore = add.underlyingScore,
      highlights = if (add.highlights.isEmpty) None else Option(add.highlights.get.copy(
        bestMatchAddress = getBestMatchAddress(add.highlights, favourPaf, favourWelsh),
        source = getSource(add.highlights, favourPaf, favourWelsh),
        lang = getLang(add.highlights, favourPaf, favourWelsh),
        hits = if (highVerbose) Option(sortHighs(add.highlights.get.hits.getOrElse(Seq()), favourPaf, favourWelsh)) else None)))
  }
}
