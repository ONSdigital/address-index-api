package uk.gov.ons.addressIndex.server.modules

import com.sksamuel.elastic4s.analyzers.CustomAnalyzer
import com.sksamuel.elastic4s.http.ElasticDsl.{geoDistanceQuery, _}
import com.sksamuel.elastic4s.http.HttpClient
import com.sksamuel.elastic4s.searches.queries.{BoolQueryDefinition, ConstantScoreDefinition, QueryDefinition}
import com.sksamuel.elastic4s.searches.sort.{FieldSortDefinition, SortOrder}
import com.sksamuel.elastic4s.searches.{SearchDefinition, SearchType}
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
class AddressIndexRepository @Inject()(conf: AddressIndexConfigModule,
                                       elasticClientProvider: ElasticClientProvider
                                      )(implicit ec: ExecutionContext) extends ElasticsearchRepository {

  private val esConf = conf.config.elasticSearch
  //  private val hybridIndex = esConf.indexes.hybridIndex + "/" + esConf.indexes.hybridMapping
  //  private val hybridIndexHistorical = esConf.indexes.hybridIndexHistorical + "/" + esConf.indexes.hybridMapping

  private def prefixPolicy(str: String): String = str match {
    case "" => "";
    case _ => "_c" + str
  }

  private val clusterPolicyUprn = prefixPolicy(esConf.clusterPolicies.uprn)
  private val clusterPolicyPartial = prefixPolicy(esConf.clusterPolicies.partial)
  private val clusterPolicyPostcode = prefixPolicy(esConf.clusterPolicies.postcode)
  private val clusterPolicyAddress = prefixPolicy(esConf.clusterPolicies.address)
  private val clusterPolicyBulk = prefixPolicy(esConf.clusterPolicies.bulk)
  private val clusterPolicyRandom = prefixPolicy(esConf.clusterPolicies.random)

  private val hybridIndexUprn = esConf.indexes.hybridIndex + clusterPolicyUprn
  private val hybridIndexHistoricalUprn = esConf.indexes.hybridIndexHistorical + clusterPolicyUprn

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

  val client: HttpClient = elasticClientProvider.client
  lazy val logger = GenericLogger("AddressIndexRepository")

  def queryHealth(): Future[String] = client.execute(clusterHealth()).map(_.toString)

  private def makeUprnQuery(args: UPRNArgs): SearchDefinition = {
    val query = termQuery("uprn", args.uprn)

    val source = if (args.historical) hybridIndexHistoricalUprn else hybridIndexUprn

    search(source + args.epochParam + hybridMapping).query(query)
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
  def makePartialSearch(args: PartialArgs, fallback: Boolean): SearchDefinition = {
    if (fallback) {
      logger.warn("best fields fallback query invoked for input string " + args.input)
    }

    val slopVal = 4
    val fieldsToSearch = Seq("lpi.nagAll.partial", "paf.mixedPaf.partial", "paf.mixedWelshPaf.partial", "nisra.mixedNisra.partial")
    val queryBase = multiMatchQuery(args.input).fields(fieldsToSearch)
    val queryWithMatchType = if (fallback) queryBase.matchType("best_fields") else queryBase.matchType("phrase").slop(slopVal)

    val filterSeq = Seq(
      if (args.filters.isEmpty) None
      else if (args.filtersType == "prefix") Some(prefixQuery("classificationCode", args.filtersValuePrefix))
      else Some(termsQuery("classificationCode", args.filtersValueTerm))
    ).flatten

    // if there is only one number, give boost for pao or sao not both.
    // if there are two or more numbers, boost for either matching pao and first matching sao
    // the usual order is (sao pao) and a higher score is given for this match
    // helper function
    def numMatchQuery(field: String, value: Any) =
      matchQuery(field, value).prefixLength(1).maxExpansions(10).fuzzyTranspositions(false)

    val numberQuery: Seq[QueryDefinition] = args.inputNumbers match {
      case first :: second :: _ if first == second => Seq(
        // allow the target pao and target sao to match once each
        // prevents (a a -> a b) from causing two matches
        numMatchQuery("lpi.paoStartNumber", first).boost(0.5D),
        numMatchQuery("lpi.saoStartNumber", first).boost(0.5D),
        numMatchQuery("nisra.buildingNumber", first).boost(0.5D))
      case first :: second :: _ => Seq(
        // allow the input pao and input sao to match once each
        // because they cannot both match the same target, matches should not overlap (usually)
        dismax(numMatchQuery("lpi.paoStartNumber", first).boost(0.2D),
          numMatchQuery("lpi.saoStartNumber", first).boost(0.5D),
          numMatchQuery("nisra.buildingNumber", first).boost(0.5D)),
        dismax(numMatchQuery("lpi.paoStartNumber", second).boost(0.5D),
          numMatchQuery("lpi.saoStartNumber", second).boost(0.2D),
          numMatchQuery("nisra.buildingNumber", second).boost(0.5D)))
      case Seq(first) => Seq(
        // otherwise, match either
        dismax(numMatchQuery("lpi.paoStartNumber", first).boost(0.5D),
          numMatchQuery("lpi.saoStartNumber", first).boost(0.2D),
          numMatchQuery("nisra.buildingNumber", first).boost(0.5D)))
      case _ => Seq.empty
    }

    val query = must(queryWithMatchType).filter(filterSeq).should(numberQuery)

    val source = if (args.historical) {
      if (args.verbose) hybridIndexHistoricalPartial else hybridIndexHistoricalSkinnyPartial
    } else {
      if (args.verbose) hybridIndexPartial else hybridIndexSkinnyPartial
    }

    search(source + args.epochParam + hybridMapping)
      .query(query)
      .start(args.start)
      .limit(args.limit)
  }

  private def makePostcodeQuery(args: PostcodeArgs): SearchDefinition = {
    val postcodeFormatted: String = if (!args.postcode.contains(" ")) {
      val (postcodeStart, postcodeEnd) = args.postcode.splitAt(args.postcode.length() - 3)
      (postcodeStart + " " + postcodeEnd).toUpperCase
    } else args.postcode.toUpperCase

    val queryFilter = if (args.filters.isEmpty) {
      Seq.empty
    } else {
      if (args.filtersType == "prefix") Seq(Option(prefixQuery("classificationCode", args.filtersValuePrefix)))
      else Seq(Option(termsQuery("classificationCode", args.filtersValueTerm)))
    }

    val query = must(termQuery("postcode", postcodeFormatted)).filter(queryFilter.flatten)

    val source = if (args.historical) {
      if (args.verbose) hybridIndexHistoricalPostcode else hybridIndexHistoricalSkinnyPostcode
    } else {
      if (args.verbose) hybridIndexPostcode else hybridIndexSkinnyPostcode
    }

    val searchBase = search(source + args.epochParam + hybridMapping)

    searchBase.query(query)
      .sortBy(FieldSortDefinition("lpi.streetDescriptor.keyword").asc(),
        FieldSortDefinition("lpi.paoStartNumber").asc(),
        FieldSortDefinition("lpi.paoStartSuffix.keyword").asc(),
        FieldSortDefinition("nisra.thoroughfare.keyword").asc(),
        FieldSortDefinition("nisra.buildingNumber.keyword").asc(),
        FieldSortDefinition("uprn").asc())
      .start(args.start)
      .limit(args.limit)
  }

  private def makeRandomQuery(args: RandomArgs): SearchDefinition = {
    val timestamp: Long = System.currentTimeMillis

    val queryInner = if (args.filters.isEmpty)
      None
    else args.filtersType match {
      case "prefix" => Option(prefixQuery("classificationCode", args.filtersValuePrefix))
      case _ => Option(termsQuery("classificationCode", args.filtersValueTerm))
    }

    val query = functionScoreQuery()
      .functions(randomScore(timestamp.toInt))
      .query(boolQuery().filter(Seq(queryInner).flatten))
      .boostMode("replace")

    val source = if (args.historical) {
      if (args.verbose) hybridIndexHistoricalRandom else hybridIndexHistoricalSkinnyRandom
    } else {
      if (args.verbose) hybridIndexRandom else hybridIndexSkinnyRandom
    }

    search(source + args.epochParam + hybridMapping)
      .query(query)
      .limit(args.limit)
  }

  private def makeAddressQuery(args: AddressArgs): SearchDefinition = {
    val queryParams = args.queryParamsConfig.getOrElse(conf.config.elasticSearch.queryParams)
    val defaultFuzziness = "1"

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
          )).boost(queryParams.subBuildingRange.lpiSaoStartEndBoost))
      ).flatten


    val subBuildingNameQuery: Seq[QueryDefinition] = Seq(
      args.tokens.get(Tokens.subBuildingName).map(token => Seq(
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
        case s => Some(Seq(dismax(s: Iterable[QueryDefinition]).tieBreaker(queryParams.includingDisMaxTieBreaker)))
      }
    ).flatten.flatten

    // this part of query should be used only when no subbuilding information has been parsed
    val saoStartNumber = args.tokens.getOrElse(Tokens.saoStartNumber, "")
    val saoStartSuffix = args.tokens.getOrElse(Tokens.saoStartSuffix, "")
    val subBuildingName = args.tokens.getOrElse(Tokens.subBuildingName, "")
    val crossPaoSao = saoStartNumber == "" && saoStartSuffix == "" && subBuildingName == ""

    val subBuildingPaoQuery: Seq[QueryDefinition] = if (crossPaoSao) Seq.empty else Seq(
      args.tokens.get(Tokens.buildingName).map(token => Seq(
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
        case s => Some(Seq(dismax(s: Iterable[QueryDefinition]).tieBreaker(queryParams.includingDisMaxTieBreaker)))
      }
    ).flatten.flatten

    val extraPaoSaoQueries = Seq(
      subBuildingPaoQuery
      // `dismax` dsl does not exist, `: _*` means that we provide a list (`queries`) as arguments (args) for the function
    ).filter(_.nonEmpty).map(queries => dismax(queries: Iterable[QueryDefinition])
      .tieBreaker(queryParams.excludingDisMaxTieBreaker)
      .boost(queryParams.subBuildingName.lpiSaoPaoStartSuffixBoost))

    // this part of query should be blank unless there is an end number or end suffix
    val paoEndNumber = args.tokens.getOrElse(Tokens.paoEndNumber, "")
    val paoEndSuffix = args.tokens.getOrElse(Tokens.paoEndSuffix, "")
    val skipPao = paoEndNumber == "" && paoEndSuffix == ""

    // TODO merge parts of this together
    val paoQuery: Seq[ConstantScoreDefinition] = if (!skipPao) Seq(
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
          field = "nisra.buildingNumber",
          value = token
        )).boost(queryParams.buildingRange.lpiPaoStartEndBoost)),
      args.tokens.get(Tokens.paoStartNumber).map(token =>
        constantScoreQuery(matchQuery(
          field = "nisra.buildingNumber",
          value = token
        )).boost(queryParams.buildingRange.lpiPaoStartEndBoost))
    ).flatten else Seq.empty


    val paoStartNumber = args.tokens.getOrElse(Tokens.paoStartNumber, "")
    val paoStartSuffix = args.tokens.getOrElse(Tokens.paoStartSuffix, "")
    val skipbuildingMust = paoStartNumber == "" && paoStartSuffix == ""

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

