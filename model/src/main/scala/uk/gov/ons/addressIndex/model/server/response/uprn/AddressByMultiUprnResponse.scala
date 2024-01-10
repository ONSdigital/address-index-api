package uk.gov.ons.addressIndex.model.server.response.uprn

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseAddress, AddressResponseAddressNonIDS}

/**
  * Contains relevant information to the requested address
  *
  * @param address found address
  */
case class AddressByMultiUprnResponse(addresses: Seq[AddressResponseAddressNonIDS],
                                      historical: Boolean,
                                      epoch: String,
                                      verbose: Boolean,
                                      pafdefault: Boolean)


object AddressByMultiUprnResponse {
  implicit lazy val addressByMultiUprnResponseFormat: Format[AddressByMultiUprnResponse] = Json.format[AddressByMultiUprnResponse]
}
