package uk.gov.ons.addressIndex.model.server.response.codelists

import play.api.libs.json.{Format, Json}

/**
  * Codelist object for list of codelists
  *
  * @param name Name of codelist
  * @param description Description of codelist
  */
case class AddressResponseCodelist(
  name: String,
  description: String
)

object AddressResponseCodelist {
  implicit lazy val addressResponseCodelistFormat: Format[AddressResponseCodelist] = Json.format[AddressResponseCodelist]

}
