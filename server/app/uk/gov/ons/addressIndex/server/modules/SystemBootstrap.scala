package uk.gov.ons.addressIndex.server.modules

import java.io.File

import com.typesafe.config.ConfigFactory
import javax.inject.{Inject, Singleton}
import play.api.Logger

@Singleton
class SystemBootstrap @Inject()() {

  def osSharedObjectName(): String = {
    val x = System.getProperty("os.name").toLowerCase
    if(x.contains("mac")) {
      "libcrftagger.so"
    } else if(x.contains("window")) {
      "libcrftagger.dll"
    } else if(x.contains("linux")) {
      "libcrftagger-linux.so"
    } else {
      "libcrftagger.so"
    }
  }

  System.load(
    new File(
      s"${new java.io.File(".").getCanonicalPath}/${ConfigFactory.load().getString("addressIndex.parserLibPath")}/$osSharedObjectName"
    ).getAbsolutePath
  )
  Logger("address-index") info "`SystemBootstrap` complete"
}
