package uk.gov.ons.addressIndex.server.modules

import javax.inject.Singleton
import play.api.Logger
import pureconfig._
import uk.gov.ons.addressIndex.model.config.AddressIndexConfig
import scala.util.Try

/**
  * Inject this into your controllers to access a type safe config.
  */
@Singleton
class AddressIndexConfigModule(optOverride: Option[AddressIndexConfig] = None) {
  private val logger = Logger("ConfigLogger")
  val tryConfig: Try[AddressIndexConfig] = {
    if(optOverride.isEmpty) {
      loadConfig[AddressIndexConfig]("addressIndex")
    } else {
      Try(optOverride.get)
    }
  }
  val config: AddressIndexConfig = tryConfig getOrElse {
    logger info "defaulting config because of errors"
    logger info s"${tryConfig.toString}"
    AddressIndexConfig.default
  }
}