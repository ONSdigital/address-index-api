package uk.gov.ons.addressIndex.server.modules

import javax.inject.Singleton

import com.google.inject.ImplementedBy
import pureconfig._
import uk.gov.ons.addressIndex.model.config.AddressIndexConfig

import scala.util.Try

@ImplementedBy(classOf[AddressIndexConfigModule])
trait ConfigModule {
  def config: AddressIndexConfig
}

/**
  * Inject this into your controllers to access a type safe config.
  */
@Singleton
class AddressIndexConfigModule() extends ConfigModule {
  private val tryConfig: Try[AddressIndexConfig] = loadConfig[AddressIndexConfig]("addressIndex")
  val config: AddressIndexConfig = tryConfig.getOrElse(throw new IllegalArgumentException("Address Index config is corrupted, verify if application.conf does not contain any typos"))
}