package uk.gov.ons.gatling.conf

import com.typesafe.config.{ConfigFactory, ConfigParseOptions}


object ConfigLoader {
  private val configParseOptions: ConfigParseOptions = ConfigParseOptions.defaults().setAllowMissing(false)

  private lazy val PathPrefix: String = "uk/gov/ons/gatling/conf/"
  private lazy val applicationConfig = ConfigFactory.parseResources(PathPrefix + "_application.conf", configParseOptions)

  private lazy val config = ConfigFactory.systemProperties()
    .withFallback(applicationConfig)
    .resolve()

  lazy val apiSpecificConfigName: String = System.getProperty("CONFIG_NAME", "generic_get_request")

  def apply(configurationKey: String, defaultValue: String = ""): String = {
    val configValue = config.getString(apiSpecificConfigName + "." + configurationKey)

    if (!configValue.isEmpty)
      configValue
    else
      defaultValue
  }

  def getPOSTRequestBodyJSONPath: String = {
    // Take payload name from System Property. If not defined, use config file and,
    // finally fallback on deriving it from config key passed in as CONFIG_NAME
    val payloadFileName = ConfigLoader("payload_name", apiSpecificConfigName)
      .replaceAll("\\.json$", "").concat(".json")

    "%s%s".format(PathPrefix, payloadFileName)
  }

}
