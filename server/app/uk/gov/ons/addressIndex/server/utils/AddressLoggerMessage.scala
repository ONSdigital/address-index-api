package uk.gov.ons.addressIndex.server.utils

class AddressLoggerMessage(msg: String) extends APILoggerMessage {
  val message: String = msg
}

object AddressLoggerMessage {

  def apply(message: String): AddressLoggerMessage = {
    new AddressLoggerMessage(message)
  }
}
