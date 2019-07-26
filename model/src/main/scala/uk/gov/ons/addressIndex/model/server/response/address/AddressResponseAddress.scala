package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.db.index._

/**
  * Contains address information retrieved in ES (PAF or NAG)
  *
  * @param uprn             uprn
  * @param formattedAddress cannonical address form
  * @param paf              optional, information from Paf index
  * @param nag              optional, information from Nag index
  * @param nisra            optional, information from Nisra index
  * @param underlyingScore  score from elastic search
  *
  */
case class AddressResponseAddress(uprn: String,
                                  parentUprn: String,
                                  relatives: Option[Seq[AddressResponseRelative]],
                                  crossRefs: Option[Seq[AddressResponseCrossRef]],
                                  formattedAddress: String,
                                  formattedAddressNag: String,
                                  formattedAddressPaf: String,
                                  formattedAddressNisra: String,
                                  welshFormattedAddressNag: String,
                                  welshFormattedAddressPaf: String,
                                  highlights: Option[AddressResponseHighlight],
                                  paf: Option[AddressResponsePaf],
                                  nag: Option[Seq[AddressResponseNag]],
                                  nisra: Option[AddressResponseNisra],
                                  geo: Option[AddressResponseGeo],
                                  classificationCode: String,
                                  lpiLogicalStatus: String,
                                  fromSource: String,
                                  confidenceScore: Double,
                                  underlyingScore: Float,
                                  bestMatchField: String)

object AddressResponseAddress {
  implicit lazy val addressResponseAddressFormat: Format[AddressResponseAddress] = Json.format[AddressResponseAddress]

  /**
    * Transforms hybrid object returned by ES into an Address that will be in the json response
    *
    * @param other HybridAddress from ES
    * @return
    */
  def fromHybridAddress(other: HybridAddress, verbose: Boolean): AddressResponseAddress = {

    val chosenNag = chooseMostRecentNag(other.lpi, NationalAddressGazetteerAddress.Languages.english)
    val formattedAddressNag = chosenNag.map(_.mixedNag).getOrElse("")
    val lpiLogicalStatus = chosenNag.map(_.lpiLogicalStatus).getOrElse("")

    val chosenWelshNag = chooseMostRecentNag(other.lpi, NationalAddressGazetteerAddress.Languages.welsh)
    val welshFormattedAddressNag = chosenWelshNag.map(_.mixedNag).getOrElse("")

    val chosenPaf = other.paf.headOption
    val formattedAddressPaf = chosenPaf.map(_.mixedPaf).getOrElse("")
    val welshFormattedAddressPaf = chosenPaf.map(_.mixedWelshPaf).getOrElse("")

    val chosenNisra = other.nisra.headOption
    val formattedAddressNisra = chosenNisra.map(_.mixedNisra).getOrElse("")

   // val formattedHighligts

    AddressResponseAddress(
      uprn = other.uprn,
      parentUprn = other.parentUprn,
      relatives = {
        if (verbose) other.relatives.map(_.map(AddressResponseRelative.fromRelative)) else None
      },
      crossRefs = {
        if (verbose) other.crossRefs.map(_.map(AddressResponseCrossRef.fromCrossRef)) else None
      },
      formattedAddress = {
        if (chosenNisra.isEmpty) formattedAddressNag else formattedAddressNisra
      },
      formattedAddressNag = formattedAddressNag,
      formattedAddressPaf = formattedAddressPaf,
      formattedAddressNisra = formattedAddressNisra,
      welshFormattedAddressNag = welshFormattedAddressNag,
      welshFormattedAddressPaf = welshFormattedAddressPaf,
      highlights = if (other.highlights.isEmpty) None else AddressResponseHighlight.fromHighlight(other.highlights.head),
      paf = {
        if (verbose) chosenPaf.map(AddressResponsePaf.fromPafAddress) else None
      },
      nag = {
        if (verbose) Some(other.lpi.map(AddressResponseNag.fromNagAddress).sortBy(_.logicalStatus)) else None
      },
      nisra = {
        if (verbose) chosenNisra.map(AddressResponseNisra.fromNisraAddress) else None
      },
      geo = {
        if (chosenNisra.isEmpty) chosenNag.flatMap(AddressResponseGeo.fromNagAddress) else chosenNisra.flatMap(AddressResponseGeo.fromNisraAddress)
      },
      classificationCode = other.classificationCode,
      lpiLogicalStatus = lpiLogicalStatus,
      fromSource = other.fromSource,
      confidenceScore = 1D,
      underlyingScore = other.score,
      bestMatchField = ""
    )
  }



  /**
    * Gets the right (most often - the most recent) address from an array of NAG addresses
    *
    * @param addresses list of Nag addresses
    * @return the NAG address that corresponds to the returned address
    */
  def chooseMostRecentNag(addresses: Seq[NationalAddressGazetteerAddress], language: String): Option[NationalAddressGazetteerAddress] = {
    addresses
      .filter(_.language == language)
      .sortBy(_.lpiLogicalStatus match {
        case "1" => 1
        case "6" => 2
        case "8" => 3
        case _ => 4
      })
      .headOption
  }
}
