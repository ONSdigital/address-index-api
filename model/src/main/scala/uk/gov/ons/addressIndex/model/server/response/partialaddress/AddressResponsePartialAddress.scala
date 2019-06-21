package uk.gov.ons.addressIndex.model.server.response.partialaddress

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.db.index.NationalAddressGazetteerAddress

object AddressResponsePartialAddress {
  implicit lazy val addressResponsePartialAddressFormat: Format[AddressResponsePartialAddress] = Json.format[AddressResponsePartialAddress]
}

/**
  * Contains address information retrieved in ES (PAF or NAG)
  *
  * @param uprn             uprn
  * @param formattedAddress cannonical address form
  * @param formattedAddressNag optional, information from Nag index
  * @param formattedAddressPaf optional, information from Paf index
  * @param welshFormattedAddressNag optional, information from Nag index
  * @param welshFormattedAddressPaf optional, information from Paf index
  * @param underlyingScore  score from elastic search
  *
  */
case class AddressResponsePartialAddress(uprn: String,
                                         formattedAddress: String,
                                         formattedAddressNag: String,
                                         formattedAddressPaf: String,
                                         welshFormattedAddressNag: String,
                                         welshFormattedAddressPaf: String,
                                         underlyingScore: Float)
