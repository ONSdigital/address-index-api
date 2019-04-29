package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

/**
  * Logical Status object for list
  *
  * @param code  Logical Status code
  * @param label Logical Status name
  */
case class AddressResponseLogicalStatus(code: String,
                                        label: String)

object AddressResponseLogicalStatus {
  implicit lazy val addressResponseLogicalStatusFormat: Format[AddressResponseLogicalStatus] = Json.format[AddressResponseLogicalStatus]

}
