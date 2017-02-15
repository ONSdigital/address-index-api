package uk.gov.ons.addressIndex.model.config

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

case class ShieldConfig(
  ssl: Boolean,
  user: String,
  password: String
)

case class AddressIndexConfig(
  parserLibPath: String,
  bulkLimit: Int,
  elasticSearch: ElasticSearchConfig,
  bulkRequestsPerBatch: Int,
  bulkMiniBatchUpscale: Float,
  bulkMiniBatchDownscale: Float
)

case class IndexesConfig(
  hybridIndex: String
)

case class queryParamsConfig(
  paoStartNumberBuildingNumberLpiBoost: Float,
  paoStartNumberPaoLpiBoost: Float,
  paoStartSuffixLpiBoost: Float,
  paoEndNumberLpiBoost: Float,
  organisationNameOrganisationLpiBoost: Float,
  organisationNameLegalNameLpiBoost: Float,
  organisationNamePaoTextLpiBoost: Float,
  organisationNameSaoTextLpiBoost: Float,
  subBuildingNameLpiBoost: Float,
  streetNameLpiBoost: Float,
  buildingNumberPafBoost: Float,
  subBuildingNameSubBuildingPafBoost: Float,
  subBuildingNameBuildingPafBoost: Float,
  streetNamePafBoost: Float,
  underlineAllBoost: Float,
  minimumShouldMatch: String
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
