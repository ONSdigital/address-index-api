package uk.gov.ons.addressIndex.server.modules.response

import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.random.{AddressByRandomResponse, AddressByRandomResponseContainer}
import uk.gov.ons.addressIndex.server.model.dao.QueryValues

trait RandomControllerResponse extends Response {

  def NoAddressFoundRandom(queryValues: QueryValues): AddressByRandomResponseContainer = {
    AddressByRandomResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByRandomResponse(
        addresses = Seq.empty,
        filter = queryValues.filterOrDefault,
        limit = queryValues.limitOrDefault,
        historical = queryValues.historicalOrDefault,
        epoch = queryValues.epochOrDefault,
        verbose = queryValues.verboseOrDefault,
        countryBoosts = CountryBoosts(queryValues.eboostOrDefault,
          queryValues.nboostOrDefault,
          queryValues.sboostOrDefault,
          queryValues.wboostOrDefault,
          queryValues.lboostOrDefault,
          queryValues.mboostOrDefault,
          queryValues.jboostOrDefault),
        pafdefault = queryValues.pafDefaultOrDefault
      ),
      status = NotFoundAddressResponseStatus,
      errors = Seq(NotFoundAddressResponseError)
    )
  }

  def RandomFilterInvalid(queryValues: QueryValues): AddressByRandomResponseContainer = {
    BadRequestRandomTemplate(queryValues, FilterInvalidError)
  }

  def RandomMixedFilter(queryValues: QueryValues): AddressByRandomResponseContainer = {
    BadRequestRandomTemplate(queryValues, MixedFilterError)
  }

  def FailedRequestToEsRandom(detail: String, queryValues: QueryValues): AddressByRandomResponseContainer = {
    val enhancedError = new AddressResponseError(FailedRequestToEsRandomError.code, FailedRequestToEsRandomError.message.replace("see logs", detail))
    AddressByRandomResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorRandom(queryValues),
      status = InternalServerErrorAddressResponseStatus,
      errors = Seq(enhancedError)
    )
  }

  def FailedRequestToEsTooBusyRandom(detail: String, queryValues: QueryValues): AddressByRandomResponseContainer = {
    val enhancedError = new AddressResponseError(FailedRequestToEsRandomError.code, FailedRequestToEsRandomError.message.replace("see logs", detail))
    AddressByRandomResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorRandom(queryValues),
      status = TooManyRequestsResponseStatus,
      errors = Seq(enhancedError)
    )
  }

  def ErrorRandom(queryValues: QueryValues): AddressByRandomResponse = {
    AddressByRandomResponse(
      addresses = Seq.empty,
      filter = queryValues.filterOrDefault,
      limit = queryValues.limitOrDefault,
      historical = queryValues.historicalOrDefault,
      epoch = queryValues.epochOrDefault,
      verbose = queryValues.verboseOrDefault,
      countryBoosts = CountryBoosts(queryValues.eboostOrDefault,
        queryValues.nboostOrDefault,
        queryValues.sboostOrDefault,
        queryValues.wboostOrDefault,
        queryValues.lboostOrDefault,
        queryValues.mboostOrDefault,
        queryValues.jboostOrDefault),
      pafdefault = queryValues.pafDefaultOrDefault
    )
  }

  def BadRequestRandomTemplate(queryValues: QueryValues, errors: AddressResponseError*): AddressByRandomResponseContainer = {
    AddressByRandomResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorRandom(queryValues),
      status = BadRequestAddressResponseStatus,
      errors = errors
    )
  }

  def LimitNotNumericRandom(queryValues: QueryValues): AddressByRandomResponseContainer = {
    BadRequestRandomTemplate(queryValues, LimitNotNumericAddressResponseError)
  }

  def LimitTooSmallRandom(queryValues: QueryValues): AddressByRandomResponseContainer = {
    BadRequestRandomTemplate(queryValues, LimitTooSmallAddressResponseError)
  }

  def LimitTooLargeRandom(queryValues: QueryValues): AddressByRandomResponseContainer = {
    BadRequestRandomTemplate(queryValues, LimitTooLargeAddressResponseError)
  }

  def RandomEpochInvalid(queryValues: QueryValues): AddressByRandomResponseContainer = {
    BadRequestRandomTemplate(queryValues, EpochNotAvailableError)
  }

  def RandomFromSourceInvalid(queryValues: QueryValues): AddressByRandomResponseContainer = {
    BadRequestRandomTemplate(queryValues, FromSourceInvalidError)
  }

  def RandomCountryBoostsInvalid(queryValues: QueryValues): AddressByRandomResponseContainer = {
    BadRequestRandomTemplate(queryValues, CountryBoostsInvalidError)
  }

  def RandomCountryDeprecation(queryValues: QueryValues): AddressByRandomResponseContainer = {
    BadRequestRandomTemplate(queryValues, CountryDeprecationError)
  }

}
