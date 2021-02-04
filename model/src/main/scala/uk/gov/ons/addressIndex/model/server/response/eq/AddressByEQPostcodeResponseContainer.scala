package uk.gov.ons.addressIndex.model.server.response.eq

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseError, AddressResponseStatus}

/**
  * Contains the reply for the address search request
  *
  * @param apiVersion  version of the API used for the response
  * @param dataVersion version of the address data used for the response
  * @param response    relevant data
  * @param status      status code / message
  * @param errors      encountered errors (or an empty list if there is no errors)
  */
case class AddressByEQPostcodeResponseContainer(apiVersion: String,
                                                dataVersion: String,
                                                termsAndConditions: String,
                                                response: AddressByEQPostcodeResponse,
                                                status: AddressResponseStatus,
                                                errors: Seq[AddressResponseError] = Seq.empty[AddressResponseError])

object AddressByEQPostcodeResponseContainer {
  implicit lazy val addressByEQPostcodeResponseContainerFormat: Format[AddressByEQPostcodeResponseContainer] =
    Json.format[AddressByEQPostcodeResponseContainer]
}