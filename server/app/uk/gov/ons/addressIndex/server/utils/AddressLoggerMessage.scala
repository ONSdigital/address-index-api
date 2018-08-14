package uk.gov.ons.addressIndex.server.utils

class AddressLoggerMessage(messg: String) extends APILoggerMessage {
  val message: String = messg
}

object AddressLoggerMessage {

  def apply(message: String): AddressLoggerMessage = {
    new AddressLoggerMessage(message)
  }
}
