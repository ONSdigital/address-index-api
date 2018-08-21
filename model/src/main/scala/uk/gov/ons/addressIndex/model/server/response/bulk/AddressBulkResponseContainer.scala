package uk.gov.ons.addressIndex.model.server.response.bulk

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseError, AddressResponseStatus}

/**
  * Contains relevant information about the result of the bulk request
  *
  * @param apiVersion version of the API used for the response
  * @param dataVersion version of the address data used for the response
  * @param bulkAddresses found bulk addresses
  * @param status   status code / message
  * @param errors   encountred errors (or an empty list if there is no errors)
  */
case class AddressBulkResponseContainer(
  apiVersion: String,
  dataVersion: String,
  bulkAddresses: Seq[AddressBulkResponseAddress],
  status: AddressResponseStatus,
  errors: Seq[AddressResponseError] = Seq.empty[AddressResponseError]
)

object AddressBulkResponseContainer {
  implicit lazy val addressBulkResponseContainer: Format[AddressBulkResponseContainer] = Json.format[AddressBulkResponseContainer]
}
