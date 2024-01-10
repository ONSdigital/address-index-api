package uk.gov.ons.addressIndex.model.server.response.uprn

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseAddress, AddressResponseAddressNonIDS}

/**
  * Contains relevant information to the requested address
  *
  * @param address found address
  */
case class AddressByUprnResponse(address: Option[AddressResponseAddressNonIDS],
                                 historical: Boolean,
                                 epoch: String,
                                 verbose: Boolean,
                                 pafdefault: Boolean)

object AddressByUprnResponse {
  implicit lazy val addressByUprnResponseFormat: Format[AddressByUprnResponse] = Json.format[AddressByUprnResponse]
}

