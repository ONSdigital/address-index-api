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
  maximumOffset: Int,
  bulkRequestsPerBatch: Int
)

case class ShieldConfig(
  ssl: Boolean,
  user: String,
  password: String
)

case class AddressIndexConfig(
  parserLibPath: String,
  bulkLimit: Int,
  elasticSearch: ElasticSearchConfig
)

case class IndexesConfig(
  hybridIndex: String
)

case class queryParamsConfig(
  buildingNumberLpiBoost: Float,
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
