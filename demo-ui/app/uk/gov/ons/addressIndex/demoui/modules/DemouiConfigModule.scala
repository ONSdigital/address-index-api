package uk.gov.ons.addressIndex.demoui.modules

import pureconfig.ConfigConvert.fromReaderAndWriter

import javax.inject.Singleton
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.generic.ProductHint
import uk.gov.ons.addressIndex.model.config.DemouiConfig

/**
  * Inject this into your controllers to access a type safe config.
  */
@Singleton
class DemouiConfigModule() {

  implicit def hint:ProductHint[DemouiConfig] = ProductHint[DemouiConfig](ConfigFieldMapping(CamelCase, CamelCase))
  //private val tryConfig: Try[DemouiConfig] = loadConfig[DemouiConfig]("demoui")
  private val tryConfig: ConfigReader.Result[DemouiConfig] = ConfigSource.default.at("demoui").load[DemouiConfig]
 // def loadConfig[A](namespace: String)(implicit reader: ConfigReader[A]): ConfigReader.Result[A]
  val config: DemouiConfig = tryConfig.getOrElse(throw new IllegalArgumentException("Address Index (Demo UI) config is corrupted, verify if application.conf does not contain any typos"))
}