//    val paoBuildingNameMust = for {
//      paoStartNumber <- args.tokens.get(Tokens.paoStartNumber)
//      paoStartSuffix <- args.tokens.get(Tokens.paoStartSuffix)
//    } yield constantScoreQuery(must(Seq(
//      matchQuery(
//        field = "lpi.paoStartNumber",
//        value = paoStartNumber
//      ),
//      matchQuery(
//        field = "lpi.paoStartSuffix",
//        value = paoStartSuffix
//      )
//    ))).boost(queryParams.buildingName.lpiPaoStartSuffixBoost)

    val buildingNameQuery: Seq[QueryDefinition] = args.tokens.get(Tokens.buildingName).map(token => Seq(
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
    )).toList.flatten ++ paoBuildingNameMust ++ extraPaoSaoQueries

    val buildingNumberQuery = if (skipPao) {
      args.tokens.get(Tokens.paoStartNumber).map(token => Seq(
        constantScoreQuery(matchQuery(
          field = "paf.buildingNumber",
          value = token
        )).boost(queryParams.buildingNumber.pafBuildingNumberBoost),
        constantScoreQuery(matchQuery(
          field = "nisra.buildingNumber",
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
    val skipPostCodeOutMust = postcodeOut == "" && postcodeIn == ""

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

//    val postcodeInOutMust = for {
//      postcodeOut <- args.tokens.get(Tokens.postcodeOut)
//      postcodeIn <- args.tokens.get(Tokens.postcodeIn)
//    } yield constantScoreQuery(must(Seq(
//      matchQuery(
//        field = "postcodeOut",
//        value = postcodeOut
//      ).fuzziness(defaultFuzziness),
//      matchQuery(
//        field = "postcodeIn",
//        value = postcodeIn
//      ).fuzziness("2")
//    ))).boost(queryParams.postcode.postcodeInOutBoost)

    val postcodeQuery: Seq[ConstantScoreDefinition] = args.tokens.get(Tokens.postcode).map(token => Seq(
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
        field = "paf.postTown",
        value = token
      ).fuzziness(defaultFuzziness)).boost(queryParams.locality.pafPostTownBoost),
      constantScoreQuery(matchQuery(
        field = "nisra.townland",
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
        Seq(geoDistanceQuery("lpi.location").point(lat, lon).distance(s"${range}km"))
      case None => Seq.empty
    }

    val prefixWithGeo = Seq(prefixQuery("classificationCode", args.filtersValuePrefix)) ++ radiusQuery
    val termWithGeo = Seq(termsQuery("classificationCode", args.filtersValueTerm)) ++ radiusQuery

    val fallbackQueryStart: BoolQueryDefinition = bool(
      Seq(dismax(
        matchQuery("lpi.nagAll", normalizedInput)
          .minimumShouldMatch(queryParams.fallback.fallbackMinimumShouldMatch)
          .analyzer(CustomAnalyzer("welsh_split_synonyms_analyzer"))
          .boost(queryParams.fallback.fallbackLpiBoost),
        matchQuery("nisra.nisraAll", normalizedInput)
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
        matchQuery("nisra.nisraAll.bigram", normalizedInput)
          .fuzziness(queryParams.fallback.bigramFuzziness)
          .boost(queryParams.fallback.fallbackLpiBigramBoost),
        matchQuery("paf.pafAll.bigram", normalizedInput)
          .fuzziness(queryParams.fallback.bigramFuzziness)
          .boost(queryParams.fallback.fallbackPafBigramBoost))
        .tieBreaker(0.0)),
      Seq.empty).boost(queryParams.fallback.fallbackQueryBoost)

    val fallbackQueryFilter = if (args.filters.isEmpty)
      radiusQuery
    else args.filtersType match {
      case "prefix" => prefixWithGeo
      case _ => termWithGeo
    }

    val fallbackQuery = fallbackQueryStart.filter(fallbackQueryFilter)

    val bestOfTheLotQueries = Seq(
      buildingNumberQuery,
      buildingNameQuery,
      subBuildingNameQuery,
      streetNameQuery,
      postcodeQuery
      // `dismax` dsl does not exist, `: _*` means that we provide a list (`queries`) as arguments (args) for the function
    ).filter(_.nonEmpty).map(queries => dismax(queries: Iterable[QueryDefinition]).tieBreaker(queryParams.excludingDisMaxTieBreaker))

    val organisationDepartmentQueries = Seq(
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
      organisationDepartmentQueries,
      townLocalityQueries,
      paoQuery,
      saoQuery
      // `dismax` dsl does not exist, `: _*` means that we provide a list (`queries`) as arguments (args) for the function
    ).filter(_.nonEmpty).map(queries => dismax(queries: Iterable[QueryDefinition]).tieBreaker(queryParams.includingDisMaxTieBreaker))

    // add extra dismax after bestOfTheLot
    val shouldQuery = bestOfTheLotQueries ++ everythingMattersQueries

    val queryFilter = if (args.filters.isEmpty) radiusQuery
    else if (args.filtersType == "prefix") prefixWithGeo
    else termWithGeo

    val query = if (shouldQuery.isEmpty)
      fallbackQuery
    else {
      dismax(
        should(shouldQuery.asInstanceOf[Iterable[QueryDefinition]])
          .minimumShouldMatch(queryParams.mainMinimumShouldMatch)
          .filter(queryFilter)
        , fallbackQuery)
        .tieBreaker(queryParams.topDisMaxTieBreaker)
    }

    val source = if (args.historical) {
      if (args.isBulk) hybridIndexHistoricalBulk else hybridIndexHistoricalAddress
    } else {
      if (args.isBulk) hybridIndexBulk else hybridIndexAddress
    }

    search(source + args.epochParam + hybridMapping).query(query)
      .sortBy(FieldSortDefinition("_score").order(SortOrder.DESC), FieldSortDefinition("uprn").order(SortOrder.ASC))
      .trackScores(true)
      .searchType(SearchType.DfsQueryThenFetch)
      .start(args.start)
      .limit(args.limit)
  }

  override def makeQuery(queryArgs: QueryArgs): SearchDefinition = queryArgs match {
    case uprnArgs: UPRNArgs =>
      makeUprnQuery(uprnArgs)
    // uprn normally runs .map(_.addresses.headOption)
    case partialArgs: PartialArgs =>
      // we default to the fast, non-fallback query here
      makePartialSearch(partialArgs, fallback = false)
    case postcodeArgs: PostcodeArgs =>
      makePostcodeQuery(postcodeArgs)
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
    client.execute(query).map(HybridAddressCollection.fromEither).map(_.addresses.headOption)
  }

  override def runMultiResultQuery(args: MultiResultArgs): Future[HybridAddressCollection] = {
    val query = makeQuery(args)

    args match {
      case partialArgs: PartialArgs =>
        // generate a slow, fuzzy fallback query for later
        lazy val fallbackQuery = makePartialSearch(partialArgs, fallback = true)
        val partResult = client.execute(query).map(HybridAddressCollection.fromEither)
        // if there are no results for the "phrase" query, delegate to an alternative "best fields" query
        partResult.map { adds =>
          if (adds.addresses.isEmpty && partialArgs.fallback) client.execute(fallbackQuery).map(HybridAddressCollection.fromEither)
          else partResult
        }.flatten
      case _ =>
        // activates for postcode, random, address
        logger.trace(query.toString)
        client.execute(query).map(HybridAddressCollection.fromEither)
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
      )
      val bulkAddressRequest: Future[Seq[AddressBulkResponseAddress]] =
        runMultiResultQuery(addressArgs).map { case HybridAddressCollection(hybridAddresses, _, _) =>

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
            val threshold = Try((args.matchThreshold / 100).toDouble).getOrElse(0.05D)
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