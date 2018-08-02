package uk.gov.ons.addressIndex.server.modules.response

import uk.gov.ons.addressIndex.model.server.response._

trait AddressIndexResponse extends Response {

  def OffsetTooLarge: AddressBySearchResponseContainer = {
    BadRequestTemplate(OffsetTooLargeAddressResponseError)
  }

  def LimitNotNumeric: AddressBySearchResponseContainer = {
    BadRequestTemplate(LimitNotNumericAddressResponseError)
  }

  def LimitTooSmall: AddressBySearchResponseContainer = {
    BadRequestTemplate(LimitTooSmallAddressResponseError)
  }

  def LimitTooLarge: AddressBySearchResponseContainer = {
    BadRequestTemplate(LimitTooLargeAddressResponseError)
  }

  def AddressFilterInvalid: AddressBySearchResponseContainer = {
    BadRequestTemplate(FilterInvalidError)
  }

  def PostcodeFilterInvalid: AddressBySearchResponseContainer = {
    BadRequestTemplate(FilterInvalidError)
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

  def OffsetTooSmall: AddressBySearchResponseContainer = {
    BadRequestTemplate(OffsetTooSmallAddressResponseError)
  }

  def UnsupportedFormat: AddressBySearchResponseContainer = {
    BadRequestTemplate(FormatNotSupportedAddressResponseError)
  }

  def EmptySearch: AddressBySearchResponseContainer = {
    BadRequestTemplate(EmptyQueryAddressResponseError)
  }

  def ThresholdNotNumeric: AddressBySearchResponseContainer = {
    BadRequestTemplate(ThresholdNotNumericAddressResponseError)
  }

  def ThresholdNotInRange: AddressBySearchResponseContainer = {
    BadRequestTemplate(ThresholdNotInRangeAddressResponseError)
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

}
