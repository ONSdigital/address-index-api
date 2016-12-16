package uk.gov.ons.addressIndex.model.config

case class ElasticSearchConfig(
  local: Boolean,
  cluster: String,
  uri: String,
  indexes: IndexesConfig,
  shield: ShieldConfig
)

object ElasticSearchConfig {
  val default : ElasticSearchConfig = ElasticSearchConfig(
    uri = "elasticsearch://localhost:9200",
    cluster = "ons-cluster",
    local = false,
    indexes = IndexesConfig(
      pafIndex = "paf/address",
      nagIndex = "nag/address"
    ),
    shield = ShieldConfig(
      ssl = true,
      user = "admin",
      password = ""
    )
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
  pafIndex: String,
  nagIndex: String
)

case class DemouiConfig (
  defaultLanguage: String,
  apiURL: String
)

object DemouiConfig {
  val default : DemouiConfig = DemouiConfig(
    defaultLanguage = "en",
    apiURL = ""
  )
}