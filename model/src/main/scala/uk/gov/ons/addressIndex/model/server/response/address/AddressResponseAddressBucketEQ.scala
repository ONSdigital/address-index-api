package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.db.index.{HybridAddress, NationalAddressGazetteerAddress}
import uk.gov.ons.addressIndex.model.server.response.address.AddressResponseAddress.{AddressTypes, chooseMostRecentNag, removeConcatenatedPostcode}

/**
  * Contains address information retrieved in ES relevant to the EQ Postcode search
  *
  * @param uprn address UPRN
  * @param formattedAddress the chosen formatted address
  * @param addressType the type of address (PAF, WELSHPAF, NAG, WELSHNAG & NISRA)
  */
case class AddressResponseAddressBucketEQ(uprn: String,
                                          formattedAddress: String,
                                          addressType: String)

object AddressResponseAddressBucketEQ {
  implicit lazy val addressResponseBucketEQFormat: Format[AddressResponseAddressBucketEQ] = Json.format[AddressResponseAddressBucketEQ]

  /**
    * Transforms hybrid object returned by ES into an Address that will be in the json response
    *
    * @param other HybridAddress from ES
    * @return
    */
  def fromHybridAddress(other: HybridAddress, favourPaf: Boolean, favourWelsh: Boolean): AddressResponseAddressBucketEQ = {

    val chosenNag = chooseMostRecentNag(other.lpi, NationalAddressGazetteerAddress.Languages.english)
    val formattedAddressNag = chosenNag.map(_.mixedNag).getOrElse(chosenNag.map(_.mixedWelshNag).getOrElse(""))

    val chosenWelshNag = chooseMostRecentNag(other.lpi, NationalAddressGazetteerAddress.Languages.welsh)
    val welshFormattedAddressNag = chosenWelshNag.map(_.mixedWelshNag).getOrElse("")

    val chosenPaf = other.paf.headOption
    val formattedAddressPaf = chosenPaf.map(_.mixedPaf).getOrElse("")
    val welshFormattedAddressPaf = chosenPaf.map(_.mixedWelshPaf).getOrElse("")

    //Rules: PAF may not exist, NAG always exists but not necessarily WELSHNAG, if chosenNisra is not empty return that
    val (formattedAddress, addressType) =
      if (favourPaf) {
        if (favourWelsh) {
          if (welshFormattedAddressPaf.isEmpty) {
            if (welshFormattedAddressNag.isEmpty) {
              (formattedAddressNag, AddressTypes.nag)
            }
            else (welshFormattedAddressNag, AddressTypes.welshNag)
          } else {
            (welshFormattedAddressPaf, AddressTypes.welshPaf)
          }
        } else {
          if (formattedAddressPaf.isEmpty) {
            (formattedAddressNag, AddressTypes.nag)
          } else {
            (formattedAddressPaf, AddressTypes.paf)
          }
        }
      } else {
        if (favourWelsh) {
            if (welshFormattedAddressNag.isEmpty) (formattedAddressNag, AddressTypes.nag) else (welshFormattedAddressNag, AddressTypes.welshNag)
        } else {
          (formattedAddressNag, AddressTypes.nag)
        }
      }

    AddressResponseAddressBucketEQ(
      uprn = other.uprn,
      formattedAddress = removeConcatenatedPostcode(formattedAddress),
      addressType = addressType
    )
  }
}

