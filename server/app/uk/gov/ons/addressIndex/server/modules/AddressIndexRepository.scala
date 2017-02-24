package uk.gov.ons.addressIndex.server.modules

import javax.inject.{Inject, Singleton}

import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import com.google.inject.ImplementedBy
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import org.elasticsearch.common.unit.Fuzziness
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
      bool {
        should(Seq(
          tokens.get(Tokens.buildingNumber).map(token =>
            matchQuery(
              field = "lpi.paoStartNumber",
              value = Try(token.toShort).getOrElse(null)
            ).boost(queryParams.paoStartNumberBuildingNumberLpiBoost)),
          tokens.get(Tokens.paoStartNumber).map(token =>
            matchQuery(
              field = "lpi.paoStartNumber",
              value = token
            ).boost(queryParams.paoStartNumberPaoLpiBoost)),
          tokens.get(Tokens.paoStartSuffix).map(token =>
            matchQuery(
              field = "lpi.paoStartSuffix",
              value = token
            ).boost(queryParams.paoStartSuffixLpiBoost)),
          tokens.get(Tokens.paoEndNumber).map(token =>
            matchQuery(
              field = "lpi.paoEndNumber",
              value = token
            ).boost(queryParams.paoEndNumberLpiBoost)),
          tokens.get(Tokens.locality).map(token =>
            matchQuery(
              field = "lpi.locality",
              value = token
            )),
          tokens.get(Tokens.organisationName).map(token =>
            matchQuery(
              field = "lpi.organisation",
              value = token
            ).boost(queryParams.organisationNameOrganisationLpiBoost)),
          tokens.get(Tokens.organisationName).map(token =>
            matchQuery(
              field = "lpi.legalName",
              value = token
            ).boost(queryParams.organisationNameLegalNameLpiBoost)),
          tokens.get(Tokens.organisationName).map(token =>
            matchQuery(
              field = "lpi.paoText",
              value = token
            ).boost(queryParams.organisationNamePaoTextLpiBoost)),
          tokens.get(Tokens.organisationName).map(token =>
            matchQuery(
              field = "lpi.saoText",
              value = token
            ).boost(queryParams.organisationNameSaoTextLpiBoost)),
          tokens.get(Tokens.subBuildingName).map(token =>
            matchQuery(
              field = "lpi.saoText",
              value = token
            ).boost(queryParams.subBuildingNameLpiBoost)),
          tokens.get(Tokens.buildingName).map(token =>
            matchQuery(
              field = "lpi.paoText",
              value = token
            )),
          tokens.get(Tokens.streetName).map(token =>
            fuzzyQuery(
              name = "lpi.streetDescriptor",
              value = token
            ).boost(queryParams.streetNameLpiBoost).fuzziness(Fuzziness.TWO)),
          tokens.get(Tokens.townName).map(token =>
            matchQuery(
              field = "lpi.townName",
              value = token
            )),
          tokens.get(Tokens.postcode).map(token =>
            matchQuery(
              field = "lpi.postcodeLocator",
              value = token
            )),
          tokens.get(Tokens.buildingNumber).map(token =>
            matchQuery(
              field = "paf.buildingNumber",
              value = token
            ).boost(queryParams.buildingNumberPafBoost)),
          tokens.get(Tokens.organisationName).map(token =>
            matchQuery(
              field = "paf.organizationName",
              value = token
            )),
          tokens.get(Tokens.departmentName).map(token =>
            matchQuery(
              field = "paf.departmentName",
              value = token
            )),
          tokens.get(Tokens.subBuildingName).map(token =>
            matchQuery(
              field = "paf.subBuildingName",
              value = token
            ).boost(queryParams.subBuildingNameSubBuildingPafBoost)),
          tokens.get(Tokens.subBuildingName).map(token =>
            matchQuery(
              field = "paf.buildingName",
              value = token
            ).boost(queryParams.subBuildingNameBuildingPafBoost)),
          tokens.get(Tokens.buildingName).map(token =>
            matchQuery(
              field = "paf.buildingName",
              value = token
            )),
          tokens.get(Tokens.streetName).map(token =>
            fuzzyQuery(
              name = "paf.thoroughfare",
              value = token
            ).boost(queryParams.streetNamePafBoost).fuzziness(Fuzziness.TWO)),
          tokens.get(Tokens.townName).map(token =>
            matchQuery(
              field = "paf.postTown",
              value = token
            )),
          tokens.get(Tokens.postcode).map(token =>
            matchQuery(
              field = "paf.postcode",
              value = token
            )),
          tokens.get(Tokens.locality).map(token =>
            matchQuery(
              field = "paf.dependentLocality",
              value = token
            )),
          Some(
            matchQuery(
              field = "_all",
              value = tokens.map(_.value).mkString(" ")
            ).boost(queryParams.underlineAllBoost)
          )
        ).flatten)
      }.minimumShouldMatch(queryParams.minimumShouldMatch)

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