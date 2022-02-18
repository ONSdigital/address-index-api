package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

/**
  * Container for version info
  *
  * @param apiVersion
  * @param dataVersion
  */
case class AddressResponseEpoch(epochList: List[String],
                                epochDates: Map[String,String])

object AddressResponseVersion {
  implicit lazy val addressResponseEpochFormat: Format[AddressResponseEpoch] = Json.format[AddressResponseEpoch]
}


