package uk.gov.ons.addressIndex.server.modules

import javax.inject.{Inject, Singleton}

import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import com.google.inject.ImplementedBy
import com.sksamuel.elastic4s.http.ElasticDsl._
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
import uk.gov.ons.addressIndex.parsers.Tokens

import scala.concurrent.{ExecutionContext, Future}

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
  def queryUprn(uprn: String): Future[Option[HybridAddress]]

  /**
    * Query the address index for addresses.
    *
    * @param start  the offset for the query
    * @param limit  maximum number of returned results
    * @param tokens address tokens
    * @return Future with found addresses and the maximum score
    */
  def queryAddresses(tokens: Map[String, String], start: Int, limit: Int, queryParamsConfig: Option[QueryParamsConfig] = None): Future[HybridAddresses]

  /**
    * Generates request to get address from ES by UPRN
    * Public so that it could be accessible to controllers for User's debug purposes
    *
    * @param tokens tokens for the ES query
    * @return Search definition containing query to the ES
    */
  def generateQueryAddressRequest(tokens: Map[String, String], queryParamsConfig: Option[QueryParamsConfig] = None): SearchDefinition

  /**
    * Query ES using MultiSearch endpoint
    *
    * @param requestsData data that will be used in the multi search request
    * @param limit        how many addresses to take per a request
    * @return a stream of `Either`, `Right` will contain resulting bulk address,
    *         `Left` will contain request data that is to be re-send
    */
  def queryBulk(requestsData: Stream[BulkAddressRequestData], limit: Int, queryParamsConfig: Option[QueryParamsConfig] = None): Future[Stream[Either[BulkAddressRequestData, Seq[BulkAddress]]]]
}

@Singleton
class AddressIndexRepository @Inject()(
  conf: AddressIndexConfigModule,
  elasticClientProvider: ElasticClientProvider
)(implicit ec: ExecutionContext) extends ElasticsearchRepository {

  private val esConf = conf.config.elasticSearch
  private val hybridIndex = esConf.indexes.hybridIndex + "/" + esConf.indexes.hybridMapping

  val client: HttpClient = elasticClientProvider.client
  val logger = Logger("AddressIndexRepository")

  def queryHealth(): Future[String] = client.execute(clusterHealth()).map(_.toString)

  def queryUprn(uprn: String): Future[Option[HybridAddress]] = {

    val request = generateQueryUprnRequest(uprn)

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
  def generateQueryUprnRequest(uprn: String): SearchDefinition = search(hybridIndex).query {
    termQuery("uprn", uprn)
  }

  def queryAddresses(tokens: Map[String, String], start: Int, limit: Int, queryParamsConfig: Option[QueryParamsConfig] = None): Future[HybridAddresses] = {

    val request = generateQueryAddressRequest(tokens, queryParamsConfig).start(start).limit(limit)

    logger.trace(request.toString)

    client.execute(request).map(HybridAddresses.fromEither)
  }

  def generateQueryAddressRequest(tokens: Map[String, String], queryParamsConfig: Option[QueryParamsConfig] = None): SearchDefinition = {

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

    val fallbackQuery =
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
      else dismax(
        should(shouldQueryItr).minimumShouldMatch(queryParams.mainMinimumShouldMatch), fallbackQuery)
        .tieBreaker(queryParams.topDisMaxTieBreaker)
      

    search(hybridIndex).query(query)
      .sortBy(FieldSortDefinition("_score").order(SortOrder.DESC), FieldSortDefinition("uprn").order(SortOrder.ASC))
      .trackScores(true)
      .searchType(SearchType.DfsQueryThenFetch)

  }

  def queryBulk(requestsData: Stream[BulkAddressRequestData], limit: Int, queryParamsConfig: Option[QueryParamsConfig] = None): Future[Stream[Either[BulkAddressRequestData, Seq[BulkAddress]]]] = {

    val addressRequests = requestsData.map { requestData =>
      val bulkAddressRequest: Future[Seq[BulkAddress]] =
        queryAddresses(requestData.tokens, 0, limit, queryParamsConfig).map { case HybridAddresses(hybridAddresses, _, _) =>

          // If we didn't find any results for an input, we still need to return
          // something that will indicate an empty result
          if (hybridAddresses.isEmpty) Seq(BulkAddress.empty(requestData))
          else hybridAddresses.map { hybridAddress =>
            BulkAddress.fromHybridAddress(hybridAddress, requestData)
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
