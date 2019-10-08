package uk.gov.ons.addressIndex.server.model.dao

import com.sksamuel.elastic4s.ElasticClient

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
    * Defines a getter for Elastic client lite
    * Currently used for GCP deployments. Internally the API gateway determines the cluster to use.
    *
    * @return
    */
  def clientFullmatch: ElasticClient
}
