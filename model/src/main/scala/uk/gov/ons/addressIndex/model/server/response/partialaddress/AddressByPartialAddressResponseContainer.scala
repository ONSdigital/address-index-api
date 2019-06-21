package uk.gov.ons.addressIndex.model.server.response.partialaddress

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseError, AddressResponseStatus}

/**
  * Contains the reply for the typeahead address search request
  *
  * @param apiVersion  version of the API used for the response
  * @param dataVersion version of the address data used for the response
  * @param response    relevant data
  * @param status      status code / message
  * @param errors      encountered errors (or an empty list if there is no errors)
  */
case class AddressByPartialAddressResponseContainer(apiVersion: String,
                                                    dataVersion: String,
                                                    response: AddressByPartialAddressResponse,
                                                    status: AddressResponseStatus,
                                                    errors: Seq[AddressResponseError] = Seq.empty[AddressResponseError])

object AddressByPartialAddressResponseContainer {
  implicit lazy val addressByPartialAddressResponseContainerFormat: Format[AddressByPartialAddressResponseContainer] =
    Json.format[AddressByPartialAddressResponseContainer]
}
