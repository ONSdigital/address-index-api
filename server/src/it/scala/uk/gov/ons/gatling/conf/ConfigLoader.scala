package uk.gov.ons.gatling.conf

import com.typesafe.config.{ConfigFactory, ConfigParseOptions}


object ConfigLoader {
  private val configParseOptions: ConfigParseOptions = ConfigParseOptions.defaults().setAllowMissing(false)

  private lazy val PATH_PREFIX: String = "uk/gov/ons/gatling/conf/"
  private lazy val applicationConfig = ConfigFactory.parseResources(PATH_PREFIX + "_application.conf", configParseOptions)

  private lazy val config = ConfigFactory.systemProperties()
    .withFallback(applicationConfig)
    .resolve()

  lazy val apiSpecificConfigName: String = System.getProperty("configName")
  def apply(configurationKey: String): String = config.getString(apiSpecificConfigName + "." + configurationKey)
  def getPOSTRequestBodyJSONPath() = "%s%s%s".format(PATH_PREFIX, apiSpecificConfigName, ".json")

}
