package uk.gov.ons.addressIndex.server.modules

import javax.inject.{Inject, Singleton}
import play.api.Logger
import pureconfig._
import uk.gov.ons.addressIndex.model.config.AddressIndexConfig
import scala.util.Try

/**
  * Inject this into your controllers to access a type safe config.
  */
@Singleton
class AddressIndexConfigModule @Inject()() {
  private val logger = Logger("ConfigLogger")
  val tryConfig: Try[AddressIndexConfig] = loadConfig[AddressIndexConfig]("addressIndex")
  val config: AddressIndexConfig = tryConfig getOrElse {
    logger info "defaulting config because of errors"
    logger info s"errors:\n${tryConfig.toString}"
    logger info s"using default config:\n${AddressIndexConfig.default}"
    AddressIndexConfig.default
  }
}