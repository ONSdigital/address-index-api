package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

object AddressResponseClassification {
  implicit lazy val addressResponseClassificationFormat: Format[AddressResponseClassification] = Json.format[AddressResponseClassification]

}

/**
  * Classification object for list
  *
  * @param code  Classification code
  * @param label Classification name
  */
case class AddressResponseClassification(code: String, label: String)
