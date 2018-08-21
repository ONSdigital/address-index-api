package uk.gov.ons.addressIndex.server.modules.response

import uk.gov.ons.addressIndex.model.server.response.address._

trait AddressResponse extends Response {

  def OffsetTooSmall: AddressBySearchResponseContainer = {
    BadRequestTemplate(OffsetTooSmallAddressResponseError)
  }

  def OffsetNotNumeric: AddressBySearchResponseContainer = {
    BadRequestTemplate(OffsetNotNumericAddressResponseError)
  }

  def OffsetTooLarge: AddressBySearchResponseContainer = {
    BadRequestTemplate(OffsetTooLargeAddressResponseError)
  }

  def LimitNotNumeric: AddressBySearchResponseContainer = {
    BadRequestTemplate(LimitNotNumericAddressResponseError)
  }

  def LimitTooLarge: AddressBySearchResponseContainer = {
    BadRequestTemplate(LimitTooLargeAddressResponseError)
  }

  def LimitTooSmall: AddressBySearchResponseContainer = {
    BadRequestTemplate(LimitTooSmallAddressResponseError)
  }

  def EmptySearch: AddressBySearchResponseContainer = {
    BadRequestTemplate(EmptyQueryAddressResponseError)
  }
}
