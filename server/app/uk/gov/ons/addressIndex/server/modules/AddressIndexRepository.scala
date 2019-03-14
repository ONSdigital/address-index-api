package uk.gov.ons.addressIndex.server.modules

import com.sksamuel.elastic4s.analyzers.CustomAnalyzer
import com.sksamuel.elastic4s.http.ElasticDsl.{geoDistanceQuery, _}
import com.sksamuel.elastic4s.http.HttpClient
import com.sksamuel.elastic4s.searches.queries.{BoolQueryDefinition, ConstantScoreDefinition, QueryDefinition}
import com.sksamuel.elastic4s.searches.sort.{FieldSortDefinition, SortOrder}
import com.sksamuel.elastic4s.searches.{SearchDefinition, SearchType}
import javax.inject.{Inject, Singleton}
import uk.gov.ons.addressIndex.model.config.QueryParamsConfig
import uk.gov.ons.addressIndex.model.db.index._
import uk.gov.ons.addressIndex.model.db.{BulkAddress, BulkAddressRequestData}
import uk.gov.ons.addressIndex.model.server.response.address.AddressResponseAddress
import uk.gov.ons.addressIndex.model.server.response.bulk.AddressBulkResponseAddress
import uk.gov.ons.addressIndex.parsers.Tokens
import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import uk.gov.ons.addressIndex.server.utils.{ConfidenceScoreHelper, GenericLogger, HopperScoreHelper}

import scala.concurrent.{ExecutionContext, Future}
import scala.math._
import scala.util.Try

