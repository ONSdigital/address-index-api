package addressIndex

import javax.inject.{Inject, Singleton}
import play.api.Logger

trait Bootstrap {
  def applicationStart(): Unit
  applicationStart()
}

@Singleton
class SystemBootstrap @Inject() extends Bootstrap {
  def applicationStart(): Unit = {
    Logger("address-index") info "`SystemBootstrap` complete"
  }
}