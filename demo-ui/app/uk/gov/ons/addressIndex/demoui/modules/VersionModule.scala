package uk.gov.ons.addressIndex.demoui.modules

trait VersionModule {
  def apiVersion: String
  def dataVersion: String
}

object DemoUIAddressIndexVersionModule extends VersionModule {

  lazy val apiVersion: String = {
    "1.2"
  }

  lazy val dataVersion: String = {
    "39"
  }

}