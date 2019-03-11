package uk.gov.ons.addressIndex.model.server.response.partialaddress

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.db.index.{HybridAddress, NationalAddressGazetteerAddress, PostcodeAddressFileAddress}

object AddressResponsePartialAddress {
  implicit lazy val addressResponsePartialAddressFormat: Format[AddressResponsePartialAddress] = Json.format[AddressResponsePartialAddress]

  /**
    * Transforms hybrid object returned by ES into an Address that will be in the json response
    *
    * @param other HybridAddress from ES
    * @return
    */
  def fromHybridAddress(other: HybridAddress): AddressResponsePartialAddress = {
    val chosenNag: Option[NationalAddressGazetteerAddress] = chooseMostRecentNag(other.lpi, NationalAddressGazetteerAddress.Languages.english)
    val formattedAddressNag = chosenNag.map(_.mixedNag).getOrElse("")

    val chosenWelshNag: Option[NationalAddressGazetteerAddress] = chooseMostRecentNag(other.lpi, NationalAddressGazetteerAddress.Languages.welsh)
    val welshFormattedAddressNag = chosenWelshNag.map(_.mixedNag).getOrElse("")

    val chosenPaf: Option[PostcodeAddressFileAddress] = other.paf.headOption
    val formattedAddressPaf = chosenPaf.map(_.mixedPaf).getOrElse("")
    val welshFormattedAddressPaf = chosenPaf.map(_.mixedWelshPaf).getOrElse("")

    AddressResponsePartialAddress(
      uprn = other.uprn,
      formattedAddress = formattedAddressNag,
      formattedAddressNag = formattedAddressNag,
      formattedAddressPaf = formattedAddressPaf,
      welshFormattedAddressNag = welshFormattedAddressNag,
      welshFormattedAddressPaf = welshFormattedAddressPaf,
      underlyingScore = other.score
    )
  }

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
  * @param paf              optional, information from Paf index
  * @param nag              optional, information from Nag index
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
