package addressIndex.modules

import javax.inject.Singleton
import pureconfig._
import uk.gov.ons.addressIndex.model.config.AddressIndexConfig
import scala.util.Try

@Singleton
class AddressIndexConfigModule {
  //TODO should we be defaulting here?
  //TODO use the `tryConfig` for errors later on?
  private val tryConfig: Try[AddressIndexConfig] = loadConfig[AddressIndexConfig]("addressIndex")
  val config: AddressIndexConfig = tryConfig.toOption.getOrElse(AddressIndexConfig.default)
}