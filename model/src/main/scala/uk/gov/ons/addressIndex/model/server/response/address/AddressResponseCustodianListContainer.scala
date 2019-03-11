package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

/**
  * Container for custodians list
  *
  * @param classifications sequence of custodians
  */
case class AddressResponseCustodianListContainer(custodians: Seq[AddressResponseCustodian] = Seq.empty[AddressResponseCustodian])

object AddressResponseCustodianListContainer {
  implicit lazy val addressResponseCustodianListContainerFormat: Format[AddressResponseCustodianListContainer] =
    Json.format[AddressResponseCustodianListContainer]
}
