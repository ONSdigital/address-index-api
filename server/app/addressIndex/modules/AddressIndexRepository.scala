package addressIndex.modules

import javax.inject.{Inject, Singleton}

import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.ElasticDsl._
import org.elasticsearch.common.settings._
import com.google.inject.ImplementedBy
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.plugins.Plugin
import org.elasticsearch.shield.ShieldPlugin
import play.api.Logger
import uk.gov.ons.addressIndex.model.db.index.{PostcodeAddressFileAddress, PostcodeIndex}
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

  def queryUprn(uprn: String) : Future[Seq[PostcodeAddressFileAddress]]
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
    val esClientSettings = Settings.settingsBuilder
      .put("transport.ping_schedule", "5s")
      .put("cluster.name", esConf.cluster)
      .put("request.headers.X-Found-Cluster", esConf.cluster)
      .put("client", esConf.uri)
      .put("shield.transport.ssl", true)
      .put("shield.user", "admin:uswlhsrw60u62geph1")
      .put("plugin.types", "org.elasticsearch.shield.ShieldPlugin")
      .build()

    if(esConf.local) {
      val client = ElasticClient local esClientSettings
      logger info "local connection to elasticsearch established"
      client
    } else {
      val plugins: Class[_ <: Plugin] = classOf[ShieldPlugin]
      val client = TransportClient.builder().addPlugin(plugins).settings(esClientSettings).build()
//      val client = ElasticClient.transport(
//        settings = esClientSettings,
//        uri = ElasticsearchClientUri(esConf.uri),
//        plugins = plugins
//      )
      logger info "remote connection to elasticsearch established"
      ElasticClient.fromClient(client)
    }
  }

  def createAll() : Future[Seq[CreateIndexResponse]] = {
    createIndex(
      PostcodeAddressFileAddress,
      PostcodeIndex
    )(client)
  }

  def deleteAll() : Future[Seq[DeleteIndexResponse]] = {
    deleteIndex(
      PostcodeAddressFileAddress,
      PostcodeIndex
    )(client)
  }

  def queryUprn(uprn: String): Future[Seq[PostcodeAddressFileAddress]] = client.execute{
    search in "paf/address" query uprn
  }.map(_.as[PostcodeAddressFileAddress])
}