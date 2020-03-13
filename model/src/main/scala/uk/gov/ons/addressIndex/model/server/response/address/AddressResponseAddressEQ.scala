package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.db.index.{HybridAddress, NationalAddressGazetteerAddress}
import uk.gov.ons.addressIndex.model.server.response.address.AddressResponseAddress.{chooseMostRecentNag, removeConcatenatedPostcode}

/**
  * Contains address information retrieved in ES relevant to EQ - initial result, actual result is extracted from this
  *
  * @param uprn address UPRN
  * @param formattedAddress the chosen formatted address
  * @param highlights matching hightlights if on
  * @param confidenceScore the confidence score
  * @param underlyingScore the underlying score
  */
case class AddressResponseAddressEQ(uprn: String,
                                    formattedAddress: String,
                                    highlights: Option[AddressResponseHighlight],
                                    confidenceScore: Double,
                                    underlyingScore: Float)

object AddressResponseAddressEQ {
  implicit lazy val addressResponseAddressEQFormat: Format[AddressResponseAddressEQ] = Json.format[AddressResponseAddressEQ]

  /**
    * Transforms hybrid object returned by ES into an Address that will be in the json response
    *
    * @param other HybridAddress from ES
    * @return
    */
  def fromHybridAddress(other: HybridAddress, favourPaf: Boolean, favourWelsh: Boolean, verbose: Boolean): AddressResponseAddressEQ = {

    val chosenNag = chooseMostRecentNag(other.lpi, NationalAddressGazetteerAddress.Languages.english)
    val formattedAddressNag = chosenNag.map(_.mixedNag).getOrElse(chosenNag.map(_.mixedWelshNag).getOrElse(""))

    val chosenWelshNag = chooseMostRecentNag(other.lpi, NationalAddressGazetteerAddress.Languages.welsh)
    val welshFormattedAddressNag = chosenWelshNag.map(_.mixedWelshNag).getOrElse("")

    val chosenPaf = other.paf.headOption
    val formattedAddressPaf = chosenPaf.map(_.mixedPaf).getOrElse("")
    val welshFormattedAddressPaf = chosenPaf.map(_.mixedWelshPaf).getOrElse("")

    val chosenNisra = other.nisra.headOption
    val formattedAddressNisra = chosenNisra.map(_.mixedNisra).getOrElse("")

    val testHigh = other.highlights.headOption.getOrElse(Map()) == Map()

    //Rules: PAF may not exist, NAG always exists but not necessarily WELSHNAG, if chosenNisra is not empty return that
    val formattedAddress =
      if (favourPaf) {
        if (favourWelsh) {
          if (welshFormattedAddressPaf.isEmpty) {
            if (welshFormattedAddressNag.isEmpty) formattedAddressNag else welshFormattedAddressNag
          } else {
            welshFormattedAddressPaf
          }
        } else {
          if (formattedAddressPaf.isEmpty) {
            formattedAddressNag
          } else {
            formattedAddressPaf
          }
        }
      } else {
        if (favourWelsh) {
          if (chosenNisra.isEmpty) {
            if (welshFormattedAddressNag.isEmpty) formattedAddressNag else welshFormattedAddressNag
          } else {
            formattedAddressNisra
          }
        } else {
          if (chosenNisra.isEmpty) formattedAddressNag else formattedAddressNisra
        }
      }

    AddressResponseAddressEQ(
      uprn = other.uprn,
      formattedAddress = removeConcatenatedPostcode(formattedAddress),
      highlights = if (testHigh) None else AddressResponseHighlight.fromHighlight("formattedAddress", other.highlights.headOption.getOrElse(Map())),
      confidenceScore = 100D,
      underlyingScore = if (other.distance == 0) other.score else (other.distance / 1000).toFloat
    )
  }
}