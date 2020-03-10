package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.db.index.{HybridAddress, NationalAddressGazetteerAddress}
import uk.gov.ons.addressIndex.model.server.response.address.AddressResponseAddress.chooseMostRecentNag

/**
  * Contains address information retrieved in ES (PAF or NAG)
  *
  * @param uprn             uprn
  * @param formattedAddress cannonical address form
  *
  */
case class AddressResponseAddressEQ(uprn: String,
                                    formattedAddress: String,
                                    highlights: Option[AddressResponseHighlight],
                                    underlyingScore: Float)

object AddressResponseAddressEQ {
  implicit lazy val addressResponseAddressEQFormat: Format[AddressResponseAddressEQ] = Json.format[AddressResponseAddressEQ]

  /**
    * Transforms hybrid object returned by ES into an Address that will be in the json response
    *
    * @param other HybridAddress from ES
    * @return
    */
  def fromHybridAddress(other: HybridAddress, verbose: Boolean): AddressResponseAddressEQ = {

    val chosenNag = chooseMostRecentNag(other.lpi, NationalAddressGazetteerAddress.Languages.english)
    val formattedAddressNag = chosenNag.map(_.mixedNag).getOrElse(chosenNag.map(_.mixedWelshNag).getOrElse(""))

    val chosenNisra = other.nisra.headOption
    val formattedAddressNisra = chosenNisra.map(_.mixedNisra).getOrElse("")

    val testHigh = other.highlights.headOption.getOrElse(Map()) == Map()

    AddressResponseAddressEQ(
      uprn = other.uprn,
      formattedAddress = {
        if (chosenNisra.isEmpty) formattedAddressNag else formattedAddressNisra
      },
      highlights = if (testHigh) None else AddressResponseHighlight.fromHighlight("formattedAddress", other.highlights.headOption.getOrElse(Map())),
      underlyingScore = if (other.distance == 0) other.score else (other.distance / 1000).toFloat
    )
  }
}