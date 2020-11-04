package uk.gov.ons.addressIndex.model.server.response.rh

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.server.response.address.AddressResponseAddressPostcodeRH

/**
  * Contains relevant, to the address request, data
  *
  * @param postcode postcode from query
  * @param addresses found addresses
  * @param filter classification filter
  * @param epoch which AB Epoch to use
  * @param limit max number of found addresses
  * @param offset offset of found addresses (for pagination)
  * @param total total number of found addresses
  * @param maxScore the max score
  */
case class AddressByRHPostcodeResponse(postcode: String,
                                       addresses: Seq[AddressResponseAddressPostcodeRH],
                                       filter: String,
                                       epoch: String,
                                       limit: Int,
                                       offset: Int,
                                       total: Long,
                                       maxScore: Double
)

object AddressByRHPostcodeResponse {
  implicit lazy val addressByRHPostcodeResponseFormat: Format[AddressByRHPostcodeResponse] = Json.format[AddressByRHPostcodeResponse]
}
