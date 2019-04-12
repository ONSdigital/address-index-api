package uk.gov.ons.addressIndex.model.server.response.partialaddress

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.db.index.{NationalAddressGazetteerAddress, PostcodeAddressFileAddress}

object AddressResponsePartialAddress {
  implicit lazy val addressResponsePartialAddressFormat: Format[AddressResponsePartialAddress] = Json.format[AddressResponsePartialAddress]

  /**
    * Gets the right (most often - the most recent) address from an array of NAG addresses
    *
    * @param addresses list of Nag addresses
    * @return the NAG address that corresponds to the returned address
    */
  def chooseMostRecentNag(addresses: Seq[NationalAddressGazetteerAddress], language: String): Option[NationalAddressGazetteerAddress] = {
    addresses.find(addr => addr.lpiLogicalStatus == "1" && addr.language == language).
      orElse(addresses.find(addr => addr.lpiLogicalStatus == "6" && addr.language == language)).
      orElse(addresses.find(addr => addr.lpiLogicalStatus == "8" && addr.language == language)).
      orElse(addresses.find(addr => addr.lpiLogicalStatus == "1")).
      orElse(addresses.find(addr => addr.lpiLogicalStatus == "6")).
      orElse(addresses.find(addr => addr.lpiLogicalStatus == "8")).
      orElse(addresses.headOption)
  }
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
