package addressIndex.modules

import javax.inject.{Inject, Singleton}
import com.sksamuel.elastic4s._
import org.elasticsearch.common.settings._
import com.google.inject.ImplementedBy

@ImplementedBy(classOf[ElasticsearchRepositoryModule])
trait ElasticsearchRepository {
  /**
    * An ElasticClient.
    */
  val defaultClient : ElasticClient

  /**
    * A configuration object which contains information
    * about how to configure the `defaultClient`.
    */
  val conf : AddressIndexConfigModule
}

@Singleton
class ElasticsearchRepositoryModule @Inject()(
  val conf : AddressIndexConfigModule
) extends ElasticsearchRepository {

  /**
    * The default ElasticClient.
    */
  val defaultClient : ElasticClient = {
    val esConf = conf.config.elasticSearch
    val esClientSettings = Settings.settingsBuilder.put("cluster" -> esConf.cluster).build
    if(esConf.local) {
      ElasticClient local esClientSettings
    } else {
      ElasticClient.transport(
        settings = esClientSettings,
        uri = ElasticsearchClientUri(esConf.uri)
      )
    }
  }
}
