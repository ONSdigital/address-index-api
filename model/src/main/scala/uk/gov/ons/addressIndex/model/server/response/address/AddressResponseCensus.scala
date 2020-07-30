package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

object AddressResponseCensus {
  implicit lazy val addressResponseCensusFormat: Format[AddressResponseCensus] = Json.format[AddressResponseCensus]
}

/**
  * Classification object for list
  *
  * @param addressType  Classification addressType
  * @param estabType Classification estabType
  * @param countryCode Classification countryCode
  */
case class AddressResponseCensus(addressType: String, estabType: String, countryCode: String)