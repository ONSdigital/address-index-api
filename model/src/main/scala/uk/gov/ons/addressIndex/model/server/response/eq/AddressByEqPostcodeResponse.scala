package uk.gov.ons.addressIndex.model.server.response.eq

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.server.response.address.AddressResponseAddressPostcodeEQ

/**
  * Contains relevant, to the address request, data
  *
  * @param postcode postcode from query
  * @param addresses found addresses
  * @param filter classification filter
  * @param historical ES index choice
  * @param epoch which AB Epoch to use
  * @param limit max number of found addresses
  * @param offset offset of found addresses (for pagination)
  * @param total total number of found addresses
  * @param maxScore the max score
  * @param verbose output type
  */
case class AddressByEqPostcodeResponse(postcode: String,
                                       addresses: Seq[AddressResponseAddressPostcodeEQ],
                                       filter: String,
                                       historical: Boolean,
                                       epoch: String,
                                       limit: Int,
                                       offset: Int,
                                       total: Long,
                                       maxScore: Double,
                                       verbose: Boolean)

object AddressByEqPostcodeResponse {
  implicit lazy val addressByEqPostcodeResponseFormat: Format[AddressByEqPostcodeResponse] = Json.format[AddressByEqPostcodeResponse]
}
