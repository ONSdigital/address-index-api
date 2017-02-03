package uk.gov.ons.addressIndex.server.modules

import java.io.File
import javax.inject.{Inject, Singleton}

import com.typesafe.config.ConfigFactory
import play.api.Logger

@Singleton
class SystemBootstrap @Inject()() {
  System.load(
    new File(
      s"${ConfigFactory.load().getString("addressIndex.parserLibPath")}/libcrftagger.so"
    ).getAbsolutePath
  )
  Logger("address-index") info "`SystemBootstrap` complete"
}
