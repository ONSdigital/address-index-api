package uk.gov.ons.addressIndex.server.model.dao

import java.security.cert.X509Certificate
import com.sksamuel.elastic4s.{ElasticClient, ElasticNodeEndpoint, ElasticProperties, ElasticRequest, HttpClient, HttpEntity, HttpResponse}
import com.sksamuel.elastic4s.http.JavaClient
import org.apache.http.HttpHost

import javax.inject.{Inject, Singleton}
import javax.net.ssl.{SSLContext, X509TrustManager}
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.config.RequestConfig.Builder
import org.apache.http.entity.ContentType
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.nio.client._
import org.apache.http.nio.entity.NStringEntity
import org.elasticsearch.client.{Request, RestClient, RestClientBuilder}
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
  val basicAuth: String = esConf.basicAuth
  val searchUser: String = esConf.searchUser
  val searchPassword: String = esConf.searchPassword
  val searchUserCen: String = esConf.searchUserCen
  val searchPasswordCen: String = esConf.searchPasswordCen
  val connectionTimeout: Int = esConf.connectionTimeout
  val connectionRequestTimeout: Int = esConf.connectionRequestTimeout
  val socketTimeout: Int = esConf.connectionRequestTimeout
  val maxESConnections: Int = esConf.maxESConnections
  private val context = SSLContext.getInstance("SSL")

  /* This code is used when authentication is setup on the ES cluster */
  val provider: BasicCredentialsProvider = {

    logger info "Connecting to Elasticsearch"

    val provider = new BasicCredentialsProvider
    val credentials = new UsernamePasswordCredentials(searchUser, searchPassword)

    provider.setCredentials(AuthScope.ANY, credentials)
    provider
  }

  /* This code is used when authentication is setup on the ES cluster */
  val providerCen: BasicCredentialsProvider = {

    logger info "Connecting to Elasticsearch"

    val provider = new BasicCredentialsProvider
    val credentials = new UsernamePasswordCredentials(searchUserCen, searchPasswordCen)

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
    val elEndpoint: ElasticNodeEndpoint = ElasticNodeEndpoint(
      if (ssl == "true") "https" else "http", host, intPort, None)
    new ElasticProperties(endpoints = Seq(elEndpoint))
  }

  val client: ElasticClient = clientBuilder(propsBuilder(host, port, ssl))

  //val rclient = new RestClient(propsBuilder(host, port, ssl))



 // import org.apache.http.HttpHost
 // import org.elasticsearch.client.RestClient

  val rclient = RestClient.builder(new HttpHost(host, 9200, "http")).build

 // val rHost: HttpHost = new HttpHost(host, 9200, "http")
//  val restClientBuilder: RestClientBuilder  = RestClient.builder(new HttpHost(,))

//  val request: Request = new Request(
//    "POST",
//    "_ml/trained_models/arinze__address-match-abp-v1/deployment/_infer")
//  request.setEntity(new NStringEntity(
//    "{  \"docs\": {    \"text_field\": \"Office For National Statistics Newport\"  }}",
//    ContentType.APPLICATION_JSON));
//  val myResponse = rClient.performRequest(request)
//  val rClientBuilder = new RestClientBuilder()
//  val jclient: JavaClient = clientBuilderJ(propsBuilder(host, port, ssl))
//
//  val underlyingClient: HttpClient = client.client

  def callback(cb: Either[Throwable,HttpResponse]): Unit = {
    cb match {
      case Left(s) => println(s"Error: $s")
      case Right(i) => println(s"Answer: $i")
    }
  }

//  val mlentity: HttpEntity = new NStringEntity(
//    "{\"json\":\"text\"}",
//    ContentType.APPLICATION_JSON).asInstanceOf;
//  val mlrequest = new ElasticRequest("POST", "_ml/trained_models/arinze__address-match-abp-v1/deployment/_infer",null, entity = Option(mlentity),null )
//  underlyingClient.send(mlrequest,callback)

  val clientFullmatch: ElasticClient = clientBuilder(propsBuilder(hostFullmatch, port, ssl))

  val clientSpecialCensus: ElasticClient = clientBuilderCen(propsBuilder(hostFullmatch, port, ssl))

  def clientBuilder(eProps: ElasticProperties): ElasticClient = ElasticClient(JavaClient(eProps, new RequestConfigCallback {

    override def customizeRequestConfig(requestConfigBuilder: Builder): Builder = {
      requestConfigBuilder.setConnectTimeout(connectionTimeout)
        .setSocketTimeout(socketTimeout)
        .setConnectionRequestTimeout(connectionRequestTimeout)
        .setContentCompressionEnabled(true)
      requestConfigBuilder
    }
  }, (httpClientBuilder: HttpAsyncClientBuilder) => {
    if (basicAuth == "true") {
      httpClientBuilder
       .setDefaultCredentialsProvider(provider)
       .setMaxConnTotal(maxESConnections)
       .setSSLContext(context)
  } else {
     httpClientBuilder
      .setMaxConnTotal(maxESConnections)
      .setSSLContext(context)
    }
  }
  ))

  def clientBuilderJ(eProps: ElasticProperties): JavaClient = JavaClient(eProps, new RequestConfigCallback {

    override def customizeRequestConfig(requestConfigBuilder: Builder): Builder = {
      requestConfigBuilder.setConnectTimeout(connectionTimeout)
        .setSocketTimeout(socketTimeout)
        .setConnectionRequestTimeout(connectionRequestTimeout)
        .setContentCompressionEnabled(true)
      requestConfigBuilder
    }
  }, (httpClientBuilder: HttpAsyncClientBuilder) => {
    if (basicAuth == "true") {
      httpClientBuilder
        .setDefaultCredentialsProvider(provider)
        .setMaxConnTotal(maxESConnections)
        .setSSLContext(context)
    } else {
      httpClientBuilder
        .setMaxConnTotal(maxESConnections)
        .setSSLContext(context)
    }
  }
  )

  def clientBuilderCen(eProps: ElasticProperties): ElasticClient = ElasticClient(JavaClient(eProps, new RequestConfigCallback {

    override def customizeRequestConfig(requestConfigBuilder: Builder): Builder = {
      requestConfigBuilder.setConnectTimeout(connectionTimeout)
        .setSocketTimeout(socketTimeout)
        .setConnectionRequestTimeout(connectionRequestTimeout)
        .setContentCompressionEnabled(true)
      requestConfigBuilder
    }
  }, (httpClientBuilder: HttpAsyncClientBuilder) => {
    if (basicAuth == "true") {
      httpClientBuilder
        .setDefaultCredentialsProvider(providerCen)
        .setMaxConnTotal(maxESConnections)
        .setSSLContext(context)
    } else {
      httpClientBuilder
        .setMaxConnTotal(maxESConnections)
        .setSSLContext(context)
    }
  }
  ))
}


