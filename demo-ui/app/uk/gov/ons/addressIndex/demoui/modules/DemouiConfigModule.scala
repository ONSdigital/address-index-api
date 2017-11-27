package uk.gov.ons.addressIndex.demoui.modules

import javax.inject.Singleton
import pureconfig._
import uk.gov.ons.addressIndex.model.config.DemouiConfig
import scala.util.Try

/**
  * Inject this into your controllers to access a type safe config.
  */
@Singleton
class DemouiConfigModule() {
  private val tryConfig: Try[DemouiConfig] = loadConfig[DemouiConfig]("demoui")

//  val config: DemouiConfig = tryConfig match {
//    case Left(l) => throw new IllegalArgumentException("Address Index (Demo UI) config is corrupted, verify if application.conf does not contain any typos")
 //   case Right(r) => r
//  }

 // case Right


 // private val tryConfig : Try[DemouiConfig] = Right[DemouiConfig]
  val config : DemouiConfig = tryConfig.getOrElse(throw new IllegalArgumentException("Address Index (Demo UI) config is corrupted, verify if application.conf does not contain any typos"))
}
