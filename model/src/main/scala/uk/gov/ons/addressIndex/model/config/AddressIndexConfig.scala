package uk.gov.ons.addressIndex.model.config

case class AddressIndexConfig(
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
  buildingName: BuildingNameConfig,
  buildingNumber: BuildingNumberConfig,
  streetName: StreetNameConfig,
  townName: TownNameConfig,
  postcode: PostcodeConfig,
  organisationName: OrganisationNameConfig,
  departmentName: DepartmentNameConfig,
  locality: LocalityConfig,
  excludingDisMaxTieBreaker: Double,
  includingDisMaxTieBreaker: Double,
  fallbackQueryBoost: Float,
  defaultBoost: Float,
  mainMinimumShouldMatch: String,
  fallbackMinimumShouldMatch: String
)

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
  lpiSaoStartNumberBoost: Float,
  lpiSaoStartSuffixBoost: Float,
  lpiSaoEndNumberBoost: Float,
  lpiSaoEndSuffixBoost: Float,
  pafSubBuildingNameBoost: Float,
  lpiSaoTextBoost: Float
 )

case class BuildingNameConfig(
  lpiPaoStartNumberBoost: Float,
  lpiPaoStartSuffixBoost: Float,
  lpiPaoEndNumberBoost: Float,
  lpiPaoEndSuffixBoost: Float,
  pafBuildingNameBoost: Float,
  lpiPaoTextBoost: Float
)

case class BuildingNumberConfig(
  pafBuildingNumberBoost: Float,
  lpiPaoStartNumberBoost: Float
)

case class StreetNameConfig(
  pafThoroughfareBoost: Float,
  pafWelshThoroughfareBoost: Float,
  pafDependentThoroughfareBoost: Float,
  pafWelshDependentThoroughfareBoost: Float,
  lpiStreetDescriptorBoost: Float
)

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

case class PostcodeConfig(
  pafPostcodeBoost: Float,
  lpiPostcodeLocatorBoost: Float,
  postcodeInOutBoost: Float,
  postcodeOutBoost: Float,
  postcodeInBoost: Float
)

case class OrganisationNameConfig(
  pafOrganisationNameBoost: Float,
  lpiOrganisationBoost: Float,
  lpiPaoTextBoost: Float,
  lpiLegalNameBoost: Float,
  lpiSaoTextBoost: Float
)

case class DepartmentNameConfig(
  pafDepartmentNameBoost: Float,
  lpiLegalNameBoost: Float
)

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
