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

case class ShieldConfig(
  ssl: Boolean,
  user: String,
  password: String
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
