package uk.gov.ons.addressIndex.server.modules.response

import uk.gov.ons.addressIndex.model.server.response.address._

trait AddressControllerResponse extends AddressResponse {

  def AddressFilterInvalid: AddressBySearchResponseContainer = {
    BadRequestTemplate(FilterInvalidError)
  }

  def AddressMixedFilter: AddressBySearchResponseContainer = {
    BadRequestTemplate(MixedFilterError)
  }

  def PostcodeFilterInvalid: AddressBySearchResponseContainer = {
    BadRequestTemplate(FilterInvalidError)
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

  def UnsupportedFormat: AddressBySearchResponseContainer = {
    BadRequestTemplate(FormatNotSupportedAddressResponseError)
  }

  def ThresholdNotNumeric: AddressBySearchResponseContainer = {
    BadRequestTemplate(ThresholdNotNumericAddressResponseError)
  }

  def ThresholdNotInRange: AddressBySearchResponseContainer = {
    BadRequestTemplate(ThresholdNotInRangeAddressResponseError)
  }

}
