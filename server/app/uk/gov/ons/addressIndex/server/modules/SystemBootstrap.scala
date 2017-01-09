package uk.gov.ons.addressIndex.server.modules

import java.io.File
import javax.inject.{Inject, Singleton}
import play.api.Logger

@Singleton
class SystemBootstrap @Inject()() {
  System.load(
    new File(
      "parsers/src/main/resources/libcrftagger.so"
    ).getAbsolutePath
  )
  Logger("address-index") info "`SystemBootstrap` complete"
}
