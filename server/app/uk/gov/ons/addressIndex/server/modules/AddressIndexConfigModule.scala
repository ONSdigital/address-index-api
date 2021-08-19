package uk.gov.ons.addressIndex.server.modules

import pureconfig.generic.ProductHint

import javax.inject.Singleton
import pureconfig.{CamelCase, ConfigFieldMapping, ConfigReader, ConfigSource}
import uk.gov.ons.addressIndex.model.config.AddressIndexConfig
import pureconfig.generic.auto.exportReader

import scala.util.Try

/**
  * Inject this into your controllers to access a type safe config.
  */
@Singleton
class AddressIndexConfigModule() extends ConfigModule {
 implicit def hint[AddressIndexConfig] = ProductHint[AddressIndexConfig](ConfigFieldMapping(CamelCase, CamelCase))
 // private val tryConfig: Try[AddressIndexConfig] = loadConfig[AddressIndexConfig]("addressIndex")
 private val tryConfig: ConfigReader.Result[AddressIndexConfig] = ConfigSource.default.at("addressIndex").load[AddressIndexConfig]
  val config: AddressIndexConfig = tryConfig.getOrElse(throw new IllegalArgumentException("Address Index config is corrupted, verify if application.conf does not contain any typos"))
}