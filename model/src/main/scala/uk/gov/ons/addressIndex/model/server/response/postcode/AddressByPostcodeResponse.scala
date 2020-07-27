package uk.gov.ons.addressIndex.model.server.response.postcode

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.server.response.address.AddressResponseAddress

/**
  * Contains relevant, to the address request, data
  *
  * @param postcode  postcode from query
  * @param addresses found addresses
  * @param limit     max number of found addresses
  * @param offset    offset of found addresses (for pagination)
  * @param total     total number of found addresses
  */
case class AddressByPostcodeResponse(postcode: String,
                                     addresses: Seq[AddressResponseAddress],
                                     filter: String,
                                     historical: Boolean,
                                     epoch: String,
                                     limit: Int,
                                     offset: Int,
                                     total: Long,
                                     maxScore: Double,
                                     verbose: Boolean,
                                     includeAuxiliarySearch: Boolean = false)

object AddressByPostcodeResponse {
  implicit lazy val addressByPostcodeResponseFormat: Format[AddressByPostcodeResponse] = Json.format[AddressByPostcodeResponse]
}
