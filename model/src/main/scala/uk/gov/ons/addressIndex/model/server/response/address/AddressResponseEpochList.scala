package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

/**
  * Container for version info
  *
  * @param epochs
  *
  */
case class AddressResponseEpochList(epochs: Seq[AddressResponseEpoch])

object AddressResponseEpochList {
  implicit lazy val addressResponseEpochListFormat: Format[AddressResponseEpochList] = Json.format[AddressResponseEpochList]
}




