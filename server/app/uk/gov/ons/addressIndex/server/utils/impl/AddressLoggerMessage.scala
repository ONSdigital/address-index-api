package uk.gov.ons.addressIndex.server.utils.impl

import uk.gov.ons.addressIndex.server.utils.APILoggerMessage

class AddressLoggerMessage(messg: String) extends APILoggerMessage {
  val message: String = messg
}

object AddressLoggerMessage {

  def apply(message: String): AddressLoggerMessage = {
    new AddressLoggerMessage(message)
  }
}
