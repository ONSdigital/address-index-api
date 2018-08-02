package uk.gov.ons.addressIndex.server.utils

trait APILogging[L <: APILogMessage] {
  def log(message: L): Unit
  def trace(message: L): Unit
  def debug(message: L): Unit
}
