package uk.gov.ons.addressIndex.server.model.dao

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import com.sksamuel.elastic4s.{ElasticClient, ElasticsearchClientUri}
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.shield.ShieldPlugin
import play.api.Logger
import uk.gov.ons.addressIndex.server.modules.AddressIndexConfigModule


/**
  * Provides access to Elastic client
  * Often used in injections
  */

@ImplementedBy(classOf[AddressIndexElasticClientProvider])
trait ElasticClientProvider {
  /**
    * Defines a getter for Elastic client
    * @return
    */
  def client: ElasticClient
}

/**
  * Gets the information from the configuration file and then creates a corresponding client
  * @param conf injected configuration
  */
@Singleton
class AddressIndexElasticClientProvider @Inject()(conf: AddressIndexConfigModule) extends ElasticClientProvider{

  private val esConf = conf.config.elasticSearch
  private val logger = Logger("address-index:ElasticsearchRepositoryModule")

  private val esClientSettings = Settings.settingsBuilder
    .put("cluster.name", esConf.cluster)
    .put("shield.transport.ssl", esConf.shieldSsl)
    .put("request.headers.X-Found-Cluster", esConf.cluster)
    .put("shield.user", esConf.shieldUser)
    .build()

  val client = if (esConf.local) {
    logger info "Connecting to local ES"
    ElasticClient local esClientSettings
  } else {
    logger info "Connecting to remote ES"
    ElasticClient.transport(
      settings = esClientSettings,
      uri = ElasticsearchClientUri(esConf.uri),
      plugins = classOf[ShieldPlugin]
    )
  }
}
