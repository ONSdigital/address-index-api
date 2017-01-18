package uk.gov.ons.addressIndex.server.modules

import javax.inject.{Inject, Singleton}
import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import com.google.inject.ImplementedBy
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import play.api.Logger
import uk.gov.ons.addressIndex.crfscala.CrfScala.{CrfTokenResult, Input}
import uk.gov.ons.addressIndex.parsers.Tokens.Token
import uk.gov.ons.addressIndex.server.modules.Model._
import scala.concurrent.{ExecutionContext, Future}

object Model {
  case class Pagination(offset: Int, limit: Int)
  implicit class AutoPaginate(searchDefinition: SearchDefinition) {
    def paginate(implicit p: Pagination): SearchDefinition = searchDefinition start p.offset limit p.limit
  }
}

@ImplementedBy(classOf[AddressIndexRepository])
trait ElasticsearchRepository {
  /**
    * An ElasticClient.
    */
  def client(): ElasticClient

  def queryAddress(tokens: Seq[CrfTokenResult])(implicit p: Pagination): Future[RichSearchResponse]
}

@Singleton
class AddressIndexRepository @Inject()(
  conf: AddressIndexConfigModule,
  elasticClientProvider: ElasticClientProvider
)(implicit ec: ExecutionContext) extends ElasticsearchRepository {

  private val esConf = conf.config.elasticSearch
  private val pafIndex = esConf.indexes.pafIndex
  private val nagIndex = esConf.indexes.nagIndex
  private val hybridIndex = esConf.indexes.hybridIndex
  private val logger = Logger("AddressIndexRepository")
  val client: ElasticClient = elasticClientProvider.client

  private def getTokenValue(token: Token, tokens: Seq[CrfTokenResult]): String = {
    tokens.filter(_.label == token).map(_.value).mkString(" ")
  }

  private def collapseTokens(tokens: Seq[CrfTokenResult]): Map[Token, Input] = {
    tokens.groupBy(_.label).map(e => e._1 -> e._2.map(_.value).mkString(" "))
  }

  def tokensToMatchQueries(tokens: Seq[CrfTokenResult], tokenFieldMap: Map[Token, String]): Iterable[QueryDefinition] = {
    collapseTokens(tokens) flatMap { case (tkn, input) =>
      tokenFieldMap get tkn map { pafField =>
        matchQuery(
          field = pafField,
          value = input
        )
      }
    }
  }


  override def queryAddress(tokens: Seq[CrfTokenResult])(implicit p: Pagination): Future[RichSearchResponse] = {
    logExecute("Query Hybrid Addresses") {
      search.in(hybridIndex).paginate query {
        query(tokens.map(_.value).mkString(" "))
      }
    }
  }

  private def logExecute[T, R, Q](info: String)(t: T)(implicit executable: Executable[T, R, Q]): Future[Q] = {
    logger info s"$info: ${t.toString}"
    client execute t
  }
}