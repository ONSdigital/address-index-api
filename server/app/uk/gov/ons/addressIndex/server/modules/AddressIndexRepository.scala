package uk.gov.ons.addressIndex.server.modules

import javax.inject.{Inject, Singleton}

import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import com.google.inject.ImplementedBy
import com.sksamuel.elastic4s.ElasticDsl.{must, should, _}
import com.sksamuel.elastic4s._
import play.api.Logger
import uk.gov.ons.addressIndex.model.db.{BulkAddress, BulkAddressRequestData}
import uk.gov.ons.addressIndex.model.db.index._
import uk.gov.ons.addressIndex.parsers.Tokens

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@ImplementedBy(classOf[AddressIndexRepository])
trait ElasticsearchRepository {

  /**
    * An ElasticClient.
    */
  def client: ElasticClient

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
    * @param originalInput original input, used against pafAll and nagAll fields
    * @return Future with found addresses and the maximum score
    */
  def queryAddresses(start: Int, limit: Int, tokens: Map[String, String], originalInput: String): Future[HybridAddresses]

  /**
    * Query ES using MultiSearch endpoint
    *
    * @param requestsData data that will be used in the multi search request
    * @param limit        how many addresses to take per a request
    * @return a stream of `Either`, `Right` will contain resulting bulk address,
    *         `Left` will contain request data that is to be re-send
    */
  def queryBulk(requestsData: Stream[BulkAddressRequestData], limit: Int): Future[Stream[Either[BulkAddressRequestData, Seq[BulkAddress]]]]
}

