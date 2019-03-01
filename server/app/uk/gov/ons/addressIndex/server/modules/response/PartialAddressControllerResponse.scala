package uk.gov.ons.addressIndex.server.modules.response

import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.partialaddress.{AddressByPartialAddressResponse, AddressByPartialAddressResponseContainer}
import uk.gov.ons.addressIndex.server.model.dao.QueryValues

trait PartialAddressControllerResponse extends AddressResponse {

  def BadRequestPartialTemplate(queryValues: QueryValues, errors: AddressResponseError*): AddressByPartialAddressResponseContainer = {
    AddressByPartialAddressResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorPartialAddress(queryValues),
      status = BadRequestAddressResponseStatus,
      errors = errors
    )
  }

  def ShortSearch(queryValues: QueryValues): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues,ShortQueryAddressResponseError)
  }

  def LimitNotNumericPartial(queryValues: QueryValues): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues,LimitNotNumericAddressResponseError)
  }

  def OffsetNotNumericPartial(queryValues: QueryValues): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues,OffsetNotNumericAddressResponseError)
  }

  def LimitTooSmallPartial(queryValues: QueryValues): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues,LimitTooSmallAddressResponseError)
  }

  def OffsetTooSmallPartial(queryValues: QueryValues): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues,OffsetTooSmallAddressResponseError)
  }

  def LimitTooLargePartial(queryValues: QueryValues): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues,LimitTooLargeAddressResponseError)
  }

  def OffsetTooLargePartial(queryValues: QueryValues): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues,OffsetTooLargeAddressResponseError)
  }

  def PartialEpochInvalid(queryValues: QueryValues): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues,EpochNotAvailableError)
  }

  def EpochNotAvailable(queryValues: QueryValues): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues,EpochNotAvailableError)
  }

  def FailedRequestToEsPartialAddress(detail: String, queryValues: QueryValues): AddressByPartialAddressResponseContainer = {
    val enhancedError = new AddressResponseError(FailedRequestToEsPartialAddressError.code,FailedRequestToEsPartialAddressError.message.replace("see logs",detail))
    AddressByPartialAddressResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorPartialAddress(queryValues),
      status = InternalServerErrorAddressResponseStatus,
      errors = Seq(enhancedError)
    )
  }

  def FailedRequestToEsTooBusyPartialAddress(detail: String, queryValues: QueryValues): AddressByPartialAddressResponseContainer = {
    val enhancedError = new AddressResponseError(FailedRequestToEsPartialAddressError.code,FailedRequestToEsPartialAddressError.message.replace("see logs",detail))
    AddressByPartialAddressResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorPartialAddress(queryValues),
      status = TooManyRequestsResponseStatus,
      errors = Seq(enhancedError)
    )
  }

  def ErrorPartialAddress(queryValues: QueryValues): AddressByPartialAddressResponse = {
    AddressByPartialAddressResponse(
      input = queryValues.input.get,
      addresses = Seq.empty,
      filter = queryValues.filter.get,
      historical = queryValues.historical.get,
      epoch = queryValues.epoch.get,
      limit = queryValues.limit.get,
      offset = queryValues.offset.get,
      total = 0,
      maxScore = 0f,
      startDate = queryValues.startDate.get,
      endDate = queryValues.endDate.get,
      verbose = queryValues.verbose.get
    )
  }

}
