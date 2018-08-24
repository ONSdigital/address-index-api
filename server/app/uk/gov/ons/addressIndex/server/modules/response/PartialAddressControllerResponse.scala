package uk.gov.ons.addressIndex.server.modules.response

import uk.gov.ons.addressIndex.model.server.response.address.{FailedRequestToEsPartialAddressError, InternalServerErrorAddressResponseStatus}
import uk.gov.ons.addressIndex.model.server.response.partialaddress.{AddressByPartialAddressResponse, AddressByPartialAddressResponseContainer}

trait PartialAddressControllerResponse extends AddressResponse {

  def FailedRequestToEsPartialAddress: AddressByPartialAddressResponseContainer = {
    AddressByPartialAddressResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorPartialAddress,
      status = InternalServerErrorAddressResponseStatus,
      errors = Seq(FailedRequestToEsPartialAddressError)
    )
  }

  def ErrorPartialAddress: AddressByPartialAddressResponse = {
    AddressByPartialAddressResponse(
      input = "",
      addresses = Seq.empty,
      filter = "",
      historical = true,
      limit = 10,
      offset = 0,
      total = 0,
      maxScore = 0f,
      startDate = "",
      endDate = "",
      verbose = true
    )
  }

}
