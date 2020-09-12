package uk.gov.ons.addressIndex.server.modules.response

import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.server.model.dao.QueryValues

trait AddressControllerResponse extends AddressResponse {

  def AddressFilterInvalid(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, FilterInvalidError)
  }

  def AddressMixedFilter(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, MixedFilterError)
  }

  def PostcodeFilterInvalid(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, FilterInvalidError)
  }

  def RangeNotNumeric(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, RangeNotNumericAddressResponseError)
  }

  def LatitiudeNotNumeric(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, LatitudeNotNumericAddressResponseError)
  }

  def LongitudeNotNumeric(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, LongitudeNotNumericAddressResponseError)
  }

  def LatitudeTooFarNorth(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, LatitudeTooFarNorthAddressResponseError)
  }

  def LongitudeTooFarEast(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, LongitudeTooFarEastAddressResponseError)
  }

  def LatitudeTooFarSouth(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, LatitudeTooFarSouthAddressResponseError)
  }

  def LongitudeTooFarWest(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, LongitudeTooFarWestAddressResponseError)
  }

  def UnsupportedFormat(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, FormatNotSupportedAddressResponseError)
  }

  def ThresholdNotNumeric(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, ThresholdNotNumericAddressResponseError)
  }

  def ThresholdNotInRange(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, ThresholdNotInRangeAddressResponseError)
  }

  def FromSourceInvalid(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, FromSourceInvalidError)
  }

  def EpochInvalid(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, EpochNotAvailableError)
  }

  def CountryBoostsInvalid(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, CountryBoostsInvalidError)
  }

  def CountryDeprecation(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, CountryDeprecationError)
  }

}
