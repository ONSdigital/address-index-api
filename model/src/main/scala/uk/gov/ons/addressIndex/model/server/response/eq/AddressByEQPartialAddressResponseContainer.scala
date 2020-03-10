package uk.gov.ons.addressIndex.model.server.response.eq

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseError, AddressResponseStatus}

/**
  * Contains the reply for the typeahead address search request
  *
  * @param response    relevant data
  */
case class AddressByEQPartialAddressResponseContainer(apiVersion: String,
                                                      dataVersion: String,
                                                      response: AddressByEQPartialAddressResponse,
                                                      status: AddressResponseStatus,
                                                      errors: Seq[AddressResponseError] = Seq.empty[AddressResponseError])

object AddressByEQPartialAddressResponseContainer {
  implicit lazy val addressByEQPartialAddressResponseContainerFormat: Format[AddressByEQPartialAddressResponseContainer] =
    Json.format[AddressByEQPartialAddressResponseContainer]
}

