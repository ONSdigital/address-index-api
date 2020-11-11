package uk.gov.ons.addressIndex.server.modules

import com.sksamuel.elastic4s.ElasticDsl.{functionScoreQuery, geoDistanceQuery, _}
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.requests.script.Script
import com.sksamuel.elastic4s.requests.searches.aggs.TermsOrder
import com.sksamuel.elastic4s.requests.searches.queries.term.TermsQuery
import com.sksamuel.elastic4s.requests.searches.queries.{BoolQuery, ConstantScore, Query}
import com.sksamuel.elastic4s.requests.searches.sort.{FieldSort, GeoDistanceSort, SortOrder}
import com.sksamuel.elastic4s.requests.searches.{GeoPoint, HighlightField, HighlightOptions, SearchBodyBuilderFn, SearchRequest, SearchType}
import javax.inject.{Inject, Singleton}
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
class AddressIndexRepository @Inject()(conf: ConfigModule,
                                       elasticClientProvider: ElasticClientProvider
                                      )(implicit ec: ExecutionContext) extends ElasticsearchRepository {

  private val esConf = conf.config.elasticSearch

  // cluster number 1 = bulk cluster
  // cluster number 2 (or blank) = general cluster
  // full match cluster on gcp and bulk cluster on prem both use esConf.urifullmatch
  private val uprnFull: Boolean = esConf.clusterPolicies.uprn == "1"
  private val partialFull: Boolean = esConf.clusterPolicies.partial == "1"
  private val postcodeFull: Boolean = esConf.clusterPolicies.postcode == "1"
  private val addressFull: Boolean = esConf.clusterPolicies.address == "1"
  private val bulkFull: Boolean = esConf.clusterPolicies.bulk == "1"
  private val randomFull: Boolean = esConf.clusterPolicies.random == "1"

  private val hybridIndexUprn = esConf.indexes.hybridIndex
  private val hybridIndexHistoricalUprn = esConf.indexes.hybridIndexHistorical

  private val hybridIndexPartial = esConf.indexes.hybridIndex
  private val hybridIndexHistoricalPartial = esConf.indexes.hybridIndexHistorical
  private val hybridIndexSkinnyPartial = esConf.indexes.hybridIndexSkinny
  private val hybridIndexHistoricalSkinnyPartial = esConf.indexes.hybridIndexHistoricalSkinny

  private val hybridIndexPostcode = esConf.indexes.hybridIndex
  private val hybridIndexHistoricalPostcode = esConf.indexes.hybridIndexHistorical
  private val hybridIndexSkinnyPostcode = esConf.indexes.hybridIndexSkinny
  private val hybridIndexHistoricalSkinnyPostcode = esConf.indexes.hybridIndexHistoricalSkinny

  private val hybridIndexAddress = esConf.indexes.hybridIndex
  private val hybridIndexHistoricalAddress = esConf.indexes.hybridIndexHistorical

  private val hybridIndexBulk = esConf.indexes.hybridIndex
  private val hybridIndexHistoricalBulk = esConf.indexes.hybridIndexHistorical

  private val hybridIndexSkinnyRandom = esConf.indexes.hybridIndexSkinny
  private val hybridIndexHistoricalSkinnyRandom = esConf.indexes.hybridIndexHistoricalSkinny
  private val hybridIndexRandom = esConf.indexes.hybridIndex
  private val hybridIndexHistoricalRandom = esConf.indexes.hybridIndexHistorical

  private val auxiliaryIndex = esConf.indexes.auxiliaryIndex

  private val gcp : Boolean = Try(esConf.gcp.toBoolean).getOrElse(false)

  val client: ElasticClient = elasticClientProvider.client
// clientFullmatch is for GCP deployments - used for fullmatch as it has a lower hardware spec
  val clientFullmatch: ElasticClient = elasticClientProvider.clientFullmatch
  lazy val logger: GenericLogger = GenericLogger("AddressIndexRepository")

  def queryHealth(): Future[String] = client.execute(clusterHealth()).map(_.toString)

  private def makeUprnQuery(args: UPRNArgs): SearchRequest = {
    val query = termQuery("uprn", args.uprn)

    val source = (if (args.historical) hybridIndexHistoricalUprn else hybridIndexUprn) + args.epochParam

    val searchIndicies = if (args.includeAuxiliarySearch) Seq(source, auxiliaryIndex) else Seq(source)

    search(searchIndicies).query(query)
  }

  /**
    * Generates request to get address from partial string (e.g typeahead)
    * Public for tests
    *
    * old param input      partial string
    * old param filters    classification filter
    * old param startDate  start date
    * old param endDate    end date
    * old param historical historical flag
    * old param fallback   flag to indicate if fallback query is required
    * old param verbose    flag to indicate that skinny index should be used when false
    *
    * @param args     partial query arguments
    * @param fallback flag indicating whether to generate a slower fallback query
    * @return Search definition containing query to the ES
    */
  def makePartialSearch(args: PartialArgs, fallback: Boolean): SearchRequest = {
    if (fallback) {
      logger.warn("best fields fallback query invoked for input string " + args.input)
    }

    val slopVal = 25

    val eboost = args.eboost
    val nboost = args.nboost
    val sboost = args.sboost
    val wboost = args.wboost

    val niFactor = args.fromsource match {
      case "niboost" => "^" + esConf.queryParams.nisra.partialNiBoostBoost
      case "ewboost" => "^" + esConf.queryParams.nisra.partialEwBoostBoost
      case _ => "^" + esConf.queryParams.nisra.partialAllBoost
    }

    val fieldsToSearch =  Seq("lpi.mixedNag.partial", "lpi.mixedWelshNag.partial", "paf.mixedPaf.partial", "paf.mixedWelshPaf.partial", "nisra.mixedNisra.partial" + niFactor)

    val queryBase = multiMatchQuery(args.input).fields(fieldsToSearch)
    val queryWithMatchType = if (fallback) queryBase.matchType("best_fields") else queryBase.matchType("phrase").slop(slopVal)

    val fromSourceQueryMust = args.fromsource match {
      case "ewonly" => Seq(termsQuery("fromSource","EW"))
      case "nionly" => Seq(termsQuery("fromSource","NI"))
      case _ => Seq.empty
    }

    val eTerms = termsQuery("countryCode","E")
    val nTerms = termsQuery("countryCode","N")
    val sTerms = termsQuery("countryCode","S")
    val wTerms = termsQuery("countryCode","W")

    // this is inelegant but we mustn't end up with a Seq(Any)
    val fromSourceQueryMustNot1 = Seq.empty[TermsQuery[String]]
    val fromSourceQueryMustNot2 = if (eboost == 0) fromSourceQueryMustNot1 :+ eTerms else fromSourceQueryMustNot1
    val fromSourceQueryMustNot3 = if (nboost == 0) fromSourceQueryMustNot2 :+ nTerms else fromSourceQueryMustNot2
    val fromSourceQueryMustNot4 = if (sboost == 0) fromSourceQueryMustNot3 :+ sTerms else fromSourceQueryMustNot3
    val fromSourceQueryMustNot5 = if (wboost == 0) fromSourceQueryMustNot4 :+ wTerms else fromSourceQueryMustNot4

    val boostExponent = 1.2

    val fromSourceQueryShould =
      if (eboost == 1 && sboost == 1 && nboost == 1 & wboost ==1) Seq.empty
      else Seq(
       termsQuery("countryCode","E").boost(eboost*boostExponent),
       termsQuery("countryCode","N").boost(nboost*boostExponent),
       termsQuery("countryCode","S").boost(sboost*boostExponent),
       termsQuery("countryCode","W").boost(wboost*boostExponent))

    // if there is only one number, give boost for pao or sao not both.
    // if there are two or more numbers, boost for either matching pao and first matching sao
    // the usual order is (sao pao) and a higher score is given for this match
    // helper function
    def numMatchQuery(field: String, value: Any) =
      matchQuery(field, value).prefixLength(1).maxExpansions(10).fuzzyTranspositions(false)

    val numberQuery: Seq[Query] = args.inputNumbers match {
      case first :: second :: _ if first == second => Seq(
        // allow the target pao and target sao to match once each
        // prevents (a a -> a b) from causing two matches
        numMatchQuery("lpi.paoStartNumber", first).boost(2D),
        numMatchQuery("lpi.saoStartNumber", first).boost(2D),
        numMatchQuery("nisra.paoStartNumber", first).boost(2D))
      case first :: second :: _ => Seq(
        // allow the input pao and input sao to match once each
        // because they cannot both match the same target, matches should not overlap (usually)
        dismax(numMatchQuery("lpi.paoStartNumber", first).boost(1D),
          numMatchQuery("lpi.saoStartNumber", first).boost(2D),
          numMatchQuery("nisra.saoStartNumber", first).boost(2D)),
        dismax(numMatchQuery("lpi.paoStartNumber", second).boost(2D),
          numMatchQuery("lpi.saoStartNumber", second).boost(1D),
          numMatchQuery("nisra.paoStartNumber", second).boost(2D)))
      case Seq(first) => Seq(
        // otherwise, match either
        dismax(numMatchQuery("lpi.paoStartNumber", first).boost(2D),
          numMatchQuery("lpi.saoStartNumber", first).boost(1D),
          numMatchQuery("nisra.paoStartNumber", first).boost(2D),
          numMatchQuery("nisra.saoStartNumber", first).boost(1D),
        ))
      case _ => Seq.empty
    }

    // Note that the mixedNagStart and similar fields should be changed to text from keyword for case insensitivity (this will also reduce the size of the index)
    val shortInput = args.input.take(12).toLowerCase.split(" ").map(_.capitalize).mkString(" ")

    val startQuery = Seq(
      prefixQuery("lpi.mixedNagStart",shortInput).boost(1),
      prefixQuery("lpi.mixedWelshNagStart",shortInput).boost(1),
      prefixQuery("paf.mixedPafStart",shortInput).boost(0.5),
      prefixQuery("paf.mixedWelshPafStart",shortInput).boost(0.5),
      prefixQuery("nisra.mixedNisraStart",shortInput).boost(2))

    val query = must(queryWithMatchType).filter(args.queryFilter ++ fromSourceQueryMust)
      .not(fromSourceQueryMustNot5)
      .should(numberQuery ++ fromSourceQueryShould ++ startQuery)

    val source = if (args.historical) {
      if (args.verbose) hybridIndexHistoricalPartial else hybridIndexHistoricalSkinnyPartial
    } else {
      if (args.verbose) hybridIndexPartial else hybridIndexSkinnyPartial
    }

      val hFields = if (args.highlight == "off") Seq() else
        Seq(HighlightField("lpi.mixedNag.partial"),
      HighlightField("lpi.mixedWelshNag.partial"),
      HighlightField("paf.mixedPaf.partial"),
      HighlightField("paf.mixedWelshPaf.partial"),
      HighlightField("nisra.mixedNisra.partial"))

    val scriptText: String = "Math.round(_score/1.8)"

    val partialScript: Script = new Script(script = scriptText)
    val hOpts = new HighlightOptions(numOfFragments=Some(0))

    search(source + args.epochParam)
      .query(
          functionScoreQuery(query).functions(
          scriptScore(partialScript))
            .boostMode("replace").minScore(1)
      )
      .highlighting(hOpts,hFields)
      .sortBy(
        FieldSort("_score").order(SortOrder.DESC),
        FieldSort("postcodeStreetTown").asc(),
        FieldSort("lpi.paoStartNumber").asc(),
        FieldSort("lpi.paoStartSuffix.keyword").asc(),
        FieldSort("lpi.secondarySort").asc(),
        FieldSort("nisra.paoStartNumber").asc(),
        FieldSort("nisra.secondarySort").asc(),
        FieldSort("uprn").asc())
      .start(args.start)
      .limit(args.limit)
  }

  def confidenceSort(score: Float): Int =
    {
      (math.round(score/4)*10).min(100)
    }

  private def makePostcodeQuery(args: PostcodeArgs): SearchRequest = {
    val postcodeFormatted: String = if (!args.postcode.contains(" ")) {
      val (postcodeStart, postcodeEnd) = args.postcode.splitAt(args.postcode.length() - 3)
      (postcodeStart + " " + postcodeEnd).toUpperCase
    } else args.postcode.toUpperCase

    val query = must(termQuery("postcode", postcodeFormatted)).filter(args.queryFilter)

    val source = (if (args.historical) {
      if (args.verbose) hybridIndexHistoricalPostcode else hybridIndexHistoricalSkinnyPostcode
    } else {
      if (args.verbose) hybridIndexPostcode else hybridIndexSkinnyPostcode
    }) + args.epochParam

    val searchIndicies = if (args.includeAuxiliarySearch) Seq(source, auxiliaryIndex) else Seq(source)

    val searchBase = search(searchIndicies)

    val sortFields: Seq[FieldSort] =
      if (args.includeAuxiliarySearch) Seq(FieldSort("uprn").asc())
      else Seq(FieldSort("lpi.streetDescriptor.keyword").asc(),
      FieldSort("lpi.paoStartNumber").asc(),
      FieldSort("lpi.paoStartSuffix.keyword").asc(),
      FieldSort("lpi.secondarySort").asc(),
      FieldSort("nisra.thoroughfare.keyword").asc(),
      FieldSort("nisra.paoStartNumber").asc(),
      FieldSort("nisra.secondarySort").asc(),
      FieldSort("uprn").asc())

    if (args.groupfullpostcodes.equals("combo"))
    searchBase.query(query)
      .aggs(termsAgg("uniquepostcodes", "postcodeStreetTown")
        .size(100)
        .order(TermsOrder("_key", asc = true))
        .subaggs(termsAgg("uprns", "uprn")
          .size(1),
          termsAgg("paftowns", "postTown")
            .size(1)))
      .start(0)
      .limit(1)
    .sortBy(sortFields)
    .start(args.start)
    .limit(args.limit)
    else
      searchBase.query(query)
        .start(0)
        .limit(1)
        .sortBy(sortFields)
        .start(args.start)
        .limit(args.limit)
}

  private def makeBucketQuery(args: BucketArgs): SearchRequest = {

    val query = must(wildcardQuery("postcodeStreetTown", args.bucketpattern)).filter(args.queryFilter)

    val source = if (args.historical) {
      if (args.verbose) hybridIndexHistoricalPostcode else hybridIndexHistoricalSkinnyPostcode
    } else {
      if (args.verbose) hybridIndexPostcode else hybridIndexSkinnyPostcode
    }

    val searchBase = search(source + args.epochParam)

    searchBase.query(query)
      .sortBy(FieldSort("lpi.streetDescriptor.keyword").asc(),
        FieldSort("lpi.paoStartNumber").asc(),
        FieldSort("lpi.paoStartSuffix.keyword").asc(),
        FieldSort("lpi.secondarySort").asc(),
        FieldSort("nisra.thoroughfare.keyword").asc(),
        FieldSort("nisra.paoStartNumber").asc(),
        FieldSort("nisra.secondarySort").asc(),
        FieldSort("uprn").asc())
      .start(args.start)
      .limit(args.limit)
  }

  private def makeGroupedPostcodeQuery(args: GroupedPostcodeArgs): SearchRequest = {

    val postcodeFormatted: String = args.postcode.toUpperCase

  //  val isNotJustOutcode: Boolean = (postcodeFormatted.trim.contains(" "))
    val isNotJustOutcode: Boolean = true
    val query = must(prefixQuery("postcode", postcodeFormatted)).filter(args.queryFilter)

    val source = if (args.historical) {
      if (args.verbose) hybridIndexHistoricalPostcode else hybridIndexHistoricalSkinnyPostcode
    } else {
      if (args.verbose) hybridIndexPostcode else hybridIndexSkinnyPostcode
    }

    val searchBase = search(source + args.epochParam)

    if (isNotJustOutcode) {
      searchBase.query(query)
        .aggs(termsAgg("uniquepostcodes", "postcodeStreetTown")
          .size(1000)
          .order(TermsOrder("_key", asc = true))
          .subaggs(termsAgg("uprns", "uprn")
            .size(1),
          termsAgg("paftowns", "postTown")
            .size(1)))
        .start(0)
        .limit(1)
    } else {
      searchBase.query(query)
        .aggs(termsAgg("uniquepostcodes", "postcodeStreetTown")
          .size(1000)
          .order(TermsOrder("_key", asc = true)))
        .start(0)
        .limit(1)
    }
  }

  private def makeRandomQuery(args: RandomArgs): SearchRequest = {
    val timestamp: Long = System.currentTimeMillis

    val fromSourceQuery = args.fromsource match {
      case "ewonly" => Seq(termsQuery("fromSource","EW"))
      case "nionly" => Seq(termsQuery("fromSource","NI"))
      case "niboost" => Seq(termsQuery("fromSource","NI"))
      case "ewboost" => Seq(termsQuery("fromSource","EW"))
      case _ => Seq.empty
    }

    val eboost = args.eboost
    val nboost = args.nboost
    val sboost = args.sboost
    val wboost = args.wboost

    val eTerms = termsQuery("countryCode","E")
    val nTerms = termsQuery("countryCode","N")
    val sTerms = termsQuery("countryCode","S")
    val wTerms = termsQuery("countryCode","W")

    // this is inelegant but we mustn't end up with a Seq(Any)
    val fromSourceQueryMustNot1 = Seq.empty[TermsQuery[String]]
    val fromSourceQueryMustNot2 = if (eboost == 0) fromSourceQueryMustNot1 :+ eTerms else fromSourceQueryMustNot1
    val fromSourceQueryMustNot3 = if (nboost == 0) fromSourceQueryMustNot2 :+ nTerms else fromSourceQueryMustNot2
    val fromSourceQueryMustNot4 = if (sboost == 0) fromSourceQueryMustNot3 :+ sTerms else fromSourceQueryMustNot3
    val fromSourceQueryMustNot5 = if (wboost == 0) fromSourceQueryMustNot4 :+ wTerms else fromSourceQueryMustNot4

    val query = functionScoreQuery()
      .functions(randomScore(timestamp.toInt))
      .query(boolQuery().filter(args.queryFilter ++ fromSourceQuery).not(fromSourceQueryMustNot5))
      .boostMode("replace")

    val source = if (args.historical) {
      if (args.verbose) hybridIndexHistoricalRandom else hybridIndexHistoricalSkinnyRandom
    } else {
      if (args.verbose) hybridIndexRandom else hybridIndexSkinnyRandom
    }

    search(source + args.epochParam)
      .query(query)
      .limit(args.limit)
  }

  private def makeAddressQuery(args: AddressArgs): SearchRequest = {
    val queryParams = args.queryParamsConfig.getOrElse(conf.config.elasticSearch.queryParams)
    val defaultFuzziness = "1"
    val isBlank = args.isBlank

    // this part of query should be blank unless there is an end number or end suffix
    val saoEndNumber = args.tokens.getOrElse(Tokens.saoEndNumber, "")
    val saoEndSuffix = args.tokens.getOrElse(Tokens.saoEndSuffix, "")
    val skipSao = saoEndNumber == "" && saoEndSuffix == ""

    val saoQuery = if (skipSao) Seq.empty else
      Seq(
        args.tokens.get(Tokens.saoStartNumber).map(token =>
          constantScoreQuery(matchQuery(
            field = "lpi.saoStartNumber",
            value = token
          )).boost(queryParams.subBuildingRange.lpiSaoStartNumberBoost)),
        args.tokens.get(Tokens.saoStartSuffix).map(token =>
          constantScoreQuery(matchQuery(
            field = "lpi.saoStartSuffix",
            value = token
          )).boost(queryParams.subBuildingRange.lpiSaoStartSuffixBoost)),
        args.tokens.get(Tokens.saoEndNumber).map(token =>
          constantScoreQuery(matchQuery(
            field = "lpi.saoEndNumber",
            value = token
          )).boost(queryParams.subBuildingRange.lpiSaoEndNumberBoost)),
        args.tokens.get(Tokens.saoEndSuffix).map(token =>
          constantScoreQuery(matchQuery(
            field = "lpi.saoEndSuffix",
            value = token
          )).boost(queryParams.subBuildingRange.lpiSaoEndSuffixBoost)),
        args.tokens.get(Tokens.saoEndNumber).map(token =>
          constantScoreQuery(matchQuery(
            field = "lpi.saoStartNumber",
            value = token
          )).boost(queryParams.subBuildingRange.lpiSaoStartEndBoost)),
        args.tokens.get(Tokens.saoStartSuffix).map(token =>
          constantScoreQuery(matchQuery(
            field = "token.saoStartSuffix",
            value = token
          )).boost(queryParams.subBuildingRange.lpiSaoStartSuffixBoost)),
        args.tokens.get(Tokens.saoEndSuffix).map(token =>
          constantScoreQuery(matchQuery(
            field = "lpi.saoEndSuffix",
            value = token
          )).boost(queryParams.subBuildingRange.lpiSaoEndSuffixBoost))
      ).flatten

    val subBuildingNameQuery: Seq[Query] = Seq(
      args.tokens.get(Tokens.subBuildingName).map(token => Seq(
        constantScoreQuery(matchQuery(
          field = "tokens.subBuildingName",
          value = token
        ).minimumShouldMatch(queryParams.paoSaoMinimumShouldMatch))
          .boost(queryParams.subBuildingName.lpiSaoStartNumberBoost),
        constantScoreQuery(matchQuery(
          field = "paf.subBuildingName",
          value = token
        ).minimumShouldMatch(queryParams.paoSaoMinimumShouldMatch))
          .boost(queryParams.subBuildingName.pafSubBuildingNameBoost),
        constantScoreQuery(matchQuery(
          field = "nisra.subBuildingName",
          value = token
        ).minimumShouldMatch(queryParams.paoSaoMinimumShouldMatch))
          .boost(queryParams.subBuildingName.pafSubBuildingNameBoost),
        constantScoreQuery(matchQuery(
          field = "lpi.saoText",
          value = token
        ).minimumShouldMatch(queryParams.paoSaoMinimumShouldMatch))
          .boost(queryParams.subBuildingName.lpiSaoTextBoost)
      )),
      Seq(
        args.tokens.get(Tokens.saoStartNumber).map(token => Seq(
          constantScoreQuery(matchQuery(
            field = "tokens.subBuildingName",
            value = token
          )).boost(queryParams.subBuildingName.lpiSaoStartNumberBoost),
          constantScoreQuery(matchQuery(
            field = "lpi.saoStartNumber",
            value = token
          )).boost(queryParams.subBuildingName.lpiSaoStartNumberBoost),
          constantScoreQuery(matchQuery(
            field = "paf.subBuildingName",
            value = token
          )).boost(queryParams.subBuildingName.lpiSaoStartNumberBoost),
          constantScoreQuery(matchQuery(
            field = "nisra.subBuildingName",
            value = token
          )).boost(queryParams.subBuildingName.lpiSaoStartNumberBoost),
          constantScoreQuery(matchQuery(
            field = "lpi.saoText",
            value = token
          )).boost(queryParams.subBuildingName.lpiSaoStartNumberBoost))),
        args.tokens.get(Tokens.saoStartSuffix).map(token => Seq(
          constantScoreQuery(matchQuery(
            field = "tokens.saoStartSuffix",
            value = token
          )).boost(queryParams.subBuildingName.lpiSaoStartSuffixBoost),
          constantScoreQuery(matchQuery(
            field = "lpi.saoStartSuffix",
            value = token
          )).boost(queryParams.subBuildingName.lpiSaoStartSuffixBoost),
          constantScoreQuery(matchQuery(
            field = "paf.subBuildingName",
            value = token
          )).boost(queryParams.subBuildingName.lpiSaoStartSuffixBoost),
          constantScoreQuery(matchQuery(
            field = "nisra.subBuildingName",
            value = token
          )).boost(queryParams.subBuildingName.lpiSaoStartSuffixBoost),
          constantScoreQuery(matchQuery(
            field = "lpi.saoText",
            value = token
          )).boost(queryParams.subBuildingName.lpiSaoStartSuffixBoost)))
      ).flatten.flatten match {
        case Seq() => None
        case s => Some(Seq(dismax(s: Iterable[Query]).tieBreaker(queryParams.includingDisMaxTieBreaker)))
      }
    ).flatten.flatten

    // this part of query should be used only when no subbuilding information has been parsed
    val saoStartNumber = args.tokens.getOrElse(Tokens.saoStartNumber, "")
    val saoStartSuffix = args.tokens.getOrElse(Tokens.saoStartSuffix, "")
    val subBuildingName = args.tokens.getOrElse(Tokens.subBuildingName, "")
    val crossPaoSao = saoStartNumber == "" && saoStartSuffix == "" && subBuildingName == ""

    val subBuildingPaoQuery: Seq[Query] = if (crossPaoSao) Seq.empty else Seq(
      args.tokens.get(Tokens.buildingName).map(token => Seq(
        constantScoreQuery(matchQuery(
          field = "tokens.buildingName",
          value = token
        ).minimumShouldMatch(queryParams.paoSaoMinimumShouldMatch))
          .boost(queryParams.subBuildingName.lpiSaoStartNumberBoost),
        constantScoreQuery(matchQuery(
          field = "paf.subBuildingName",
          value = token
        ).minimumShouldMatch(queryParams.paoSaoMinimumShouldMatch))
          .boost(queryParams.subBuildingName.pafSubBuildingNameBoost),
        constantScoreQuery(matchQuery(
          field = "nisra.subBuildingName",
          value = token
        ).minimumShouldMatch(queryParams.paoSaoMinimumShouldMatch))
          .boost(queryParams.subBuildingName.pafSubBuildingNameBoost),
        constantScoreQuery(matchQuery(
          field = "lpi.saoText",
          value = token
        ).minimumShouldMatch(queryParams.paoSaoMinimumShouldMatch))
          .boost(queryParams.subBuildingName.lpiSaoTextBoost)
      )),
      Seq(
        args.tokens.get(Tokens.paoStartNumber).map(token => Seq(
          constantScoreQuery(matchQuery(
            field = "tokens.paoStartNumber",
            value = token
          )).boost(queryParams.subBuildingName.lpiSaoStartNumberBoost),
          constantScoreQuery(matchQuery(
            field = "lpi.saoStartNumber",
            value = token
          )).boost(queryParams.subBuildingName.lpiSaoStartNumberBoost),
          constantScoreQuery(matchQuery(
            field = "paf.subBuildingName",
            value = token
          )).boost(queryParams.subBuildingName.lpiSaoStartNumberBoost),
          constantScoreQuery(matchQuery(
            field = "nisra.subBuildingName",
            value = token
          )).boost(queryParams.subBuildingName.lpiSaoStartNumberBoost),
          constantScoreQuery(matchQuery(
            field = "lpi.saoText",
            value = token
          )).boost(queryParams.subBuildingName.lpiSaoStartNumberBoost))),

        args.tokens.get(Tokens.paoStartSuffix).map(token => Seq(
          constantScoreQuery(matchQuery(
            field = "tokens.paoStartSuffix",
            value = token
          )).boost(queryParams.subBuildingName.lpiSaoStartSuffixBoost),
          constantScoreQuery(matchQuery(
            field = "lpi.saoStartSuffix",
            value = token
          )).boost(queryParams.subBuildingName.lpiSaoStartSuffixBoost),
          constantScoreQuery(matchQuery(
            field = "paf.subBuildingName",
            value = token
          )).boost(queryParams.subBuildingName.lpiSaoStartSuffixBoost),
          constantScoreQuery(matchQuery(
            field = "nisra.subBuildingName",
            value = token
          )).boost(queryParams.subBuildingName.lpiSaoStartSuffixBoost),
          constantScoreQuery(matchQuery(
            field = "lpi.saoText",
            value = token
          )).boost(queryParams.subBuildingName.lpiSaoStartSuffixBoost)))
      ).flatten.flatten match {
        case Seq() => None
        case s => Some(Seq(dismax(s: Iterable[Query]).tieBreaker(queryParams.includingDisMaxTieBreaker)))
      }
    ).flatten.flatten

    val extraPaoSaoQueries = Seq(
      subBuildingPaoQuery
      // `dismax` dsl does not exist, `: _*` means that we provide a list (`queries`) as arguments (args) for the function
    ).filter(_.nonEmpty).map(queries => dismax(queries: Iterable[Query])
      .tieBreaker(queryParams.excludingDisMaxTieBreaker)
      .boost(queryParams.subBuildingName.lpiSaoPaoStartSuffixBoost))

    // this part of query should be blank unless there is an end number or end suffix
    val paoEndNumber = args.tokens.getOrElse(Tokens.paoEndNumber, "")
    val paoEndSuffix = args.tokens.getOrElse(Tokens.paoEndSuffix, "")
    val skipPao = paoEndNumber == "" && paoEndSuffix == ""

    // TODO merge parts of this together
    val paoQuery: Seq[ConstantScore] = if (!skipPao) Seq(
      args.tokens.get(Tokens.paoEndNumber).map(token =>
        constantScoreQuery(matchQuery(
          field = "tokens.paoStartNumber",
          value = token
        )).boost(queryParams.buildingRange.lpiPaoStartEndBoost)),
      args.tokens.get(Tokens.paoStartNumber).map(token =>
        constantScoreQuery(matchQuery(
          field = "tokens.paoStartNumber",
          value = token
        )).boost(queryParams.buildingRange.lpiPaoStartEndBoost)),
      args.tokens.get(Tokens.paoStartNumber).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.paoStartNumber",
          value = token
        )).boost(queryParams.buildingRange.lpiPaoStartNumberBoost)),
      args.tokens.get(Tokens.paoStartSuffix).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.paoStartSuffix",
          value = token
        )).boost(queryParams.buildingRange.lpiPaoStartSuffixBoost)),
      args.tokens.get(Tokens.paoEndNumber).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.paoEndNumber",
          value = token
        )).boost(queryParams.buildingRange.lpiPaoEndNumberBoost)),
      args.tokens.get(Tokens.paoEndSuffix).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.paoEndSuffix",
          value = token
        )).boost(queryParams.buildingRange.lpiPaoEndSuffixBoost)),
      args.tokens.get(Tokens.paoEndNumber).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.paoStartNumber",
          value = token
        )).boost(queryParams.buildingRange.lpiPaoStartEndBoost)),
      args.tokens.get(Tokens.paoEndNumber).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.buildingNumber",
          value = token
        )).boost(queryParams.buildingRange.lpiPaoStartEndBoost)),
      args.tokens.get(Tokens.paoStartNumber).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.buildingNumber",
          value = token
        )).boost(queryParams.buildingRange.lpiPaoStartEndBoost)),
      args.tokens.get(Tokens.paoEndNumber).map(token =>
        constantScoreQuery(matchQuery(
          field = "nisra.paoStartNumber",
          value = token
        )).boost(queryParams.buildingRange.lpiPaoStartEndBoost)),
      args.tokens.get(Tokens.paoStartNumber).map(token =>
        constantScoreQuery(matchQuery(
          field = "nisra.paoStartNumber",
          value = token
        )).boost(queryParams.buildingRange.lpiPaoStartEndBoost))
    ).flatten else Seq.empty


    val paoStartNumber = args.tokens.getOrElse(Tokens.paoStartNumber, "")
    val paoStartSuffix = args.tokens.getOrElse(Tokens.paoStartSuffix, "")
    val skipbuildingMust = paoStartNumber == "" || paoStartSuffix == ""

    val paoBuildingNameMust = if (skipbuildingMust) Seq.empty else
      Seq(constantScoreQuery(must(Seq(
        matchQuery(
          field = "lpi.paoStartNumber",
          value = paoStartNumber
        ),
        matchQuery(
          field = "lpi.paoStartSuffix",
          value = paoStartSuffix
        )
      ))).boost(queryParams.buildingName.lpiPaoStartSuffixBoost))

    val buildingNameQuery: Seq[Query] = args.tokens.get(Tokens.buildingName).map(token => Seq(
      constantScoreQuery(matchQuery(
        field = "tokens.buildingName",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.buildingName.pafBuildingNameBoost),
      constantScoreQuery(matchQuery(
        field = "paf.buildingName",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.buildingName.pafBuildingNameBoost),
      constantScoreQuery(matchQuery(
        field = "nisra.buildingName",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.buildingName.pafBuildingNameBoost),
      constantScoreQuery(matchQuery(
        field = "lpi.paoText",
        value = token
      ).fuzziness(defaultFuzziness).minimumShouldMatch(queryParams.paoSaoMinimumShouldMatch))
        .boost(queryParams.buildingName.lpiPaoTextBoost)
    )).toList.flatten ++ paoBuildingNameMust

    val buildingNumberQuery = if (skipPao) {
      args.tokens.get(Tokens.paoStartNumber).map(token => Seq(
        constantScoreQuery(matchQuery(
          field = "tokens.paoStartNumber",
          value = token
        )).boost(queryParams.buildingNumber.lpiPaoStartNumberBoost),
        constantScoreQuery(matchQuery(
          field = "paf.buildingNumber",
          value = token
        )).boost(queryParams.buildingNumber.pafBuildingNumberBoost),
        constantScoreQuery(matchQuery(
          field = "nisra.paoStartNumber",
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

    val streetNameQuery = args.tokens.get(Tokens.streetName).map(token => Seq(
      constantScoreQuery(matchQuery(
        field = "tokens.streetName",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.streetName.lpiStreetDescriptorBoost),
      constantScoreQuery(matchQuery(
        field = "paf.thoroughfare",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.streetName.pafThoroughfareBoost),
      constantScoreQuery(matchQuery(
        field = "nisra.thoroughfare",
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
        field = "nisra.dependentThoroughfare",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.streetName.pafWelshDependentThoroughfareBoost),
      constantScoreQuery(matchQuery(
        field = "nisra.altThoroughfare",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.streetName.pafWelshDependentThoroughfareBoost),
      constantScoreQuery(matchQuery(
        field = "lpi.streetDescriptor",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.streetName.lpiStreetDescriptorBoost)
    )).toList.flatten

    val townNameQuery = args.tokens.get(Tokens.townName).map(token => Seq(
      constantScoreQuery(matchQuery(
        field = "tokens.townName",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.townName.lpiTownNameBoost),
      constantScoreQuery(matchQuery(
        field = "paf.postTown",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.townName.pafPostTownBoost),
      constantScoreQuery(matchQuery(
        field = "paf.welshPostTown",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.townName.pafWelshPostTownBoost),
      constantScoreQuery(matchQuery(
        field = "nisra.townName",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.townName.pafPostTownBoost),
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

    val postcodeOut = args.tokens.getOrElse(Tokens.postcodeOut, "")
    val postcodeIn = args.tokens.getOrElse(Tokens.postcodeIn, "")
    val skipPostCodeOutMust = postcodeOut == "" || postcodeIn == ""

    val postcodeInOutMust = if (skipPostCodeOutMust) Seq.empty else
      Seq(constantScoreQuery(must(Seq(
        matchQuery(
          field = "postcodeOut",
          value = postcodeOut
        ).fuzziness(defaultFuzziness),
        matchQuery(
          field = "postcodeIn",
          value = postcodeIn
        ).fuzziness("2")
      ))).boost(queryParams.postcode.postcodeInOutBoost))

    val postcodeQuery: Seq[ConstantScore] = args.tokens.get(Tokens.postcode).map(token => Seq(
      constantScoreQuery(matchQuery(
        field = "postcode",
        value = token
      )).boost(queryParams.postcode.lpiPostcodeLocatorBoost),
      constantScoreQuery(matchQuery(
        field = "paf.postcode",
        value = token
      )).boost(queryParams.postcode.pafPostcodeBoost),
      constantScoreQuery(matchQuery(
        field = "nisra.postcode",
        value = token
      )).boost(queryParams.postcode.pafPostcodeBoost),
      constantScoreQuery(matchQuery(
        field = "lpi.postcodeLocator",
        value = token
      )).boost(queryParams.postcode.lpiPostcodeLocatorBoost),
    )).toList.flatten ++ postcodeInOutMust

    val organisationNameQuery = args.tokens.get(Tokens.organisationName).map(token => Seq(
      constantScoreQuery(matchQuery(
        field = "tokens.organisationName",
        value = token
      ).minimumShouldMatch(queryParams.organisationDepartmentMinimumShouldMatch)).boost(queryParams.organisationName.lpiOrganisationBoost),
      constantScoreQuery(matchQuery(
        field = "paf.organisationName",
        value = token
      ).minimumShouldMatch(queryParams.organisationDepartmentMinimumShouldMatch)).boost(queryParams.organisationName.pafOrganisationNameBoost),
      constantScoreQuery(matchQuery(
        field = "nisra.organisationName",
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

    val departmentNameQuery = args.tokens.get(Tokens.departmentName).map(token => Seq(
      constantScoreQuery(matchQuery(
        field = "tokens.departmentName",
        value = token
      ).minimumShouldMatch(queryParams.organisationDepartmentMinimumShouldMatch)).boost(queryParams.departmentName.pafDepartmentNameBoost),
      constantScoreQuery(matchQuery(
        field = "paf.departmentName",
        value = token
      ).minimumShouldMatch(queryParams.organisationDepartmentMinimumShouldMatch)).boost(queryParams.departmentName.pafDepartmentNameBoost),
      constantScoreQuery(matchQuery(
        field = "lpi.legalName",
        value = token
      ).minimumShouldMatch(queryParams.organisationDepartmentMinimumShouldMatch)).boost(queryParams.departmentName.lpiLegalNameBoost)
    )).toList.flatten

    val localityQuery = args.tokens.get(Tokens.locality).map(token => Seq(
      constantScoreQuery(matchQuery(
        field = "tokens.locality",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.locality.lpiLocalityBoost),
      constantScoreQuery(matchQuery(
        field = "paf.postTown",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.locality.pafPostTownBoost),
      constantScoreQuery(matchQuery(
        field = "nisra.townName",
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
        field = "nisra.locality",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.locality.lpiLocalityBoost),
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

    val normalizedInput = Tokens.concatenate(args.tokens)

    // appended to other queries if found
    // was once geoDistanceQueryInner
    val radiusQuery = args.region match {
      case Some(Region(range, lat, lon)) =>
        Seq(bool(Seq(),
        Seq(geoDistanceQuery("lpi.location", lat, lon).distance(s"${range}km"),
          geoDistanceQuery("nisra.location", lat, lon).distance(s"${range}km")),Seq()))
      case None => Seq.empty
    }

    val fromSourceQuery1 = args.fromsource match {
      case "ewonly" => Seq(termsQuery("fromSource","EW"))
      case "nionly" => Seq(termsQuery("fromSource","NI"))
      case _ => Seq.empty
    }

    val fromSourceQuery2 = args.fromsource match {
      case "ewonly" => Seq(termsQuery("fromSource","EW"))
      case "nionly" => Seq(termsQuery("fromSource","NI"))
      case "niboost" => Seq(termsQuery("fromSource","NI"))
      case "ewboost" => Seq(termsQuery("fromSource","EW"))
      case _ => Seq.empty
    }

    val eboost = args.eboost
    val nboost = args.nboost
    val sboost = args.sboost
    val wboost = args.wboost

    val eTerms = termsQuery("countryCode","E")
    val nTerms = termsQuery("countryCode","N")
    val sTerms = termsQuery("countryCode","S")
    val wTerms = termsQuery("countryCode","W")

    // this is inelegant but we mustn't end up with a Seq(Any)
    val fromSourceQueryMustNot1 = Seq.empty[TermsQuery[String]]
    val fromSourceQueryMustNot2 = if (eboost == 0) fromSourceQueryMustNot1 :+ eTerms else fromSourceQueryMustNot1
    val fromSourceQueryMustNot3 = if (nboost == 0) fromSourceQueryMustNot2 :+ nTerms else fromSourceQueryMustNot2
    val fromSourceQueryMustNot4 = if (sboost == 0) fromSourceQueryMustNot3 :+ sTerms else fromSourceQueryMustNot3
    val fromSourceQueryMustNot5 = if (wboost == 0) fromSourceQueryMustNot4 :+ wTerms else fromSourceQueryMustNot4


    val fallbackQueryStart: BoolQuery = bool(
      Seq(dismax(
        matchQuery("tokens.addressAll", normalizedInput)
          .minimumShouldMatch(queryParams.fallback.fallbackMinimumShouldMatch)
          .analyzer("welsh_split_synonyms_analyzer")
          .boost(queryParams.fallback.fallbackAuxBoost),
        matchQuery("lpi.nagAll", normalizedInput)
          .minimumShouldMatch(queryParams.fallback.fallbackMinimumShouldMatch)
          .analyzer("welsh_split_synonyms_analyzer")
          .boost(queryParams.fallback.fallbackLpiBoost),
        matchQuery("nisra.nisraAll", normalizedInput)
          .minimumShouldMatch(queryParams.fallback.fallbackMinimumShouldMatch)
          .analyzer("welsh_split_synonyms_analyzer")
          .boost(queryParams.nisra.fullFallBackNiBoost),
        matchQuery("paf.pafAll", normalizedInput)
          .minimumShouldMatch(queryParams.fallback.fallbackMinimumShouldMatch)
          .analyzer("welsh_split_synonyms_analyzer")
          .boost(queryParams.fallback.fallbackPafBoost))
        .tieBreaker(0.0)),
      Seq(dismax(
        matchQuery("tokens.addressAll.bigram", normalizedInput)
          .fuzziness(queryParams.fallback.bigramFuzziness)
          .boost(queryParams.fallback.fallbackAuxBigramBoost),
        matchQuery("lpi.nagAll.bigram", normalizedInput)
          .fuzziness(queryParams.fallback.bigramFuzziness)
          .boost(queryParams.fallback.fallbackLpiBigramBoost),
        matchQuery("nisra.nisraAll.bigram", normalizedInput)
          .fuzziness(queryParams.fallback.bigramFuzziness)
          .boost(queryParams.nisra.fullFallBackBigramNiBoost),
        matchQuery("paf.pafAll.bigram", normalizedInput)
          .fuzziness(queryParams.fallback.bigramFuzziness)
          .boost(queryParams.fallback.fallbackPafBigramBoost))
        .tieBreaker(0.0)),
      Seq.empty).boost(queryParams.fallback.fallbackQueryBoost)

    val fallbackQueryFilter = args.queryFilter ++ radiusQuery ++ fromSourceQuery2

    val fallbackQuery = fallbackQueryStart.filter(fallbackQueryFilter).not(fromSourceQueryMustNot5)

    val blankQuery : BoolQuery = bool(
    Seq(matchAllQuery()),Seq(),Seq()).filter(fallbackQueryFilter).not(fromSourceQueryMustNot5)

    val bestOfTheLotQueries = Seq(

      subBuildingNameQuery,
      streetNameQuery,
      postcodeQuery
      // `dismax` dsl does not exist, `: _*` means that we provide a list (`queries`) as arguments (args) for the function
    ).filter(_.nonEmpty).map(queries => dismax(queries: Iterable[Query]).tieBreaker(queryParams.excludingDisMaxTieBreaker))

    val organisationDepartmentQueries = Seq(
      organisationNameQuery,
      departmentNameQuery
      // `dismax` dsl does not exist, `: _*` means that we provide a list (`queries`) as arguments (args) for the function
    ).filter(_.nonEmpty).map(queries => dismax(queries: Iterable[Query]).tieBreaker(queryParams.excludingDisMaxTieBreaker))

    val townLocalityQueries = Seq(
      townNameQuery,
      localityQuery
      // `dismax` dsl does not exist, `: _*` means that we provide a list (`queries`) as arguments (args) for the function
    ).filter(_.nonEmpty).map(queries => dismax(queries: Iterable[Query]).tieBreaker(queryParams.excludingDisMaxTieBreaker))

    val widerBuildingNameQueries = Seq(
      buildingNameQuery,
      buildingNumberQuery,
      extraPaoSaoQueries
      // `dismax` dsl does not exist, `: _*` means that we provide a list (`queries`) as arguments (args) for the function
    ).filter(_.nonEmpty).map(queries => dismax(queries: Iterable[Query]).tieBreaker(queryParams.excludingDisMaxTieBreaker))

    val everythingMattersQueries = Seq(
      widerBuildingNameQueries,
      organisationDepartmentQueries,
      townLocalityQueries,
      paoQuery,
      saoQuery
      // `dismax` dsl does not exist, `: _*` means that we provide a list (`queries`) as arguments (args) for the function
    ).filter(_.nonEmpty).map(queries => dismax(queries: Iterable[Query]).tieBreaker(queryParams.includingDisMaxTieBreaker))

    // add extra dismax after bestOfTheLot
    val shouldQuery = bestOfTheLotQueries ++ everythingMattersQueries

    val query = if (isBlank)
      blankQuery
     else {
       if (shouldQuery.isEmpty)
         fallbackQuery
       else {
         dismax(
           should(shouldQuery.asInstanceOf[Iterable[Query]])
             .minimumShouldMatch(queryParams.mainMinimumShouldMatch)
             .filter(args.queryFilter ++ radiusQuery ++ fromSourceQuery1)
             .not(fromSourceQueryMustNot5)
           , fallbackQuery)
           .tieBreaker(queryParams.topDisMaxTieBreaker)
       }
     }

    val source = (if (args.historical) {
      if (args.isBulk) hybridIndexHistoricalBulk else hybridIndexHistoricalAddress
    } else {
      if (args.isBulk) hybridIndexBulk else hybridIndexAddress
    }) + args.epochParam

    val radiusSort = args.region match {
      case Some(Region(_, lat, lon)) =>
        Seq(GeoDistanceSort(field = "lpi.location", points = Seq(GeoPoint(lat, lon))),
          GeoDistanceSort(field = "nisra.location", points = Seq(GeoPoint(lat, lon))))
      case None => Seq.empty
    }

    val searchIndicies = if (args.includeAuxiliarySearch) Seq(source, auxiliaryIndex) else Seq(source)

    if (isBlank) {
      search(searchIndicies)
        .query(query)
        .sortBy(
          radiusSort
        )
        .trackScores(true)
        .searchType(SearchType.DfsQueryThenFetch)
        .start(args.start)
        .limit(args.limit)
    } else {
      search(searchIndicies)
        .query(query)
        .sortBy(
          FieldSort("_score").order(SortOrder.DESC), FieldSort("uprn").order(SortOrder.ASC)
        )
        .trackScores(true)
        .searchType(SearchType.DfsQueryThenFetch)
        .start(args.start)
        .limit(args.limit)
    }
  }

  override def makeQuery(queryArgs: QueryArgs): SearchRequest = queryArgs match {
    case uprnArgs: UPRNArgs =>
      makeUprnQuery(uprnArgs)
    // uprn normally runs .map(_.addresses.headOption)
    case partialArgs: PartialArgs =>
      // we default to the fast, non-fallback query here
      makePartialSearch(partialArgs, fallback = false)
    case postcodeArgs: PostcodeArgs =>
      makePostcodeQuery(postcodeArgs)
    case groupedPostcodeArgs: GroupedPostcodeArgs =>
      makeGroupedPostcodeQuery(groupedPostcodeArgs)
    case bucketArgs: BucketArgs =>
      makeBucketQuery(bucketArgs)
    case randomArgs: RandomArgs =>
      makeRandomQuery(randomArgs)
    case addressArgs: AddressArgs =>
      makeAddressQuery(addressArgs)
    case _: BulkArgs =>
      null
  }

  override def runUPRNQuery(args: UPRNArgs): Future[Option[HybridAddress]] = {
    val query = makeQuery(args)
    logger.trace(query.toString)
    if (gcp || uprnFull) clientFullmatch.execute(query).map(HybridAddressCollection.fromResponse).map(_.addresses.headOption) else
      client.execute(query).map(HybridAddressCollection.fromResponse).map(_.addresses.headOption)
  }

  override def runMultiResultQuery(args: MultiResultArgs): Future[HybridAddressCollection] = {
    val query = makeQuery(args)
 // uncomment to see generated query
 //   val searchString = SearchBodyBuilderFn(query).string()
 //   println(searchString)
    args match {
      case partialArgs: PartialArgs =>
        val minimumFallback: Int = esConf.minimumFallback
        val tokenCount = args.inputOrDefault.split(" ").length
        // generate a slow, fuzzy fallback query for later
        lazy val fallbackQuery = makePartialSearch(partialArgs, fallback = true)
        val partResult = if ((gcp && args.verboseOrDefault) || partialFull) clientFullmatch.execute(query).map(HybridAddressCollection.fromResponse) else
          client.execute(query).map(HybridAddressCollection.fromResponse)
        // if there are no results for the "phrase" query, delegate to an alternative "best fields" query
        partResult.map { adds =>
          if (adds.addresses.isEmpty && partialArgs.fallback && (args.inputOpt.nonEmpty && args.inputOpt.get.length >= minimumFallback && tokenCount > 2)) {
            logger.info(s"minimumFallback: $minimumFallback")
            logger.info(s"tokenCount: $tokenCount")
            logger.info(s"Partial query is empty and fall back is on. Input length: ${args.inputOpt.get.length}. Run fallback query.")
            if ((gcp && args.verboseOrDefault) || partialFull) clientFullmatch.execute(fallbackQuery).map(HybridAddressCollection.fromResponse) else
            client.execute(fallbackQuery).map(HybridAddressCollection.fromResponse)}
          else partResult
        }.flatten
      case addressArgs: AddressArgs =>
        if (gcp || (!addressArgs.isBulk && addressFull) || (addressArgs.isBulk && bulkFull)) clientFullmatch.execute(query).map(HybridAddressCollection.fromResponse) else
          client.execute(query).map(HybridAddressCollection.fromResponse)
      case _: PostcodeArgs =>
        if ((gcp && args.verboseOrDefault) || (gcp && args.includeAuxiliarySearchOrDefault) || postcodeFull) clientFullmatch.execute(query).map(HybridAddressCollection.fromResponse) else
          client.execute(query).map(HybridAddressCollection.fromResponse)
      case _: BucketArgs =>
        if ((gcp && args.verboseOrDefault) || postcodeFull) clientFullmatch.execute(query).map(HybridAddressCollection.fromResponse) else
          client.execute(query).map(HybridAddressCollection.fromResponse)
      case _: GroupedPostcodeArgs =>
        if ((gcp && args.verboseOrDefault) || postcodeFull) clientFullmatch.execute(query).map(HybridAddressCollection.fromResponse) else
          client.execute(query).map(HybridAddressCollection.fromResponse)
      case _: RandomArgs =>
        if ((gcp && args.verboseOrDefault) || randomFull) clientFullmatch.execute(query).map(HybridAddressCollection.fromResponse) else
          client.execute(query).map(HybridAddressCollection.fromResponse)
      case _ =>
        if (gcp && args.verboseOrDefault) clientFullmatch.execute(query).map(HybridAddressCollection.fromResponse) else
        // catchall for any other endpoint
        client.execute(query).map(HybridAddressCollection.fromResponse)
    }
  }

  override def runBulkQuery(args: BulkArgs): Future[Stream[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]] = {
    val minimumSample = conf.config.bulk.minimumSample
    val addressRequests = args.requestsData.map { requestData =>
      val addressArgs = AddressArgs(
        input = "",
        tokens = requestData.tokens,
        limit = max(args.limit * 2, minimumSample),
        filters = "",
        region = None,
        filterDateRange = DateRange(args.filterDateRange.start, args.filterDateRange.end),
        queryParamsConfig = args.queryParamsConfig,
        historical = args.historical,
        verbose = false,
        isBulk = true,
        epoch = args.epoch,
        fromsource = "all",
        eboost = 1.0,
        nboost = 1.0,
        sboost = 1.0,
        wboost = 1.0
      )
      val bulkAddressRequest: Future[Seq[AddressBulkResponseAddress]] =
        runMultiResultQuery(addressArgs).map { case HybridAddressCollection(hybridAddresses, _, _, _) =>

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
            val threshold = Try(args.matchThreshold.toDouble).getOrElse(5.0D)
            val scoredAddresses = HopperScoreHelper.getScoresForAddresses(addressResponseAddresses, tokens, elasticDenominator)
            val addressBulkResponseAddresses = (bulkAddresses zip scoredAddresses).map { case (b, s) =>
              AddressBulkResponseAddress.fromBulkAddress(b, s, args.includeFullAddress)
            }
            val thresholdedAddresses = addressBulkResponseAddresses.filter(_.confidenceScore > threshold).sortBy(_.confidenceScore)(Ordering[Double].reverse).take(args.limit)

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