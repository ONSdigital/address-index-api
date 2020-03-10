package uk.gov.ons.addressIndex.model.server.response.eq

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.db.index.{HybridAddress, NationalAddressGazetteerAddress}
import uk.gov.ons.addressIndex.model.server.response.address.AddressResponseAddress.chooseMostRecentNag
import uk.gov.ons.addressIndex.model.server.response.address.AddressResponseAddressUPRNEQ

import scala.collection.SortedMap

/**
  * Contains relevant information to the requested address
  *
  * @param address found address
  */
case class AddressByEqUprnResponse(address: Option[AddressResponseAddressUPRNEQ],
                                   addressType: String,
                                   historical: Boolean,
                                   epoch: String,
                                   verbose: Boolean)

object AddressByEqUprnResponse {
  implicit lazy val addressByEqUprnResponseFormat: Format[AddressByEqUprnResponse] = Json.format[AddressByEqUprnResponse]

  /**
    * Transforms hybrid object returned by ES into an Address that will be in the json response
    *
    * @param other HybridAddress from ES
    * @return
    */
  def fromHybridAddress(other: HybridAddress, verbose: Boolean, addressType: String): AddressResponseAddressUPRNEQ = {

    val chosenNag = chooseMostRecentNag(other.lpi, NationalAddressGazetteerAddress.Languages.english)
    val formattedAddressNag = chosenNag.map(_.mixedNag).getOrElse(chosenNag.map(_.mixedWelshNag).getOrElse(""))

    val chosenWelshNag = chooseMostRecentNag(other.lpi, NationalAddressGazetteerAddress.Languages.welsh)
    val welshFormattedAddressNag = chosenWelshNag.map(_.mixedWelshNag).getOrElse("")

    val chosenPaf = other.paf.headOption
    val formattedAddressPaf = chosenPaf.map(_.mixedPaf).getOrElse("")
    val welshFormattedAddressPaf = chosenPaf.map(_.mixedWelshPaf).getOrElse("")

    val chosenNisra = other.nisra.headOption
    val formattedAddressNisra = chosenNisra.map(_.mixedNisra).getOrElse("")

    val formattedAddress = addressType match {
      case "PAF" => formattedAddressPaf
      case "WELSHPAF" => welshFormattedAddressPaf
      case "NAG" => formattedAddressNag
      case "WELSHNAG" => welshFormattedAddressNag
      case "NISRA" => if (chosenNisra.isEmpty) formattedAddressNag else formattedAddressNisra
    }

    val townName = addressType match {
      case "PAF" => chosenPaf match {
        case Some(pafAddress) => pafAddress.postTown
        case None => ""
      }
      case "WELSHPAF" => chosenPaf match {
        case Some(pafAddress) => pafAddress.welshPostTown
        case None => ""
      }
      case "NAG" => chosenNag match {
        case Some(nagAddress) => nagAddress.townName
        case None => ""
      }
      case "WELSHNAG" => chosenWelshNag match {
        case Some(nagAddress) => nagAddress.townName
        case None => ""
      }
      case "NISRA" => chosenNisra match {
        case Some(nisraAddress) => nisraAddress.postTown
        case None => ""
      }
    }

    val postcode = addressType match {
      case "PAF" | "WELSHPAF" => chosenPaf match {
        case Some(pafAddress) => pafAddress.postcode
        case None => ""
      }
      case "NAG" => chosenNag match {
        case Some(nagAddress) => nagAddress.postcodeLocator
        case None => ""
      }
      case "WELSHNAG" => chosenWelshNag match {
        case Some(nagAddress) => nagAddress.postcodeLocator
        case None => ""
      }
      case "NISRA" => chosenNisra match {
        case Some(nisraAddress) => nisraAddress.postcode
        case None => ""
      }
    }

    val addressLines = splitFormattedAddress(formattedAddress, townName, postcode)

    AddressResponseAddressUPRNEQ(
      uprn = other.uprn,
      formattedAddress = formattedAddress,
      addressLine1 = addressLines.getOrElse("addressLine1", ""),
      addressLine2 = addressLines.getOrElse("addressLine2", ""),
      addressLine3 = addressLines.getOrElse("addressLine3", ""),
      townName = townName,
      postcode = postcode
    )
  }

  /**
    * A temporary best endeavour approach until Neil's magic algorithm is revealed.
    *
    * @param formattedAddress
    * @param townName
    * @param postcode
    * @return
    */
  def splitFormattedAddress(formattedAddress: String, townName: String, postcode: String): Map[String, String] = {

    // Split the formattedAddress by comma and remove townName and postcode as they have their own attribute
    val split: Seq[String] = formattedAddress.replace(townName, "")
      .replace(postcode, "")
      .split(",")
      .map(_.trim)
      .filter(_.nonEmpty)

    // addressLine1 and addressLine2
    val part1: Seq[String] = split.take(2)
    // All the other address lines
    val part2: Seq[String] = split.drop(2)

    // As addressLine3 is the last one combine the rest of the address parts
    val addressLine3: Seq[String] = if (part2.nonEmpty) Seq(part2.mkString(", ").trim) else Seq()
    val addressLines: Seq[String] = Seq(part1, addressLine3).flatten

    // Create a map of address parts
    val addressMap: Map[Int, String] = (1 to addressLines.size).zip(addressLines).toMap

    // Return a map with appropriately named keys
    addressMap.map { newMap: (Int, String) => newMap match {
        case (key, value) => (s"addressLine$key", value)
      }
    }
  }
}


