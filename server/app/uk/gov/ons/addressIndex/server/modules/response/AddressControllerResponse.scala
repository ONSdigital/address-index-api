package uk.gov.ons.addressIndex.server.modules.response

import uk.gov.ons.addressIndex.model.server.response.address._

trait AddressControllerResponse extends AddressResponse {

  def AddressFilterInvalid(queryValues: Map[String,Any]): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues,FilterInvalidError)
  }

  def AddressMixedFilter(queryValues: Map[String,Any]): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues,MixedFilterError)
  }

  def PostcodeFilterInvalid(queryValues: Map[String,Any]): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues,FilterInvalidError)
  }

  def RangeNotNumeric(queryValues: Map[String,Any]): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues,RangeNotNumericAddressResponseError)
  }

  def LatitiudeNotNumeric(queryValues: Map[String,Any]): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues,LatitudeNotNumericAddressResponseError)
  }

  def LongitudeNotNumeric(queryValues: Map[String,Any]): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues,LongitudeNotNumericAddressResponseError)
  }

  def LatitudeTooFarNorth(queryValues: Map[String,Any]): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues,LatitudeTooFarNorthAddressResponseError)
  }

  def LongitudeTooFarEast(queryValues: Map[String,Any]): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues,LongitudeTooFarEastAddressResponseError)
  }

  def LatitudeTooFarSouth(queryValues: Map[String,Any]): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues,LatitudeTooFarSouthAddressResponseError)
  }

  def LongitudeTooFarWest(queryValues: Map[String,Any]): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues,LongitudeTooFarWestAddressResponseError)
  }

  def UnsupportedFormat(queryValues: Map[String,Any]): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues,FormatNotSupportedAddressResponseError)
  }

  def ThresholdNotNumeric(queryValues: Map[String,Any]): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues,ThresholdNotNumericAddressResponseError)
  }

  def ThresholdNotInRange(queryValues: Map[String,Any]): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues,ThresholdNotInRangeAddressResponseError)
  }

  def EpochInvalid(queryValues: Map[String,Any]): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues,EpochNotAvailableError)
  }

}
