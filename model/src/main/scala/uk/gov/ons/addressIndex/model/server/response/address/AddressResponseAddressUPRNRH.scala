package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

/**
  * Contains address information retrieved in ES relevant to the RH UPRN search
  *
  * @param uprn address UPRN
  * @param formattedAddress the chosen formatted address
  * @param addressLine1 address line 1
  * @param addressLine2 address line 2
  * @param addressLine3 address line 3
  * @param townName the determined town name
  * @param postcode the determined postcode
  * @param censusAddressType census bespoke address type derived from ABP code
  * @param censusEstabType census bespoke establishment type derived from ABP code
  * @param countryCode E="England" W="Wales" S="Scotland" N="Northern Ireland"
  */
case class AddressResponseAddressUPRNRH(uprn: String,
                                        formattedAddress: String,
                                        addressLine1: String,
                                        addressLine2: String,
                                        addressLine3: String,
                                        townName: String,
                                        postcode: String,
                                        foundAddressType: String,
                                        censusAddressType: String,
                                        censusEstabType: String,
                                        countryCode:String,
                                        organisationName: String)

object AddressResponseAddressUPRNRH {
  implicit lazy val addressResponseAddressUPRNRHFormat: Format[AddressResponseAddressUPRNRH] = Json.format[AddressResponseAddressUPRNRH]

}