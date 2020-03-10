package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

case class AddressResponseAddressUPRNEQ(uprn: String,
                                        formattedAddress: String,
                                        addressLine1: String,
                                        addressLine2: String,
                                        addressLine3: String,
                                        townName: String,
                                        postcode: String)

object AddressResponseAddressUPRNEQ {
  implicit lazy val addressResponseAddressUPRNEQFormat: Format[AddressResponseAddressUPRNEQ] = Json.format[AddressResponseAddressUPRNEQ]

}