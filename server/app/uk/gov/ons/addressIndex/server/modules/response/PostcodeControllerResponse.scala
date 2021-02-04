package uk.gov.ons.addressIndex.server.modules.response

import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.eq.{AddressByEQBucketResponse, AddressByEQBucketResponseContainer}
import uk.gov.ons.addressIndex.model.server.response.postcode.{AddressByPostcodeResponse, AddressByPostcodeResponseContainer}
import uk.gov.ons.addressIndex.server.model.dao.QueryValues

trait PostcodeControllerResponse extends Response {

  def NoAddressFoundPostcode(queryValues: QueryValues): AddressByPostcodeResponseContainer = {
    AddressByPostcodeResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByPostcodeResponse(
        postcode = queryValues.postcodeOrDefault,
        addresses = Seq.empty,
        filter = queryValues.filterOrDefault,
        historical = queryValues.historicalOrDefault,
        epoch = queryValues.epochOrDefault,
        limit = queryValues.limitOrDefault,
        offset = queryValues.offsetOrDefault,
        total = 0,
        maxScore = 0f,
        verbose = queryValues.verboseOrDefault,
        includeauxiliarysearch = queryValues.includeAuxiliarySearchOrDefault
      ),
      status = NotFoundAddressResponseStatus,
      errors = Seq(NotFoundAddressResponseError)
    )
  }

  def PostcodeFilterInvalid(queryValues: QueryValues): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues, FilterInvalidError)
  }

  def PostcodeMixedFilter(queryValues: QueryValues): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues, MixedFilterError)
  }

  def EQBucketInvalid(queryValues: QueryValues): AddressByEQBucketResponseContainer = {
    BadRequestBucketTemplate(queryValues, InvalidEQBucketError)
  }

  def PostcodeEpochInvalid(queryValues: QueryValues): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues, EpochNotAvailableError)
  }

  def FailedRequestToEsPostcode(detail: String, queryValues: QueryValues): AddressByPostcodeResponseContainer = {
    val enhancedError = new AddressResponseError(FailedRequestToEsPostcodeError.code, FailedRequestToEsPostcodeError.message.replace("see logs", detail))
    AddressByPostcodeResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorPostcode(queryValues),
      status = InternalServerErrorAddressResponseStatus,
      errors = Seq(enhancedError)
    )
  }

  def FailedRequestToEsTooBusyPostCode(detail: String, queryValues: QueryValues): AddressByPostcodeResponseContainer = {
    val enhancedError = new AddressResponseError(FailedRequestToEsPostcodeError.code, FailedRequestToEsPostcodeError.message.replace("see logs", detail))
    AddressByPostcodeResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorPostcode(queryValues),
      status = TooManyRequestsResponseStatus,
      errors = Seq(enhancedError)
    )
  }

  def ErrorPostcode(queryValues: QueryValues): AddressByPostcodeResponse = {
    AddressByPostcodeResponse(
      postcode = queryValues.postcodeOrDefault,
      addresses = Seq.empty,
      filter = queryValues.filterOrDefault,
      historical = queryValues.historicalOrDefault,
      epoch = queryValues.epochOrDefault,
      limit = queryValues.limitOrDefault,
      offset = queryValues.offsetOrDefault,
      total = 0,
      maxScore = 0f,
      verbose = queryValues.verboseOrDefault,
      includeauxiliarysearch = queryValues.includeAuxiliarySearchOrDefault
    )
  }

  def ErrorBucket(queryValues: QueryValues): AddressByEQBucketResponse = {
    AddressByEQBucketResponse(
      postcode = queryValues.postcodeOrDefault,
      streetname = queryValues.streetnameOrDefault,
      townname = queryValues.townnameOrDefault,
      addresses = Seq.empty,
      filter = queryValues.filterOrDefault,
      epoch = queryValues.epochOrDefault,
      limit = queryValues.limitOrDefault,
      offset = queryValues.offsetOrDefault,
      total = 0,
      maxScore = 0f
    )
  }

  def OffsetNotNumericPostcode(queryValues: QueryValues): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues, OffsetNotNumericAddressResponseError)
  }

  def BadRequestPostcodeTemplate(queryValues: QueryValues, errors: AddressResponseError*): AddressByPostcodeResponseContainer = {
    AddressByPostcodeResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorPostcode(queryValues),
      status = BadRequestAddressResponseStatus,
      errors = errors
    )
  }

  def BadRequestBucketTemplate(queryValues: QueryValues, errors: AddressResponseError*): AddressByEQBucketResponseContainer = {
    AddressByEQBucketResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      termsAndConditions = termsAndConditions,
      response = ErrorBucket(queryValues),
      status = BadRequestAddressResponseStatus,
      errors = errors
    )
  }

  def LimitNotNumericPostcode(queryValues: QueryValues): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues, LimitNotNumericAddressResponseError)
  }

  def LimitTooSmallPostcode(queryValues: QueryValues): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues, LimitTooSmallAddressResponseError)
  }

  def OffsetTooSmallPostcode(queryValues: QueryValues): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues, OffsetTooSmallAddressResponseError)
  }

  def LimitTooLargePostcode(queryValues: QueryValues): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues, LimitTooLargeAddressResponseError)
  }

  def OffsetTooLargePostcode(queryValues: QueryValues): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues, OffsetTooLargeAddressResponseError)
  }

  def EmptySearchPostcode(queryValues: QueryValues): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues, EmptyQueryPostcodeAddressResponseError)
  }

  def InvalidPostcode(queryValues: QueryValues): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues, InvalidPostcodeAddressResponseError)
  }

  def EpochNotAvailable(queryValues: QueryValues): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues, EpochNotAvailableError)
  }

}
