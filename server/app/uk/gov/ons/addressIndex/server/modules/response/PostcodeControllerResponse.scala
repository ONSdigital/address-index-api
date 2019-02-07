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
        epoch = "",
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

  def PostcodeFilterInvalid(queryValues: Map[String,Any]): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues,FilterInvalidError)
  }

  def PostcodeMixedFilter(queryValues: Map[String,Any]): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues,MixedFilterError)
  }

  def PostcodeEpochInvalid(queryValues: Map[String,Any]): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues,EpochNotAvailableError)
  }

  def FailedRequestToEsPostcode(detail: String, queryValues: Map[String,Any]): AddressByPostcodeResponseContainer = {
    val enhancedError = new AddressResponseError(FailedRequestToEsPostcodeError.code,FailedRequestToEsPostcodeError.message.replace("see logs",detail))
    AddressByPostcodeResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorPostcode(queryValues),
      status = InternalServerErrorAddressResponseStatus,
      errors = Seq(enhancedError)
    )
  }

  def FailedRequestToEsTooBusyPostCode(detail: String, queryValues: Map[String,Any]): AddressByPostcodeResponseContainer = {
    val enhancedError = new AddressResponseError(FailedRequestToEsPostcodeError.code,FailedRequestToEsPostcodeError.message.replace("see logs",detail))
    AddressByPostcodeResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorPostcode(queryValues),
      status = TooManyRequestsResponseStatus,
      errors = Seq(enhancedError)
    )
  }

  def ErrorPostcode(queryValues: Map[String,Any]): AddressByPostcodeResponse = {
    AddressByPostcodeResponse(
      postcode = queryValues("postcode").toString,
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

  def OffsetNotNumericPostcode(queryValues: Map[String,Any]): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues,OffsetNotNumericAddressResponseError)
  }

  def BadRequestPostcodeTemplate(queryValues: Map[String,Any], errors: AddressResponseError*): AddressByPostcodeResponseContainer = {
    AddressByPostcodeResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorPostcode(queryValues),
      status = BadRequestAddressResponseStatus,
      errors = errors
    )
  }

  def LimitNotNumericPostcode(queryValues: Map[String,Any]): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues,LimitNotNumericAddressResponseError)
  }

  def LimitTooSmallPostcode(queryValues: Map[String,Any]): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues,LimitTooSmallAddressResponseError)
  }

  def OffsetTooSmallPostcode(queryValues: Map[String,Any]): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues,OffsetTooSmallAddressResponseError)
  }

  def LimitTooLargePostcode(queryValues: Map[String,Any]): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues,LimitTooLargeAddressResponseError)
  }

  def OffsetTooLargePostcode(queryValues: Map[String,Any]): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues,OffsetTooLargeAddressResponseError)
  }

  def EmptySearchPostcode(queryValues: Map[String,Any]): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues,EmptyQueryPostcodeAddressResponseError)
  }

  def InvalidPostcode(queryValues: Map[String,Any]): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues,InvalidPostcodeAddressResponseError)
  }

  def EpochNotAvailable(queryValues: Map[String,Any]): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues,EpochNotAvailableError)
  }

}
