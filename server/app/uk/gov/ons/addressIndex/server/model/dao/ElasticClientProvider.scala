package uk.gov.ons.addressIndex.server.model.dao

import com.sksamuel.elastic4s.ElasticClient
import org.elasticsearch.client.RestClient

/**
  * Provides access to Elastic client
  */

trait ElasticClientProvider {

  /**
    * Defines a getter for Elastic client
    *
    * @return
    */
  def client: ElasticClient

  /**
    * Exposes the underlying REST client
    *
    * @return
    */
  def rclient: RestClient

  /**
    * Defines a getter for Elastic client lite
    * Currently used for GCP deployments. Internally the API gateway determines the cluster to use.
    *
    * @return
    */
  def clientFullmatch: ElasticClient

  /**
    * Defines a getter for Elastic limited access Census client
    * Available on network only if key matches
    *
    * @return
    */
  def clientSpecialCensus: ElasticClient
}
