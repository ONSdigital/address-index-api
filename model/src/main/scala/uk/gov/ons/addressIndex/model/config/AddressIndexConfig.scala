package uk.gov.ons.addressIndex.model.config

case class AddressIndexConfig(
  parserLibPath: String,
  elasticSearch: ElasticSearchConfig,
  bulk: BulkConfig
)

case class ElasticSearchConfig(
  local: Boolean,
  cluster: String,
  uri: String,
  indexes: IndexesConfig,
  shield: ShieldConfig,
  queryParams: queryParamsConfig,
  defaultLimit: Int,
  defaultOffset: Int,
  maximumLimit: Int,
  maximumOffset: Int
)

case class queryParamsConfig(
  subBuildingName: subBuildingNameConfig,
  buildingName: buildingNameConfig,
  buildingNumber: buildingNumberConfig,
  streetName: streetNameConfig,
  townName: townNameConfig,
  postcode: postcodeConfig,
  organisationName: organisationNameConfig,
  departmentName: departmentNameConfig,
  locality: localityConfig,
  defaultBoost: Float,
  minimumShouldMatch: String
)

case class ShieldConfig(
  ssl: Boolean,
  user: String,
  password: String
)

case class IndexesConfig(
  hybridIndex: String
)

case class subBuildingNameConfig(
  lpiSaoStartNumberBoost: Float,
  lpiSaoStartSuffixBoost: Float,
  lpiSaoEndNumberBoost: Float,
  lpiSaoEndSuffixBoost: Float,
  pafSubBuildingNameBoost: Float,
  lpiSaoTextBoost: Float
 )

case class buildingNameConfig(
  lpiPaoStartNumberBoost: Float,
  lpiPaoStartSuffixBoost: Float,
  lpiPaoEndNumberBoost: Float,
  lpiPaoEndSuffixBoost: Float,
  pafBuildingNameBoost: Float,
  lpiPaoTextBoost: Float
)

case class buildingNumberConfig(
  pafBuildingNumberBoost: Float,
  lpiPaoStartNumberBoost: Float
)

case class streetNameConfig(
  pafThoroughfareBoost: Float,
  pafWelshThoroughfareBoost: Float,
  pafDependentThoroughfareBoost: Float,
  pafWelshDependentThoroughfareBoost: Float,
  lpiStreetDescriptorBoost: Float
)

case class townNameConfig(
  pafPostTownBoost: Float,
  pafWelshPostTownBoost: Float,
  lpiTownNameBoost: Float,
  pafDependentLocalityBoost: Float,
  pafWelshDependentLocalityBoost: Float,
  lpiLocalityBoost: Float,
  pafDoubleDependentLocalityBoost: Float,
  pafWelshDoubleDependentLocalityBoost: Float
)

case class postcodeConfig(
  pafPostcodeBoost: Float,
  lpiPostcodeLocatorBoost: Float,
  pafOutcodeBoost: Float,
  pafIncodeBoost: Float
)

case class organisationNameConfig(
  pafOrganisationNameBoost: Float,
  lpiOrganisationBoost: Float,
  lpiPaoTextBoost: Float,
  lpiLegalNameBoost: Float,
  lpiSaoTextBoost: Float
)

case class departmentNameConfig(
  pafDepartmentNameBoost: Float,
  lpiLegalNameBoost: Float
)

case class localityConfig(
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
  customErrorDev: Boolean,
  customErrorTest: Boolean,
  customErrorProd: Boolean,
  apiURL: ApiConfig,
  limit: Int,
  offset: Int,
  maxLimit: Int,
  maxOffset: Int
 )
