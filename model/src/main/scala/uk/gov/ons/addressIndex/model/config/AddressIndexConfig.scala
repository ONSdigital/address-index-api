package uk.gov.ons.addressIndex.model.config

import play.api.libs.json.{Format, Json}

case class AddressIndexConfig(
  apiKeyRequired: Boolean,
  masterKey: String,
  parserLibPath: String,
  pathToResources: String,
  elasticSearch: ElasticSearchConfig,
  bulk: BulkConfig
)

case class ElasticSearchConfig(
  local: Boolean,
  cluster: String,
  uri: String,
  indexes: IndexesConfig,
  shield: ShieldConfig,
  queryParams: QueryParamsConfig,
  defaultLimit: Int,
  defaultOffset: Int,
  maximumLimit: Int,
  maximumOffset: Int
)

case class QueryParamsConfig(
  subBuildingName: SubBuildingNameConfig,
  subBuildingRange: SubBuildingRangeConfig,
  buildingName: BuildingNameConfig,
  buildingNumber: BuildingNumberConfig,
  buildingRange: BuildingRangeConfig,
  streetName: StreetNameConfig,
  townName: TownNameConfig,
  postcode: PostcodeConfig,
  organisationName: OrganisationNameConfig,
  departmentName: DepartmentNameConfig,
  locality: LocalityConfig,
  excludingDisMaxTieBreaker: Double,
  includingDisMaxTieBreaker: Double,
  topDisMaxTieBreaker: Double,
  defaultBoost: Float,
  paoSaoMinimumShouldMatch: String,
  organisationDepartmentMinimumShouldMatch: String,
  mainMinimumShouldMatch: String
)

// This is required for the bulk request as Data Scientists want to provide query params dynamically
object QueryParamsConfig {
  implicit val queryParamsConfigFormat: Format[QueryParamsConfig] = Json.format[QueryParamsConfig]
}

case class ShieldConfig(
  ssl: Boolean,
  user: String,
  password: String
)

case class IndexesConfig(
  hybridIndex: String,
  hybridMapping: String
)

case class SubBuildingNameConfig(
  lpiSaoStartSuffixBoost: Float,
  pafSubBuildingNameBoost: Float,
  lpiSaoTextBoost: Float
)

object SubBuildingNameConfig {
  implicit val subBuildingNameConfigFormat: Format[SubBuildingNameConfig] = Json.format[SubBuildingNameConfig]
}

case class SubBuildingRangeConfig(
  lpiSaoStartNumberBoost: Float,
  lpiSaoStartSuffixBoost: Float,
  lpiSaoEndNumberBoost: Float,
  lpiSaoEndSuffixBoost: Float
)

object SubBuildingRangeConfig {
  implicit val subBuildingRangeConfigFormat: Format[SubBuildingRangeConfig] = Json.format[SubBuildingRangeConfig]
}

case class BuildingNameConfig(
  lpiPaoStartSuffixBoost: Float,
  pafBuildingNameBoost: Float,
  lpiPaoTextBoost: Float
)

object BuildingNameConfig {
  implicit val buildingNameConfigFormat: Format[BuildingNameConfig] = Json.format[BuildingNameConfig]
}

case class BuildingRangeConfig(
  lpiPaoStartNumberBoost: Float,
  lpiPaoStartSuffixBoost: Float,
  lpiPaoEndNumberBoost: Float,
  lpiPaoEndSuffixBoost: Float,
  pafBuildingNumberBoost: Float,
  lpiPaoStartEndBoost: Float
)

object BuildingRangeConfig {
  implicit val buildingRangeConfigFormat: Format[BuildingRangeConfig] = Json.format[BuildingRangeConfig]
}

case class BuildingNumberConfig(
  pafBuildingNumberBoost: Float,
  lpiPaoStartNumberBoost: Float
)

