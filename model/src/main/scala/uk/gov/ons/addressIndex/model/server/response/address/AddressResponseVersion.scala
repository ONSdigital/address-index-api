package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

/**
  * Container for version info
  *
  * @param apiVersion
  * @param dataVersion
  */
case class AddressResponseVersion(apiVersion: String,
                                  dataVersion: String)

object AddressResponseVersion {
  implicit lazy val addressResponseVersionFormat: Format[AddressResponseVersion] = Json.format[AddressResponseVersion]
}
