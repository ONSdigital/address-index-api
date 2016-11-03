package addressIndex.modules

import javax.inject.{Inject, Singleton}

import com.sksamuel.elastic4s._
import org.elasticsearch.common.settings._
import com.google.inject.ImplementedBy
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse
import play.api.Logger
import uk.gov.ons.addressIndex.model.db.index.{PostcodeAddressFileIndex, PostcodeIndex}
import uk.gov.ons.addressIndex.model.db.ElasticIndexSugar
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[AddressIndexRepository])
trait ElasticsearchRepository extends ElasticIndexSugar {
  /**
    * An ElasticClient.
    */
  def client() : ElasticClient

  /**
    * Create the repository.
    *
    * @return
    */
  def createAll() : Future[Seq[_]]

  /**
    * Delete the repository.
    *
    * @return
    */
  def deleteAll() : Future[Seq[_]]
}

@Singleton
class AddressIndexRepository @Inject()(conf : AddressIndexConfigModule)(implicit ec: ExecutionContext) extends ElasticsearchRepository {

  val logger = Logger("address-index:ElasticsearchRepositoryModule")
  val esConf = conf.config.elasticSearch

  /**
    * The default ElasticClient.
    */
  val client : ElasticClient = {
    logger info s"attempting to connect to elasticsearch uri: ${esConf.uri} cluster: ${esConf.cluster}"
    val esClientSettings = Settings.settingsBuilder.put(
      "cluster.name" -> esConf.cluster,
      "client" -> esConf.uri
    ).build

    if(esConf.local) {
      val client = ElasticClient local esClientSettings
      logger info "local connection to elasticsearch established"
      client
    } else {
      val client = ElasticClient.transport(
        settings = esClientSettings,
        uri = ElasticsearchClientUri(esConf.uri)
      )
      logger info "remote connection to elasticsearch established"
      client
    }
  }

  def createAll() : Future[Seq[CreateIndexResponse]] = {
    createIndex(
      PostcodeAddressFileIndex,
      PostcodeIndex
    )(client)
  }

  def deleteAll() : Future[Seq[DeleteIndexResponse]] = {
    deleteIndex(
      PostcodeAddressFileIndex,
      PostcodeIndex
    )(client)
  }
}