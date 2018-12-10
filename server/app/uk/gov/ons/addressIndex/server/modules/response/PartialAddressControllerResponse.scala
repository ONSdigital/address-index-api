package uk.gov.ons.addressIndex.server.modules.response

import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.partialaddress.{AddressByPartialAddressResponse, AddressByPartialAddressResponseContainer}

trait PartialAddressControllerResponse extends AddressResponse {

  def BadRequestPartialTemplate(errors: AddressResponseError*): AddressByPartialAddressResponseContainer = {
    AddressByPartialAddressResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorPartialAddress,
      status = BadRequestAddressResponseStatus,
      errors = errors
    )
  }

  def ShortSearch: AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(ShortQueryAddressResponseError)
  }

  def LimitNotNumericPartial: AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(LimitNotNumericAddressResponseError)
  }

  def OffsetNotNumericPartial: AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(OffsetNotNumericAddressResponseError)
  }

  def LimitTooSmallPartial: AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(LimitTooSmallAddressResponseError)
  }

  def OffsetTooSmallPartial: AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(OffsetTooSmallAddressResponseError)
  }

  def LimitTooLargePartial: AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(LimitTooLargeAddressResponseError)
  }

  def OffsetTooLargePartial: AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(OffsetTooLargeAddressResponseError)
  }

  def FailedRequestToEsPartialAddress(detail: String): AddressByPartialAddressResponseContainer = {
    val enhancedError = new AddressResponseError(FailedRequestToEsPartialAddressError.code,FailedRequestToEsPartialAddressError.message.replace("see logs",detail))
    AddressByPartialAddressResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorPartialAddress,
      status = InternalServerErrorAddressResponseStatus,
      errors = Seq(enhancedError)
    )
  }

  def FailedRequestToEsTooBusyPartialAddress(detail: String): AddressByPartialAddressResponseContainer = {
    val enhancedError = new AddressResponseError(FailedRequestToEsPartialAddressError.code,FailedRequestToEsPartialAddressError.message.replace("see logs",detail))
    AddressByPartialAddressResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorPartialAddress,
      status = TooManyRequestsResponseStatus,
      errors = Seq(enhancedError)
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
