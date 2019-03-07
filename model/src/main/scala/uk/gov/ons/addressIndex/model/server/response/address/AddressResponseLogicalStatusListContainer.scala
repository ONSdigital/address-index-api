package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

/**
  * Container for logical status list
  *
  * @param logicalStatuses sequence of logical statuses
  */
case class AddressResponseLogicalStatusListContainer(
                                                      logicalStatuses: Seq[AddressResponseLogicalStatus] = Seq.empty[AddressResponseLogicalStatus]
                                                    )

object AddressResponseLogicalStatusListContainer {
  implicit lazy val addressResponseLogicalStatusListContainerFormat: Format[AddressResponseLogicalStatusListContainer] =
    Json.format[AddressResponseLogicalStatusListContainer]
}
