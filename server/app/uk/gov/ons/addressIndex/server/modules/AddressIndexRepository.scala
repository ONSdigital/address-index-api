package uk.gov.ons.addressIndex.server.modules

import javax.inject.{Inject, Singleton}
import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import com.google.inject.ImplementedBy
import com.sksamuel.elastic4s.http.ElasticDsl.{geoDistanceQuery, _}
import com.sksamuel.elastic4s.analyzers.CustomAnalyzer
import com.sksamuel.elastic4s.http.HttpClient
import com.sksamuel.elastic4s.searches.{SearchDefinition, SearchType}
import com.sksamuel.elastic4s.searches.queries.QueryDefinition
import com.sksamuel.elastic4s.searches.sort.FieldSortDefinition
import com.sksamuel.elastic4s.searches.sort.SortOrder
import play.api.Logger
import uk.gov.ons.addressIndex.model.config.QueryParamsConfig
import uk.gov.ons.addressIndex.model.db.{BulkAddress, BulkAddressRequestData}
import uk.gov.ons.addressIndex.model.db.index._
import uk.gov.ons.addressIndex.model.server.response.{AddressBulkResponseAddress, AddressResponseAddress}
import uk.gov.ons.addressIndex.parsers.Tokens
import uk.gov.ons.addressIndex.server.utils.{ConfidenceScoreHelper, HopperScoreHelper}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.math._

@ImplementedBy(classOf[AddressIndexRepository])
trait ElasticsearchRepository {

  /**
    * Queries if ES is up
    */
  def queryHealth(): Future[String]

  /**
    * Query the address index by UPRN.
    *
    * @param uprn the identificator of the address
    * @return Fucture containing a address or `None` if not in the index
    */
  def queryUprn(uprn: String, historical: Boolean = true): Future[Option[HybridAddress]]

  /**
    * Query the address index by postcode.
    *
    * @param postcode the identificator of the address
    * @return Future containing a address or `None` if not in the index
    */
  def queryPostcode(postcode: String, start: Int, limit: Int, filters: String, queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true): Future[HybridAddresses]

  /**
    * Query the address index for addresses.
    *
    * @param start  the offset for the query
    * @param limit  maximum number of returned results
    * @param tokens address tokens
    * @return Future with found addresses and the maximum score
    */
  def queryAddresses(tokens: Map[String, String], start: Int, limit: Int, filters: String, range: String, lat: String, lon: String, queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true): Future[HybridAddresses]

  /**
    * Generates request to get address from ES by UPRN
    * Public so that it could be accessible to controllers for User's debug purposes
    *
    * @param tokens tokens for the ES query
    * @return Search definition containing query to the ES
    */
  def generateQueryAddressRequest(tokens: Map[String, String], filters: String, range: String, lat: String, lon: String, queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true): SearchDefinition

  /**
    * Query ES using MultiSearch endpoint
    *
    * @param requestsData data that will be used in the multi search request
    * @param limit        how many addresses to take per a request
    * @return a stream of `Either`, `Right` will contain resulting bulk address,
    *         `Left` will contain request data that is to be re-send
    */
  def queryBulk(requestsData: Stream[BulkAddressRequestData], limit: Int, queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true, matchThreshold: Float): Future[Stream[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]]
}

