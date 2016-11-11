package uk.gov.ons.addressIndex.model.config

case class ElasticSearchConfig(
  local : Boolean,
  cluster : String,
  uri : String
)

object ElasticSearchConfig {
  val default : ElasticSearchConfig = ElasticSearchConfig(
    uri = "elasticsearch://localhost:9200",
    cluster = "ons-cluster",
    local = true
  )
}

//sealed trait RunMode
//case object Development extends RunMode
//case object Production extends RunMode

//object RunMode {
//  implicit def runMode(runMode : String) : RunMode  = runMode toLowerCase match {
//    case "dev" | "development" => Development
//    case "prod" | "production" => Production
//  }
//}


case class AddressIndexConfig(
  runMode : String,// RunMode,
  elasticSearch : ElasticSearchConfig
)

object AddressIndexConfig {
  val default : AddressIndexConfig = AddressIndexConfig(
    runMode = "dev",
    elasticSearch = ElasticSearchConfig.default
  )
}