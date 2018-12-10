package uk.gov.ons.addressIndex.model.server.response.random

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.server.response.address.AddressResponseAddress

/**
  * Contains relevant, to the address request, data
  *
  * @param addresses found addresses
  * @param limit     max number of found addresses
  * @param total     total number of found addresses
  */
case class AddressByRandomResponse(
                                      addresses: Seq[AddressResponseAddress],
                                      filter: String,
                                      historical: Boolean,
                                      limit: Int,
                                      verbose: Boolean
                                    )

object AddressByRandomResponse {
  implicit lazy val addressByRandomResponseFormat: Format[AddressByRandomResponse] = Json.format[AddressByRandomResponse]
}
