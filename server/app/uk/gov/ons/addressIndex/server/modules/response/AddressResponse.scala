package uk.gov.ons.addressIndex.server.modules.response

import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.server.model.dao.QueryValues

trait AddressResponse extends Response {

  def OffsetTooSmall(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, OffsetTooSmallAddressResponseError)
  }

  def OffsetNotNumeric(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, OffsetNotNumericAddressResponseError)
  }

  def OffsetTooLarge(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, OffsetTooLargeAddressResponseError)
  }

  def LimitNotNumeric(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, LimitNotNumericAddressResponseError)
  }

  def LimitTooLarge(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, LimitTooLargeAddressResponseError)
  }

  def LimitTooSmall(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, LimitTooSmallAddressResponseError)
  }

  def EmptySearch(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, EmptyQueryAddressResponseError)
  }

}
