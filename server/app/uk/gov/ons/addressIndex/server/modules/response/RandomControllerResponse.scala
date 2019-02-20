package uk.gov.ons.addressIndex.server.modules.response

import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.random.{AddressByRandomResponse, AddressByRandomResponseContainer}

trait RandomControllerResponse extends Response {

  def NoAddressFoundRandom(queryValues: Map[String,Any]): AddressByRandomResponseContainer = {
    AddressByRandomResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByRandomResponse(
        addresses = Seq.empty,
        filter = queryValues("filter").toString,
        limit = queryValues("limit").asInstanceOf[Int],
        historical = queryValues("historical").asInstanceOf[Boolean],
        epoch = queryValues("epoch").toString,
        verbose = queryValues("verbose").asInstanceOf[Boolean]
      ),
      status = NotFoundAddressResponseStatus,
      errors = Seq(NotFoundAddressResponseError)
    )
  }

  def RandomFilterInvalid(queryValues: Map[String,Any]): AddressByRandomResponseContainer = {
    BadRequestRandomTemplate(queryValues,FilterInvalidError)
  }

  def RandomMixedFilter(queryValues: Map[String,Any]): AddressByRandomResponseContainer = {
    BadRequestRandomTemplate(queryValues,MixedFilterError)
  }

  def FailedRequestToEsRandom(detail: String, queryValues: Map[String,Any]): AddressByRandomResponseContainer = {
    val enhancedError = new AddressResponseError(FailedRequestToEsRandomError.code,FailedRequestToEsRandomError.message.replace("see logs",detail))
    AddressByRandomResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorRandom(queryValues),
      status = InternalServerErrorAddressResponseStatus,
      errors = Seq(enhancedError)
    )
  }

  def FailedRequestToEsTooBusyRandom(detail: String, queryValues: Map[String,Any]): AddressByRandomResponseContainer = {
    val enhancedError = new AddressResponseError(FailedRequestToEsRandomError.code,FailedRequestToEsRandomError.message.replace("see logs",detail))
    AddressByRandomResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorRandom(queryValues),
      status = TooManyRequestsResponseStatus,
      errors = Seq(enhancedError)
    )
  }

  def ErrorRandom(queryValues: Map[String,Any]): AddressByRandomResponse = {
    AddressByRandomResponse(
      addresses = Seq.empty,
      filter = queryValues("filter").toString,
      limit = queryValues("limit").asInstanceOf[Int],
      historical = queryValues("historical").asInstanceOf[Boolean],
      epoch = queryValues("epoch").toString,
      verbose = queryValues("verbose").asInstanceOf[Boolean]
    )
  }

  def BadRequestRandomTemplate(queryValues: Map[String,Any], errors: AddressResponseError*): AddressByRandomResponseContainer = {
    AddressByRandomResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorRandom(queryValues),
      status = BadRequestAddressResponseStatus,
      errors = errors
    )
  }

  def LimitNotNumericRandom(queryValues: Map[String,Any]): AddressByRandomResponseContainer = {
    BadRequestRandomTemplate(queryValues,LimitNotNumericAddressResponseError)
  }

  def LimitTooSmallRandom(queryValues: Map[String,Any]): AddressByRandomResponseContainer = {
    BadRequestRandomTemplate(queryValues,LimitTooSmallAddressResponseError)
  }

  def LimitTooLargeRandom(queryValues: Map[String,Any]): AddressByRandomResponseContainer = {
    BadRequestRandomTemplate(queryValues,LimitTooLargeAddressResponseError)
  }

  def RandomEpochInvalid(queryValues: Map[String,Any]): AddressByRandomResponseContainer = {
    BadRequestRandomTemplate(queryValues,EpochNotAvailableError)
  }

}
