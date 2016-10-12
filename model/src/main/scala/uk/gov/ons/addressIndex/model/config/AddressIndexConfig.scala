package uk.gov.ons.addressIndex.model.config

case class ElasticSearchConfig(
  local   : Boolean,
  cluster : String,
  uri     : String
)

object ElasticSearchConfig {
  val default : ElasticSearchConfig = ElasticSearchConfig(
    uri     = "elasticsearch://localhost:9200",
    cluster = "address-index",
    local   = true
  )
}

case class AddressIndexConfig(
  elasticSearch : ElasticSearchConfig
)

object AddressIndexConfig {
  val default : AddressIndexConfig = AddressIndexConfig(
    elasticSearch = ElasticSearchConfig.default
  )
}