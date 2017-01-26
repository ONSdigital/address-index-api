package uk.gov.ons.addressIndex.server.modules

import javax.inject.{Inject, Singleton}
import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import com.google.inject.ImplementedBy
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import play.api.Logger
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.{AddressScheme, BritishStandard7666, PostcodeAddressFile}
import uk.gov.ons.addressIndex.model.db.index.HybridIndex
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
    search.in(conf.config.elasticSearch.indexes.hybridIndex).format.paginate query {
      query(tokens.map(_.value).mkString(" "))
    }
  }
}