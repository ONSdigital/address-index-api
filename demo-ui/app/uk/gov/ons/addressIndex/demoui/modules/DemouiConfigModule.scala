package uk.gov.ons.addressIndex.demoui.modules

/**
  * Created by ONS21880 on 02/12/2016.
  */

import javax.inject.Singleton
import pureconfig._
import uk.gov.ons.addressIndex.model.config.DemouiConfig
import scala.util.Try

/**
  * Inject this into your controllers to access a type safe config.
  */
@Singleton
class DemouiConfigModule() {
  //TODO should we be defaulting here?
  //TODO use the `tryConfig` for errors later on?
  private val tryConfig : Try[DemouiConfig] = loadConfig[DemouiConfig]("demoui")
  val config : DemouiConfig = tryConfig getOrElse DemouiConfig.default
}
