package uk.gov.ons.addressIndex.server.modules.response

import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.eq.{AddressByEQUprnResponse, AddressByEQUprnResponseContainer}
import uk.gov.ons.addressIndex.model.server.response.uprn.{AddressByUprnResponse, AddressByUprnResponseContainer}
import uk.gov.ons.addressIndex.server.model.dao.QueryValues

trait UPRNControllerResponse extends Response {

  def UprnNotNumeric(queryValues: QueryValues): AddressByUprnResponseContainer = {
    BadRequestNonNumericUprn(queryValues)
  }

  def BadRequestNonNumericUprn(queryValues: QueryValues): AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByUprnResponse(
        address = None,
        historical = queryValues.historicalOrDefault,
        epoch = queryValues.epochOrDefault,
        verbose = queryValues.verboseOrDefault,
        includeauxiliarysearch = queryValues.includeAuxiliarySearchOrDefault
      ),
      status = BadRequestAddressResponseStatus,
      errors = Seq(UprnNotNumericAddressResponseError)
    )
  }

  def ErrorUprn(queryValues: QueryValues): AddressByUprnResponse = {
    AddressByUprnResponse(
      address = None,
      historical = queryValues.historicalOrDefault,
      epoch = queryValues.epochOrDefault,
      verbose = queryValues.verboseOrDefault,
      includeauxiliarysearch = queryValues.includeAuxiliarySearchOrDefault
    )
  }

  def BadRequestUprnTemplate(queryValues: QueryValues, errors: AddressResponseError*): AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorUprn(queryValues),
      status = BadRequestAddressResponseStatus,
      errors = errors
    )
  }

  def searchUprnContainerTemplate(queryValues: QueryValues, optAddresses: Option[AddressResponseAddress]): AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByUprnResponse(
        address = optAddresses,
        historical = queryValues.historicalOrDefault,
        epoch = queryValues.epochOrDefault,
        verbose = queryValues.verboseOrDefault,
        includeauxiliarysearch = queryValues.includeAuxiliarySearchOrDefault
      ),
      status = OkAddressResponseStatus
    )
  }

  def NoAddressFoundUprn(queryValues: QueryValues): AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByUprnResponse(
        address = None,
        historical = queryValues.historicalOrDefault,
        epoch = queryValues.epochOrDefault,
        verbose = queryValues.verboseOrDefault,
        includeauxiliarysearch = queryValues.includeAuxiliarySearchOrDefault
      ),
      status = NotFoundAddressResponseStatus,
      errors = Seq(NotFoundAddressResponseError)
    )
  }

  def UnsupportedFormatUprn(queryValues: QueryValues): AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByUprnResponse(
        address = None,
        historical = queryValues.historicalOrDefault,
        epoch = queryValues.epochOrDefault,
        verbose = queryValues.verboseOrDefault,
        includeauxiliarysearch = queryValues.includeAuxiliarySearchOrDefault
      ),
      status = BadRequestAddressResponseStatus,
      errors = Seq(FormatNotSupportedAddressResponseError)
    )
  }

  def UprnEpochInvalid(queryValues: QueryValues): AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByUprnResponse(
        address = None,
        historical = queryValues.historicalOrDefault,
        epoch = queryValues.epochOrDefault,
        verbose = queryValues.verboseOrDefault,
        includeauxiliarysearch = queryValues.includeAuxiliarySearchOrDefault
      ),
      status = BadRequestAddressResponseStatus,
      errors = Seq(EpochNotAvailableError)
    )
  }

  def FailedRequestToEsUprn(detail: String, queryValues: QueryValues): AddressByUprnResponseContainer = {
    val enhancedError = new AddressResponseError(FailedRequestToEsUprnError.code, FailedRequestToEsUprnError.message.replace("see logs", detail))
    AddressByUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorUprn(queryValues),
      status = InternalServerErrorAddressResponseStatus,
      errors = Seq(enhancedError)
    )
  }

  def FailedRequestToEsTooBusyUprn(detail: String, queryValues: QueryValues): AddressByUprnResponseContainer = {
    val enhancedError = new AddressResponseError(FailedRequestToEsUprnError.code, FailedRequestToEsUprnError.message.replace("see logs", detail))
    AddressByUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorUprn(queryValues),
      status = TooManyRequestsResponseStatus,
      errors = Seq(enhancedError)
    )
  }

  def AddressTypeInvalid(queryValues: QueryValues): AddressByEQUprnResponseContainer = {
    BadRequestInvalidAddressType(queryValues)
  }

  def BadRequestInvalidAddressType(queryValues: QueryValues): AddressByEQUprnResponseContainer = {
    AddressByEQUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      termsAndConditions = termsAndConditions,
      response = AddressByEQUprnResponse(
        address = None,
        addressType = queryValues.addressTypeOrDefault,
        epoch = queryValues.epochOrDefault
      ),
      status = BadRequestAddressResponseStatus,
      errors = Seq(InvalidAddressTypeAddressResponseError)
    )
  }

}
