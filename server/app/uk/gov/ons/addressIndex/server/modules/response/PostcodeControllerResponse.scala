package uk.gov.ons.addressIndex.server.modules.response

import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.postcode.{AddressByPostcodeResponse, AddressByPostcodeResponseContainer}

trait PostcodeControllerResponse extends Response {

  def NoAddressFoundPostcode: AddressByPostcodeResponseContainer = {
    AddressByPostcodeResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByPostcodeResponse(
        postcode = "",
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
      ),
      status = NotFoundAddressResponseStatus,
      errors = Seq(NotFoundAddressResponseError)
    )
  }

  def PostcodeFilterInvalid: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(FilterInvalidError)
  }

  def FailedRequestToEsPostcode(detail: String): AddressByPostcodeResponseContainer = {
    val enhancedError = new AddressResponseError(FailedRequestToEsPostcodeError.code,FailedRequestToEsPostcodeError.message.replace("see logs",detail))
    AddressByPostcodeResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorPostcode,
      status = InternalServerErrorAddressResponseStatus,
      errors = Seq(enhancedError)
    )
  }

  def FailedRequestToEsTooBusyPostCode(detail: String): AddressByPostcodeResponseContainer = {
    val enhancedError = new AddressResponseError(FailedRequestToEsPostcodeError.code,FailedRequestToEsPostcodeError.message.replace("see logs",detail))
    AddressByPostcodeResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorPostcode,
      status = TooManyRequestsResponseStatus,
      errors = Seq(enhancedError)
    )
  }

  def ErrorPostcode: AddressByPostcodeResponse = {
    AddressByPostcodeResponse(
      postcode = "",
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

  def OffsetNotNumericPostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(OffsetNotNumericAddressResponseError)
  }

  def BadRequestPostcodeTemplate(errors: AddressResponseError*): AddressByPostcodeResponseContainer = {
    AddressByPostcodeResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorPostcode,
      status = BadRequestAddressResponseStatus,
      errors = errors
    )
  }

  def LimitNotNumericPostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(LimitNotNumericAddressResponseError)
  }

  def LimitTooSmallPostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(LimitTooSmallAddressResponseError)
  }

  def OffsetTooSmallPostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(OffsetTooSmallAddressResponseError)
  }

  def LimitTooLargePostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(LimitTooLargeAddressResponseError)
  }

  def OffsetTooLargePostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(OffsetTooLargeAddressResponseError)
  }

  def EmptySearchPostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(EmptyQueryPostcodeAddressResponseError)
  }

  def InvalidPostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(InvalidPostcodeAddressResponseError)
  }

}
