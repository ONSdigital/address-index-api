package uk.gov.ons.addressIndex.server.model.dao

import javax.inject.{Inject, Singleton}
import com.google.inject.ImplementedBy
import com.sksamuel.elastic4s.ElasticsearchClientUri
import com.sksamuel.elastic4s.http.HttpClient
import com.sksamuel.elastic4s.http.HttpRequestClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.client.RestClientBuilder._
import org.apache.http.impl.nio.client._
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.auth.AuthScope
import org.apache.http.client.config.RequestConfig.Builder
import org.elasticsearch.shield.ShieldPlugin
import play.api.Logger
import uk.gov.ons.addressIndex.server.modules.AddressIndexConfigModule

/**
  * Provides access to Elastic client
  */

@ImplementedBy(classOf[AddressIndexElasticClientProvider])
trait ElasticClientProvider {
  /**
    * Defines a getter for Elastic client
    *
    * @return
    */
  def client: HttpClient
}

/**
  * Gets the information from the configuration file and then creates a corresponding client
  * Often used in injections
  * @param conf injected configuration
  */
@Singleton
class AddressIndexElasticClientProvider @Inject()(conf: AddressIndexConfigModule) extends ElasticClientProvider {

  private val esConf = conf.config.elasticSearch
  private val shieldConf = esConf.shield
  private val logger = Logger("address-index:ElasticsearchRepositoryModule")

  private val esClientSettings = {
    Settings.builder()
      .put("cluster.name", esConf.cluster)
      .put("shield.transport.ssl", shieldConf.ssl)
      .put("request.headers.X-Found-Cluster", esConf.cluster)
      .put("shield.user", s"${shieldConf.user}:${shieldConf.password}")
      .build
  }

  lazy val provider = {
    val provider = new BasicCredentialsProvider
    val credentials = new UsernamePasswordCredentials("elastic", "changeme")
    provider.setCredentials(AuthScope.ANY, credentials)
    provider
  }
  val client = HttpClient(ElasticsearchClientUri("localhost", 9200), new RequestConfigCallback  {
    override def customizeRequestConfig(requestConfigBuilder: Builder) = {
      requestConfigBuilder
    }
  }, new HttpClientConfigCallback {
    override def customizeHttpClient(httpClientBuilder: HttpAsyncClientBuilder) = {
      httpClientBuilder.setDefaultCredentialsProvider(provider)
    }
  })


 // val client: HttpClient = {
 //   if (esConf.local) {
 ////     logger info "Connecting to local ES"
 ////     HttpClient(ElasticsearchClientUri("localhost", 9200))
 //   } else {
  //    logger info "Connecting to remote ES"
 //     HttpClient(ElasticsearchClientUri(esConf.uri, 9200))
 //   }
 // }
}
