package uk.gov.ons.addressIndex.model.server.response.codelists

import play.api.libs.json.{Format, Json}

object AddressResponseCodelistListContainer {
  implicit lazy val addressResponseCodelistListContainerFormat: Format[AddressResponseCodelistListContainer] =
    Json.format[AddressResponseCodelistListContainer]
}

/**
  * Container for codelists list
  *
  * @param codelists  sequence of codelists
  */
case class AddressResponseCodelistListContainer(
  codelists: Seq[AddressResponseCodelist] = Seq.empty[AddressResponseCodelist]
)
