package uk.gov.ons.addressIndex.model.server.response.rh

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.db.index.{HybridAddress, NationalAddressGazetteerAddress}
import uk.gov.ons.addressIndex.model.server.response.address.AddressResponseAddress.{AddressTypes, chooseMostRecentNag, removeConcatenatedPostcode}
import uk.gov.ons.addressIndex.model.server.response.address.AddressResponseAddressUPRNRH

/**
  * Contains relevant information to the requested address
  *
  * @param address found address
  * @param addressType the type of address (PAF, WELSHPAF, NAG, WELSHNAG & NISRA)
  * @param epoch AB Epoch
  */
case class AddressByRHUprnResponse(address: Option[AddressResponseAddressUPRNRH],
                                   addressType: String,
                                   epoch: String)


object AddressByRHUprnResponse {
  implicit lazy val addressByRHUprnResponseFormat: Format[AddressByRHUprnResponse] = Json.format[AddressByRHUprnResponse]

  /**
    * Transforms hybrid object returned by ES into an Address that will be in the json response
    *
    * @param other HybridAddress from ES
    * @return
    */
  def fromHybridAddress(other: HybridAddress, addressType: String): AddressResponseAddressUPRNRH = {

    val chosenNag = chooseMostRecentNag(other.lpi, NationalAddressGazetteerAddress.Languages.english)
    val formattedAddressNag = chosenNag.map(_.mixedNag).getOrElse(chosenNag.map(_.mixedWelshNag).getOrElse(""))

    val chosenWelshNag = chooseMostRecentNag(other.lpi, NationalAddressGazetteerAddress.Languages.welsh)
    val welshFormattedAddressNag = chosenWelshNag.map(_.mixedWelshNag).getOrElse("")

    val chosenPaf = other.paf.headOption
    val formattedAddressPaf = chosenPaf.map(_.mixedPaf).getOrElse("")
    val welshFormattedAddressPaf = chosenPaf.map(_.mixedWelshPaf).getOrElse("")

    val foundAddressTypeTemp = addressType match {
      case AddressTypes.paf => if (formattedAddressPaf.isEmpty) AddressTypes.nag else AddressTypes.paf
      case AddressTypes.welshPaf => if (welshFormattedAddressPaf.isEmpty)
                {if (welshFormattedAddressNag.isEmpty) AddressTypes.nag else AddressTypes.welshNag}
                  else AddressTypes.welshPaf
      case AddressTypes.nag => AddressTypes.nag
      case AddressTypes.welshNag => if (welshFormattedAddressNag.isEmpty) AddressTypes.nag else AddressTypes.welshNag
      case AddressTypes.nisra => AddressTypes.nag
    }

    val foundAddressType = foundAddressTypeTemp

    val townName = foundAddressType match {
      case AddressTypes.paf => chosenPaf match {
        case Some(pafAddress) => pafAddress.postTown
        case None => ""
      }
      case AddressTypes.welshPaf => chosenPaf match {
        case Some(pafAddress) => pafAddress.welshPostTown
        case None => ""
      }
      case AddressTypes.nag => chosenNag match {
        case Some(nagAddress) => nagAddress.townName
        case None => ""
      }
      case AddressTypes.welshNag => chosenWelshNag match {
        case Some(nagAddress) => nagAddress.townName
        case None => ""
      }
      case AddressTypes.nisra => chosenNag match {
        case Some(nagAddress) => nagAddress.townName
        case None => ""
      }
    }

    val organisationName = foundAddressType match {
      case AddressTypes.paf => chosenPaf match {
        case Some(pafAddress) => pafAddress.organisationName
        case None => ""
      }
      case AddressTypes.welshPaf => chosenPaf match {
        case Some(pafAddress) => pafAddress.organisationName
        case None => ""
      }
      case AddressTypes.nag => chosenNag match {
        case Some(nagAddress) => nagAddress.organisation
        case None => ""
      }
      case AddressTypes.welshNag => chosenWelshNag match {
        case Some(nagAddress) => nagAddress.organisation
        case None => ""
      }
      case AddressTypes.nisra => chosenNag match {
        case Some(nagAddress) => nagAddress.organisation
        case None => ""
      }
    }

    val postcode = foundAddressType match {
      case AddressTypes.paf | AddressTypes.welshPaf => chosenPaf match {
        case Some(pafAddress) => pafAddress.postcode
        case None => ""
      }
      case AddressTypes.nag => chosenNag match {
        case Some(nagAddress) => nagAddress.postcodeLocator
        case None => ""
      }
      case AddressTypes.welshNag => chosenWelshNag match {
        case Some(nagAddress) => nagAddress.postcodeLocator
        case None => ""
      }
      case AddressTypes.nisra => chosenNag match {
        case Some(nagAddress) => nagAddress.postcodeLocator
        case None => ""
      }
    }

    val formattedAddress = foundAddressType match {
      case AddressTypes.paf => formattedAddressPaf
      case AddressTypes.welshPaf => welshFormattedAddressPaf
      case AddressTypes.nag => formattedAddressNag
      case AddressTypes.welshNag => welshFormattedAddressNag
      case AddressTypes.nisra => formattedAddressNag
    }

    val addressLines = foundAddressType match {
      case AddressTypes.nisra => formatAddressLines(Nil, removeConcatenatedPostcode(formattedAddress), townName, postcode)
      case _ => formatAddressLines(Nil, removeConcatenatedPostcode(formattedAddress), townName, postcode)
    }

    AddressResponseAddressUPRNRH(
      uprn = other.uprn,
      formattedAddress = removeConcatenatedPostcode(formattedAddress),
      addressLine1 = addressLines.getOrElse("addressLine1", ""),
      addressLine2 = addressLines.getOrElse("addressLine2", ""),
      addressLine3 = addressLines.getOrElse("addressLine3", ""),
      townName = townName,
      postcode = postcode,
      foundAddressType = foundAddressType,
      censusAddressType = other.censusAddressType.trim,
      censusEstabType = other.censusEstabType,
      countryCode = other.countryCode,
      organisationName = organisationName
    )
  }