@Singleton
class AddressIndexRepository @Inject()(
  conf: AddressIndexConfigModule,
  elasticClientProvider: ElasticClientProvider
)(implicit ec: ExecutionContext) extends ElasticsearchRepository {

  private val esConf = conf.config.elasticSearch
  private val hybridIndex = esConf.indexes.hybridIndex
  private val queryParams = conf.config.elasticSearch.queryParams

  val client: ElasticClient = elasticClientProvider.client
  val logger = Logger("AddressIndexRepository")

  def queryUprn(uprn: String): Future[Option[HybridAddress]] = {

    val request = generateQueryUprnRequest(uprn)

    logger.trace(request.toString)

    client.execute(request)
      .map(HybridAddresses.fromRichSearchResponse)
      .map(_.addresses.headOption)
  }

  /**
    * Generates request to get address from ES by UPRN
    * Public for tests
    *
    * @param uprn the uprn of the fetched address
    * @return Seqrch definition containing query to the ES
    */
  def generateQueryUprnRequest(uprn: String): SearchDefinition = search.in(hybridIndex).query {
    termQuery("uprn", uprn)
  }

  def queryAddresses(start: Int, limit: Int, tokens: Map[String, String], originalInput: String): Future[HybridAddresses] = {

    val request = generateQueryAddressRequest(tokens, originalInput).start(start).limit(limit)

    logger.trace(request.toString)

    client.execute(request).map(HybridAddresses.fromRichSearchResponse)
  }

  /**
    * Generates request to get address from ES by UPRN
    * Public for tests
    *
    * @param tokens tokens for the ES query
    * @return Search definition containing query to the ES
    */
  def generateQueryAddressRequest(tokens: Map[String, String], originalInput: String): SearchDefinition = {

    val defaultFuzziness = "1"

    val saoQuery = Seq(
      tokens.get(Tokens.saoStartNumber).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.saoStartNumber",
          value = token
        )).boost(queryParams.subBuildingName.lpiSaoStartNumberBoost)),
      tokens.get(Tokens.saoStartSuffix).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.saoStartSuffix",
          value = token
        )).boost(queryParams.subBuildingName.lpiSaoStartSuffixBoost)),
      tokens.get(Tokens.saoEndNumber).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.saoEndNumber",
          value = token
        )).boost(queryParams.subBuildingName.lpiSaoEndNumberBoost)),
      tokens.get(Tokens.saoEndSuffix).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.saoEndSuffix",
          value = token
        )).boost(queryParams.subBuildingName.lpiSaoEndSuffixBoost))
    ).flatten

    val subBuildingNameQuery = Seq(
      tokens.get(Tokens.subBuildingName).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.subBuildingName",
          value = token
        )).boost(queryParams.subBuildingName.pafSubBuildingNameBoost)),
      tokens.get(Tokens.subBuildingName).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.saoText",
          value = token
        )).boost(queryParams.subBuildingName.lpiSaoTextBoost)),
      tokens.get(Tokens.saoStartSuffix).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.saoStartSuffix",
          value = token
        )).boost(queryParams.subBuildingName.lpiSaoStartSuffixBoost))
    ).flatten

    val paoQuery = Seq(
      tokens.get(Tokens.paoStartNumber).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.paoStartNumber",
          value = token
        )).boost(queryParams.buildingName.lpiPaoStartNumberBoost)),
      tokens.get(Tokens.paoStartSuffix).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.paoStartSuffix",
          value = token
        )).boost(queryParams.buildingName.lpiPaoStartSuffixBoost)),
      tokens.get(Tokens.paoEndNumber).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.paoEndNumber",
          value = token
        )).boost(queryParams.buildingName.lpiPaoEndNumberBoost)),
      tokens.get(Tokens.paoEndSuffix).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.paoEndSuffix",
          value = token
        )).boost(queryParams.buildingName.lpiPaoEndSuffixBoost))
    ).flatten

    val buildingNameQuery = Seq(
      tokens.get(Tokens.buildingName).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.buildingName",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.buildingName.pafBuildingNameBoost)),
      tokens.get(Tokens.buildingName).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.paoText",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.buildingName.lpiPaoTextBoost)),
      tokens.get(Tokens.paoStartSuffix).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.paoStartSuffix",
          value = token
        ).fuzziness(defaultFuzziness)).boost(queryParams.buildingName.lpiPaoStartSuffixBoost))
    ).flatten


    val buildingNumberQuery = Seq(
      tokens.get(Tokens.buildingNumber).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.buildingNumber",
          value = token
        )).boost(queryParams.buildingNumber.pafBuildingNumberBoost)),
      tokens.get(Tokens.buildingNumber).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.paoStartNumber",
          value = token
        )).boost(queryParams.buildingNumber.lpiPaoStartNumberBoost))
    ).flatten

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
        )).boost(queryParams.postcode.lpiPostcodeLocatorBoost))
    ).flatten

    val postcodeInOutQuery = Seq(
      tokens.get(Tokens.postcodeOut).map(token =>
        constantScoreQuery(matchQuery(
          field = "postcodeOut",
          value = token
        )).boost(queryParams.postcode.postcodeOutBoost)),
      tokens.get(Tokens.postcodeIn).map(token =>
        constantScoreQuery(matchQuery(
          field = "postcodeIn",
          value = token
        )).boost(queryParams.postcode.postcodeInBoost))
    ).flatten

    val organisationNameQuery = Seq(
      tokens.get(Tokens.organisationName).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.organizationName",
          value = token
        )).boost(queryParams.organisationName.pafOrganisationNameBoost)),
      tokens.get(Tokens.organisationName).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.organisation",
          value = token
        )).boost(queryParams.organisationName.lpiOrganisationBoost)),
      tokens.get(Tokens.organisationName).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.paoText",
          value = token
        )).boost(queryParams.organisationName.lpiPaoTextBoost)),
      tokens.get(Tokens.organisationName).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.legalName",
          value = token
        )).boost(queryParams.organisationName.lpiLegalNameBoost)),
      tokens.get(Tokens.organisationName).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.saoText",
          value = token
        )).boost(queryParams.organisationName.lpiSaoTextBoost))
    ).flatten

    val departmentNameQuery = Seq(
      tokens.get(Tokens.departmentName).map(token =>
        constantScoreQuery(matchQuery(
          field = "paf.departmentName",
          value = token
        )).boost(queryParams.departmentName.pafDepartmentNameBoost)),
      tokens.get(Tokens.departmentName).map(token =>
        constantScoreQuery(matchQuery(
          field = "lpi.legalName",
          value = token
        )).boost(queryParams.departmentName.lpiLegalNameBoost))
    ).flatten

    val localityQuery = Seq(
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

    val allQuery = Seq(
      matchQuery(
        field = "paf.pafAll",
        value = originalInput.toUpperCase
      ).boost(queryParams.pafAllBoost),
      matchQuery(
        field = "lpi.nagAll",
        value = originalInput.toUpperCase
      ).boost(queryParams.nagAllBoost)
    )

    val preciseQuery = Seq(
      buildingNumberQuery,
      buildingNameQuery,
      subBuildingNameQuery,
      streetNameQuery,
      townNameQuery,
      postcodeQuery,
      postcodeInOutQuery,
      paoQuery,
      saoQuery,
      organisationNameQuery,
      departmentNameQuery,
      localityQuery
    )
      .filter(_.nonEmpty)
      // `dismax` dsl does not exist, `: _*` means that we provide a list (`queries`) as arguments (args) for the function
      .map(queries => dismax.query(queries: _*).tieBreaker(queryParams.disMaxTieBreaker))

    val fallbackQuery = should(allQuery)

    val query =
      if (preciseQuery.isEmpty) fallbackQuery
      else should(
        Seq(should(preciseQuery), fallbackQuery)
      )

    search.in(hybridIndex).query(query).searchType(SearchType.DfsQueryThenFetch)

  }

  def queryBulk(requestsData: Stream[BulkAddressRequestData], limit: Int): Future[Stream[Either[BulkAddressRequestData, Seq[BulkAddress]]]] = {

    val addressRequests = requestsData.map { requestData =>
      val bulkAddressRequest: Future[Seq[BulkAddress]] =
        queryAddresses(0, limit, requestData.tokens, requestData.inputAddress).map { case HybridAddresses(hybridAddresses, _, _) =>

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
          logger.info(s"#bulk query: rejected request to ES (this might be an indicator of low resource) for id $id: ${exception.getMessage}")
          Left(requestData.copy(lastFailExceptionMessage = exception.getMessage))
      }
    }

    Future.sequence(addressRequests)
  }

}