@Singleton
class AddressIndexRepository @Inject()(conf: AddressIndexConfigModule,
                                       elasticClientProvider: ElasticClientProvider
                                      )(implicit ec: ExecutionContext) extends ElasticsearchRepository {

  private val esConf = conf.config.elasticSearch
  //  private val hybridIndex = esConf.indexes.hybridIndex + "/" + esConf.indexes.hybridMapping
  //  private val hybridIndexHistorical = esConf.indexes.hybridIndexHistorical + "/" + esConf.indexes.hybridMapping

  private val clusterPolicyUprn = if (esConf.clusterPolicies.uprn.isEmpty) "" else "_c" + esConf.clusterPolicies.uprn
  private val clusterPolicyPartial = if (esConf.clusterPolicies.partial.isEmpty) "" else "_c" + esConf.clusterPolicies.partial
  private val clusterPolicyPostcode = if (esConf.clusterPolicies.postcode.isEmpty) "" else "_c" + esConf.clusterPolicies.postcode
  private val clusterPolicyAddress = if (esConf.clusterPolicies.address.isEmpty) "" else "_c" + esConf.clusterPolicies.address
  private val clusterPolicyBulk = if (esConf.clusterPolicies.bulk.isEmpty) "" else "_c" + esConf.clusterPolicies.bulk
  private val clusterPolicyRandom = if (esConf.clusterPolicies.random.isEmpty) "" else "_c" + esConf.clusterPolicies.random

  private val hybridIndexUprn = esConf.indexes.hybridIndex + clusterPolicyUprn
  private val hybridIndexHistoricalUprn = esConf.indexes.hybridIndexHistorical + clusterPolicyUprn
  private val hybridIndexSkinnyUprn = esConf.indexes.hybridIndexSkinny + clusterPolicyUprn
  private val hybridIndexHistoricalSkinnyUprn = esConf.indexes.hybridIndexHistoricalSkinny + clusterPolicyUprn
  private val hybridIndexPartial = esConf.indexes.hybridIndex + clusterPolicyPartial
  private val hybridIndexHistoricalPartial = esConf.indexes.hybridIndexHistorical + clusterPolicyPartial
  private val hybridIndexSkinnyPartial = esConf.indexes.hybridIndexSkinny + clusterPolicyPartial
  private val hybridIndexHistoricalSkinnyPartial = esConf.indexes.hybridIndexHistoricalSkinny + clusterPolicyPartial
  private val hybridIndexPostcode = esConf.indexes.hybridIndex + clusterPolicyPostcode
  private val hybridIndexHistoricalPostcode = esConf.indexes.hybridIndexHistorical + clusterPolicyPostcode
  private val hybridIndexSkinnyPostcode = esConf.indexes.hybridIndexSkinny + clusterPolicyPostcode
  private val hybridIndexHistoricalSkinnyPostcode = esConf.indexes.hybridIndexHistoricalSkinny + clusterPolicyPostcode
  private val hybridIndexAddress = esConf.indexes.hybridIndex + clusterPolicyAddress
  private val hybridIndexHistoricalAddress = esConf.indexes.hybridIndexHistorical + clusterPolicyAddress
  private val hybridIndexBulk = esConf.indexes.hybridIndex + clusterPolicyBulk
  private val hybridIndexHistoricalBulk = esConf.indexes.hybridIndexHistorical + clusterPolicyBulk
  private val hybridIndexSkinnyRandom = esConf.indexes.hybridIndexSkinny + clusterPolicyRandom
  private val hybridIndexHistoricalSkinnyRandom = esConf.indexes.hybridIndexHistoricalSkinny + clusterPolicyRandom
  private val hybridIndexRandom = esConf.indexes.hybridIndex + clusterPolicyRandom
  private val hybridIndexHistoricalRandom = esConf.indexes.hybridIndexHistorical + clusterPolicyRandom
  private val hybridMapping = "/" + esConf.indexes.hybridMapping

  private val dateFormat = "yyyy-MM-dd"

  val client: HttpClient = elasticClientProvider.client
  lazy val logger = GenericLogger("AddressIndexRepository")

  private def getFilterType(filters: String): String = filters match {
    case "residential" | "commercial" => "prefix"
    case f if f.endsWith("*") => "prefix"
    case _ => "term"
  }

  private def getFilterValuePrefix(filters: String): String = filters match {
    case "residential" => "R"
    case "commercial" => "C"
    case f if f.endsWith("*") => filters.substring(0, filters.length - 1).toUpperCase
    case f => f.toUpperCase()
  }

  private def getFilterValueTerm(filters: String): Seq[String] = filters.toUpperCase.split(",")

  private def getEpochParam(epoch: String): String = if (epoch.isEmpty) "_current" else "_" + epoch

  private def makeDateQuery(startDate: String, endDate: String): Option[QueryDefinition] = {
    if (startDate.isEmpty && endDate.isEmpty) None
    else Some(should(
      must(rangeQuery("paf.startDate").gte(startDate).format(dateFormat),
        rangeQuery("paf.endDate").lte(endDate).format(dateFormat)),
      must(rangeQuery("lpi.lpiStartDate").gte(startDate).format(dateFormat),
        rangeQuery("lpi.lpiEndDate").lte(endDate).format(dateFormat))))
  }

  def queryHealth(): Future[String] = client.execute(clusterHealth()).map(_.toString)

  def generateUprnQueryDefinition(uprn: String,
                                  startDate: String = "",
                                  endDate: String = ""): QueryDefinition =
    makeDateQuery(startDate, endDate) match {
      case Some(q) => must(termQuery("uprn", uprn)).filter(q)
      case None => termQuery("uprn", uprn)
    }

  /**
    * Generates request to get address from ES by UPRN
    * Public for tests
    *
    * @param uprn the uprn of the fetched address
    * @return Search definition containing query to the ES
    */
  def generateQueryUprnRequest(uprn: String,
                               startDate: String = "",
                               endDate: String = "",
                               historical: Boolean = true,
                               epoch: String): SearchDefinition = {

    val query = generateUprnQueryDefinition(uprn, startDate, endDate)

    val epochParam = getEpochParam(epoch)

    val source =
      if (historical) hybridIndexHistoricalUprn
      else hybridIndexUprn

    search(source + epochParam + hybridMapping).query(query)
  }

  /**
    * Generates request to get address from ES by UPRN
    * Public for tests
    *
    * @param uprn the uprn of the fetched address
    * @return Seqrch definition containing query to the ES
    */
  def generateQueryUprnSkinnyRequest(uprn: String,
                                     startDate: String = "",
                                     endDate: String = "",
                                     historical: Boolean = true,
                                     epoch: String): SearchDefinition = {

    val query = generateUprnQueryDefinition(uprn, startDate, endDate)

    val epochParam = getEpochParam(epoch)

    val source = if (historical) hybridIndexHistoricalSkinnyUprn else hybridIndexSkinnyUprn

    search(source + epochParam + hybridMapping).query(query)
  }

  def queryUprn(uprn: String,
                startDate: String = "",
                endDate: String = "",
                historical: Boolean = true,
                epoch: String): Future[Option[HybridAddress]] = {

    val request = generateQueryUprnRequest(uprn, startDate, endDate, historical, epoch)

    logger.trace(request.toString)

    client.execute(request)
      .map(HybridAddresses.fromEither)
      .map(_.addresses.headOption)
  }

  def queryUprnSkinny(uprn: String,
                      startDate: String = "",
                      endDate: String = "",
                      historical: Boolean = true,
                      epoch: String): Future[Option[HybridAddressSkinny]] = {

    val request = generateQueryUprnSkinnyRequest(uprn, startDate, endDate, historical, epoch)

    logger.trace(request.toString)

    client.execute(request)
      .map(HybridAddressesSkinny.fromEither)
      .map(_.addresses.headOption)
  }

  /**
    * Generates request to get address from partial string (e.g typeahead)
    * Pass on to fallback if needed
    *
    * @param input      the partial string to be searched
    * @param start      start result
    * @param limit      maximum number of results
    * @param filters    classification filter
    * @param startDate  start date
    * @param endDate    end date
    * @param historical historical flag
    * @param verbose    verbose flag (use skinny index if false)
    * @return Search definition containing query to the ES
    */
  def queryPartialAddress(input: String, start: Int, limit: Int, filters: String, startDate: String = "", endDate: String = "", historical: Boolean = true, verbose: Boolean = true, epoch: String = ""): Future[HybridAddresses] = {

    val request = generateQueryPartialAddressRequest(input, filters, startDate, endDate, historical, fallback = false, verbose, epoch).start(start).limit(limit)
    val partResult = client.execute(request).map(HybridAddresses.fromEither)

    // if there are no results for the "phrase" query, delegate to an alternative "best fields" query
    partResult.map { adds =>
      if (adds.addresses.isEmpty) queryPartialAddressFallback(input, start, limit, filters, startDate, endDate, historical, verbose, epoch)
      else partResult
    }.flatten
  }

  /**
    * Generates request to get address from partial string (e.g typeahead)
    * Pass on to fallback if needed
    *
    * @param input      the partial string to be searched
    * @param start      start result
    * @param limit      maximum number of results
    * @param filters    classification filter
    * @param startDate  start date
    * @param endDate    end date
    * @param historical historical flag
    * @param verbose    verbose flag (use skinny index if false)
    * @return Search definition containing query to the ES
    */
  def queryPartialAddressSkinny(input: String, start: Int, limit: Int, filters: String, startDate: String = "", endDate: String = "", historical: Boolean = true, verbose: Boolean = false, epoch: String = ""): Future[HybridAddressesSkinny] = {

    val request = generateQueryPartialAddressRequest(input, filters, startDate, endDate, historical, fallback = false, verbose, epoch).start(start).limit(limit)
    val partResult = client.execute(request).map(HybridAddressesSkinny.fromEither)

    // if there are no results for the "phrase" query, delegate to an alternative "best fields" query
    partResult.map { adds =>
      if (adds.addresses.isEmpty) queryPartialAddressFallbackSkinny(input, start, limit, filters, startDate, endDate, historical, verbose, epoch)
      else partResult
    }.flatten
  }

  /**
    * Generates request to get address from partial string (e.g typeahead)
    * Fallback version
    *
    * @param input      the partial string to be searched
    * @param start      start result
    * @param limit      maximum number of results
    * @param filters    classification filter
    * @param startDate  start date
    * @param endDate    end date
    * @param historical historical flag
    * @param verbose    verbose flag (use skinny index if false)
    * @return Search definition containing query to the ES
    */
  def queryPartialAddressFallback(input: String, start: Int, limit: Int, filters: String, startDate: String = "", endDate: String = "", historical: Boolean = true, verbose: Boolean = true, epoch: String = ""): Future[HybridAddresses] = {
    logger.warn("best fields fallback query invoked for input string " + input)
    val fallback = generateQueryPartialAddressRequest(input, filters, startDate, endDate, historical, fallback = true, verbose, epoch).start(start).limit(limit)
    client.execute(fallback).map(HybridAddresses.fromEither)
  }

  /**
    * Generates request to get address from partial string (e.g typeahead)
    * Fallback version
    *
    * @param input      the partial string to be searched
    * @param start      start result
    * @param limit      maximum number of results
    * @param filters    classification filter
    * @param startDate  start date
    * @param endDate    end date
    * @param historical historical flag
    * @param verbose    verbose flag (use skinny index if false)
    * @param epoch      Epoch param
    * @return Search definition containing query to the ES
    */
  def queryPartialAddressFallbackSkinny(input: String, start: Int, limit: Int, filters: String, startDate: String = "", endDate: String = "", historical: Boolean = true, verbose: Boolean = false, epoch: String): Future[HybridAddressesSkinny] = {
    logger.warn("best fields fallback query invoked for input string " + input)
    val fallback = generateQueryPartialAddressRequest(input, filters, startDate, endDate, historical, fallback = true, verbose = verbose, epoch).start(start).limit(limit)
    client.execute(fallback).map(HybridAddressesSkinny.fromEither)
  }

  /**
    * Generates request to get address from partial string (e.g typeahead)
    * Public for tests
    *
    * @param input      partial string
    * @param filters    classification filter
    * @param startDate  start date
    * @param endDate    end date
    * @param historical historical flag
    * @param fallback   flag to indicate if fallback query is required
    * @param verbose    flag to indicate that skinny index should be used when false
    * @return Search definition containing query to the ES
    */
  def generateQueryPartialAddressRequest(input: String, filters: String, startDate: String, endDate: String, historical: Boolean = true, fallback: Boolean = false, verbose: Boolean = true, epoch: String): SearchDefinition = {

    val filterType = getFilterType(filters)
    val filterValuePrefix = getFilterValuePrefix(filters)
    val filterValueTerm = getFilterValueTerm(filters)

    // collect all numbers in input as separate tokens
    val inputNumberList: List[String] = input.split("\\D+").filter(_.nonEmpty).toList

    val slopVal = 4

    val dateQuery = makeDateQuery(startDate, endDate)

    val abQuery: Option[QueryDefinition] = {
      if (verbose) {
        Option(not(termQuery("lpi.addressBasePostal", "N")))
      } else None
    }

    val fieldsToSearch = Seq("lpi.nagAll.partial", "paf.mixedPaf.partial", "paf.mixedWelshPaf.partial")

    val queryBase = multiMatchQuery(input).fields(fieldsToSearch)

    val queryWithMatchType =
      if (fallback) queryBase.matchType("best_fields")
      else queryBase.matchType("phrase").slop(slopVal)

    val filterSeq = Seq(
      if (filters.isEmpty) {
        None
      } else if (filterType == "prefix") {
        Some(prefixQuery("classificationCode", filterValuePrefix))
      } else {
        Some(termsQuery("classificationCode", filterValueTerm))
      },
      abQuery,
      dateQuery,
    ).flatten

    // if there is only one number, give boost for pao or sao not both.
    // if there are two or more numbers, boost for either matching pao and first matching sao
    // the usual order is (sao pao) and a higher score is given for this match
    // helper function
    def numMatchQuery(field: String, value: Any) = matchQuery(field, value).prefixLength(1).maxExpansions(10).fuzzyTranspositions(false)

    val numberQuery: Seq[QueryDefinition] = inputNumberList match {
      case first :: second :: _ if first == second => Seq(
        // allow the target pao and target sao to match once each
        // prevents (a a -> a b) from causing two matches
        numMatchQuery("lpi.paoStartNumber", first).boost(0.5D),
        numMatchQuery("lpi.saoStartNumber", first).boost(0.5D))
      case first :: second :: _ => Seq(
        // allow the input pao and input sao to match once each
        // because they cannot both match the same target, matches should not overlap (usually)
        dismax(
          numMatchQuery("lpi.paoStartNumber", first).boost(0.2D),
          numMatchQuery("lpi.saoStartNumber", first).boost(0.5D)),
        dismax(
          numMatchQuery("lpi.paoStartNumber", second).boost(0.5D),
          numMatchQuery("lpi.saoStartNumber", second).boost(0.2D)))
      case Seq(first) => Seq(
        // otherwise, match either
        dismax(
          numMatchQuery("lpi.paoStartNumber", first).boost(0.5D),
          numMatchQuery("lpi.saoStartNumber", first).boost(0.2D)))
      case _ => Seq.empty
    }

    val query = must(queryWithMatchType).filter(filterSeq).should(numberQuery)

    val epochParam = getEpochParam(epoch)

    val source = if (historical) {
      if (verbose) hybridIndexHistoricalPartial else hybridIndexHistoricalSkinnyPartial
    } else {
      if (verbose) hybridIndexPartial else hybridIndexSkinnyPartial
    }

    search(source + epochParam + hybridMapping).query(query)
  }

  def queryPostcode(postcode: String, start: Int, limit: Int, filters: String, startDate: String = "", endDate: String = "", historical: Boolean = true, verbose: Boolean = true, epoch: String): Future[HybridAddresses] = {
    val request = generateQueryPostcodeRequest(postcode, filters, startDate, endDate, historical, verbose, epoch).start(start).limit(limit)
    logger.trace(request.toString)
    client.execute(request).map(HybridAddresses.fromEither)
  }

  def queryPostcodeSkinny(postcode: String, start: Int, limit: Int, filters: String, startDate: String = "", endDate: String = "", historical: Boolean = true, verbose: Boolean = false, epoch: String): Future[HybridAddressesSkinny] = {
    val request = generateQueryPostcodeRequest(postcode, filters, startDate, endDate, historical, verbose, epoch).start(start).limit(limit)
    logger.trace(request.toString)
    client.execute(request).map(HybridAddressesSkinny.fromEither)
  }

  /**
    * Generates request to get address from ES by Postcode
    * Public for tests
    *
    * @param postcode the postcode of the fetched address
    * @param verbose  flag to indicate that skinny index should be used when false
    * @return Search definition containing query to the ES
    */
  def generateQueryPostcodeRequest(postcode: String, filters: String, startDate: String, endDate: String, historical: Boolean = true, verbose: Boolean = true, epoch: String): SearchDefinition = {

    val filterType = getFilterType(filters)
    val filterValuePrefix = getFilterValuePrefix(filters)
    val filterValueTerm = getFilterValueTerm(filters)

    val postcodeFormatted: String = {
      if (!postcode.contains(" ")) {
        val postcodeLength = postcode.length()
        val (postcodeStart, postcodeEnd) = postcode.splitAt(postcodeLength - 3)
        (postcodeStart + " " + postcodeEnd).toUpperCase
      }
      else postcode.toUpperCase
    }

    val dateQuery = makeDateQuery(startDate, endDate)

    val abQuery: Option[QueryDefinition] = {
      if (verbose) Option(not(termQuery("lpi.addressBasePostal", "N")))
      else None
    }

    val queryFilter = if (filters.isEmpty) {
      Seq(abQuery, dateQuery)
    } else {
      if (filterType == "prefix") Seq(Option(prefixQuery("classificationCode", filterValuePrefix)), abQuery, dateQuery)
      else Seq(Option(termsQuery("classificationCode", filterValueTerm)), abQuery, dateQuery)
    }

    val query = must(termQuery("lpi.postcodeLocator", postcodeFormatted)).filter(queryFilter.flatten)

    val epochParam = getEpochParam(epoch)

    val source = if (historical) {
      if (verbose) hybridIndexHistoricalPostcode else hybridIndexHistoricalSkinnyPostcode
    } else {
      if (verbose) hybridIndexPostcode else hybridIndexSkinnyPostcode
    }

    val searchBase = search(source + epochParam + hybridMapping)

    searchBase.query(query)
      .sortBy(FieldSortDefinition("lpi.streetDescriptor.keyword").asc(),
        FieldSortDefinition("lpi.paoStartNumber").asc(),
        FieldSortDefinition("lpi.paoStartSuffix.keyword").asc(),
        FieldSortDefinition("uprn").asc())
  }

  def queryRandom(filters: String, limit: Int, historical: Boolean = true, verbose: Boolean = true, epoch: String): Future[HybridAddresses] = {
    val request = generateQueryRandomRequest(filters, historical, verbose, epoch).limit(limit)
    logger.trace(request.toString)
    client.execute(request).map(HybridAddresses.fromEither)
  }

  def queryRandomSkinny(filters: String, limit: Int, historical: Boolean = true, verbose: Boolean = false, epoch: String): Future[HybridAddressesSkinny] = {
    val request = generateQueryRandomRequest(filters, historical, verbose, epoch).limit(limit)
    logger.trace(request.toString)
    client.execute(request).map(HybridAddressesSkinny.fromEither)
  }

  /**
    * Generates request to get random address from ES
    * Public for tests
    *
    * @return Search definition containing query to the ES
    */
  def generateQueryRandomRequest(filters: String, historical: Boolean = true, verbose: Boolean = false, epoch: String): SearchDefinition = {

    val filterType = getFilterType(filters)
    val filterValuePrefix = getFilterValuePrefix(filters)
    val filterValueTerm = getFilterValueTerm(filters)

    val timestamp: Long = System.currentTimeMillis

    val abQuery: Option[QueryDefinition] = {
      if (verbose) {
        Option(not(termQuery("lpi.addressBasePostal", "N")))
      }
      else None
    }

    val queryInner = filters match {
      case "" => boolQuery().filter(Seq(abQuery).flatten)
      case _ => filterType match {
        case "prefix" => boolQuery().filter(Seq(Option(prefixQuery("classificationCode", filterValuePrefix)), abQuery).flatten)
        case _ => boolQuery().filter(Seq(Option(termsQuery("classificationCode", filterValueTerm)), abQuery).flatten)
      }
    }

    val query = functionScoreQuery()
      .functions(randomScore(timestamp.toInt))
      .query(queryInner)
      .boostMode("replace")

    val epochParam = getEpochParam(epoch)

    val source = if (historical) {
      if (verbose) hybridIndexHistoricalRandom else hybridIndexHistoricalSkinnyRandom
    } else {
      if (verbose) hybridIndexRandom else hybridIndexSkinnyRandom
    }

    search(source + epochParam + hybridMapping).query(query)
  }

  def queryAddresses(tokens: Map[String, String],
                     start: Int,
                     limit: Int,
                     filters: String,
                     range: String,
                     lat: String,
                     lon: String,
                     startDate: String,
                     endDate: String,
                     queryParamsConfig: Option[QueryParamsConfig] = None,
                     historical: Boolean = true,
                     isBulk: Boolean = false,
                     epoch: String): Future[HybridAddresses] = {

    val request = generateQueryAddressRequest(tokens, filters, range, lat, lon, startDate, endDate, queryParamsConfig, historical, isBulk, epoch).start(start).limit(limit)

    logger.trace(request.toString)

    client.execute(request).map(HybridAddresses.fromEither)
  }

  def generateQueryAddressRequest(tokens: Map[String, String],
                                  filters: String,
                                  range: String,
                                  lat: String,
                                  lon: String,
                                  startDate: String,
                                  endDate: String,
                                  queryParamsConfig: Option[QueryParamsConfig] = None,
                                  historical: Boolean = true,
                                  isBulk: Boolean = false,
                                  epoch: String): SearchDefinition = {

    val queryParams = queryParamsConfig.getOrElse(conf.config.elasticSearch.queryParams)
    val defaultFuzziness = "1"

    // this part of query should be blank unless there is an end number or end suffix
    val saoEndNumber = tokens.getOrElse(Tokens.saoEndNumber, "")
    val saoEndSuffix = tokens.getOrElse(Tokens.saoEndSuffix, "")
    val skipSao = saoEndNumber == "" && saoEndSuffix == ""

    val dateQuery = makeDateQuery(startDate, endDate)

    val saoQuery = if (skipSao) Seq.empty else
      Seq(
        tokens.get(Tokens.saoStartNumber).map(token =>
          constantScoreQuery(matchQuery(
            field = "lpi.saoStartNumber",
            value = token
          )).boost(queryParams.subBuildingRange.lpiSaoStartNumberBoost)),
        tokens.get(Tokens.saoStartSuffix).map(token =>
          constantScoreQuery(matchQuery(
            field = "lpi.saoStartSuffix",
            value = token
          )).boost(queryParams.subBuildingRange.lpiSaoStartSuffixBoost)),
        tokens.get(Tokens.saoEndNumber).map(token =>
          constantScoreQuery(matchQuery(
            field = "lpi.saoEndNumber",
            value = token
          )).boost(queryParams.subBuildingRange.lpiSaoEndNumberBoost)),
        tokens.get(Tokens.saoEndSuffix).map(token =>
          constantScoreQuery(matchQuery(
            field = "lpi.saoEndSuffix",
            value = token
          )).boost(queryParams.subBuildingRange.lpiSaoEndSuffixBoost)),
        tokens.get(Tokens.saoEndNumber).map(token =>
          constantScoreQuery(matchQuery(
            field = "lpi.saoStartNumber",
            value = token
          )).boost(queryParams.subBuildingRange.lpiSaoStartEndBoost))
      ).flatten

    val subBuildingNameQuery = Seq(Seq(
      tokens.get(Tokens.subBuildingName).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.subBuildingName",
          value = token
        )).boost(queryParams.subBuildingName.pafSubBuildingNameBoost)),
      tokens.get(Tokens.subBuildingName).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.saoText",
          value = token
        ).minimumShouldMatch(queryParams.paoSaoMinimumShouldMatch))
          .boost(queryParams.subBuildingName.lpiSaoTextBoost))
    ).flatten,
      Seq(Seq(
        tokens.get(Tokens.saoStartNumber).map(token =>
          constantScoreQuery(matchQuery(
            field = "lpi.saoStartNumber",
            value = token
          )).boost(queryParams.subBuildingName.lpiSaoStartNumberBoost)),
        tokens.get(Tokens.saoStartSuffix).map(token =>
          constantScoreQuery(matchQuery(
            field = "lpi.saoStartSuffix",
            value = token
          )).boost(queryParams.subBuildingName.lpiSaoStartSuffixBoost))
      ).flatten
      ).filter(_.nonEmpty).map(queries => dismax(queries: Iterable[QueryDefinition])
        .tieBreaker(queryParams.includingDisMaxTieBreaker))
    ).flatten

    // this part of query should be blank unless there is an end number or end suffix
    val paoEndNumber = tokens.getOrElse(Tokens.paoEndNumber, "")
    val paoEndSuffix = tokens.getOrElse(Tokens.paoEndSuffix, "")
    val skipPao = paoEndNumber == "" && paoEndSuffix == ""

    val paoQuery: Seq[ConstantScoreDefinition] = if (!skipPao) Seq(
      tokens.get(Tokens.paoStartNumber).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.paoStartNumber",
          value = token
        )).boost(queryParams.buildingRange.lpiPaoStartNumberBoost)),
      tokens.get(Tokens.paoStartSuffix).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.paoStartSuffix",
          value = token
        )).boost(queryParams.buildingRange.lpiPaoStartSuffixBoost)),
      tokens.get(Tokens.paoEndNumber).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.paoEndNumber",
          value = token
        )).boost(queryParams.buildingRange.lpiPaoEndNumberBoost)),
      tokens.get(Tokens.paoEndSuffix).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.paoEndSuffix",
          value = token
        )).boost(queryParams.buildingRange.lpiPaoEndSuffixBoost)),
      tokens.get(Tokens.paoEndNumber).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.paoStartNumber",
          value = token
        )).boost(queryParams.buildingRange.lpiPaoStartEndBoost)),
      tokens.get(Tokens.paoEndNumber).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.buildingNumber",
          value = token
        )).boost(queryParams.buildingRange.lpiPaoStartEndBoost)),
      tokens.get(Tokens.paoStartNumber).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.buildingNumber",
          value = token
        )).boost(queryParams.buildingRange.lpiPaoStartEndBoost))
    ).flatten else Seq.empty

    val paoBuildingNameMust = for {
      paoStartNumber <- tokens.get(Tokens.paoStartNumber)
      paoStartSuffix <- tokens.get(Tokens.paoStartSuffix)
    } yield constantScoreQuery(must(Seq(
      matchQuery(
        field = "lpi.paoStartNumber",
        value = paoStartNumber
      ),
      matchQuery(
        field = "lpi.paoStartSuffix",
        value = paoStartSuffix
      )
    ))).boost(queryParams.buildingName.lpiPaoStartSuffixBoost)

    val buildingNameQuery: Seq[QueryDefinition] = tokens.get(Tokens.buildingName).map(token => Seq(
      constantScoreQuery(matchQuery(
        field = "paf.buildingName",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.buildingName.pafBuildingNameBoost),
      constantScoreQuery(matchQuery(
        field = "lpi.paoText",
        value = token
      ).fuzziness(defaultFuzziness).minimumShouldMatch(queryParams.paoSaoMinimumShouldMatch)).boost(queryParams.buildingName.lpiPaoTextBoost)
    )).toList.flatten ++ paoBuildingNameMust

    val buildingNumberQuery = if (skipPao) {
      tokens.get(Tokens.paoStartNumber).map(token => Seq(
        constantScoreQuery(matchQuery(
          field = "paf.buildingNumber",
          value = token
        )).boost(queryParams.buildingNumber.pafBuildingNumberBoost),
        constantScoreQuery(matchQuery(
          field = "lpi.paoStartNumber",
          value = token
        )).boost(queryParams.buildingNumber.lpiPaoStartNumberBoost),
        constantScoreQuery(matchQuery(
          field = "lpi.paoEndNumber",
          value = token
        )).boost(queryParams.buildingNumber.lpiPaoEndNumberBoost)
      )).toList.flatten
    } else Seq.empty

    val streetNameQuery = tokens.get(Tokens.streetName).map(token => Seq(
      constantScoreQuery(matchQuery(
        field = "paf.thoroughfare",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.streetName.pafThoroughfareBoost),
      constantScoreQuery(matchQuery(
        field = "paf.welshThoroughfare",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.streetName.pafWelshThoroughfareBoost),
      constantScoreQuery(matchQuery(
        field = "paf.dependentThoroughfare",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.streetName.pafDependentThoroughfareBoost),
      constantScoreQuery(matchQuery(
        field = "paf.welshDependentThoroughfare",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.streetName.pafWelshDependentThoroughfareBoost),
      constantScoreQuery(matchQuery(
        field = "lpi.streetDescriptor",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.streetName.lpiStreetDescriptorBoost)
    )).toList.flatten

    val townNameQuery = tokens.get(Tokens.townName).map(token => Seq(
      constantScoreQuery(matchQuery(
        field = "paf.postTown",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.townName.pafPostTownBoost),
      constantScoreQuery(matchQuery(
        field = "paf.welshPostTown",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.townName.pafWelshPostTownBoost),
      constantScoreQuery(matchQuery(
        field = "lpi.townName",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.townName.lpiTownNameBoost),
      constantScoreQuery(matchQuery(
        field = "paf.dependentLocality",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.townName.pafDependentLocalityBoost),
      constantScoreQuery(matchQuery(
        field = "paf.welshDependentLocality",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.townName.pafWelshDependentLocalityBoost),
      constantScoreQuery(matchQuery(
        field = "lpi.locality",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.townName.lpiLocalityBoost),
      constantScoreQuery(matchQuery(
        field = "paf.doubleDependentLocality",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.townName.pafDoubleDependentLocalityBoost),
      constantScoreQuery(matchQuery(
        field = "paf.welshDoubleDependentLocality",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.townName.pafWelshDoubleDependentLocalityBoost)
    )).toList.flatten

    val postcodeInOutMust = for {
      postcodeOut <- tokens.get(Tokens.postcodeOut)
      postcodeIn <- tokens.get(Tokens.postcodeIn)
    } yield constantScoreQuery(must(Seq(
      matchQuery(
        field = "postcodeOut",
        value = postcodeOut
      ).fuzziness(defaultFuzziness),
      matchQuery(
        field = "postcodeIn",
        value = postcodeIn
      ).fuzziness("2")
    ))).boost(queryParams.postcode.postcodeInOutBoost)

    val postcodeQuery: Seq[ConstantScoreDefinition] = tokens.get(Tokens.postcode).map(token => Seq(
      constantScoreQuery(matchQuery(
        field = "paf.postcode",
        value = token
      )).boost(queryParams.postcode.pafPostcodeBoost),
      constantScoreQuery(matchQuery(
        field = "lpi.postcodeLocator",
        value = token
      )).boost(queryParams.postcode.lpiPostcodeLocatorBoost),
    )).toList.flatten ++ postcodeInOutMust

    val organisationNameQuery = tokens.get(Tokens.organisationName).map(token => Seq(
      constantScoreQuery(matchQuery(
        field = "paf.organisationName",
        value = token
      ).minimumShouldMatch(queryParams.organisationDepartmentMinimumShouldMatch)).boost(queryParams.organisationName.pafOrganisationNameBoost),
      constantScoreQuery(matchQuery(
        field = "lpi.organisation",
        value = token
      ).minimumShouldMatch(queryParams.organisationDepartmentMinimumShouldMatch)).boost(queryParams.organisationName.lpiOrganisationBoost),
      constantScoreQuery(matchQuery(
        field = "lpi.paoText",
        value = token
      ).minimumShouldMatch(queryParams.organisationDepartmentMinimumShouldMatch)).boost(queryParams.organisationName.lpiPaoTextBoost),
      constantScoreQuery(matchQuery(
        field = "lpi.legalName",
        value = token
      ).minimumShouldMatch(queryParams.organisationDepartmentMinimumShouldMatch)).boost(queryParams.organisationName.lpiLegalNameBoost),
      constantScoreQuery(matchQuery(
        field = "lpi.saoText",
        value = token
      ).minimumShouldMatch(queryParams.organisationDepartmentMinimumShouldMatch)).boost(queryParams.organisationName.lpiSaoTextBoost)
    )).toList.flatten

    val departmentNameQuery = tokens.get(Tokens.departmentName).map(token => Seq(
      constantScoreQuery(matchQuery(
        field = "paf.departmentName",
        value = token
      ).minimumShouldMatch(queryParams.organisationDepartmentMinimumShouldMatch)).boost(queryParams.departmentName.pafDepartmentNameBoost),
      constantScoreQuery(matchQuery(
        field = "lpi.legalName",
        value = token
      ).minimumShouldMatch(queryParams.organisationDepartmentMinimumShouldMatch)).boost(queryParams.departmentName.lpiLegalNameBoost)
    )).toList.flatten

    val localityQuery = tokens.get(Tokens.locality).map(token => Seq(
      constantScoreQuery(matchQuery(
        field = "paf.postTown",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.locality.pafPostTownBoost),
      constantScoreQuery(matchQuery(
        field = "paf.welshPostTown",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.locality.pafWelshPostTownBoost),
      constantScoreQuery(matchQuery(
        field = "lpi.townName",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.locality.lpiTownNameBoost),
      constantScoreQuery(matchQuery(
        field = "paf.dependentLocality",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.locality.pafDependentLocalityBoost),
      constantScoreQuery(matchQuery(
        field = "paf.welshDependentLocality",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.locality.pafWelshDependentLocalityBoost),
      constantScoreQuery(matchQuery(
        field = "lpi.locality",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.locality.lpiLocalityBoost),
      constantScoreQuery(matchQuery(
        field = "paf.doubleDependentLocality",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.locality.pafDoubleDependentLocalityBoost),
      constantScoreQuery(matchQuery(
        field = "paf.welshDoubleDependentLocality",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.locality.pafWelshDoubleDependentLocalityBoost)
    )).toList.flatten

    val normalizedInput = Tokens.concatenate(tokens)

    val filterType = getFilterType(filters)
    val filterValuePrefix = getFilterValuePrefix(filters)
    val filterValueTerm = getFilterValueTerm(filters)

    // appended to other queries if found
    // was once geoDistanceQueryInner
    val radiusQuery = if (!range.isEmpty)
      Seq(geoDistanceQuery("lpi.location").point(lat.toDouble, lon.toDouble).distance(s"${range}km"))
    else Seq.empty

    val prefixWithGeo = Seq(prefixQuery("classificationCode", filterValuePrefix)) ++ radiusQuery
    val termWithGeo = Seq(termsQuery("classificationCode", filterValueTerm)) ++ radiusQuery

    val fallbackQueryStart: BoolQueryDefinition = bool(
      Seq(dismax(
        matchQuery("lpi.nagAll", normalizedInput)
          .minimumShouldMatch(queryParams.fallback.fallbackMinimumShouldMatch)
          .analyzer(CustomAnalyzer("welsh_split_synonyms_analyzer"))
          .boost(queryParams.fallback.fallbackLpiBoost),
        matchQuery("paf.pafAll", normalizedInput)
          .minimumShouldMatch(queryParams.fallback.fallbackMinimumShouldMatch)
          .analyzer(CustomAnalyzer("welsh_split_synonyms_analyzer"))
          .boost(queryParams.fallback.fallbackPafBoost))
        .tieBreaker(0.0)),
      Seq(dismax(
        matchQuery("lpi.nagAll.bigram", normalizedInput)
          .fuzziness(queryParams.fallback.bigramFuzziness)
          .boost(queryParams.fallback.fallbackLpiBigramBoost),
        matchQuery("paf.pafAll.bigram", normalizedInput)
          .fuzziness(queryParams.fallback.bigramFuzziness)
          .boost(queryParams.fallback.fallbackPafBigramBoost))
        .tieBreaker(0.0)),
      Seq.empty).boost(queryParams.fallback.fallbackQueryBoost)

    val fallbackQuery = filters match {
      case "" => fallbackQueryStart.filter(radiusQuery ++ Seq(dateQuery).flatten)
      case _ => filterType match {
        case "prefix" => fallbackQueryStart.filter(prefixWithGeo ++ Seq(dateQuery).flatten)
        case _ => fallbackQueryStart.filter(termWithGeo ++ Seq(dateQuery).flatten)
      }
    }

    val bestOfTheLotQueries = Seq(
      buildingNumberQuery,
      buildingNameQuery,
      subBuildingNameQuery,
      streetNameQuery,
      postcodeQuery,
      organisationNameQuery,
      departmentNameQuery
      // `dismax` dsl does not exist, `: _*` means that we provide a list (`queries`) as arguments (args) for the function
    ).filter(_.nonEmpty).map(queries => dismax(queries: Iterable[QueryDefinition]).tieBreaker(queryParams.excludingDisMaxTieBreaker))

    val townLocalityQueries = Seq(
      townNameQuery,
      localityQuery
      // `dismax` dsl does not exist, `: _*` means that we provide a list (`queries`) as arguments (args) for the function
    ).filter(_.nonEmpty).map(queries => dismax(queries: Iterable[QueryDefinition]).tieBreaker(queryParams.excludingDisMaxTieBreaker))

    val everythingMattersQueries = Seq(
      townLocalityQueries,
      paoQuery,
      saoQuery
      // `dismax` dsl does not exist, `: _*` means that we provide a list (`queries`) as arguments (args) for the function
    ).filter(_.nonEmpty).map(queries => dismax(queries: Iterable[QueryDefinition]).tieBreaker(queryParams.includingDisMaxTieBreaker))

    val shouldQuery = bestOfTheLotQueries ++ everythingMattersQueries

    val queryFilter = if (filters.isEmpty)
      radiusQuery
    else if (filterType == "prefix")
      prefixWithGeo
    else
      termWithGeo

    val query = shouldQuery match {
      case Seq() => fallbackQuery
      case _ => dismax(
        should(shouldQuery.asInstanceOf[Iterable[QueryDefinition]])
          .minimumShouldMatch(queryParams.mainMinimumShouldMatch)
          .filter(queryFilter ++ dateQuery)
        , fallbackQuery)
        .tieBreaker(queryParams.topDisMaxTieBreaker)
    }

    val source = if (historical) {
      if (isBulk) hybridIndexHistoricalBulk else hybridIndexHistoricalAddress
    } else {
      if (isBulk) hybridIndexBulk else hybridIndexAddress
    }

    search(source + getEpochParam(epoch) + hybridMapping).query(query)
      .sortBy(FieldSortDefinition("_score").order(SortOrder.DESC), FieldSortDefinition("uprn").order(SortOrder.ASC))
      .trackScores(true)
      .searchType(SearchType.DfsQueryThenFetch)
  }

  def queryBulk(requestsData: Stream[BulkAddressRequestData],
                limit: Int,
                startDate: String = "",
                endDate: String = "",
                queryParamsConfig: Option[QueryParamsConfig] = None,
                historical: Boolean = true,
                matchThreshold: Float,
                includeFullAddress: Boolean = false,
                epoch: String = ""): Future[Stream[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]] = {
    val minimumSample = conf.config.bulk.minimumSample
    val addressRequests = requestsData.map { requestData =>
      val bulkAddressRequest: Future[Seq[AddressBulkResponseAddress]] =
        queryAddresses(requestData.tokens, 0, max(limit * 2, minimumSample), "", "", "50.71", "-3.51", startDate, endDate, queryParamsConfig, historical, isBulk = true, epoch).map { case HybridAddresses(hybridAddresses, _, _) =>

          // If we didn't find any results for an input, we still need to return
          // something that will indicate an empty result
          val tokens = requestData.tokens
          val emptyBulk = BulkAddress.empty(requestData)
          val emptyScored = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(emptyBulk.hybridAddress, verbose = true)), tokens, 1D)
          val emptyBulkAddress = AddressBulkResponseAddress.fromBulkAddress(emptyBulk, emptyScored.head, includeFullAddress = false)
          if (hybridAddresses.isEmpty) Seq(emptyBulkAddress)
          else {
            val bulkAddresses = hybridAddresses.map { hybridAddress =>
              BulkAddress.fromHybridAddress(hybridAddress, requestData)
            }

            val addressResponseAddresses = hybridAddresses.map { hybridAddress =>
              AddressResponseAddress.fromHybridAddress(hybridAddress, verbose = true)
            }

            //  calculate the elastic denominator value which will be used when scoring each address
            val elasticDenominator = Try(ConfidenceScoreHelper.calculateElasticDenominator(addressResponseAddresses.map(_.underlyingScore))).getOrElse(1D)
            // add the Hopper and hybrid scores to the address
            // val matchThreshold = 5
            val threshold = Try((matchThreshold / 100).toDouble).getOrElse(0.05D)
            val scoredAddresses = HopperScoreHelper.getScoresForAddresses(addressResponseAddresses, tokens, elasticDenominator)
            val addressBulkResponseAddresses = (bulkAddresses zip scoredAddresses).map { case (b, s) =>
              AddressBulkResponseAddress.fromBulkAddress(b, s, includeFullAddress)
            }
            val thresholdedAddresses = addressBulkResponseAddresses.filter(_.confidenceScore > threshold).sortBy(_.confidenceScore)(Ordering[Double].reverse).take(limit)

            if (thresholdedAddresses.isEmpty) Seq(emptyBulkAddress) else thresholdedAddresses
          }
        }

      // Successful requests are stored in the `Right`
      // Failed requests will be stored in the `Left`
      bulkAddressRequest.map(Right(_)).recover {
        case exception: Exception =>
          logger.info(s"#bulk query: rejected request to ES (this might be an indicator of low resource) : ${exception.getMessage}")
          Left(requestData.copy(lastFailExceptionMessage = exception.getMessage))
      }
    }

    Future.sequence(addressRequests)
  }
}
