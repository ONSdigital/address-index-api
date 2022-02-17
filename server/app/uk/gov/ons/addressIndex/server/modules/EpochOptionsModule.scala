package uk.gov.ons.addressIndex.server.modules

trait EpochOptionsModule {
  def apiVersion: String

  def dataVersion: String

  def termsAndConditions: String
}
