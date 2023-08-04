package uk.gov.ons.addressIndex.model.server.response.rh

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseAddressCustomRH, AddressResponseAddressRH, CountryBoosts}

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
case class AddressByRHPartialAddressResponse(input: String,
                                             addresses: Seq[AddressResponseAddressCustomRH],
                                             filter: String,
                                             fallback: Boolean,
                                             epoch: String,
                                             limit: Int,
                                             offset: Int,
                                             total: Long,
                                             maxScore: Double,
                                             favourpaf: Boolean,
                                             favourwelsh: Boolean,
                                             countryBoosts: CountryBoosts
                                            )

object AddressByRHPartialAddressResponse {
  implicit lazy val addressByPartialRHAddressResponseFormat: Format[AddressByRHPartialAddressResponse] = Json.format[AddressByRHPartialAddressResponse]

  def toRHAddressByPartialResponse(sortedAddresses: Seq[AddressResponseAddressRH]): Seq[AddressResponseAddressCustomRH] = {

    sortedAddresses.map(
      address => AddressResponseAddressCustomRH.fromAddressResponseAddressRH(address)
    )
  }
}
