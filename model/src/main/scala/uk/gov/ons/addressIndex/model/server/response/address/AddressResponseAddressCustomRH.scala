package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

/**
  * Contains address information retrieved in ES (PAF, NAG or NISRA)
  *
  * @param uprn the UPRN
  * @param bestMatchAddress the bestMatchAddress
  * @param bestMatchAddressType the type of address (PAF, WELSHPAF, NAG, WELSHNAG & NISRA)
  * @param censusAddressType census bespoke address type derived from ABP code
  * @param censusEstabType census bespoke establishment type derived from ABP code
  * @param countryCode E="England" W="Wales" S="Scotland" N="Northern Ireland"
  */
case class AddressResponseAddressCustomRH(uprn: String,
                                          bestMatchAddress: String,
                                          bestMatchAddressType: String,
                                          censusAddressType: String,
                                          censusEstabType: String,
                                          countryCode:String
                                         )

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
      case None => ""
    }

    AddressResponseAddressCustomRH(
      uprn = address.uprn,
      bestMatchAddress = bestMatchAddress,
      bestMatchAddressType = bestMatchAddressType,
      censusAddressType = address.censusAddressType,
      censusEstabType = address.censusEstabType,
      countryCode = address.countryCode
    )
  }
}
