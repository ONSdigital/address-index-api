package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

/**
  * Source object for list
  *
  * @param code  Source code
  * @param label Source name
  */
case class AddressResponseSource(code: String,
                                 label: String)

object AddressResponseSource {
  implicit lazy val addressResponseSourceFormat: Format[AddressResponseSource] = Json.format[AddressResponseSource]

}
