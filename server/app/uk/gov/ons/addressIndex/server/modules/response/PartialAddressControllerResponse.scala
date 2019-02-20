package uk.gov.ons.addressIndex.server.modules.response

import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.partialaddress.{AddressByPartialAddressResponse, AddressByPartialAddressResponseContainer}

trait PartialAddressControllerResponse extends AddressResponse {

  def BadRequestPartialTemplate(queryValues: Map[String,Any], errors: AddressResponseError*): AddressByPartialAddressResponseContainer = {
    AddressByPartialAddressResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorPartialAddress(queryValues),
      status = BadRequestAddressResponseStatus,
      errors = errors
    )
  }

  def ShortSearch(queryValues: Map[String,Any]): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues,ShortQueryAddressResponseError)
  }

  def LimitNotNumericPartial(queryValues: Map[String,Any]): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues,LimitNotNumericAddressResponseError)
  }

  def OffsetNotNumericPartial(queryValues: Map[String,Any]): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues,OffsetNotNumericAddressResponseError)
  }

  def LimitTooSmallPartial(queryValues: Map[String,Any]): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues,LimitTooSmallAddressResponseError)
  }

  def OffsetTooSmallPartial(queryValues: Map[String,Any]): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues,OffsetTooSmallAddressResponseError)
  }

  def LimitTooLargePartial(queryValues: Map[String,Any]): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues,LimitTooLargeAddressResponseError)
  }

  def OffsetTooLargePartial(queryValues: Map[String,Any]): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues,OffsetTooLargeAddressResponseError)
  }

  def PartialEpochInvalid(queryValues: Map[String,Any]): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues,EpochNotAvailableError)
  }

  def EpochNotAvailable(queryValues: Map[String,Any]): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues,EpochNotAvailableError)
  }

  def FailedRequestToEsPartialAddress(detail: String, queryValues: Map[String,Any]): AddressByPartialAddressResponseContainer = {
    val enhancedError = new AddressResponseError(FailedRequestToEsPartialAddressError.code,FailedRequestToEsPartialAddressError.message.replace("see logs",detail))
    AddressByPartialAddressResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorPartialAddress(queryValues),
      status = InternalServerErrorAddressResponseStatus,
      errors = Seq(enhancedError)
    )
  }

  def FailedRequestToEsTooBusyPartialAddress(detail: String, queryValues: Map[String,Any]): AddressByPartialAddressResponseContainer = {
    val enhancedError = new AddressResponseError(FailedRequestToEsPartialAddressError.code,FailedRequestToEsPartialAddressError.message.replace("see logs",detail))
    AddressByPartialAddressResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorPartialAddress(queryValues),
      status = TooManyRequestsResponseStatus,
      errors = Seq(enhancedError)
    )
  }

  def ErrorPartialAddress(queryValues: Map[String,Any]): AddressByPartialAddressResponse = {
    AddressByPartialAddressResponse(
      input = queryValues("input").toString,
      addresses = Seq.empty,
      filter = queryValues("filter").toString,
      historical = queryValues("historical").asInstanceOf[Boolean],
      epoch = queryValues("epoch").toString,
      limit = queryValues("limit").asInstanceOf[Int],
      offset = queryValues("offset").asInstanceOf[Int],
      total = 0,
      maxScore = 0f,
      startDate = queryValues("startDate").toString,
      endDate = queryValues("endDate").toString,
      verbose = queryValues("verbose").asInstanceOf[Boolean]
    )
  }

}
