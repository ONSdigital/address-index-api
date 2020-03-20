package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

/**
  * Contains address information retrieved in ES (PAF, NAG or NISRA)
  *
  * @param uprn the UPRN
  * @param bestMatchAddress the bestMatchAddress
  * @param bestMatchAddressType the type of address (PAF, WELSHPAF, NAG, WELSHNAG & NISRA)
  */
case class AddressResponseAddressCustomRH(uprn: String,
                                          bestMatchAddress: String,
                                          bestMatchAddressType: String)

object AddressResponseAddressCustomRH {
  implicit lazy val addressResponseAddressRHFormat: Format[AddressResponseAddressCustomRH] = Json.format[AddressResponseAddressCustomRH]

  def fromAddressResponseAddressRH(address: AddressResponseAddressRH): AddressResponseAddressCustomRH = {

    val bestMatchAddress: String = address.highlights match {
      case Some(highlight) => highlight.bestMatchAddress
      case None => ""
    }

    val bestMatchAddressType: String = address.highlights match {
      case Some(highlight) if highlight.source == "P" && highlight.lang == "E" => AddressResponseAddress.AddressTypes.paf
      case Some(highlight) if highlight.source == "P" && highlight.lang == "W" => AddressResponseAddress.AddressTypes.welshPaf
      case Some(highlight) if highlight.source == "L" && highlight.lang == "E" => AddressResponseAddress.AddressTypes.nag
      case Some(highlight) if highlight.source == "L" && highlight.lang == "W" => AddressResponseAddress.AddressTypes.welshNag
      case Some(highlight) if highlight.source == "N" => AddressResponseAddress.AddressTypes.nisra
      case None => ""
    }

    AddressResponseAddressCustomRH(
      uprn = address.uprn,
      bestMatchAddress = bestMatchAddress,
      bestMatchAddressType = bestMatchAddressType
    )
  }
}
