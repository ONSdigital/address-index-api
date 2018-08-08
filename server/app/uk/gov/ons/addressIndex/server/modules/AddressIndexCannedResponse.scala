package uk.gov.ons.addressIndex.server.modules

import uk.gov.ons.addressIndex.model.server.response._

trait AddressIndexCannedResponse {
  
  def apiVersion: String
  def dataVersion: String

  def searchUprnContainerTemplate(optAddresses: Option[AddressResponseAddress]): AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByUprnResponse(
        address = optAddresses
      ),
      status = OkAddressResponseStatus
    )
  }

  def NoAddressFoundUprn: AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByUprnResponse(
        address = None
      ),
      status = NotFoundAddressResponseStatus,
      errors = Seq(NotFoundAddressResponseError)
    )
  }

  def UnsupportedFormatUprn: AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByUprnResponse(
        address = None
      ),
      status = BadRequestAddressResponseStatus,
      errors = Seq(FormatNotSupportedAddressResponseError)
    )
  }

  def NoAddressFoundPostcode: AddressByPostcodeResponseContainer = {
    AddressByPostcodeResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByPostcodeResponse(
        postcode = "",
        addresses = Seq.empty,
        filter= "",
        historical=true,
        limit = 10,
        offset = 0,
        total = 0,
        maxScore = 0f
      ),
      status = NotFoundAddressResponseStatus,
      errors = Seq(NotFoundAddressResponseError)
    )
  }

  private def BadRequestTemplate(errors: AddressResponseError*): AddressBySearchResponseContainer = {
    AddressBySearchResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = Error,
      status = BadRequestAddressResponseStatus,
      errors = errors
    )
  }

  def BadRequestNonNumericUprn: AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByUprnResponse(
        address = None
      ),
      status = BadRequestAddressResponseStatus,
      errors = Seq(UprnNotNumericAddressResponseError)
    )
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

  private def UnauthorizedRequestTemplate(errors: AddressResponseError*): AddressBySearchResponseContainer = {
    AddressBySearchResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = Error,
      status = UnauthorizedRequestAddressResponseStatus,
      errors = errors
    )
  }

  def SourceMissing: AddressBySearchResponseContainer = {
    UnauthorizedRequestTemplate(SourceMissingError)
  }

  def SourceInvalid: AddressBySearchResponseContainer = {
    UnauthorizedRequestTemplate(SourceInvalidError)
  }

  def KeyMissing: AddressBySearchResponseContainer = {
    UnauthorizedRequestTemplate(ApiKeyMissingError)
  }

  def KeyInvalid: AddressBySearchResponseContainer = {
    UnauthorizedRequestTemplate(ApiKeyInvalidError)
  }

  def FilterInvalid: AddressBySearchResponseContainer = {
    BadRequestTemplate(FilterInvalidError)
  }

  def FilterInvalidPostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(FilterInvalidPostcodeError)
  }

  def OffsetNotNumeric: AddressBySearchResponseContainer = {
    BadRequestTemplate(OffsetNotNumericAddressResponseError)
  }

  def RangeNotNumeric: AddressBySearchResponseContainer = {
    BadRequestTemplate(RangeNotNumericAddressResponseError)
  }

  def LatitiudeNotNumeric: AddressBySearchResponseContainer = {
    BadRequestTemplate(LatitudeNotNumericAddressResponseError)
  }

  def LongitudeNotNumeric: AddressBySearchResponseContainer = {
    BadRequestTemplate(LongitudeNotNumericAddressResponseError)
  }

  def LatitudeTooFarNorth: AddressBySearchResponseContainer = {
    BadRequestTemplate(LatitudeTooFarNorthAddressResponseError)
  }

  def LongitudeTooFarEast: AddressBySearchResponseContainer = {
    BadRequestTemplate(LongitudeTooFarEastAddressResponseError)
  }

  def LatitudeTooFarSouth: AddressBySearchResponseContainer = {
    BadRequestTemplate(LatitudeTooFarSouthAddressResponseError)
  }

  def LongitudeTooFarWest: AddressBySearchResponseContainer = {
    BadRequestTemplate(LongitudeTooFarWestAddressResponseError)
  }

  def OffsetNotNumericPostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(OffsetNotNumericPostcodeAddressResponseError)
  }

  def LimitNotNumeric: AddressBySearchResponseContainer = {
    BadRequestTemplate(LimitNotNumericAddressResponseError)
  }

  def LimitNotNumericPostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(LimitNotNumericPostcodeAddressResponseError)
  }

  def LimitTooSmall: AddressBySearchResponseContainer = {
      BadRequestTemplate(LimitTooSmallAddressResponseError)
  }

  def LimitTooSmallPostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(LimitTooSmallPostcodeAddressResponseError)
  }

  def OffsetTooSmall: AddressBySearchResponseContainer = {
    BadRequestTemplate(OffsetTooSmallAddressResponseError)
  }

  def OffsetTooSmallPostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(OffsetTooSmallPostcodeAddressResponseError)
  }

  def LimitTooLarge: AddressBySearchResponseContainer = {
    BadRequestTemplate(LimitTooLargeAddressResponseError)
  }

  def LimitTooLargePostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(LimitTooLargePostcodeAddressResponseError)
  }

  def OffsetTooLarge: AddressBySearchResponseContainer = {
      BadRequestTemplate(OffsetTooLargeAddressResponseError)
  }

  def OffsetTooLargePostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(OffsetTooLargePostcodeAddressResponseError)
  }

  def UprnNotNumeric: AddressByUprnResponseContainer = {
    BadRequestNonNumericUprn
  }

  def UnsupportedFormat: AddressBySearchResponseContainer = {
      BadRequestTemplate(FormatNotSupportedAddressResponseError)
  }

  def EmptySearch: AddressBySearchResponseContainer = {
      BadRequestTemplate(EmptyQueryAddressResponseError)
  }

  def EmptySearchPostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(EmptyQueryPostcodeAddressResponseError)
  }

  def ThresholdNotNumeric: AddressBySearchResponseContainer = {
    BadRequestTemplate(ThresholdNotNumericAddressResponseError)
  }

  def ThresholdNotInRange: AddressBySearchResponseContainer = {
    BadRequestTemplate(ThresholdNotInRangeAddressResponseError)
  }

  def FailedRequestToEs: AddressBySearchResponseContainer = {
    AddressBySearchResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = Error,
      status = InternalServerErrorAddressResponseStatus,
      errors = Seq(FailedRequestToEsError)
    )
  }

  def FailedRequestToEsTooBusy: AddressBySearchResponseContainer = {
    AddressBySearchResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = Error,
      status = TooManyRequestsResponseStatus,
      errors = Seq(FailedRequestToEsError)
    )
  }

  def Error: AddressBySearchResponse = {
    AddressBySearchResponse(
      Map.empty,
      addresses = Seq.empty,
      filter = "",
      historical = true,
      rangekm = "",
      latitude = "",
      longitude = "",
      limit = 10,
      offset = 0,
      total = 0,
      sampleSize = 20,
      maxScore = 0f,
      matchthreshold = 5f
    )
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
      filter= "",
      historical = true,
      limit = 10,
      offset = 0,
      total = 0,
      maxScore = 0f
    )
  }

  def FailedRequestToEsPartialAddress: AddressByPartialAddressResponseContainer = {
    AddressByPartialAddressResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorPartialAddress,
      status = InternalServerErrorAddressResponseStatus,
      errors = Seq(FailedRequestToEsPartialAddressError)
    )
  }

  def ErrorPartialAddress: AddressByPartialAddressResponse = {
    AddressByPartialAddressResponse(
      input = "",
      addresses = Seq.empty,
      filter= "",
      historical = true,
      limit = 10,
      offset = 0,
      total = 0,
      maxScore = 0f
    )
  }

}
