package uk.gov.ons.addressIndex.server.modules

import javax.inject.{Inject, Singleton}
import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import com.google.inject.ImplementedBy
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import play.api.Logger
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.AddressScheme
import uk.gov.ons.addressIndex.model.db.index.HybridIndex
import uk.gov.ons.addressIndex.server.modules.Model._
import scala.concurrent.{ExecutionContext, Future}

object Model {
  case class Pagination(offset: Int, limit: Int)
  implicit class AutoPaginate(searchDefinition: SearchDefinition) {
    def paginate(implicit p: Pagination): SearchDefinition = {
      searchDefinition start p.offset limit p.limit
    }
  }

  implicit class AutoSource(searchDefinition: SearchDefinition) {
    def format(implicit optFmt: Option[AddressScheme]) = {
      optFmt map { fmt =>
        searchDefinition sourceInclude fmt.toString
      } getOrElse searchDefinition
    }
  }
}

@ImplementedBy(classOf[AddressIndexRepository])
trait ElasticSearchRepository {
  /**
    * An ElasticClient.
    */
  def client(): ElasticClient

  /**
    *
    * @param tokens
    * @param p
    * @param fmt
    * @return
    */
  def queryAddress(tokens: Seq[CrfTokenResult])
    (implicit p: Pagination, fmt: Option[AddressScheme]): Future[RichSearchResponse]

  /**
    *
    * @param uprn
    * @param fmt
    * @return
    */
  def queryUprn(uprn: String)(implicit fmt: Option[AddressScheme]): Future[RichSearchResponse]
}

@Singleton
class AddressIndexRepository @Inject()(
  conf: AddressIndexConfigModule,
  elasticClientProvider: ElasticClientProvider
)(implicit ec: ExecutionContext) extends ElasticSearchRepository {

  private val logger = Logger("AddressIndexRepository")
  val client: ElasticClient = elasticClientProvider.client

  override def queryUprn(uprn: String)(implicit fmt: Option[AddressScheme]): Future[RichSearchResponse] = {
    logExecute("UPRN") {
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
  }

  override def queryAddress(tokens: Seq[CrfTokenResult])
    (implicit p: Pagination, fmt: Option[AddressScheme]): Future[RichSearchResponse] = {
    logExecute("Address") {
      search.in(conf.config.elasticSearch.indexes.hybridIndex).format.paginate query {
        query(tokens.map(_.value).mkString(" "))
      }
    }
  }

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
  private def logExecute[T, R, Q](info: String)(t: T)(implicit executable: Executable[T, R, Q]): Future[Q] = {

    logger info s"$info: ${t.toString}"

    client execute t
  }
}