  /**
    * A temporary best endeavour approach until Neil H's magic algorithm is revealed.
    *
    * @param addressLines previously determined address lines if available
    * @param formattedAddress formattedAddress
    * @param townName previously determined town name
    * @param postcode previously determined postcode
    * @return a Map of addressLines
    */
  def formatAddressLines(addressLines: Seq[String], formattedAddress: String, townName: String, postcode: String): Map[String, String] = {

    val extractedAddressLines: Seq[String] = if (addressLines.isEmpty)
      // Split the formattedAddress by comma and remove postcode as it has its own attribute
      formattedAddress.replace(postcode, "").split(",")
      else addressLines

    // Remove townName as it has its own attribute
    val split: Seq[String] = extractedAddressLines
      .map(_.trim)
      .filter(_.nonEmpty)
      .filter(p => !p.contentEquals(townName)) // Filter out exact townName entries as these are likely to be the townName and not part of the address that includes the town name

    // addressLine1 and addressLine2
    val part1: Seq[String] = split.take(2)
    // All the other address lines
    val part2: Seq[String] = split.drop(2)

    // As addressLine3 is the last one combine the rest of the address parts
    val addressLine3: Seq[String] = if (part2.nonEmpty) Seq(part2.mkString(", ").trim) else Seq()
    val formattedAddressLines: Seq[String] = Seq(part1, addressLine3).flatten

    // Create a map of address parts (Integer key)
    val addressMap: Map[Int, String] = (1 to formattedAddressLines.size).zip(formattedAddressLines).toMap

    // Return a map with appropriately named keys
    addressMap.map { newMap: (Int, String) =>
      newMap match {
        case (key, value) => (s"addressLine$key", value)
      }
    }
  }
}

