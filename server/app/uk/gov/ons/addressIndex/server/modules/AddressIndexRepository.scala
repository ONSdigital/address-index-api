package uk.gov.ons.addressIndex.server.modules

import java.util.concurrent.{ArrayBlockingQueue, ThreadPoolExecutor, TimeUnit}
import javax.inject.{Inject, Singleton}

import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import com.google.inject.ImplementedBy
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import org.elasticsearch.common.unit.Fuzziness
import play.api.Logger
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.db.index._
import uk.gov.ons.addressIndex.parsers.Tokens

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[AddressIndexRepository])
trait ElasticsearchRepository {

  /**
    * An ElasticClient.
    */
  def client: ElasticClient

  def logger: Logger

  /**
    * @param tokens
    * @return
    */
  def queryAddresses(start: Int, limit: Int, tokens: Seq[CrfTokenResult]): Future[HybridAddresses]

  def queryUprn(uprn: String): Future[Option[HybridAddress]]

  def generateQueryUprnRequest(uprn: String): SearchDefinition

  def generateQueryAddressRequest(tokens: Seq[CrfTokenResult]): SearchDefinition

  /**
    * Helper which logs an ElasticSearch Query before executing it.
    *
    * @param info
    * @param t
    * @param executable
    * @tparam T
    * @tparam R
    * @tparam Q
    * @return
    */
  def logExecute[T, R, Q](info: String)(t: T)(implicit executable: Executable[T, R, Q]): Future[Q] = {

    logger info s"$info: ${t.toString}"

    client execute t
  }
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

    client.execute(request).map(_.as[HybridAddress].headOption)
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

  def queryAddresses(start: Int, limit: Int, tokens: Seq[CrfTokenResult]): Future[HybridAddresses] = {

    val request = generateQueryAddressRequest(tokens).start(start).limit(limit)

    logger.trace(request.toString)

    client.execute(request).map { response =>
      val total = response.totalHits
      // if the query doesn't find anything, the score is `Nan` that messes up with Json converter
      val maxScore = if (total == 0) 0 else response.maxScore

      HybridAddresses(
        addresses = response.as[HybridAddress],
        maxScore = maxScore,
        total = total
      )
    }
  }

  /**
    * Generates request to get address from ES by UPRN
    * Public for tests
    * @param tokens tokens for the ES query
    * @return Search definition containing query to the ES
    */
  def generateQueryAddressRequest(tokens: Seq[CrfTokenResult]): SearchDefinition = {

    val tokensMap = Tokens.tokensToValidMap(tokens)

    val query =
      bool {
        should(Seq(
          tokensMap.get(Tokens.buildingNumber).map(token =>
            matchQuery(
              field = "lpi.paoStartNumber",
              value = token
            ).boost(queryParams.buildingNumberLpiBoost)),
          tokensMap.get(Tokens.locality).map(token =>
            matchQuery(
              field = "lpi.locality",
              value = token
            )),
          tokensMap.get(Tokens.organisationName).map(token =>
            matchQuery(
              field = "lpi.organisation",
              value = token
            ).boost(queryParams.organisationNameOrganisationLpiBoost)),
          tokensMap.get(Tokens.organisationName).map(token =>
            matchQuery(
              field = "lpi.legalName",
              value = token
            ).boost(queryParams.organisationNameLegalNameLpiBoost)),
          tokensMap.get(Tokens.organisationName).map(token =>
            matchQuery(
              field = "lpi.paoText",
              value = token
            ).boost(queryParams.organisationNamePaoTextLpiBoost)),
          tokensMap.get(Tokens.organisationName).map(token =>
            matchQuery(
              field = "lpi.saoText",
              value = token
            ).boost(queryParams.organisationNameSaoTextLpiBoost)),
          tokensMap.get(Tokens.subBuildingName).map(token =>
            matchQuery(
              field = "lpi.saoText",
              value = token
            ).boost(queryParams.subBuildingNameLpiBoost)),
          tokensMap.get(Tokens.buildingName).map(token =>
            matchQuery(
              field = "lpi.paoText",
              value = token
            )),
          tokensMap.get(Tokens.streetName).map(token =>
            fuzzyQuery(
              name = "lpi.streetDescriptor",
              value = token
            ).boost(queryParams.streetNameLpiBoost).fuzziness(Fuzziness.TWO)),
          tokensMap.get(Tokens.townName).map(token =>
            matchQuery(
              field = "lpi.townName",
              value = token
            )),
          tokensMap.get(Tokens.postcode).map(token =>
            matchQuery(
              field = "lpi.postcodeLocator",
              value = token
            )),
          tokensMap.get(Tokens.buildingNumber).map(token =>
            matchQuery(
              field = "paf.buildingNumber",
              value = token
            ).boost(queryParams.buildingNumberPafBoost)),
          tokensMap.get(Tokens.organisationName).map(token =>
            matchQuery(
              field = "paf.organizationName",
              value = token
            )),
          tokensMap.get(Tokens.departmentName).map(token =>
            matchQuery(
              field = "paf.departmentName",
              value = token
            )),
          tokensMap.get(Tokens.subBuildingName).map(token =>
            matchQuery(
              field = "paf.subBuildingName",
              value = token
            ).boost(queryParams.subBuildingNameSubBuildingPafBoost)),
          tokensMap.get(Tokens.subBuildingName).map(token =>
            matchQuery(
              field = "paf.buildingName",
              value = token
            ).boost(queryParams.subBuildingNameBuildingPafBoost)),
          tokensMap.get(Tokens.buildingName).map(token =>
            matchQuery(
              field = "paf.buildingName",
              value = token
            )),
          tokensMap.get(Tokens.streetName).map(token =>
            fuzzyQuery(
              name = "paf.thoroughfare",
              value = token
            ).boost(queryParams.streetNamePafBoost).fuzziness(Fuzziness.TWO)),
          tokensMap.get(Tokens.townName).map(token =>
            matchQuery(
              field = "paf.postTown",
              value = token
            )),
          tokensMap.get(Tokens.postcode).map(token =>
            matchQuery(
              field = "paf.postcode",
              value = token
            )),
          tokensMap.get(Tokens.locality).map(token =>
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

}