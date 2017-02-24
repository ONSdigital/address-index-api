package uk.gov.ons.addressIndex.server.modules

import javax.inject.{Inject, Singleton}

import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import com.google.inject.ImplementedBy
import com.sksamuel.elastic4s.ElasticDsl.{bool, must, should, _}
import com.sksamuel.elastic4s._
import org.elasticsearch.common.unit.Fuzziness
import play.api.Logger
import uk.gov.ons.addressIndex.model.db.{BulkAddress, BulkAddressRequestData}
import uk.gov.ons.addressIndex.model.db.index._
import uk.gov.ons.addressIndex.parsers.Tokens

import scala.concurrent.{ExecutionContext, Future}

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
    * @param start the offset for the query
    * @param limit maximum number of returned results
    * @param tokens address tokens
    * @return Future with found addresses and the maximum score
    */
  def queryAddresses(start: Int, limit: Int, tokens: Map[String, String]): Future[HybridAddresses]

  /**
    * Query ES using MultiSearch endpoint
    * @param requestsData data that will be used in the multi search request
    * @param limit how many addresses to take per a request
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
  private val queryParams =conf.config.elasticSearch.queryParams

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
    * @param uprn the uprn of the fetched address
    * @return Seqrch definition containing query to the ES
    */
  def generateQueryUprnRequest(uprn: String): SearchDefinition = search.in(hybridIndex).query {
    termQuery("uprn", uprn)
  }

  def queryAddresses(start: Int, limit: Int, tokens: Map[String, String]): Future[HybridAddresses] = {

    val request = generateQueryAddressRequest(tokens).start(start).limit(limit)

    logger.trace(request.toString)

    client.execute(request).map(HybridAddresses.fromRichSearchResponse)
  }

  /**
    * Generates request to get address from ES by UPRN
    * Public for tests
    * @param tokens tokens for the ES query
    * @return Search definition containing query to the ES
    */
  def generateQueryAddressRequest(tokens: Map[String, String]): SearchDefinition = {

      val query =
        bool(
          must(
            Seq(
              bool(
                must(
                Seq(
                  tokens.get(Tokens.saoStartNumber).map(token =>
                  matchQuery(
                    field = "lpi.saoStartNumber",
                    value = token
                  ).boost(queryParams.subBuildingNameLpiSaoStartNumberBoost)),
                  tokens.get(Tokens.saoStartSuffix).map(token =>
                    matchQuery(
                      field = "lpi.saoStartSuffix",
                      value = token
                    ).boost(queryParams.subBuildingNameLpiSaoStartSuffixBoost)),
                  tokens.get(Tokens.saoEndNumber).map(token =>
                    matchQuery(
                      field = "lpi.saoEndNumber",
                      value = token
                    ).boost(queryParams.subBuildingNameLpiSaoEndNumberBoost)),
                  tokens.get(Tokens.saoEndSuffix).map(token =>
                    matchQuery(
                      field = "lpi.saoEndSuffix",
                      value = token
                    ).boost(queryParams.subBuildingNameLpiSaoEndSuffixBoost))
                ).flatten
                ).should(
                Seq(
                  tokens.get(Tokens.subBuildingName).map(token =>
                    matchQuery(
                      field = "paf.subBuildingName",
                      value = token
                    ).boost(queryParams.subBuildingNamePafSubBuildingNameBoost)),
                  tokens.get(Tokens.subBuildingName).map(token =>
                    matchQuery(
                      field = "lpi.saoText",
                      value = token
                    ).boost(queryParams.subBuildingNameLpiSaoTextBoost))
                ).flatten
                )),
              bool(
                must(
                  Seq(
                    tokens.get(Tokens.paoStartNumber).map(token =>
                      matchQuery(
                        field = "lpi.paoStartNumber",
                        value = token
                      ).boost(queryParams.buildingNameLpiPaoStartNumberBoost)),
                    tokens.get(Tokens.saoStartSuffix).map(token =>
                      matchQuery(
                        field = "lpi.paoStartSuffix",
                        value = token
                      ).boost(queryParams.buildingNameLpiPaoStartSuffixBoost)),
                    tokens.get(Tokens.paoEndNumber).map(token =>
                      matchQuery(
                        field = "lpi.paoEndNumber",
                        value = token
                      ).boost(queryParams.buildingNameLpiPaoEndNumberBoost)),
                    tokens.get(Tokens.paoEndSuffix).map(token =>
                      matchQuery(
                        field = "lpi.paoEndSuffix",
                        value = token
                      ).boost(queryParams.buildingNameLpiPaoEndSuffixBoost))
                  ).flatten
                ).should(
                  Seq(
                    tokens.get(Tokens.buildingName).map(token =>
                      matchQuery(
                        field = "paf.buildingName",
                        value = token
                      ).boost(queryParams.buildingNamePafSubBuildingNameBoost)),
                    tokens.get(Tokens.buildingName).map(token =>
                      matchQuery(
                        field = "lpi.paoText",
                        value = token
                      ).boost(queryParams.buildingNameLpiPaoTextBoost))
                  ).flatten
                )),
              bool(should(
                Seq(
                  tokens.get(Tokens.buildingNumber).map(token =>
                    matchQuery(
                      field = "paf.buildingNumber",
                      value = token
                    ).boost(queryParams.buildingNumberPafBuildingNumberBoost)),
                  tokens.get(Tokens.buildingNumber).map(token =>
                    matchQuery(
                      field = "lpi.paoStartNumber",
                      value = token
                    ).boost(queryParams.buildingNumberLpiPaoStartNumberBoost))
                ).flatten
              )),
              bool(should(
                Seq(
                  tokens.get(Tokens.streetName).map(token =>
                    matchQuery(
                      field = "paf.thoroughfare",
                      value = token
                    ).boost(queryParams.streetNamePafThoroughfareBoost)),
                  tokens.get(Tokens.streetName).map(token =>
                    matchQuery(
                      field = "paf.welshThoroughfare",
                      value = token
                    ).boost(queryParams.streetNamePafWelshThoroughfareBoost)),
                  tokens.get(Tokens.streetName).map(token =>
                    matchQuery(
                      field = "paf.dependentThoroughfare",
                      value = token
                    ).boost(queryParams.streetNamePafDependentThoroughfareBoost)),
                  tokens.get(Tokens.streetName).map(token =>
                    matchQuery(
                      field = "paf.welshDependentThoroughfare",
                      value = token
                    ).boost(queryParams.streetNamePafWelshDependentThoroughfareBoost)),
                  tokens.get(Tokens.streetName).map(token =>
                    matchQuery(
                      field = "lpi.streetDescriptor",
                      value = token
                    ).boost(queryParams.streetNameLpiStreetDescriptorBoost))
                ).flatten
              )),
              bool(should(
                Seq(
                  tokens.get(Tokens.townName).map(token =>
                    matchQuery(
                      field = "paf.postTown",
                      value = token
                    ).boost(queryParams.townNamePafPostTownBoost)),
                  tokens.get(Tokens.townName).map(token =>
                    matchQuery(
                      field = "paf.welshPostTown",
                      value = token
                    ).boost(queryParams.townNamePafWelshPostTownBoost)),
                  tokens.get(Tokens.townName).map(token =>
                    matchQuery(
                      field = "lpi.townName",
                      value = token
                    ).boost(queryParams.townNameLpiTownNameBoost)),
                  tokens.get(Tokens.townName).map(token =>
                    matchQuery(
                      field = "paf.dependentLocality",
                      value = token
                    ).boost(queryParams.townNamePafDependentLocalityBoost)),
                  tokens.get(Tokens.townName).map(token =>
                    matchQuery(
                      field = "paf.welshDependentLocality",
                      value = token
                    ).boost(queryParams.townNamePafWelshDependentLocalityBoost)),
                  tokens.get(Tokens.townName).map(token =>
                    matchQuery(
                      field = "lpi.locality",
                      value = token
                    ).boost(queryParams.townNameLpiLocalityBoost)),
                  tokens.get(Tokens.townName).map(token =>
                    matchQuery(
                      field = "paf.doubleDependentLocality",
                      value = token
                    ).boost(queryParams.townNamePafDoubleDependentLocalityBoost)),
                  tokens.get(Tokens.townName).map(token =>
                    matchQuery(
                      field = "paf.doubleDependentLocality",
                      value = token
                    ).boost(queryParams.townNamePafWelshDoubleDependentLocalityBoost))
                ).flatten
              )),
              bool(should(
                Seq(
                  tokens.get(Tokens.postcode).map(token =>
                    matchQuery(
                      field = "paf.postcode",
                      value = token
                    ).boost(queryParams.postcodePafPostcodeBoost)),
  //                tokens.get(Tokens.postcodeOut).map(token =>
  //                  matchQuery(
  //                    field = "lpi.paoStartNumber",
  //                    value = token
  //                  ).boost(queryParams.paoStartNumberBuildingNumberLpiBoost)),
  //                tokens.get(Tokens.postcodeIn).map(token =>
  //                  matchQuery(
  //                    field = "lpi.paoStartNumber",
  //                    value = token
  //                  ).boost(queryParams.paoStartNumberBuildingNumberLpiBoost)),
                  tokens.get(Tokens.postcode).map(token =>
                    matchQuery(
                      field = "lpi.postcodeLocator",
                      value = token
                    ).boost(queryParams.postcodeLpiPostcodeLocatorBoost))
                ).flatten
              ))
            )
          ).
          should(Seq(
            bool(
              should(
                Seq(
                  tokens.get(Tokens.organisationName).map(token =>
                    matchQuery(
                      field = "paf.organizationName",
                      value = token
                    ).boost(queryParams.origanisationNamePafOrganisationNameBoost)),
                  tokens.get(Tokens.organisationName).map(token =>
                    matchQuery(
                      field = "lpi.organisation",
                      value = token
                    ).boost(queryParams.origanisationNameLpiOrganisationBoost)),
                  tokens.get(Tokens.organisationName).map(token =>
                    matchQuery(
                      field = "lpi.paoText",
                      value = token
                    ).boost(queryParams.origanisationNameLpiPaoTextBoost)),
                  tokens.get(Tokens.organisationName).map(token =>
                    matchQuery(
                      field = "lpi.legalName",
                      value = token
                    ).boost(queryParams.origanisationNameLpiLegalNameBoost)),
                  tokens.get(Tokens.organisationName).map(token =>
                    matchQuery(
                      field = "lpi.saoText",
                      value = token
                    ).boost(queryParams.origanisationNameLpiSaoTextBoost))
              ).flatten
              )),
            bool(should(
              Seq(
                tokens.get(Tokens.departmentName).map(token =>
                  matchQuery(
                    field = "paf.departmentName",
                    value = token
                  ).boost(queryParams.departmentNamePafDepartmentNameBoost)),
                tokens.get(Tokens.departmentName).map(token =>
                  matchQuery(
                    field = "lpi.legalName",
                    value = token
                  ).boost(queryParams.departmentNameLpiLegalNameBoost))
              ).flatten
            )),
            bool(should(
              Seq(
                tokens.get(Tokens.locality).map(token =>
                  matchQuery(
                    field = "paf.dependentLocality",
                    value = token
                  ).boost(queryParams.localityPafDependentLocalityBoost)),
                tokens.get(Tokens.locality).map(token =>
                  matchQuery(
                    field = "paf.welshDependentLocality",
                    value = token
                  ).boost(queryParams.localityPafWelshDependentLocalityBoost)),
                tokens.get(Tokens.locality).map(token =>
                  matchQuery(
                    field = "lpi.locality",
                    value = token
                  ).boost(queryParams.localityLpiLocalityBoost)),
                tokens.get(Tokens.locality).map(token =>
                  matchQuery(
                    field = "paf.doubleDependentLocality",
                    value = token
                  ).boost(queryParams.localityPafDoubleDependentLocalityBoost)),
                tokens.get(Tokens.locality).map(token =>
                  matchQuery(
                    field = "paf.welshDoubleDependentLocality",
                    value = token
                  ).boost(queryParams.localityPafWelshDoubleDependentLocalityBoost))
              ).flatten
            ))
          )).
          not()
        )

    logger.info(query.toString)

    search.in(hybridIndex).query(query).searchType(SearchType.DfsQueryThenFetch)

  }

  def queryBulk(requestsData: Stream[BulkAddressRequestData], limit: Int): Future[Stream[Either[BulkAddressRequestData, Seq[BulkAddress]]]] = {

    val addressRequests = requestsData.map { requestData =>
      val bulkAddressRequest: Future[Seq[BulkAddress]] =
        queryAddresses(0, limit, requestData.tokens).map { case HybridAddresses(hybridAddresses, _, _) =>

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