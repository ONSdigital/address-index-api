package uk.gov.ons.addressIndex.model.config

import play.api.libs.json.{Format, Json}

case class AddressIndexConfig(apiKeyRequired: Boolean,
                              masterKey: String,
                              sourceRequired: Boolean,
                              sourceKey: String,
                              parserLibPath: String,
                              pathToResources: String,
                              elasticSearch: ElasticSearchConfig,
                              bulk: BulkConfig)

case class ElasticSearchConfig(local: Boolean,
                               cluster: String,
                               uri: String,
                               port: String,
                               ssl: String,
                               connectionTimeout: Int,
                               connectionRequestTimeout: Int,
                               socketTimeout: Int,
                               maxESConnections: Int,
                               clusterPolicies: ClusterPoliciesConfig,
                               indexes: IndexesConfig,
                               queryParams: QueryParamsConfig,
                               defaultLimit: Int,
                               defaultLimitPartial: Int,
                               defaultLimitPostcode: Int,
                               defaultLimitRandom: Int,
                               defaultOffset: Int,
                               maximumLimit: Int,
                               maximumLimitRandom: Int,
                               maximumOffset: Int,
                               matchThreshold: Float,
                               minimumSample: Int,
                               circuitBreakerMaxFailures: Int,
                               circuitBreakerCallTimeout: Int,
                               circuitBreakerResetTimeout: Int,
                               minimumPartial: Int,
                               defaultStartBoost: Int,
                               validEpochs: String)

case class QueryParamsConfig(// the number of cases has to be at most 22
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
                             fallback: FallbackConfig,
                             excludingDisMaxTieBreaker: Double,
                             includingDisMaxTieBreaker: Double,
                             topDisMaxTieBreaker: Double,
                             paoSaoMinimumShouldMatch: String,
                             organisationDepartmentMinimumShouldMatch: String,
                             mainMinimumShouldMatch: String)

// This is required for the bulk request as Data Scientists want to provide query params dynamically
object QueryParamsConfig {
  implicit val queryParamsConfigFormat: Format[QueryParamsConfig] = Json.format[QueryParamsConfig]
}

// for now each endpoint runs on one cluster, so the values are numbers.
case class ClusterPoliciesConfig(bulk: String,
                                 address: String,
                                 partial: String,
                                 postcode: String,
                                 uprn: String,
                                 version: String,
                                 random: String)

case class IndexesConfig(hybridIndexHistorical: String,
                         hybridIndex: String,
                         hybridIndexHistoricalSkinny: String,
                         hybridIndexSkinny: String,
                         hybridMapping: String)

case class SubBuildingNameConfig(pafSubBuildingNameBoost: Double,
                                 lpiSaoTextBoost: Double,
                                 lpiSaoStartNumberBoost: Double,
                                 lpiSaoStartSuffixBoost: Double)

object SubBuildingNameConfig {
  implicit val subBuildingNameConfigFormat: Format[SubBuildingNameConfig] = Json.format[SubBuildingNameConfig]
}

case class SubBuildingRangeConfig(lpiSaoStartNumberBoost: Double,
                                  lpiSaoStartSuffixBoost: Double,
                                  lpiSaoEndNumberBoost: Double,
                                  lpiSaoEndSuffixBoost: Double,
                                  lpiSaoStartEndBoost: Double)

object SubBuildingRangeConfig {
  implicit val subBuildingRangeConfigFormat: Format[SubBuildingRangeConfig] = Json.format[SubBuildingRangeConfig]
}

case class BuildingNameConfig(lpiPaoStartSuffixBoost: Double,
                              pafBuildingNameBoost: Double,
                              lpiPaoTextBoost: Double)

object BuildingNameConfig {
  implicit val buildingNameConfigFormat: Format[BuildingNameConfig] = Json.format[BuildingNameConfig]
}

case class BuildingRangeConfig(lpiPaoStartNumberBoost: Double,
                               lpiPaoStartSuffixBoost: Double,
                               lpiPaoEndNumberBoost: Double,
                               lpiPaoEndSuffixBoost: Double,
                               pafBuildingNumberBoost: Double,
                               lpiPaoStartEndBoost: Double)