@Singleton
class AddressIndexRepository @Inject()(
  conf: AddressIndexConfigModule,
  elasticClientProvider: ElasticClientProvider
)(implicit ec: ExecutionContext) extends ElasticsearchRepository {

  private val esConf = conf.config.elasticSearch
  private val hybridIndex = esConf.indexes.hybridIndex + "/" + esConf.indexes.hybridMapping
  private val hybridIndexHistorical = esConf.indexes.hybridIndexHistorical + "/" + esConf.indexes.hybridMapping

  val client: HttpClient = elasticClientProvider.client
  val logger = Logger("AddressIndexRepository")

  def queryHealth(): Future[String] = client.execute(clusterHealth()).map(_.toString)

  def queryUprn(uprn: String, historical: Boolean = true): Future[Option[HybridAddress]] = {

    val request = generateQueryUprnRequest(uprn, historical)

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
  def generateQueryUprnRequest(uprn: String, historical: Boolean = true): SearchDefinition =
    if (historical) {
      search(hybridIndexHistorical).query(termQuery("uprn", uprn))
    } else {
      search(hybridIndex).query(termQuery("uprn", uprn))
    }

  def queryPostcode(postcode: String, start: Int, limit: Int, filters: String, queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true): Future[HybridAddresses] = {

    val request = generateQueryPostcodeRequest(postcode, filters, queryParamsConfig, historical).start(start).limit(limit)

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
  def generateQueryPostcodeRequest(postcode: String, filters: String, queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true): SearchDefinition = {

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
        val (postcodeStart, postcodeEnd) = postcode.splitAt(postcodeLength-3)
        (postcodeStart + " " + postcodeEnd).toUpperCase
      }
      else postcode.toUpperCase
    }

    val query =
    if (filters.isEmpty) {
      must(termQuery("lpi.postcodeLocator", postcodeFormatted)).filter(not(termQuery("lpi.addressBasePostal", "N")))
    }else {
      if (filterType == "prefix") {
        must(termQuery("lpi.postcodeLocator", postcodeFormatted)).filter(prefixQuery("lpi.classificationCode", filterValue), not(termQuery("lpi.addressBasePostal", "N")))
      }
      else {
        must(termQuery("lpi.postcodeLocator", postcodeFormatted)).filter(termQuery("lpi.classificationCode", filterValue), not(termQuery("lpi.addressBasePostal", "N")))
      }
    }

    if (historical) {
      search(hybridIndexHistorical).query(query)
        .sortBy(FieldSortDefinition("lpi.streetDescriptor.keyword").order(SortOrder.ASC), FieldSortDefinition("lpi.paoStartNumber").order(SortOrder.ASC), FieldSortDefinition("lpi.paoStartSuffix.keyword").order(SortOrder.ASC), FieldSortDefinition("uprn").order(SortOrder.ASC))
    } else {
      search(hybridIndex).query(query)
        .sortBy(FieldSortDefinition("lpi.streetDescriptor.keyword").order(SortOrder.ASC), FieldSortDefinition("lpi.paoStartNumber").order(SortOrder.ASC), FieldSortDefinition("lpi.paoStartSuffix.keyword").order(SortOrder.ASC), FieldSortDefinition("uprn").order(SortOrder.ASC))
    }
  }

  def queryAddresses(tokens: Map[String, String], start: Int, limit: Int, filters: String, range: String, lat: String, lon: String, queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true): Future[HybridAddresses] = {

    val request = generateQueryAddressRequest(tokens, filters, range, lat, lon, queryParamsConfig, historical).start(start).limit(limit)


    logger.trace(request.toString)

    client.execute(request).map(HybridAddresses.fromEither)
  }

  def generateQueryAddressRequest(tokens: Map[String, String], filters: String, range: String, lat: String, lon: String, queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true): SearchDefinition = {

    val queryParams = queryParamsConfig.getOrElse(conf.config.elasticSearch.queryParams)
    val defaultFuzziness = "1"

    // this part of query should be blank unless there is an end number or end suffix
    val saoEndNumber = tokens.getOrElse(Tokens.saoEndNumber, "")
    val saoEndSuffix = tokens.getOrElse(Tokens.saoEndSuffix, "")
    val skipSao = (saoEndNumber == "" && saoEndSuffix == "")

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
    val skipPao = (paoEndNumber == "" && paoEndSuffix == "")

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


    val buildingNumberQuery =  if (skipPao) Seq(
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
          geoDistanceQuery("lpi.location").point(lat.toDouble, lon.toDouble).distance(range + "km")
        )
    }

    val prefixWithGeo = if (range.equals(""))
      Seq(prefixQuery("lpi.classificationCode", filterValue))
    else
      Seq(prefixQuery("lpi.classificationCode", filterValue),geoDistanceQuery("lpi.location").point(lat.toDouble, lon.toDouble).distance(range + "km"))

    val termWithGeo = if (range.equals(""))
      Seq(termQuery("lpi.classificationCode", filterValue))
    else
      Seq(termQuery("lpi.classificationCode", filterValue),geoDistanceQuery("lpi.location").point(lat.toDouble, lon.toDouble).distance(range + "km"))


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
          Seq()).boost(queryParams.fallback.fallbackQueryBoost).filter(radiusQuery)
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
            .filter(prefixWithGeo)
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
            Seq()).boost(queryParams.fallback.fallbackQueryBoost)
            .filter(termWithGeo)
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
          should(shouldQueryItr).minimumShouldMatch(queryParams.mainMinimumShouldMatch).filter(radiusQuery), fallbackQuery)
          .tieBreaker(queryParams.topDisMaxTieBreaker)
      else if (filterType == "prefix") dismax(
        should(shouldQueryItr).minimumShouldMatch(queryParams.mainMinimumShouldMatch).filter(prefixWithGeo), fallbackQuery)
        .tieBreaker(queryParams.topDisMaxTieBreaker)
      else dismax(
        should(shouldQueryItr).minimumShouldMatch(queryParams.mainMinimumShouldMatch).filter(termWithGeo), fallbackQuery)
        .tieBreaker(queryParams.topDisMaxTieBreaker)


    if (historical) {
      search(hybridIndexHistorical).query(query)
        .sortBy(FieldSortDefinition("_score").order(SortOrder.DESC), FieldSortDefinition("uprn").order(SortOrder.ASC))
        .trackScores(true)
        .searchType(SearchType.DfsQueryThenFetch)
    } else {
      search(hybridIndex).query(query)
        .sortBy(FieldSortDefinition("_score").order(SortOrder.DESC), FieldSortDefinition("uprn").order(SortOrder.ASC))
        .trackScores(true)
        .searchType(SearchType.DfsQueryThenFetch)
    }
  }

  def queryBulk(requestsData: Stream[BulkAddressRequestData], limit: Int, queryParamsConfig: Option[QueryParamsConfig] = None, historical: Boolean = true, matchThreshold: Float): Future[Stream[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]] = {

    val addressRequests = requestsData.map { requestData =>
      val bulkAddressRequest: Future[Seq[AddressBulkResponseAddress]] =
        queryAddresses(requestData.tokens, 0, max(limit,5), "","","50.71","-3.51", queryParamsConfig, historical).map { case HybridAddresses(hybridAddresses, _, _) =>

          // If we didn't find any results for an input, we still need to return
          // something that will indicate an empty result
          val tokens = requestData.tokens
          val emptyBulk = BulkAddress.empty(requestData)
          val emptyScored = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(emptyBulk.hybridAddress)),tokens, 1D)
          val emptyBulkAddress =  AddressBulkResponseAddress.fromBulkAddress(emptyBulk, emptyScored.head, false)
          if (hybridAddresses.isEmpty) Seq(emptyBulkAddress)
          else {
            val bulkAddresses = hybridAddresses.map { hybridAddress =>
              BulkAddress.fromHybridAddress(hybridAddress, requestData)
            }

            val addressResponseAddresses = hybridAddresses.map { hybridAddress =>
              AddressResponseAddress.fromHybridAddress(hybridAddress)
            }

            //  calculate the elastic denominator value which will be used when scoring each address
            val elasticDenominator = Try(ConfidenceScoreHelper.calculateElasticDenominator(addressResponseAddresses.map(_.underlyingScore))).getOrElse(1D)
            // add the Hopper and hybrid scores to the address
           // val matchThreshold = 5
            val threshold = Try((matchThreshold / 100).toDouble).getOrElse(0.05D)
            val scoredAddresses = HopperScoreHelper.getScoresForAddresses(addressResponseAddresses, tokens, elasticDenominator)
            val addressBulkResponseAddresses = (bulkAddresses zip scoredAddresses).map{ case (b, s) =>
                AddressBulkResponseAddress.fromBulkAddress(b, s, false)
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
