package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

/**
  * Contains the reply for the address search request
  *
  * @param apiVersion  version of the API used for the response
  * @param dataVersion version of the address data used for the response
  * @param response    relevant data
  * @param status      status code / message
  * @param errors      encountred errors (or an empty list if there is no errors)
  */
case class AddressBySearchResponseContainer(
                                             apiVersion: String,
                                             dataVersion: String,
                                             response: AddressBySearchResponse,
                                             status: AddressResponseStatus,
                                             errors: Seq[AddressResponseError] = Seq.empty[AddressResponseError]
                                           )

object AddressBySearchResponseContainer {
  implicit lazy val addressBySearchResponseContainerFormat: Format[AddressBySearchResponseContainer] =
    Json.format[AddressBySearchResponseContainer]
}
