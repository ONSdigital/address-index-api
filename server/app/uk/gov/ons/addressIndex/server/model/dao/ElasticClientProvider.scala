package uk.gov.ons.addressIndex.server.model.dao

import com.sksamuel.elastic4s.http.HttpClient

/**
  * Provides access to Elastic client
  */

trait ElasticClientProvider {
  /**
    * Defines a getter for Elastic client
    *
    * @return
    */
  def client: HttpClient
}
