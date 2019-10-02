package uk.gov.ons.addressIndex.server.model.dao

import java.security.cert.X509Certificate

import com.sksamuel.elastic4s.{ElasticClient, ElasticNodeEndpoint, ElasticProperties, ElasticsearchClientUri, HttpClient}
import com.sksamuel.elastic4s.http.JavaClient
import javax.inject.{Inject, Singleton}
import javax.net.ssl.{SSLContext, X509TrustManager}
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.config.RequestConfig.Builder
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client._
import org.elasticsearch.client.RestClientBuilder._
import uk.gov.ons.addressIndex.server.modules.ConfigModule
import uk.gov.ons.addressIndex.server.utils.GenericLogger

import scala.util.Try

/**
  * Gets the information from the configuration file and then creates a corresponding client
  * Often used in injections
  *
  * @param conf injected configuration
  */
@Singleton
class AddressIndexElasticClientProvider @Inject()
(conf: ConfigModule) extends ElasticClientProvider {

  private val esConf = conf.config.elasticSearch
  private val logger = GenericLogger("address-index:ElasticsearchRepositoryModule")

  val host: String = esConf.uri
  val hostFullmatch: String = esConf.uriFullmatch
  val port: String = esConf.port
  val ssl: String = esConf.ssl
  val connectionTimeout: Int = esConf.connectionTimeout
  val connectionRequestTimeout: Int = esConf.connectionRequestTimeout
  val socketTimeout: Int = esConf.connectionRequestTimeout
  val maxESConnections: Int = esConf.maxESConnections
  private val context = SSLContext.getInstance("SSL")

  /* This code is used when authentication is setup on the ES cluster */
  val provider: BasicCredentialsProvider = {

    logger info "Connecting to Elasticsearch"

    val provider = new BasicCredentialsProvider
    val credentials = new UsernamePasswordCredentials("mrsearchy", "mrsearchy")

    provider.setCredentials(AuthScope.ANY, credentials)
    provider
  }

  context.init(null, Array(
    new X509TrustManager {
      def checkClientTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = {}

      def checkServerTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = {}

      def getAcceptedIssuers: Array[X509Certificate] = Array()
    }
  ), null)

  def propsBuilder(host: String, port: String, ssl: String): ElasticProperties = {
    val intPort = Try(port.toInt).getOrElse(9200)
    val elEndpoint: ElasticNodeEndpoint = new ElasticNodeEndpoint(
      if (ssl == "true") "https" else "http", host, intPort, None)
    new ElasticProperties(endpoints = Seq(elEndpoint))
  }

  val client: ElasticClient = clientBuilder(propsBuilder(host, port, ssl))

  val clientFullmatch: ElasticClient = clientBuilder(propsBuilder(hostFullmatch, port, ssl))

  def clientBuilder(eProps: ElasticProperties): ElasticClient = ElasticClient(JavaClient(eProps, new RequestConfigCallback {

    override def customizeRequestConfig(requestConfigBuilder: Builder): Builder = {
      requestConfigBuilder.setConnectTimeout(connectionTimeout)
        .setSocketTimeout(socketTimeout)
        .setConnectionRequestTimeout(connectionRequestTimeout)
        .setContentCompressionEnabled(true)
      requestConfigBuilder
    }
  }, (httpClientBuilder: HttpAsyncClientBuilder) => {
    httpClientBuilder
   //   .setDefaultCredentialsProvider(provider)
      .setMaxConnTotal(maxESConnections)
      .setSSLContext(context)
  }))
}