object BuildingNumberConfig {
  implicit val buildingNumberConfigFormat: Format[BuildingNumberConfig] = Json.format[BuildingNumberConfig]
}

case class StreetNameConfig(
  pafThoroughfareBoost: Float,
  pafWelshThoroughfareBoost: Float,
  pafDependentThoroughfareBoost: Float,
  pafWelshDependentThoroughfareBoost: Float,
  lpiStreetDescriptorBoost: Float
)

object StreetNameConfig {
  implicit val streetNameConfigFormat: Format[StreetNameConfig] = Json.format[StreetNameConfig]
}

case class TownNameConfig(
  pafPostTownBoost: Float,
  pafWelshPostTownBoost: Float,
  lpiTownNameBoost: Float,
  pafDependentLocalityBoost: Float,
  pafWelshDependentLocalityBoost: Float,
  lpiLocalityBoost: Float,
  pafDoubleDependentLocalityBoost: Float,
  pafWelshDoubleDependentLocalityBoost: Float
)

object TownNameConfig {
  implicit val townNameConfigFormat: Format[TownNameConfig] = Json.format[TownNameConfig]
}

case class PostcodeConfig(
  pafPostcodeBoost: Float,
  lpiPostcodeLocatorBoost: Float,
  postcodeInOutBoost: Float,
  postcodeOutBoost: Float,
  postcodeInBoost: Float
)

object PostcodeConfig {
  implicit val postcodeConfigFormat: Format[PostcodeConfig] = Json.format[PostcodeConfig]
}

case class OrganisationNameConfig(
  pafOrganisationNameBoost: Float,
  lpiOrganisationBoost: Float,
  lpiPaoTextBoost: Float,
  lpiLegalNameBoost: Float,
  lpiSaoTextBoost: Float
)

object OrganisationNameConfig {
  implicit val organisationNameConfigFormat: Format[OrganisationNameConfig] = Json.format[OrganisationNameConfig]
}

case class DepartmentNameConfig(
  pafDepartmentNameBoost: Float,
  lpiLegalNameBoost: Float
)

object DepartmentNameConfig {
  implicit val departmentConfigFormat: Format[DepartmentNameConfig] = Json.format[DepartmentNameConfig]
}

case class LocalityConfig(
  pafPostTownBoost: Float,
  pafWelshPostTownBoost: Float,
  lpiTownNameBoost: Float,
  pafDependentLocalityBoost: Float,
  pafWelshDependentLocalityBoost: Float,
  lpiLocalityBoost: Float,
  pafDoubleDependentLocalityBoost: Float,
  pafWelshDoubleDependentLocalityBoost: Float
)

object LocalityConfig {
  implicit val localityConfigFormat: Format[LocalityConfig] = Json.format[LocalityConfig]
}

case class FallbackConfig(
  fallbackQueryBoost: Float,
  fallbackMinimumShouldMatch: String,
  fallbackPafBoost: Float,
  fallbackLpiBoost: Float,
  fallbackPafBigramBoost: Float,
  fallbackLpiBigramBoost: Float,
  bigramFuzziness: String
)

object FallbackConfig {
  implicit val fallbackConfigFormat: Format[FallbackConfig] = Json.format[FallbackConfig]
}

case class BulkConfig(
  batch: BatchConfig,
  limitPerAddress: Int
)

case class BatchConfig(
  perBatch: Int,
  upscale: Float,
  downscale: Float,
  warningThreshold: Float
)

case class ApiConfig(
  host: String,
  port: Int
)

case class DemouiConfig (
  loginRequired: Boolean,
  realGatewayDev: Boolean,
  realGatewayTest: Boolean,
  realGatewayProd: Boolean,
  customErrorDev: Boolean,
  customErrorTest: Boolean,
  customErrorProd: Boolean,
  gatewayURL: String,
  apiURL: ApiConfig,
  limit: Int,
  offset: Int,
  maxLimit: Int,
  maxOffset: Int
 )
