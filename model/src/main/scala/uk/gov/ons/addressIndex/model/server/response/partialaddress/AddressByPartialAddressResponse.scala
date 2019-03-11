package uk.gov.ons.addressIndex.model.server.response.partialaddress

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.server.response.address.AddressResponseAddress

/**
  * Contains relevant, to the address request, data
  *
  * @param input     input from query
  * @param addresses found addresses
  * @param limit     max number of found addresses
  * @param offset    offset of found addresses (for pagination)
  * @param total     total number of found addresses
  */
case class AddressByPartialAddressResponse(input: String,
                                           addresses: Seq[AddressResponseAddress],
                                           filter: String,
                                           historical: Boolean,
                                           epoch: String,
                                           limit: Int,
                                           offset: Int,
                                           total: Long,
                                           maxScore: Double,
                                           startDate: String,
                                           endDate: String,
                                           verbose: Boolean)

object AddressByPartialAddressResponse {
  implicit lazy val addressByPartialAddressResponseFormat: Format[AddressByPartialAddressResponse] = Json.format[AddressByPartialAddressResponse]
}