object BuildingRangeConfig {
  implicit val buildingRangeConfigFormat: Format[BuildingRangeConfig] = Json.format[BuildingRangeConfig]
}

case class BuildingNumberConfig(pafBuildingNumberBoost: Double,
                                lpiPaoStartNumberBoost: Double,
                                lpiPaoEndNumberBoost: Double)

object BuildingNumberConfig {
  implicit val buildingNumberConfigFormat: Format[BuildingNumberConfig] = Json.format[BuildingNumberConfig]
}

case class StreetNameConfig(pafThoroughfareBoost: Double,
                            pafWelshThoroughfareBoost: Double,
                            pafDependentThoroughfareBoost: Double,
                            pafWelshDependentThoroughfareBoost: Double,
                            lpiStreetDescriptorBoost: Double)

object StreetNameConfig {
  implicit val streetNameConfigFormat: Format[StreetNameConfig] = Json.format[StreetNameConfig]
}

case class TownNameConfig(pafPostTownBoost: Double,
                          pafWelshPostTownBoost: Double,
                          lpiTownNameBoost: Double,
                          pafDependentLocalityBoost: Double,
                          pafWelshDependentLocalityBoost: Double,
                          lpiLocalityBoost: Double,
                          pafDoubleDependentLocalityBoost: Double,
                          pafWelshDoubleDependentLocalityBoost: Double)

object TownNameConfig {
  implicit val townNameConfigFormat: Format[TownNameConfig] = Json.format[TownNameConfig]
}

case class PostcodeConfig(pafPostcodeBoost: Double,
                          lpiPostcodeLocatorBoost: Double,
                          postcodeInOutBoost: Double)

object PostcodeConfig {
  implicit val postcodeConfigFormat: Format[PostcodeConfig] = Json.format[PostcodeConfig]
}

case class OrganisationNameConfig(pafOrganisationNameBoost: Double,
                                  lpiOrganisationBoost: Double,
                                  lpiPaoTextBoost: Double,
                                  lpiLegalNameBoost: Double,
                                  lpiSaoTextBoost: Double)

object OrganisationNameConfig {
  implicit val organisationNameConfigFormat: Format[OrganisationNameConfig] = Json.format[OrganisationNameConfig]
}

case class DepartmentNameConfig(pafDepartmentNameBoost: Double,
                                lpiLegalNameBoost: Double)

object DepartmentNameConfig {
  implicit val departmentConfigFormat: Format[DepartmentNameConfig] = Json.format[DepartmentNameConfig]
}

case class LocalityConfig(pafPostTownBoost: Double,
                          pafWelshPostTownBoost: Double,
                          lpiTownNameBoost: Double,
                          pafDependentLocalityBoost: Double,
                          pafWelshDependentLocalityBoost: Double,
                          lpiLocalityBoost: Double,
                          pafDoubleDependentLocalityBoost: Double,
                          pafWelshDoubleDependentLocalityBoost: Double)

object LocalityConfig {
  implicit val localityConfigFormat: Format[LocalityConfig] = Json.format[LocalityConfig]
}

case class FallbackConfig(fallbackQueryBoost: Double,
                          fallbackMinimumShouldMatch: String,
                          fallbackPafBoost: Double,
                          fallbackLpiBoost: Double,
                          fallbackPafBigramBoost: Double,
                          fallbackLpiBigramBoost: Double,
                          bigramFuzziness: String)

object FallbackConfig {
  implicit val fallbackConfigFormat: Format[FallbackConfig] = Json.format[FallbackConfig]
}

case class BulkConfig(batch: BatchConfig,
                      limitperaddress: Int,
                      maxLimitperaddress: Int,
                      matchThreshold: Float,
                      minimumSample: Int)

case class BatchConfig(perBatch: Int,
                       upscale: Float,
                       downscale: Float,
                       warningThreshold: Float)

case class ApiConfig(host: String,
                     port: Int,
                     ajaxHost: String,
                     ajaxPort: String,
                     gatewayPath: String,
                     apidocs: String,
                     swaggerui: String)

case class DemouiConfig(loginRequired: Boolean,
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
                        maxOffset: Int,
                        pauseMillis: Int)
