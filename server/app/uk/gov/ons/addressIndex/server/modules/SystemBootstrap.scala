package uk.gov.ons.addressIndex.server.modules

import java.io.File

import com.typesafe.config.ConfigFactory
import javax.inject.{Inject, Singleton}
import play.api.Logger

@Singleton
class SystemBootstrap @Inject()() {
  def osSharedObjectName(): String = {
    val x = System.getProperty("os.name").toLowerCase
    x match {
      case s if s.contains("mac") => "libcrftagger.so"
      case s if s.contains("window") => "libcrftagger.dll"
      case s if s.contains("linux") => "libcrftagger-linux.so"
      case _ => "libcrftagger.so"
    }
  }

  System.load(
    new File(
      s"${new java.io.File(".").getCanonicalPath}/${ConfigFactory.load().getString("addressIndex.parserLibPath")}/$osSharedObjectName"
    ).getAbsolutePath
  )
  Logger("address-index") info "`SystemBootstrap` complete"
}
