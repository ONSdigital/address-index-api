package uk.gov.ons.addressIndex.server.modules.response

import uk.gov.ons.addressIndex.model.server.response.address._

trait AddressResponse extends Response {

  def OffsetTooSmall(queryValues: Map[String,Any]): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues,OffsetTooSmallAddressResponseError)
  }

  def OffsetNotNumeric(queryValues: Map[String,Any]): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues,OffsetNotNumericAddressResponseError)
  }

  def OffsetTooLarge(queryValues: Map[String,Any]): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues,OffsetTooLargeAddressResponseError)
  }

  def LimitNotNumeric(queryValues: Map[String,Any]): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues,LimitNotNumericAddressResponseError)
  }

  def LimitTooLarge(queryValues: Map[String,Any]): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues,LimitTooLargeAddressResponseError)
  }

  def LimitTooSmall(queryValues: Map[String,Any]): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues,LimitTooSmallAddressResponseError)
  }

  def EmptySearch(queryValues: Map[String,Any]): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues,EmptyQueryAddressResponseError)
  }

}
