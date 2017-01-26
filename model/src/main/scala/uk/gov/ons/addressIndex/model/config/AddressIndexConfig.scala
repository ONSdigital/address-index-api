package uk.gov.ons.addressIndex.model.config

case class ElasticSearchConfig(
  local: Boolean,
  cluster: String,
  uri: String,
  indexes: IndexesConfig,
  shield: ShieldConfig,
  defaultLimit: Int,
  defaultOffset: Int,
  maximumLimit: Int,
  maximumOffset: Int
)

object ElasticSearchConfig {
  val default: ElasticSearchConfig = ElasticSearchConfig(
    uri = "elasticsearch://localhost:9200",
    cluster = "ons-cluster",
    local = false,
    indexes = IndexesConfig(
      hybridIndex = "hybrid/address"
    ),
    shield = ShieldConfig(
      ssl = true,
      user = "admin",
      password = ""
    ),
    defaultLimit=10,
    defaultOffset=0,
    maximumLimit=100,
    maximumOffset=1000
  )
}

case class ShieldConfig(
  ssl: Boolean,
  user: String,
  password: String
)

case class AddressIndexConfig(
  elasticSearch: ElasticSearchConfig
)

object AddressIndexConfig {
  val default: AddressIndexConfig = AddressIndexConfig(
    elasticSearch = ElasticSearchConfig.default
  )
}

case class IndexesConfig(
  hybridIndex: String
)

case class ApiConfig(
  host: String,
  port: Int
)

object ApiConfig{
  val default: ApiConfig = ApiConfig(
    host = "http://localhost",
    port = 9001
  )
}

case class DemouiConfig (
  customErrorDev: Boolean,
  customErrorTest: Boolean,
  customErrorProd: Boolean,
  apiURL: ApiConfig
)

object DemouiConfig {
  val default: DemouiConfig = DemouiConfig(
    customErrorDev = false,
    customErrorTest = false,
    customErrorProd = true,
    apiURL = ApiConfig.default
  )
}