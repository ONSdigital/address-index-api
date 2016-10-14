package addressIndex.modules

import javax.inject.{Inject, Singleton}
import com.sksamuel.elastic4s._
import org.elasticsearch.common.settings._
import com.google.inject.ImplementedBy
import play.api.Logger

@ImplementedBy(classOf[ElasticsearchRepositoryModule])
trait ElasticsearchRepository {
  /**
    * An ElasticClient.
    */
  def client() : ElasticClient
}

@Singleton
class ElasticsearchRepositoryModule @Inject()(
  conf : AddressIndexConfigModule
) extends ElasticsearchRepository {

  val logger = Logger("address-index:ElasticsearchRepositoryModule")

  /**
    * The default ElasticClient.
    */
  def client() : ElasticClient = {
    val esConf = conf.config.elasticSearch
    logger.info(s"attempting to connect to elasticsearch. uri: ${esConf.uri} cluster: ${esConf.cluster}")
    val esClientSettings = Settings.settingsBuilder.put(
      "cluster.name" -> esConf.cluster,
      "client"       -> esConf.uri
    ).build

    if(esConf.local) {
      val client = ElasticClient local esClientSettings
      logger.info("local connection to elasticsearch established")
      client
    } else {
      val client = ElasticClient.transport(
        settings = esClientSettings,
        uri = ElasticsearchClientUri(esConf.uri)
      )
      logger.info("remote connection to elasticsearch established")
      client
    }
  }
}
