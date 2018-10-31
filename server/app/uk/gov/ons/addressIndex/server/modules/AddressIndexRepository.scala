package uk.gov.ons.addressIndex.server.modules

import com.sksamuel.elastic4s.analyzers.CustomAnalyzer
import com.sksamuel.elastic4s.http.ElasticDsl.{geoDistanceQuery, _}
import com.sksamuel.elastic4s.http.HttpClient
import com.sksamuel.elastic4s.http.search.SearchBodyBuilderFn
import com.sksamuel.elastic4s.searches.queries.QueryDefinition
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
  private val hybridIndex = esConf.indexes.hybridIndex + "/" + esConf.indexes.hybridMapping
  private val hybridIndexHistorical = esConf.indexes.hybridIndexHistorical + "/" + esConf.indexes.hybridMapping
  private val hybridIndexUprn = esConf.indexes.hybridIndex + esConf.clusterPolicies.uprn + "/" + esConf.indexes.hybridMapping
  private val hybridIndexHistoricalUprn = esConf.indexes.hybridIndexHistorical + esConf.clusterPolicies.uprn + "/" + esConf.indexes.hybridMapping
  private val hybridIndexPartial = esConf.indexes.hybridIndex + esConf.clusterPolicies.partial + "/" + esConf.indexes.hybridMapping
  private val hybridIndexHistoricalPartial = esConf.indexes.hybridIndexHistorical + esConf.clusterPolicies.partial + "/" + esConf.indexes.hybridMapping
  private val hybridIndexPostcode = esConf.indexes.hybridIndex + esConf.clusterPolicies.postcode + "/" + esConf.indexes.hybridMapping
  private val hybridIndexHistoricalPostcode = esConf.indexes.hybridIndexHistorical + esConf.clusterPolicies.postcode + "/" + esConf.indexes.hybridMapping
  private val hybridIndexAddress = esConf.indexes.hybridIndex + esConf.clusterPolicies.address + "/" + esConf.indexes.hybridMapping
  private val hybridIndexHistoricalAddress = esConf.indexes.hybridIndexHistorical + esConf.clusterPolicies.address + "/" + esConf.indexes.hybridMapping
  private val hybridIndexBulk = esConf.indexes.hybridIndex + esConf.clusterPolicies.bulk + "/" + esConf.indexes.hybridMapping
  private val hybridIndexHistoricalBulk = esConf.indexes.hybridIndexHistorical + esConf.clusterPolicies.bulk + "/" + esConf.indexes.hybridMapping


  private val DATE_FORMAT = "yyyy-MM-dd"

  val client: HttpClient = elasticClientProvider.client
  lazy val logger = GenericLogger("AddressIndexRepository")

  def queryHealth(): Future[String] = client.execute(clusterHealth()).map(_.toString)

  def queryUprn(uprn: String, startDate:String = "", endDate:String = "", historical: Boolean = true): Future[Option[HybridAddress]] = {

    val request = generateQueryUprnRequest(uprn, startDate, endDate, historical)

    logger.trace(request.toString)

    client.execute(request)
      .map(HybridAddresses.fromEither)
      .map(_.addresses.headOption)
  }

  /**
    * Generates request to get address from ES by UPRN
    * Public for tests
    *
    * @param uprn the uprn of the fetched address
    * @return Seqrch definition containing query to the ES
    */
  def generateQueryUprnRequest(uprn: String, startDate: String = "", endDate: String = "", historical: Boolean = true): SearchDefinition = {

    val query = {
      if(!startDate.isEmpty && !endDate.isEmpty) {
        must(termQuery("uprn", uprn))
          .filter(
            should(
              must(rangeQuery("paf.startDate").gte(startDate).format(DATE_FORMAT),
                   rangeQuery("paf.endDate").lte(endDate).format(DATE_FORMAT)),
              must(rangeQuery("lpi.lpiStartDate").gte(startDate).format(DATE_FORMAT),
                   rangeQuery("lpi.lpiEndDate").lte(endDate).format(DATE_FORMAT))))

      } else {
        termQuery("uprn", uprn)
      }
    }


      if (historical) {
      search(hybridIndexHistoricalUprn).query(query)
    } else {
      search(hybridIndexUprn).query(query)
    }
  }

  /**
    * Generates request to get address from partial string (e.g typeahead)
    * Pass on to fallback if needed
    *
    * @param input             the partial string to be searched
    * @param start start result
    * @param limit maximum number of results
    * @param filters           classification filter
    * @param startDate         start date
    * @param endDate           end date
    * @param queryParamsConfig config
    * @param historical        historical flag
    * @return Search definition containing query to the ES
    */
  def queryPartialAddress(input: String, start: Int, limit: Int, filters: String, startDate: String = "", endDate: String = "", queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true): Future[HybridAddresses] = {

    val request = generateQueryPartialAddressRequest(input, filters, startDate, endDate, queryParamsConfig, historical, false).start(start).limit(limit)

    val requestString = SearchBodyBuilderFn(request).string()
    logger.warn(requestString)
    val partResult = client.execute(request).map(HybridAddresses.fromEither)
    // if there are no results for the "phrase" query, delegate to an alternative "best fields" query
    val endResult = partResult.map {adds =>
      if (adds.addresses.isEmpty) queryPartialAddressFallback(input,start,limit,filters,startDate,endDate,queryParamsConfig,historical)
      else partResult
    }
    endResult.flatten
  }

  /**
    * Generates request to get address from partial string (e.g typeahead)
    * Fallback version
    *
    * @param input the partial string to be searched
    * @param start start result
    * @param limit maximum number of results
    * @param filters classification filter
    * @param startDate start date
    * @param endDate end date
    * @param queryParamsConfig config
    * @param historical historical flag
    * @return Search definition containing query to the ES
    */
  def queryPartialAddressFallback(input: String, start: Int, limit: Int, filters: String, startDate: String = "", endDate: String = "", queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true): Future[HybridAddresses] = {
    logger.warn("best fields fallback query invoked for input string " + input)
    val fallback = generateQueryPartialAddressRequest(input, filters, startDate, endDate, queryParamsConfig, historical, true).start(start).limit(limit)
    client.execute(fallback).map(HybridAddresses.fromEither)
  }

  /**
    * Generates request to get address from partial string (e.g typeahead)
    * Public for tests
    *
    * @param input partial string
    * @param filters classification filter
    * @param startDate start date
    * @param endDate end date
    * @param queryParamsConfig config
    * @param historical historical flag
    * @param fallback flag to indicate if fallback query is required
    * @return Search definition containing query to the ES
    */
  def generateQueryPartialAddressRequest(input: String, filters: String, startDate: String, endDate: String, queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true, fallback: Boolean = false): SearchDefinition = {

    val filterType: String = {
      if (filters == "residential" || filters == "commercial" || filters.endsWith("*")) "prefix"
      else "term"
    }

    val filterValue: String = {
      if (filters == "residential") "R"
      else if (filters == "commercial") "C"
      else if (filters.endsWith("*")) filters.substring(0, filters.length - 1).toUpperCase
      else filters.toUpperCase
    }

    val inputNumberOnlyPattern = "([0-9]+)".r

   // val inputNumber = input.replaceAll("[^0-9]", "")

    val inputNumberList: List[String] = input.split("\\D+").filter(_.nonEmpty).toList

    val slopVal = 4

    val dateQuery: Option[QueryDefinition] = {
      if (!startDate.isEmpty && !endDate.isEmpty) {
        Some(
        should(
          must(rangeQuery("paf.startDate").gte(startDate).format(DATE_FORMAT),
            rangeQuery("paf.endDate").lte(endDate).format(DATE_FORMAT)),
          must(rangeQuery("lpi.lpiStartDate").gte(startDate).format(DATE_FORMAT),
            rangeQuery("lpi.lpiEndDate").lte(endDate).format(DATE_FORMAT))))
      }
      else None
    }

    val query =
      if (inputNumberList.isEmpty) {
        if (filters.isEmpty) {
          if (fallback) {
            must(multiMatchQuery(input)
              .matchType("best_fields")
              .fields("lpi.nagAll.partial", "paf.mixedPaf.partial", "paf.mixedWelshPaf"))
              .filter(Seq(Option(not(termQuery("lpi.addressBasePostal", "N"))), dateQuery)
                .flatten)
          }
          else {
            must(multiMatchQuery(input)
              .matchType("phrase")
              .slop(slopVal)
              .fields("lpi.nagAll.partial", "paf.mixedPaf.partial", "paf.mixedWelshPaf"))
              .filter(Seq(Option(not(termQuery("lpi.addressBasePostal", "N"))), dateQuery)
                .flatten)
          }
        } else {
          if (filterType == "prefix") {
            if (fallback) {
              must(multiMatchQuery(input)
                .matchType("best_fields")
                .fields("lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf"))
                .filter(Seq(Option(prefixQuery("classificationCode", filterValue)), Option(not(termQuery("lpi.addressBasePostal", "N"))), dateQuery)
                  .flatten)
            }
            else {
              must(multiMatchQuery(input)
                .matchType("phrase")
                .slop(slopVal)
                .fields("lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf"))
                .filter(Seq(Option(prefixQuery("classificationCode", filterValue)), Option(not(termQuery("lpi.addressBasePostal", "N"))), dateQuery)
                  .flatten)
            }
          }
          else {
            if (fallback) {
              must(multiMatchQuery(input)
                .matchType("best_fields")
                .fields("lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf"))
                .filter(Seq(Option(termQuery("classificationCode", filterValue)), Option(not(termQuery("lpi.addressBasePostal", "N"))), dateQuery)
                  .flatten)
            }
            else {
              must(multiMatchQuery(input)
                .matchType("phrase").slop(slopVal)
                .fields("lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf"))
                .filter(Seq(Option(termQuery("classificationCode", filterValue)), Option(not(termQuery("lpi.addressBasePostal", "N"))), dateQuery)
                  .flatten)
            }
          }
        }
      }
      else {
        if (filters.isEmpty) {
          if (fallback) {
            must(multiMatchQuery(input)
              .matchType("best_fields")
              .fields("lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf"))
              .should(matchQuery("lpi.paoStartNumber",inputNumberList(0)).prefixLength(1).maxExpansions(10),
              matchQuery("lpi.paoStartNumber",inputNumberList(max(1,inputNumberList.length-1))).prefixLength(1).maxExpansions(10),
                matchQuery("lpi.saoStartNumber",inputNumberList(0)).prefixLength(1).maxExpansions(10).boost(0.2D))
              .filter(Seq(Option(not(termQuery("lpi.addressBasePostal", "N"))), dateQuery)
                .flatten)
          }
          else {
            must(multiMatchQuery(input)
              .matchType("phrase").slop(slopVal)
              .fields("lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf"))
              .should(matchQuery("lpi.paoStartNumber",inputNumberList(0)).prefixLength(1).maxExpansions(10),
                matchQuery("lpi.paoStartNumber",inputNumberList(max(1,inputNumberList.length-1))).prefixLength(1).maxExpansions(10),
                matchQuery("lpi.saoStartNumber",inputNumberList(0)).prefixLength(1).maxExpansions(10).boost(0.2D))
              .filter(Seq(Option(not(termQuery("lpi.addressBasePostal", "N"))), dateQuery)
                .flatten)
          }
        } else {
          if (filterType == "prefix") {
            if (fallback) {
              must(multiMatchQuery(input)
                .matchType("best_fields")
                .fields("lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf"))
                .should(matchQuery("lpi.paoStartNumber",inputNumberList(0)).prefixLength(1).maxExpansions(10),
                  matchQuery("lpi.paoStartNumber",inputNumberList(max(1,inputNumberList.length-1))).prefixLength(1).maxExpansions(10),
                  matchQuery("lpi.saoStartNumber",inputNumberList(0)).prefixLength(1).maxExpansions(10).boost(0.2D))
                .filter(Seq(Option(prefixQuery("classificationCode", filterValue)), Option(not(termQuery("lpi.addressBasePostal", "N"))), dateQuery)
                  .flatten)
            }
            else {
              must(multiMatchQuery(input)
                .matchType("phrase")
                .slop(slopVal)
                .fields("lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf"))
                .should(matchQuery("lpi.paoStartNumber",inputNumberList(0)).prefixLength(1).maxExpansions(10),
                  matchQuery("lpi.paoStartNumber",inputNumberList(max(1,inputNumberList.length-1))).prefixLength(1).maxExpansions(10),
                  matchQuery("lpi.saoStartNumber",inputNumberList(0)).prefixLength(1).maxExpansions(10).boost(0.2D))
                .filter(Seq(Option(prefixQuery("classificationCode", filterValue)), Option(not(termQuery("lpi.addressBasePostal", "N"))), dateQuery)
                  .flatten)
            }
          }
          else {
            if (fallback) {
              must(multiMatchQuery(input)
                .matchType("best_fields")
                .fields("lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf"))
                .should(matchQuery("lpi.paoStartNumber",inputNumberList(0)),
                  matchQuery("lpi.paoStartNumber",inputNumberList(1)))
                .filter(Seq(Option(termQuery("classificationCode", filterValue)), Option(not(termQuery("lpi.addressBasePostal", "N"))), dateQuery)
                  .flatten)
            }
            else {
              must(multiMatchQuery(input)
                .matchType("phrase").slop(slopVal)
                .fields("lpi.nagAll.partial","paf.mixedPaf.partial","paf.mixedWelshPaf"))
                .should(matchQuery("lpi.paoStartNumber",inputNumberList(0)),
                  matchQuery("lpi.paoStartNumber",inputNumberList(1)))
                .filter(Seq(Option(termQuery("classificationCode", filterValue)), Option(not(termQuery("lpi.addressBasePostal", "N"))), dateQuery)
                  .flatten)
            }
          }
        }
      }

    if (historical) {
      search(hybridIndexHistoricalPartial).query(query)
    } else {
      search(hybridIndexPartial).query(query)
    }
  }

  def queryPostcode(postcode: String, start: Int, limit: Int, filters: String, startDate: String = "", endDate: String = "", queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true): Future[HybridAddresses] = {

    val request = generateQueryPostcodeRequest(postcode, filters, startDate, endDate, queryParamsConfig, historical).start(start).limit(limit)

    logger.trace(request.toString)

    client.execute(request).map(HybridAddresses.fromEither)
  }

  /**
    * Generates request to get address from ES by Postcode
    * Public for tests
    *
    * @param postcode the postcode of the fetched address
    * @return Search definition containing query to the ES
    */
  def generateQueryPostcodeRequest(postcode: String, filters: String, startDate: String, endDate: String, queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true): SearchDefinition = {

    val filterType: String = {
      if (filters == "residential" || filters == "commercial" || filters.endsWith("*")) "prefix"
      else "term"
    }

    val filterValue: String = {
      if (filters == "residential") "R"
      else if (filters == "commercial") "C"
      else if (filters.endsWith("*")) filters.substring(0, filters.length - 1).toUpperCase
      else filters.toUpperCase
    }

    val postcodeFormatted: String = {
      if (!postcode.contains(" ")) {
        val postcodeLength = postcode.length()
        val (postcodeStart, postcodeEnd) = postcode.splitAt(postcodeLength - 3)
        (postcodeStart + " " + postcodeEnd).toUpperCase
      }
      else postcode.toUpperCase
    }

    val dateQuery: Option[QueryDefinition] = {
      if (!startDate.isEmpty && !endDate.isEmpty) {
        Some(
          should(
            must(rangeQuery("paf.startDate").gte(startDate).format(DATE_FORMAT),
              rangeQuery("paf.endDate").lte(endDate).format(DATE_FORMAT)),
            must(rangeQuery("lpi.lpiStartDate").gte(startDate).format(DATE_FORMAT),
              rangeQuery("lpi.lpiEndDate").lte(endDate).format(DATE_FORMAT))))
      }
      else None
    }

    val query =
      if (filters.isEmpty) {
        must(termQuery("lpi.postcodeLocator", postcodeFormatted)).filter(Seq(Option(not(termQuery("lpi.addressBasePostal", "N"))), dateQuery).flatten)
    }else {
        if (filterType == "prefix") {
          must(termQuery("lpi.postcodeLocator", postcodeFormatted)).filter(Seq(Option(prefixQuery("classificationCode", filterValue)), Option(not(termQuery("lpi.addressBasePostal", "N"))), dateQuery).flatten)
        }
        else {
          must(termQuery("lpi.postcodeLocator", postcodeFormatted)).filter(Seq(Option(termQuery("classificationCode", filterValue)), Option(not(termQuery("lpi.addressBasePostal", "N"))), dateQuery).flatten)
        }
      }

    if (historical) {
      search(hybridIndexHistoricalPostcode).query(query)
        .sortBy(FieldSortDefinition("lpi.streetDescriptor.keyword").order(SortOrder.ASC), FieldSortDefinition("lpi.paoStartNumber").order(SortOrder.ASC), FieldSortDefinition("lpi.paoStartSuffix.keyword").order(SortOrder.ASC), FieldSortDefinition("uprn").order(SortOrder.ASC))
    } else {
      search(hybridIndexPostcode).query(query)
        .sortBy(FieldSortDefinition("lpi.streetDescriptor.keyword").order(SortOrder.ASC), FieldSortDefinition("lpi.paoStartNumber").order(SortOrder.ASC), FieldSortDefinition("lpi.paoStartSuffix.keyword").order(SortOrder.ASC), FieldSortDefinition("uprn").order(SortOrder.ASC))
    }
  }

  def queryAddresses(tokens: Map[String, String], start: Int, limit: Int, filters: String, range: String, lat: String, lon: String, startDate: String, endDate: String, queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true, isBulk: Boolean = false): Future[HybridAddresses] = {

    val request = generateQueryAddressRequest(tokens, filters, range, lat, lon, startDate, endDate, queryParamsConfig, historical, isBulk).start(start).limit(limit)

    logger.trace(request.toString)

    client.execute(request).map(HybridAddresses.fromEither)
  }

  def generateQueryAddressRequest(tokens: Map[String, String], filters: String, range: String, lat: String, lon: String, startDate: String, endDate: String, queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true, isBulk: Boolean = false): SearchDefinition = {

    val queryParams = queryParamsConfig.getOrElse(conf.config.elasticSearch.queryParams)
    val defaultFuzziness = "1"

    // this part of query should be blank unless there is an end number or end suffix
    val saoEndNumber = tokens.getOrElse(Tokens.saoEndNumber, "")
    val saoEndSuffix = tokens.getOrElse(Tokens.saoEndSuffix, "")
    val skipSao = saoEndNumber == "" && saoEndSuffix == ""

    val dateQuery: Option[QueryDefinition] = {
      if (!startDate.isEmpty && !endDate.isEmpty) {
        Some(
          should(
            must(rangeQuery("paf.startDate").gte(startDate).format(DATE_FORMAT),
              rangeQuery("paf.endDate").lte(endDate).format(DATE_FORMAT)),
            must(rangeQuery("lpi.lpiStartDate").gte(startDate).format(DATE_FORMAT),
              rangeQuery("lpi.lpiEndDate").lte(endDate).format(DATE_FORMAT))))
      }
      else None
    }

    val saoQuery = if (skipSao) Seq() else
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

    val paoQuery = if (skipPao) Seq() else
      Seq(
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
      ).flatten

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

    val buildingNameQuery: Seq[QueryDefinition] = Seq(
      tokens.get(Tokens.buildingName).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.buildingName",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.buildingName.pafBuildingNameBoost)),
      tokens.get(Tokens.buildingName).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.paoText",
          value = token
        ).fuzziness(defaultFuzziness).minimumShouldMatch(queryParams.paoSaoMinimumShouldMatch)).boost(queryParams.buildingName.lpiPaoTextBoost)),

      paoBuildingNameMust

    ).flatten

    val buildingNumberQuery = if (skipPao) Seq(
      tokens.get(Tokens.paoStartNumber).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.buildingNumber",
          value = token
        )).boost(queryParams.buildingNumber.pafBuildingNumberBoost)),
      tokens.get(Tokens.paoStartNumber).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.paoStartNumber",
          value = token
        )).boost(queryParams.buildingNumber.lpiPaoStartNumberBoost)),
      tokens.get(Tokens.paoStartNumber).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.paoEndNumber",
          value = token
        )).boost(queryParams.buildingNumber.lpiPaoEndNumberBoost))
    ).flatten else Seq()

    val streetNameQuery = Seq(
      tokens.get(Tokens.streetName).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.thoroughfare",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.streetName.pafThoroughfareBoost)),
      tokens.get(Tokens.streetName).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.welshThoroughfare",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.streetName.pafWelshThoroughfareBoost)),
      tokens.get(Tokens.streetName).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.dependentThoroughfare",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.streetName.pafDependentThoroughfareBoost)),
      tokens.get(Tokens.streetName).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.welshDependentThoroughfare",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.streetName.pafWelshDependentThoroughfareBoost)),
      tokens.get(Tokens.streetName).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.streetDescriptor",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.streetName.lpiStreetDescriptorBoost))
    ).flatten

    val townNameQuery = Seq(
      tokens.get(Tokens.townName).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.postTown",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.townName.pafPostTownBoost)),
      tokens.get(Tokens.townName).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.welshPostTown",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.townName.pafWelshPostTownBoost)),
      tokens.get(Tokens.townName).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.townName",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.townName.lpiTownNameBoost)),
      tokens.get(Tokens.townName).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.dependentLocality",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.townName.pafDependentLocalityBoost)),
      tokens.get(Tokens.townName).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.welshDependentLocality",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.townName.pafWelshDependentLocalityBoost)),
      tokens.get(Tokens.townName).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.locality",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.townName.lpiLocalityBoost)),
      tokens.get(Tokens.townName).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.doubleDependentLocality",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.townName.pafDoubleDependentLocalityBoost)),
      tokens.get(Tokens.townName).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.welshDoubleDependentLocality",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.townName.pafWelshDoubleDependentLocalityBoost))
    ).flatten

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

    val postcodeQuery = Seq(
      tokens.get(Tokens.postcode).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.postcode",
          value = token
        )).boost(queryParams.postcode.pafPostcodeBoost)),
      tokens.get(Tokens.postcode).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.postcodeLocator",
          value = token
        )).boost(queryParams.postcode.lpiPostcodeLocatorBoost)),

      postcodeInOutMust

    ).flatten

    val organisationNameQuery = Seq(
      tokens.get(Tokens.organisationName).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.organisationName",
          value = token
        ).minimumShouldMatch(queryParams.organisationDepartmentMinimumShouldMatch)).boost(queryParams.organisationName.pafOrganisationNameBoost)),
      tokens.get(Tokens.organisationName).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.organisation",
          value = token
        ).minimumShouldMatch(queryParams.organisationDepartmentMinimumShouldMatch)).boost(queryParams.organisationName.lpiOrganisationBoost)),
      tokens.get(Tokens.organisationName).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.paoText",
          value = token
        ).minimumShouldMatch(queryParams.organisationDepartmentMinimumShouldMatch)).boost(queryParams.organisationName.lpiPaoTextBoost)),
      tokens.get(Tokens.organisationName).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.legalName",
          value = token
        ).minimumShouldMatch(queryParams.organisationDepartmentMinimumShouldMatch)).boost(queryParams.organisationName.lpiLegalNameBoost)),
      tokens.get(Tokens.organisationName).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.saoText",
          value = token
        ).minimumShouldMatch(queryParams.organisationDepartmentMinimumShouldMatch)).boost(queryParams.organisationName.lpiSaoTextBoost))
    ).flatten

    val departmentNameQuery = Seq(
      tokens.get(Tokens.departmentName).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.departmentName",
          value = token
        ).minimumShouldMatch(queryParams.organisationDepartmentMinimumShouldMatch)).boost(queryParams.departmentName.pafDepartmentNameBoost)),
      tokens.get(Tokens.departmentName).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.legalName",
          value = token
        ).minimumShouldMatch(queryParams.organisationDepartmentMinimumShouldMatch)).boost(queryParams.departmentName.lpiLegalNameBoost))
    ).flatten

    val localityQuery = Seq(
      tokens.get(Tokens.locality).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.postTown",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.locality.pafPostTownBoost)),
      tokens.get(Tokens.locality).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.welshPostTown",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.locality.pafWelshPostTownBoost)),
      tokens.get(Tokens.locality).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.townName",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.locality.lpiTownNameBoost)),
      tokens.get(Tokens.locality).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.dependentLocality",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.locality.pafDependentLocalityBoost)),
      tokens.get(Tokens.locality).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.welshDependentLocality",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.locality.pafWelshDependentLocalityBoost)),
      tokens.get(Tokens.locality).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.locality",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.locality.lpiLocalityBoost)),
      tokens.get(Tokens.locality).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.doubleDependentLocality",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.locality.pafDoubleDependentLocalityBoost)),
      tokens.get(Tokens.locality).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.welshDoubleDependentLocality",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.locality.pafWelshDoubleDependentLocalityBoost))
    ).flatten

    val normalizedInput = Tokens.concatenate(tokens)

    val filterType: String = {
      if (filters == "residential" || filters == "commercial" || filters.endsWith("*")) "prefix"
      else "term"
    }

    val filterValue: String = {
      if (filters == "residential") "R"
      else if (filters == "commercial") "C"
      else if (filters.endsWith("*")) filters.substring(0, filters.length - 1).toUpperCase
      else filters.toUpperCase
    }

    val radiusQuery = {
      if (range.equals(""))
        Seq()
      else
        Seq(
          geoDistanceQuery("lpi.location").point(lat.toDouble, lon.toDouble).distance(s"${range}km")
        )
    }

    val prefixWithGeo = if (range.equals(""))
      Seq(prefixQuery("classificationCode", filterValue))
    else
      Seq(prefixQuery("classificationCode", filterValue),geoDistanceQuery("lpi.location").point(lat.toDouble, lon.toDouble).distance(s"${range}km"))

    val termWithGeo = if (range.equals(""))
      Seq(termQuery("classificationCode", filterValue))
    else
      Seq(termQuery("classificationCode", filterValue),geoDistanceQuery("lpi.location").point(lat.toDouble, lon.toDouble).distance(s"${range}km"))

    val fallbackQuery =
      if (filters.isEmpty) {
        bool(
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
          Seq()).boost(queryParams.fallback.fallbackQueryBoost).filter(radiusQuery ++ Seq(dateQuery).flatten)
      }
      else {
        if (filterType == "prefix") {
          bool(
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
            Seq()).boost(queryParams.fallback.fallbackQueryBoost)
            .filter(prefixWithGeo ++ Seq(dateQuery).flatten)
        }
        else {
          bool(
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
            Seq()).boost(queryParams.fallback.fallbackQueryBoost).filter(termWithGeo ++ Seq(dateQuery).flatten)
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
    val shouldQueryItr = shouldQuery.asInstanceOf[Iterable[QueryDefinition]]

    val query =
      if (shouldQuery.isEmpty) fallbackQuery
      else if (filters.isEmpty)
        dismax(
          should(shouldQueryItr).minimumShouldMatch(queryParams.mainMinimumShouldMatch).filter(radiusQuery ++ Seq(dateQuery).flatten), fallbackQuery)
          .tieBreaker(queryParams.topDisMaxTieBreaker)
      else if (filterType == "prefix") dismax(
        should(shouldQueryItr).minimumShouldMatch(queryParams.mainMinimumShouldMatch).filter(prefixWithGeo ++ Seq(dateQuery).flatten), fallbackQuery)
        .tieBreaker(queryParams.topDisMaxTieBreaker)
      else dismax(
        should(shouldQueryItr).minimumShouldMatch(queryParams.mainMinimumShouldMatch).filter(termWithGeo ++ Seq(dateQuery).flatten), fallbackQuery)
        .tieBreaker(queryParams.topDisMaxTieBreaker)

    if (historical) {
      if (isBulk) {
        search(hybridIndexHistoricalBulk).query(query)
          .sortBy(FieldSortDefinition("_score").order(SortOrder.DESC), FieldSortDefinition("uprn").order(SortOrder.ASC))
          .trackScores(true)
          .searchType(SearchType.DfsQueryThenFetch)
      } else {
      search(hybridIndexHistoricalAddress).query(query)
        .sortBy(FieldSortDefinition("_score").order(SortOrder.DESC), FieldSortDefinition("uprn").order(SortOrder.ASC))
        .trackScores(true)
        .searchType(SearchType.DfsQueryThenFetch)
      }
    } else {
      if (isBulk) {
        search(hybridIndexBulk).query(query)
          .sortBy(FieldSortDefinition("_score").order(SortOrder.DESC), FieldSortDefinition("uprn").order(SortOrder.ASC))
          .trackScores(true)
          .searchType(SearchType.DfsQueryThenFetch)
      } else {
        search(hybridIndexAddress).query(query)
          .sortBy(FieldSortDefinition("_score").order(SortOrder.DESC), FieldSortDefinition("uprn").order(SortOrder.ASC))
          .trackScores(true)
          .searchType(SearchType.DfsQueryThenFetch)
      }
    }
  }

  def queryBulk(requestsData: Stream[BulkAddressRequestData], limit: Int, startDate: String = "", endDate: String = "", queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true, matchThreshold: Float, includeFullAddress: Boolean = false): Future[Stream[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]] = {
    val minimumSample = conf.config.bulk.minimumSample
    val addressRequests = requestsData.map { requestData =>
      val bulkAddressRequest: Future[Seq[AddressBulkResponseAddress]] =
        queryAddresses(requestData.tokens, 0, max(limit*2,minimumSample), "","","50.71","-3.51", startDate, endDate, queryParamsConfig, historical, true).map { case HybridAddresses(hybridAddresses, _, _) =>

          // If we didn't find any results for an input, we still need to return
          // something that will indicate an empty result
          val tokens = requestData.tokens
          val emptyBulk = BulkAddress.empty(requestData)
          val emptyScored = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(emptyBulk.hybridAddress, true)), tokens, 1D)
          val emptyBulkAddress = AddressBulkResponseAddress.fromBulkAddress(emptyBulk, emptyScored.head, false)
          if (hybridAddresses.isEmpty) Seq(emptyBulkAddress)
          else {
            val bulkAddresses = hybridAddresses.map { hybridAddress =>
              BulkAddress.fromHybridAddress(hybridAddress, requestData)
            }

            val addressResponseAddresses = hybridAddresses.map { hybridAddress =>
              AddressResponseAddress.fromHybridAddress(hybridAddress, true)
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

            if (thresholdedAddresses.length == 0) Seq(emptyBulkAddress) else thresholdedAddresses
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
