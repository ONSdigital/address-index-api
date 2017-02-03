package uk.gov.ons.addressIndex.model.config

import org.elasticsearch.common.lucene.MinimumScoreCollector

case class ElasticSearchConfig(
  local: Boolean,
  cluster: String,
  uri: String,
  indexes: IndexesConfig,
  shield: ShieldConfig,
  defaultLimit: Int,
  defaultOffset: Int,
  maximumLimit: Int,
  maximumOffset: Int,
  minimumShouldMatch: String,
  underlineAllBoost: Float,
  streetNameBoost: Float
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
    maximumOffset=1000,
    minimumShouldMatch = "-2",
    underlineAllBoost = 0.5f,
    streetNameBoost = 1.0f
  )
}

case class ShieldConfig(
  ssl: Boolean,
  user: String,
  password: String
)

case class AddressIndexConfig(
  parserLibPath: String,
  elasticSearch: ElasticSearchConfig
)

object AddressIndexConfig {
  val default: AddressIndexConfig = AddressIndexConfig(
    parserLibPath = "/",
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
  apiURL: ApiConfig,
  limit: Int,
  offset: Int,
  maxLimit: Int,
  maxOffset: Int
 )

object DemouiConfig {
  val default: DemouiConfig = DemouiConfig(
    customErrorDev = false,
    customErrorTest = false,
    customErrorProd = true,
    apiURL = ApiConfig.default,
    limit = ElasticSearchConfig.default.defaultLimit,
    offset = ElasticSearchConfig.default.defaultOffset,
    maxLimit = ElasticSearchConfig.default.maximumLimit,
    maxOffset = ElasticSearchConfig.default.maximumOffset
  )
}