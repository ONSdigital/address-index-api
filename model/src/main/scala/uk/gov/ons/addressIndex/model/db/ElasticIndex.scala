package uk.gov.ons.addressIndex.model.db

/**
  * Implement this trait and use ElasticIndexSugar.
  */
trait ElasticIndex[T <: Product with Serializable] {
  /**
    * The name of the index
    */
  def name() : String
}
