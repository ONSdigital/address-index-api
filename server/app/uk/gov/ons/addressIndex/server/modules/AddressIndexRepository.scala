package uk.gov.ons.addressIndex.server.modules

import javax.inject.{Inject, Singleton}
import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import com.google.inject.ImplementedBy
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import org.elasticsearch.common.unit.Fuzziness
import play.api.Logger
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.AddressScheme
import uk.gov.ons.addressIndex.model.db.index.HybridIndex
import uk.gov.ons.addressIndex.parsers.Tokens
import uk.gov.ons.addressIndex.server.modules.ElasticDsl._
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[AddressIndexRepository])
trait ElasticSearchRepository {

  /**
    * An ElasticClient.
    */
  def client: ElasticClient

  def logger: Logger

  /**
    * @param tokens
    * @param p
    * @param fmt
    * @return
    */
  def queryAddress(tokens: Seq[CrfTokenResult])
    (implicit p: Pagination, fmt: Option[AddressScheme]): Future[RichSearchResponse] = {
    logExecute("Address")(queryAddressSearchDefinition(tokens))
  }

  /**
    * @param uprn
    * @param fmt
    * @return
    */
  def queryUprn(uprn: String)(implicit fmt: Option[AddressScheme]): Future[RichSearchResponse] = {
    logExecute("UPRN")(queryUprnSearchDefinition(uprn))
  }

  def queryUprnSearchDefinition(uprn: String)(implicit fmt: Option[AddressScheme]): SearchDefinition

  def queryAddressSearchDefinition(tokens: Seq[CrfTokenResult])
    (implicit p: Pagination, fmt: Option[AddressScheme]): SearchDefinition

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
)(implicit ec: ExecutionContext) extends ElasticSearchRepository {

  val logger = Logger("AddressIndexRepository")
  val client: ElasticClient = elasticClientProvider.client

  def queryUprnSearchDefinition(uprn: String)(implicit fmt: Option[AddressScheme]): SearchDefinition = {
    search.in(conf.config.elasticSearch.indexes.hybridIndex).format query {
      bool(
        must(
          matchQuery(
            field = HybridIndex.Fields.uprn,
            value = uprn
          )
        )
      )
    }
  }

  def queryAddressSearchDefinition(tokens: Seq[CrfTokenResult])
    (implicit p: Pagination, fmt: Option[AddressScheme]): SearchDefinition = {
    val tokensMap = tokensToMap(tokens)
    search.in(conf.config.elasticSearch.indexes.hybridIndex).format.paginate query {
      bool {
        should(Seq(
          tokensMap.get(Tokens.buildingNumber).map(token =>
            matchQuery(
              field = "lpi.paoStartNumber",
              value = token
            )),
          tokensMap.get(Tokens.locality).map(token =>
            matchQuery(
              field = "lpi.locality",
              value = token
            )),
          tokensMap.get(Tokens.organisationName).map(token =>
            matchQuery(
              field = "lpi.organisation",
              value = token
            )),
          tokensMap.get(Tokens.organisationName).map(token =>
            matchQuery(
              field = "lpi.legalName",
              value = token
            )),
          tokensMap.get(Tokens.subBuildingName).map(token =>
            matchQuery(
              field = "lpi.saoText",
              value = token
            )),
          tokensMap.get(Tokens.buildingName).map(token =>
            matchQuery(
              field = "lpi.paoText",
              value = token
            )),
          tokensMap.get(Tokens.streetName).map(token =>
            fuzzyQuery(
              name = "lpi.streetDescriptor",
              value = token
            ).fuzziness(Fuzziness.TWO)),
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
            )),
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
            )),
          tokensMap.get(Tokens.buildingName).map(token =>
            matchQuery(
              field = "paf.buildingName",
              value = token
            )),
          tokensMap.get(Tokens.streetName).map(token =>
            fuzzyQuery(
              name = "paf.thoroughfare",
              value = token
            ).fuzziness(Fuzziness.TWO)),
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
          Some(
            matchQuery(
              field = "_all",
              value = tokens.map(_.value).mkString(" ")
            )
          )
        ).flatten)
      }
    }
  }
  def tokensToMap(tokens: Seq[CrfTokenResult]): Map[String, String] = {
    tokens.groupBy(_.label).map {
      case (token, originalInputSeq) => (token, originalInputSeq.map(_.value).mkString(" "))
    }
  }
}