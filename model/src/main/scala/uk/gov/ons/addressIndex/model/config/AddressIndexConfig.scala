package uk.gov.ons.addressIndex.model.config

case class ElasticSearchConfig(
  local : Boolean,
  cluster : String,
  uri : String,
  shieldSsl : Boolean,
  shieldUser : String,
  indexes: IndexesConfig
)

object ElasticSearchConfig {
  val default : ElasticSearchConfig = ElasticSearchConfig(
    uri = "elasticsearch://localhost:9200",
    cluster = "ons-cluster",
    local = false,
    shieldSsl = true,
    shieldUser = "default:default",
    indexes = IndexesConfig(
      pafIndex = "paf/address",
      nagIndex = "nag/address"
    )
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

case class IndexesConfig(
  pafIndex: String,
  nagIndex: String
)

case class DemouiConfig (
  customErrorDev: Boolean,
  customErrorTest: Boolean,
  customErrorProd: Boolean,
  apiURL: String
)

object DemouiConfig {
  val default : DemouiConfig = DemouiConfig(
    customErrorDev = false,
    customErrorTest = false,
    customErrorProd = true,
    apiURL = ""
  )
}