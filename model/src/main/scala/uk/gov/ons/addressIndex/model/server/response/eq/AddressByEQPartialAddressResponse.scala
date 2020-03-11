package uk.gov.ons.addressIndex.model.server.response.eq

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseAddressCustomEQ, AddressResponseAddressEQ}

/**
  * Contains relevant, to the address request, data
  *
  * @param addresses found addresses
  */
case class AddressByEQPartialAddressResponse(input: String,
                                             addresses: Seq[AddressResponseAddressCustomEQ],
                                             filter: String,
                                             fallback: Boolean,
                                             historical: Boolean,
                                             epoch: String,
                                             limit: Int,
                                             offset: Int,
                                             total: Long,
                                             maxScore: Double,
                                             verbose: Boolean,
                                             fromsource: String,
                                             highlight: String,
                                             favourpaf: Boolean,
                                             favourwelsh: Boolean)


object AddressByEQPartialAddressResponse {
  implicit lazy val addressByPartialEQAddressResponseFormat: Format[AddressByEQPartialAddressResponse] = Json.format[AddressByEQPartialAddressResponse]

  def toEQAddressByPartialResponse(sortedAddresses: Seq[AddressResponseAddressEQ]): Seq[AddressResponseAddressCustomEQ] = {

    sortedAddresses.map(
      address => AddressResponseAddressCustomEQ.fromAddressResponseAddressEQ(address)
    )
  }
}