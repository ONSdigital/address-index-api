package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

/**
  * Container for version info
  *
  * @param epoch
  * @param default
  * @param description
  */
case class AddressResponseEpoch(epoch: String,
                                default: String,
                                description: String)


object AddressResponseEpoch {
  implicit lazy val addressResponseEpochFormat: Format[AddressResponseEpoch] = Json.format[AddressResponseEpoch]
}


