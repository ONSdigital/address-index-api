package uk.gov.ons.addressIndex.server.modules.response

import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.random.{AddressByRandomResponse, AddressByRandomResponseContainer}

trait RandomControllerResponse extends Response {

  def NoAddressFoundRandom: AddressByRandomResponseContainer = {
    AddressByRandomResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByRandomResponse(
        addresses = Seq.empty,
        filter = "",
        limit = 1,
        historical = true,
        epoch = "",
        verbose = true
      ),
      status = NotFoundAddressResponseStatus,
      errors = Seq(NotFoundAddressResponseError)
    )
  }

  def RandomFilterInvalid: AddressByRandomResponseContainer = {
    BadRequestRandomTemplate(FilterInvalidError)
  }

  def RandomMixedFilter: AddressByRandomResponseContainer = {
    BadRequestRandomTemplate(MixedFilterError)
  }

  def FailedRequestToEsRandom(detail: String): AddressByRandomResponseContainer = {
    val enhancedError = new AddressResponseError(FailedRequestToEsRandomError.code,FailedRequestToEsRandomError.message.replace("see logs",detail))
    AddressByRandomResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorRandom,
      status = InternalServerErrorAddressResponseStatus,
      errors = Seq(enhancedError)
    )
  }

  def FailedRequestToEsTooBusyRandom(detail: String): AddressByRandomResponseContainer = {
    val enhancedError = new AddressResponseError(FailedRequestToEsRandomError.code,FailedRequestToEsRandomError.message.replace("see logs",detail))
    AddressByRandomResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorRandom,
      status = TooManyRequestsResponseStatus,
      errors = Seq(enhancedError)
    )
  }

  def ErrorRandom: AddressByRandomResponse = {
    AddressByRandomResponse(
      addresses = Seq.empty,
      filter = "",
      limit = 1,
      historical = true,
      epoch = "",
      verbose = true
    )
  }

  def BadRequestRandomTemplate(errors: AddressResponseError*): AddressByRandomResponseContainer = {
    AddressByRandomResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorRandom,
      status = BadRequestAddressResponseStatus,
      errors = errors
    )
  }

  def LimitNotNumericRandom: AddressByRandomResponseContainer = {
    BadRequestRandomTemplate(LimitNotNumericAddressResponseError)
  }

  def LimitTooSmallRandom: AddressByRandomResponseContainer = {
    BadRequestRandomTemplate(LimitTooSmallAddressResponseError)
  }

  def LimitTooLargeRandom: AddressByRandomResponseContainer = {
    BadRequestRandomTemplate(LimitTooLargeAddressResponseError)
  }

}
