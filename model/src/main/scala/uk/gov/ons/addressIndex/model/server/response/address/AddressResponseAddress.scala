package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.db.index.{HybridAddress, HybridAddressPartial, NationalAddressGazetteerAddress, PostcodeAddressFileAddress}

/**
  * Contains address information retrieved in ES (PAF or NAG)
  *
  * @param uprn               uprn
  * @param formattedAddress   cannonical address form
  * @param paf                optional, information from Paf index
  * @param nag                optional, information from Nag index
  * @param underlyingScore    score from elastic search
  *
  */
case class AddressResponseAddress(
  uprn: String,
  parentUprn: String,
  relatives: Option[Seq[AddressResponseRelative]],
  crossRefs: Option[Seq[AddressResponseCrossRef]],
  formattedAddress: String,
  formattedAddressNag: String,
  formattedAddressPaf: String,
  welshFormattedAddressNag: String,
  welshFormattedAddressPaf: String,
  paf: Option[AddressResponsePaf],
  nag: Option[Seq[AddressResponseNag]],
  geo: Option[AddressResponseGeo],
  classificationCode: String,
  lpiLogicalStatus: String,
  confidenceScore: Double,
  underlyingScore: Float,
)

object AddressResponseAddress {
  implicit lazy val addressResponseAddressFormat: Format[AddressResponseAddress] = Json.format[AddressResponseAddress]

  /**
    * Transforms hybrid object returned by ES into an Address that will be in the json response
    * @param other HybridAddress from ES
    * @return
    */
  def fromHybridAddress(other: HybridAddress, verbose: Boolean): AddressResponseAddress = {

    val chosenNag: Option[NationalAddressGazetteerAddress] = chooseMostRecentNag(other.lpi, NationalAddressGazetteerAddress.Languages.english)
    val formattedAddressNag = if (chosenNag.isEmpty) "" else chosenNag.get.mixedNag
    val lpiLogicalStatus = if (chosenNag.isEmpty) "" else chosenNag.get.lpiLogicalStatus

    val chosenWelshNag: Option[NationalAddressGazetteerAddress] = chooseMostRecentNag(other.lpi, NationalAddressGazetteerAddress.Languages.welsh)
    val welshFormattedAddressNag = if (chosenWelshNag.isEmpty) "" else chosenWelshNag.get.mixedNag

    val chosenPaf: Option[PostcodeAddressFileAddress] =  other.paf.headOption
    val formattedAddressPaf = if (chosenPaf.isEmpty) "" else chosenPaf.get.mixedPaf
    val welshFormattedAddressPaf = if (chosenPaf.isEmpty) "" else chosenPaf.get.mixedWelshPaf

    AddressResponseAddress(
      uprn = other.uprn,
      parentUprn = other.parentUprn,
      relatives = {if (verbose) Some(other.relatives.map(AddressResponseRelative.fromRelative)) else None},
      crossRefs = {if (verbose) Some(other.crossRefs.map(AddressResponseCrossRef.fromCrossRef)) else None},
      formattedAddress = formattedAddressNag,
      formattedAddressNag = formattedAddressNag,
      formattedAddressPaf = formattedAddressPaf,
      welshFormattedAddressNag = welshFormattedAddressNag,
      welshFormattedAddressPaf = welshFormattedAddressPaf,
      paf = {if (verbose) chosenPaf.map(AddressResponsePaf.fromPafAddress) else None},
      nag = {if (verbose) Some(other.lpi.map(AddressResponseNag.fromNagAddress).sortBy(_.logicalStatus)) else None},
      geo = chosenNag.flatMap(AddressResponseGeo.fromNagAddress),
      classificationCode = other.classificationCode,
      lpiLogicalStatus = lpiLogicalStatus,
      confidenceScore = 1D,
      underlyingScore = other.score

    )
  }

  /**
    * Transforms hybrid object returned by ES into an Address that will be in the json response
    * @param other HybridAddress from ES
    * @return
    */
  def fromHybridAddressPartial(other: HybridAddressPartial, verbose: Boolean): AddressResponseAddress = {

    val chosenNag: Option[NationalAddressGazetteerAddress] = chooseMostRecentNag(other.lpi, NationalAddressGazetteerAddress.Languages.english)
    val formattedAddressNag = if (chosenNag.isEmpty) "" else chosenNag.get.mixedNag
    val lpiLogicalStatus = if (chosenNag.isEmpty) "" else chosenNag.get.lpiLogicalStatus

    val chosenWelshNag: Option[NationalAddressGazetteerAddress] = chooseMostRecentNag(other.lpi, NationalAddressGazetteerAddress.Languages.welsh)
    val welshFormattedAddressNag = if (chosenWelshNag.isEmpty) "" else chosenWelshNag.get.mixedNag

    val chosenPaf: Option[PostcodeAddressFileAddress] =  other.paf.headOption
    val formattedAddressPaf = if (chosenPaf.isEmpty) "" else chosenPaf.get.mixedPaf
    val welshFormattedAddressPaf = if (chosenPaf.isEmpty) "" else chosenPaf.get.mixedWelshPaf

    AddressResponseAddress(
      uprn = other.uprn,
      parentUprn = other.parentUprn,
      relatives = None,
      crossRefs = None,
      formattedAddress = formattedAddressNag,
      formattedAddressNag = formattedAddressNag,
      formattedAddressPaf = formattedAddressPaf,
      welshFormattedAddressNag = welshFormattedAddressNag,
      welshFormattedAddressPaf = welshFormattedAddressPaf,
      paf = {if (verbose) chosenPaf.map(AddressResponsePaf.fromPafAddress) else None},
      nag = {if (verbose) Some(other.lpi.map(AddressResponseNag.fromNagAddress).sortBy(_.logicalStatus)) else None},
      geo = chosenNag.flatMap(AddressResponseGeo.fromNagAddress),
      classificationCode = other.classificationCode,
      lpiLogicalStatus = lpiLogicalStatus,
      confidenceScore = 1D,
      underlyingScore = other.score
    )
  }

  /**
    * Gets the right (most often - the most recent) address from an array of NAG addresses
    * @param addresses list of Nag addresses
    * @return the NAG address that corresponds to the returned address
    */
  def chooseMostRecentNag(addresses: Seq[NationalAddressGazetteerAddress], language: String): Option[NationalAddressGazetteerAddress] ={
    // "if" is more readable than "getOrElse" in this case
    if (addresses.exists(address => address.lpiLogicalStatus == "1" && address.language == language ))
      addresses.find(_.lpiLogicalStatus == "1")
    else if (addresses.exists(address => address.lpiLogicalStatus == "6" && address.language == language))
      addresses.find(_.lpiLogicalStatus == "6")
    else if (addresses.exists(address => address.lpiLogicalStatus == "8" && address.language == language))
      addresses.find(_.lpiLogicalStatus == "8")
    else if (addresses.exists(address => address.lpiLogicalStatus == "1"))
      addresses.find(_.lpiLogicalStatus == "1")
    else if (addresses.exists(address => address.lpiLogicalStatus == "6"))
      addresses.find(_.lpiLogicalStatus == "6")
    else if (addresses.exists(address => address.lpiLogicalStatus == "8"))
      addresses.find(_.lpiLogicalStatus == "8")
    else addresses.headOption
  }
}
