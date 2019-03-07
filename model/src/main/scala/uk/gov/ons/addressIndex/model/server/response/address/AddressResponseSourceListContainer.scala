package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

/**
  * Container for sources list
  *
  * @param sources sequence of sources
  */
case class AddressResponseSourceListContainer(
                                               sources: Seq[AddressResponseSource] = Seq.empty[AddressResponseSource]
                                             )

object AddressResponseSourceListContainer {
  implicit lazy val addressResponseSourceListContainerFormat: Format[AddressResponseSourceListContainer] =
    Json.format[AddressResponseSourceListContainer]
}
