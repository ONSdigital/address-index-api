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

/**
  *     Tokens.buildingNumber -> hybridPafBuildingNumber,
        Tokens.paoStartNumber -> hybridNagPaoStartNumber,
        Tokens.paoStartSuffix -> hybridNagPaoStartSuffix,
        Tokens.paoEndNumber -> hybridNagPaoEndNumber,
        Tokens.paoEndSuffix -> hybridNagPaoEndSuffix,
        Tokens.saoStartNumber -> hybridNagSaoStartNumber,
        Tokens.saoStartSuffix -> hybridNagSaoStartSuffix,
        Tokens.saoEndNumber -> hybridNagSaoEndNumber,
        Tokens.saoEndSuffix -> hybridNagPaoEndSuffix,
        Tokens.locality -> hybridNagLocality,
        Tokens.organisationName -> hybridNagOrganisation,
        Tokens.postcode -> hybridNagPostcodeLocator,
        Tokens.departmentName -> hybridPafDepartmentName,
        Tokens.subBuildingName -> hybridPafSubBuildingName,
        Tokens.buildingName -> hybridPafBuildingName,
        Tokens.streetName -> hybridNagStreetDescriptor,
        Tokens.townName -> hybridNagTownName
  */


case class queryParamsConfig(
  subBuildingNameLpiSaoStartNumberBoost: Float,
  subBuildingNameLpiSaoStartSuffixBoost: Float,
  subBuildingNameLpiSaoEndNumberBoost: Float,
  subBuildingNameLpiSaoEndSuffixBoost: Float,
  subBuildingNamePafSubBuildingNameBoost: Float,
  subBuildingNameLpiSaoTextBoost: Float,
  buildingNameLpiPaoStartNumberBoost: Float,
  buildingNameLpiPaoStartSuffixBoost: Float,
  buildingNameLpiPaoEndNumberBoost: Float,
  buildingNameLpiPaoEndSuffixBoost: Float,
  buildingNamePafSubBuildingNameBoost: Float,
  buildingNameLpiPaoTextBoost: Float,
  buildingNumberPafBuildingNumberBoost: Float,
  buildingNumberLpiPaoStartNumberBoost: Float,
  streetNamePafThoroughfareBoost: Float,
  streetNamePafWelshThoroughfareBoost: Float,
  streetNamePafDependentThoroughfareBoost: Float,
  streetNamePafWelshDependentThoroughfareBoost: Float,
  streetNameLpiStreetDescriptorBoost: Float,
  townNamePafPostTownBoost: Float,
  townNamePafWelshPostTownBoost: Float,
  townNameLpiTownNameBoost: Float,
  townNamePafDependentLocalityBoost: Float,
  townNamePafWelshDependentLocalityBoost: Float,
  townNameLpiLocalityBoost: Float,
  townNamePafDoubleDependentLocalityBoost: Float,
  townNamePafWelshDoubleDependentLocalityBoost: Float,
  postcodePafPostcodeBoost: Float,
  postcodeLpiPostcodeLocatorBoost: Float,
  postcodePafOutcodeBoost: Float,
  postcodePafIncodeBoost: Float,
  origanisationNamePafOrganisationNameBoost: Float,
  origanisationNameLpiOrganisationBoost: Float,
  origanisationNameLpiPaoTextBoost: Float,
  origanisationNameLpiLegalNameBoost: Float,
  origanisationNameLpiSaoTextBoost: Float,
  departmentNamePafDepartmentNameBoost: Float,
  departmentNameLpiLegalNameBoost: Float,
  localityPafDependentLocalityBoost: Float,
  localityPafWelshDependentLocalityBoost: Float,
  localityLpiLocalityBoost: Float,
  localityPafDoubleDependentLocalityBoost: Float,
  localityPafWelshDoubleDependentLocalityBoost: Float,
  defaultBoost: Float,
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
