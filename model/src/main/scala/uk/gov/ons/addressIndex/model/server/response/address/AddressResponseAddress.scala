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
                                  censusAddressType: String,
                                  censusEstabType: String,
                                  countryCode: String,
                                  lpiLogicalStatus: String,
                                  confidenceScore: Double,
                                  underlyingScore: Float
                                 )

object AddressResponseAddress {
  implicit lazy val addressResponseAddressFormat: Format[AddressResponseAddress] = Json.format[AddressResponseAddress]

  object AddressTypes extends Enumeration {
    type AddressType = String

    val paf = "PAF"
    val welshPaf = "WELSHPAF"
    val nag = "NAG"
    val welshNag = "WELSHNAG"
    val nisra = "NISRA"
  }

  /**
    * Transforms hybrid object returned by ES into an Address that will be in the json response
    *
    * @param other HybridAddress from ES
    * @return
    */
  def fromHybridAddress(other: HybridAddress, verbose: Boolean): AddressResponseAddress = {

    val chosenNag = chooseMostRecentNag(other.lpi, NationalAddressGazetteerAddress.Languages.english)
    val formattedAddressNag = chosenNag.map(_.mixedNag).getOrElse(chosenNag.map(_.mixedWelshNag).getOrElse(""))
    val lpiLogicalStatus = chosenNag.map(_.lpiLogicalStatus).getOrElse("")

    val chosenWelshNag = chooseMostRecentNag(other.lpi, NationalAddressGazetteerAddress.Languages.welsh)
    val welshFormattedAddressNag = chosenWelshNag.map(_.mixedWelshNag).getOrElse("")

    val chosenPaf = other.paf.headOption
    val formattedAddressPaf = chosenPaf.map(_.mixedPaf).getOrElse("")
    val welshFormattedAddressPaf = chosenPaf.map(_.mixedWelshPaf).getOrElse("")

    val chosenNisra = other.nisra.headOption
    val formattedAddressNisra = chosenNisra.map(_.mixedNisra).getOrElse("")

    val testHigh = other.highlights.headOption.getOrElse(Map()) == Map()

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
        if (chosenNisra.isEmpty) removeConcatenatedPostcode(formattedAddressNag) else removeConcatenatedPostcode(formattedAddressNisra)
      },
      formattedAddressNag = removeConcatenatedPostcode(formattedAddressNag),
      formattedAddressPaf = removeConcatenatedPostcode(formattedAddressPaf),
      formattedAddressNisra = removeConcatenatedPostcode(formattedAddressNisra),
      welshFormattedAddressNag = removeConcatenatedPostcode(welshFormattedAddressNag),
      welshFormattedAddressPaf = removeConcatenatedPostcode(welshFormattedAddressPaf),
      highlights = if (testHigh) None else AddressResponseHighlight.fromHighlight("formattedAddress",other.highlights.headOption.getOrElse(Map())),
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
      censusAddressType = other.censusAddressType,
      censusEstabType = other.censusEstabType,
      countryCode = other.countryCode,
      lpiLogicalStatus = lpiLogicalStatus,
      confidenceScore = 100D,
      underlyingScore = if (other.distance == 0) other.score else (other.distance/1000).toFloat
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

  def removeConcatenatedPostcode(formattedAddress: String) : String = {
    // if last token = last but two + last but one then remove last token
    val faTokens = formattedAddress.split(" ")
    val concatPostcode = faTokens.takeRight(1).headOption.getOrElse("")
    val faTokensTemp1 = faTokens.dropRight(1)
    val incode = faTokensTemp1.takeRight(1).headOption.getOrElse("")
    val faTokensTemp2 =  faTokensTemp1.dropRight(1)
    val outcode = faTokensTemp2.takeRight(1).headOption.getOrElse("")
    val testCode = outcode + incode
    if (testCode.equals(concatPostcode))
      formattedAddress.replaceAll(concatPostcode,"").trim()
    else formattedAddress
  }

  def removeEms(formattedAddress: String) : String = {
    formattedAddress.replaceAll("<em>","").replaceAll("</em>","")
  }
}
