package uk.gov.ons.addressIndex.model.server.response.postcode

import play.api.libs.json.{Format, Json}

/**
  * Postcode grouping DTO
  * Captures output from ES aggregation for part postcode
  */
case class AddressResponsePostcodeGroup(postcode: String,
                                        streetName: String,
                                        townName: String,
                                        addressCount: Int)

object AddressResponsePostcodeGroup {
  implicit lazy val addressResponsePostcodeGroupFormat: Format[AddressResponsePostcodeGroup] = Json.format[AddressResponsePostcodeGroup]
}

