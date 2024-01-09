package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

/**
  * Contains relevant, to the address request, data
  *
  * @param tokens    address decomposed into relevant parts (building number, city, street, etc.)
  * @param addresses found addresses
  * @param limit     max number of found addresses
  * @param offset    offset of found addresses (for pagination)
  * @param total     total number of found addresses
  */
case class AddressBySearchResponseIDS(addresses: Seq[AddressResponseAddressIDS],
                                      matchtype: String,
                                      recommendationCode: String
)

object AddressBySearchResponseIDS {
  implicit lazy val addressBySearchResponseIDSFormat: Format[AddressBySearchResponseIDS] = Json.format[AddressBySearchResponseIDS]
}
