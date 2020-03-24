package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

/**
  * Contains address information retrieved in ES relevant to the EQ UPRN search
  *
  * @param uprn address UPRN
  * @param formattedAddress the chosen formatted address
  * @param addressLine1 address line 1
  * @param addressLine2 address line 2
  * @param addressLine3 address line 3
  * @param townName the determined town name
  * @param postcode the determined postcode
  */
case class AddressResponseAddressUPRNRH(uprn: String,
                                        formattedAddress: String,
                                        addressLine1: String,
                                        addressLine2: String,
                                        addressLine3: String,
                                        townName: String,
                                        postcode: String,
                                        censusAddressType: String,
                                        censusEstabType: String,
                                        countryCode:String)

object AddressResponseAddressUPRNRH {
  implicit lazy val addressResponseAddressUPRNRHFormat: Format[AddressResponseAddressUPRNRH] = Json.format[AddressResponseAddressUPRNRH]

}