package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

/**
  * Contains address information retrieved in ES (PAF, NAG or NISRA)
  * @param uprn the UPRN
  * @param bestMatchAddress the bestMatchAddress
  * @param bestMatchAddressType type of address
  */
case class AddressResponseAddressCustomEQ(uprn: String,
                                    bestMatchAddress: String,
                                    bestMatchAddressType: String)

object AddressResponseAddressCustomEQ {
  implicit lazy val addressResponseAddressEQFormat: Format[AddressResponseAddressCustomEQ] = Json.format[AddressResponseAddressCustomEQ]

  def fromAddressResponseAddressEQ(address: AddressResponseAddressEQ): AddressResponseAddressCustomEQ = {

    val bestMatchAddress: String = address.highlights match {
      case Some(highlight) => highlight.bestMatchAddress
      case None => ""
    }

    val bestMatchAddressType: String = address.highlights match {
      case Some(highlight) if highlight.source == "P" && highlight.lang == "E" => "PAF"
      case Some(highlight) if highlight.source == "P" && highlight.lang == "W" => "WELSHPAF"
      case Some(highlight) if highlight.source == "L" && highlight.lang == "E" => "NAG"
      case Some(highlight) if highlight.source == "L" && highlight.lang == "W" => "WELSHNAG"
      case Some(highlight) if highlight.source == "N" => "NISRA"
      case None => ""
    }

    AddressResponseAddressCustomEQ(
          uprn = address.uprn,
          bestMatchAddress = bestMatchAddress,
          bestMatchAddressType = bestMatchAddressType
    )
  }
}