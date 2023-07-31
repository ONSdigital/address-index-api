package uk.gov.ons.addressIndex.model.server.response.eq

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseAddressCustomEQ, AddressResponseAddressEQ, CountryBoosts}

/**
  * Contains relevant, to the address request, data
  *
  * @param input partial address input
  * @param addresses found addresses
  * @param filter classification filter
  * @param fallback fallback switch
  * @param epoch which AB Epoch to use
  * @param limit max number of found addresses
  * @param offset offset of found addresses (for pagination)
  * @param total total number of found addresses
  * @param maxScore the max score
  * @param favourpaf paf switch
  * @param favourwelsh welsh switch
  */
case class AddressByEQPartialAddressResponse(input: String,
                                             addresses: Seq[AddressResponseAddressCustomEQ],
                                             filter: String,
                                             fallback: Boolean,
                                             epoch: String,
                                             limit: Int,
                                             offset: Int,
                                             total: Long,
                                             maxScore: Double,
                                             favourpaf: Boolean,
                                             favourwelsh: Boolean,
                                             countryBoosts: CountryBoosts)


object AddressByEQPartialAddressResponse {
  implicit lazy val addressByPartialEQAddressResponseFormat: Format[AddressByEQPartialAddressResponse] = Json.format[AddressByEQPartialAddressResponse]

  def toEQAddressByPartialResponse(sortedAddresses: Seq[AddressResponseAddressEQ]): Seq[AddressResponseAddressCustomEQ] = {

    sortedAddresses.map(
      address => AddressResponseAddressCustomEQ.fromAddressResponseAddressEQ(address)
    )
  }
}