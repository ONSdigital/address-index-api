package uk.gov.ons.addressIndex.model.server.response.eq

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseError, AddressResponseStatus}

/**
  * Contains the reply for address by uprn request
  *
  * @param apiVersion  version of the API used for the response
  * @param dataVersion version of the address data used for the response
  * @param response    found content
  * @param status      response status / message
  * @param errors      encountered errors (or an empty list if there is no errors)
  */
case class AddressByEqUprnResponseContainer(apiVersion: String,
                                          dataVersion: String,
                                          response: AddressByEqUprnResponse,
                                          status: AddressResponseStatus,
                                          errors: Seq[AddressResponseError] = Seq.empty[AddressResponseError])

object AddressByEqUprnResponseContainer {
  implicit val addressByEqUprnResponseContainerFormat: Format[AddressByEqUprnResponseContainer] =
    Json.format[AddressByEqUprnResponseContainer]
}
