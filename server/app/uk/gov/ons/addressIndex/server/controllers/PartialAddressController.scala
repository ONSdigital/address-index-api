package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import uk.gov.ons.addressIndex.model.db.index.HybridAddressCollection
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseAddress, AddressResponseHighlight, FailedRequestToEsPartialAddressError, OkAddressResponseStatus}
import uk.gov.ons.addressIndex.model.server.response.partialaddress.{AddressByPartialAddressResponse, AddressByPartialAddressResponseContainer}
import uk.gov.ons.addressIndex.server.model.dao.QueryValues
import uk.gov.ons.addressIndex.server.modules._
import uk.gov.ons.addressIndex.server.modules.response.PartialAddressControllerResponse
import uk.gov.ons.addressIndex.server.modules.validation.PartialAddressControllerValidation
import uk.gov.ons.addressIndex.server.utils.{APIThrottle, AddressAPILogger}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.control.NonFatal

@Singleton
class PartialAddressController @Inject()(val controllerComponents: ControllerComponents,
                                         esRepo: ElasticsearchRepository,
                                         conf: ConfigModule,
                                         versionProvider: VersionModule,
                                         overloadProtection: APIThrottle,
                                         partialAddressValidation: PartialAddressControllerValidation
                                        )(implicit ec: ExecutionContext)
  extends PlayHelperController(versionProvider) with PartialAddressControllerResponse {

  lazy val logger: AddressAPILogger = AddressAPILogger("address-index-server:PartialAddressController")

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
                          highverbose: Option[String] = None,
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
    val hist = historical.flatMap(x => Try(x.toBoolean).toOption).getOrElse(false)
    val verb = verbose.flatMap(x => Try(x.toBoolean).toOption).getOrElse(false)
    val favourPaf = favourpaf.flatMap(x => Try(x.toBoolean).toOption).getOrElse(true)
    val favourWelsh = favourwelsh.flatMap(x => Try(x.toBoolean).toOption).getOrElse(true)
    val highVerbose = highverbose.flatMap(x => Try(x.toBoolean).toOption).getOrElse(true)

    val epochVal = epoch.getOrElse("")
    val fromsourceVal = {if (fromsource.getOrElse("all").isEmpty) "all" else fromsource.getOrElse("all")}

    val sboost = conf.config.elasticSearch.defaultStartBoost

    def boostAtStart(inAddresses: Seq[AddressResponseAddress]): Seq[AddressResponseAddress] = {
      val boostedAddresses: Seq[AddressResponseAddress] = inAddresses.map { add => boostAddress(add) }
      boostedAddresses.sortBy(_.underlyingScore)(Ordering[Float].reverse)
    }

    def boostAddress(add: AddressResponseAddress): AddressResponseAddress = {
      if (add.formattedAddress.toUpperCase().replaceAll("[,]", "").startsWith(input.toUpperCase().replaceAll("[,]", ""))) {
        add.copy(underlyingScore = add.underlyingScore + sboost, highlights = Option(add.highlights.get.copy(
        bestMatchField = getBestMatchField(add.highlights, favourPaf, favourWelsh, add.formattedAddressNag,add.formattedAddressPaf,add.welshFormattedAddressNag, add.welshFormattedAddressPaf))))
      } else add.copy(underlyingScore = add.underlyingScore, highlights = Option(add.highlights.get.copy(
        bestMatchField = getBestMatchField(add.highlights, favourPaf, favourWelsh, add.formattedAddressNag,add.formattedAddressPaf,add.welshFormattedAddressNag, add.welshFormattedAddressPaf))))
    }

    def getBestMatchField(highlights: Option[AddressResponseHighlight], favourPaf: Boolean = true, favourWelsh: Boolean = false, nag: String, paf: String, welshNag: String, welshPaf: String): String =
    {

      val highs:AddressResponseHighlight = highlights match {
        case Some(value) => value
        case None => null
      }

      val nags = highs.highlight.filter(_._1 == "lpi.nagAll.partial").map {
        hlist => hlist._2.map {lin =>
          val hLine = lin.mkString.split(" ").distinct.mkString
  //        println("hString = " + hLine)
          hLine.count(_ == '<')
        }
      }

      val pafs_w = highs.highlight.filter(_._1 == "paf.mixedWelshPaf.partial").map {
        hlist => val hString = hlist._2.mkString.split(" ").distinct.mkString
 //         println("hString = " + hString)
          hString.count(_ == '<')
      }

      val pafs_e = highs.highlight.filter(_._1 == "paf.mixedPaf.partial").map {
        hlist => val hString = hlist._2.mkString.split(" ").distinct.mkString
   //       println("hString = " + hString)
          hString.count(_ == '<')
      }

      val maxpafs_e = if(pafs_e.isEmpty) 0 else pafs_e.max / 2
      val maxpafs_w = if(pafs_w.isEmpty) 0 else pafs_w.max / 2
      val maxnags = if(nags.isEmpty) 0 else nags.head.max / 2
      val maxpafs = math.max(maxpafs_e,maxpafs_w)
  //    println("pafs_e = " + maxpafs_e)
  //    println("pafs_w = " + maxpafs_w)
  //    println("nags = " + maxnags)

// this logic is not complete
     if (maxnags > maxpafs) {
       if (favourWelsh && !welshNag.isEmpty) welshNag else nag
     } else {
       if (maxpafs_e > maxpafs_w) paf else welshPaf
     }
    }



    /**
      * Calculates the edit distance between two strings
      *
      * @param str1 string 1
      * @param str2 string 2
      * @return int number of edits required
      */
    def levenshtein(str1: String, str2: String): Int = {
      val lenStr1 = str1.length
      val lenStr2 = str2.length
      val d: Array[Array[Int]] = Array.ofDim(lenStr1 + 1, lenStr2 + 1)
      for (i <- 0 to lenStr1) d(i)(0) = i
      for (j <- 0 to lenStr2) d(0)(j) = j
      for (i <- 1 to lenStr1; j <- 1 to lenStr2) {
        val cost = if (str1(i - 1) == str2(j - 1)) 0 else 1
        d(i)(j) = min(
          d(i - 1)(j) + 1, // deletion
          d(i)(j - 1) + 1, // insertion
          d(i - 1)(j - 1) + cost // substitution
        )
      }
      d(lenStr1)(lenStr2)
    }

    /**
      * Return the smallest number in a list
      *
      * @param nums nums
      * @return
      */
    def min(nums: Int*): Int = nums.min

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
      highverbose = Some(highVerbose),
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
          highverbose = highVerbose,
          favourpaf = favourPaf,
          favourwelsh = favourWelsh
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

            jsonOk(
              AddressByPartialAddressResponseContainer(
                apiVersion = apiVersion,
                dataVersion = dataVersion,
                response = AddressByPartialAddressResponse(
                  input = input,
                  addresses = sortAddresses,
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
                  highverbose = highVerbose,
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
}
