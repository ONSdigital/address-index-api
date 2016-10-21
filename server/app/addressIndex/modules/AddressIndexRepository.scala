package addressIndex.modules

import javax.inject.{Inject, Singleton}
import com.sksamuel.elastic4s._
import org.elasticsearch.common.settings._
import com.google.inject.ImplementedBy
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse
import play.api.Logger
import uk.gov.ons.addressIndex.model.db.index.{PostcodeIndex, PostcodeAddressFileIndex}
import uk.gov.ons.addressIndex.model.db.ElasticIndexSugar
import scala.concurrent.Future

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
class AddressIndexRepository @Inject()(conf : AddressIndexConfigModule) extends ElasticsearchRepository {

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

  def createAll(): Future[Seq[CreateIndexResponse]] = {
    implicit val c : ElasticClient = client
    createIndex(
      PostcodeAddressFileIndex,
      PostcodeIndex
    )
  }

  def deleteAll(): Future[Seq[DeleteIndexResponse]] = {
    implicit val c : ElasticClient = client
    deleteIndex(
      PostcodeAddressFileIndex,
      PostcodeIndex
    )
  }
}