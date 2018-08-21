package uk.gov.ons.addressIndex.server.modules

trait VersionModule {
  def apiVersion: String
  def dataVersion: String
}
