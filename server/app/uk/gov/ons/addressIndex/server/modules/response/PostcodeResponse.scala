package uk.gov.ons.addressIndex.server.modules.response

import uk.gov.ons.addressIndex.model.server.response._

trait PostcodeResponse extends Response {

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
        endDate = ""
      ),
      status = NotFoundAddressResponseStatus,
      errors = Seq(NotFoundAddressResponseError)
    )
  }

  def PostcodeFilterInvalid: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(FilterInvalidPostcodeError)
  }

  def FailedRequestToEsPostcode: AddressByPostcodeResponseContainer = {
    AddressByPostcodeResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorPostcode,
      status = InternalServerErrorAddressResponseStatus,
      errors = Seq(FailedRequestToEsPostcodeError)
    )
  }

  def FailedRequestToEsTooBusyPostCode: AddressByPostcodeResponseContainer = {
    AddressByPostcodeResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorPostcode,
      status = TooManyRequestsResponseStatus,
      errors = Seq(FailedRequestToEsError)
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
      endDate = ""
    )
  }

  def OffsetNotNumericPostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(OffsetNotNumericPostcodeAddressResponseError)
  }

  private def BadRequestPostcodeTemplate(errors: AddressResponseError*): AddressByPostcodeResponseContainer = {
    AddressByPostcodeResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorPostcode,
      status = BadRequestAddressResponseStatus,
      errors = errors
    )
  }

  def LimitNotNumericPostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(LimitNotNumericPostcodeAddressResponseError)
  }

  def LimitTooSmallPostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(LimitTooSmallPostcodeAddressResponseError)
  }

  def OffsetTooSmallPostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(OffsetTooSmallPostcodeAddressResponseError)
  }

  def LimitTooLargePostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(LimitTooLargePostcodeAddressResponseError)
  }

  def OffsetTooLargePostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(OffsetTooLargePostcodeAddressResponseError)
  }

  def EmptySearchPostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(EmptyQueryPostcodeAddressResponseError)
  }

}
