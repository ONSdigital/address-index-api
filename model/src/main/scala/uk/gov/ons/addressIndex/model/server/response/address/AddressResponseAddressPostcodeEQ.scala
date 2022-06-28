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
case class AddressResponseAddressPostcodeEQ(onsAddressId: String,
                                            uprn: String,
                                            formattedAddress: String,
                                            addressType: String)

object AddressResponseAddressPostcodeEQ {
  implicit lazy val addressResponseAddressEQFormat: Format[AddressResponseAddressPostcodeEQ] = Json.format[AddressResponseAddressPostcodeEQ]

  /**
    * Transforms hybrid object returned by ES into an Address that will be in the json response
    *
    * @param other HybridAddress from ES
    * @return
    */
  def fromHybridAddress(other: HybridAddress, favourPaf: Boolean, favourWelsh: Boolean): AddressResponseAddressPostcodeEQ = {

    val chosenNag = chooseMostRecentNag(other.lpi, NationalAddressGazetteerAddress.Languages.english)
    val formattedAddressNag = chosenNag.map(_.mixedNag).getOrElse(chosenNag.map(_.mixedWelshNag).getOrElse(""))

    val chosenWelshNag = chooseMostRecentNag(other.lpi, NationalAddressGazetteerAddress.Languages.welsh)
    val welshFormattedAddressNag = chosenWelshNag.map(_.mixedWelshNag).getOrElse("")

    val chosenPaf = other.paf.headOption
    val formattedAddressPaf = chosenPaf.map(_.mixedPaf).getOrElse("")
    val welshFormattedAddressPaf = chosenPaf.map(_.mixedWelshPaf).getOrElse("")

    val chosenNisra = other.nisra.headOption
    val formattedAddressNisra = chosenNisra.map(_.mixedNisra).getOrElse("")

    //Rules: PAF may not exist, NAG always exists but not necessarily WELSHNAG, if chosenNisra is not empty return that
    val (formattedAddress, addressType) =
      if (favourPaf) {
        if (favourWelsh) {
          if (welshFormattedAddressPaf.isEmpty) {
            if (welshFormattedAddressNag.isEmpty) {
              if (chosenNisra.isEmpty) (formattedAddressNag, AddressTypes.nag) else (formattedAddressNisra, AddressTypes.nisra)
            }
            else (welshFormattedAddressNag, AddressTypes.welshNag)
          } else {
            (welshFormattedAddressPaf, AddressTypes.welshPaf)
          }
        } else {
          if (formattedAddressPaf.isEmpty) {
            if (chosenNisra.isEmpty) (formattedAddressNag, AddressTypes.nag) else (formattedAddressNisra, AddressTypes.nisra)
          } else {
            (formattedAddressPaf, AddressTypes.paf)
          }
        }
      } else {
        if (favourWelsh) {
          if (chosenNisra.isEmpty) {
            if (welshFormattedAddressNag.isEmpty) (formattedAddressNag, AddressTypes.nag) else (welshFormattedAddressNag, AddressTypes.welshNag)
          } else {
            (formattedAddressNisra, AddressTypes.nisra)
          }
        } else {
          if (chosenNisra.isEmpty) (formattedAddressNag, AddressTypes.nag) else (formattedAddressNisra, AddressTypes.nisra)
        }
      }

    AddressResponseAddressPostcodeEQ(
      onsAddressId = other.onsAddressId,
      uprn = other.uprn,
      formattedAddress = removeConcatenatedPostcode(formattedAddress),
      addressType = addressType
    )
  }
}