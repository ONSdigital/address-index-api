package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

/**
  * Container for classifications list
  *
  * @param classifications  sequence of classifications
  */
case class AddressResponseClassificationListContainer(
  classifications: Seq[AddressResponseClassification] = Seq.empty[AddressResponseClassification]
)

object AddressResponseClassificationListContainer {
  implicit lazy val addressResponseClassificationListContainerFormat: Format[AddressResponseClassificationListContainer] =
    Json.format[AddressResponseClassificationListContainer]
}
