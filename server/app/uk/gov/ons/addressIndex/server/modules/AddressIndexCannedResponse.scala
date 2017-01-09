package uk.gov.ons.addressIndex.server.modules

import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.server.response._

trait AddressIndexCannedResponse {

  def searchContainerTemplate(
    tokens: Seq[CrfTokenResult],
    addresses: Seq[AddressResponseAddress],
    total: Int,
    limit: Int,
    offset: Int
  ): AddressBySearchResponseContainer = {
    AddressBySearchResponseContainer(
      response = AddressBySearchResponse(
        tokens = tokens
          .groupBy(_.label).map { case (tkn, seqTknRslt) =>
            CrfTokenResult(
              value = tkn,
              label = seqTknRslt.map(_.value).mkString(" ")
            )
          }.toSeq,
        addresses = addresses,
        limit = limit,
        offset = offset,
        total = addresses.size
      ),
      status = OkAddressResponseStatus
    )
  }

  def searchUprnContainerTemplate(optAddresses: Option[AddressResponseAddress]): AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      response = AddressByUprnResponse(
        address = optAddresses
      ),
      status = OkAddressResponseStatus
    )
  }

  def NoAddressFoundUprn: AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      response = AddressByUprnResponse(
        address = None
      ),
      status = NotFoundAddressResponseStatus,
      errors = Seq(NotFoundAddressResponseError)
    )
  }

  def UnsupportedFormatUprn: AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      response = AddressByUprnResponse(
        address = None
      ),
      status = BadRequestAddressResponseStatus,
      errors = Seq(FormatNotSupportedAddressResponseError)
    )
  }

  def LimitNotNumeric: AddressBySearchResponseContainer = {
    AddressBySearchResponseContainer(
      response = Error,
      status = BadRequestAddressResponseStatus,
      errors = Seq(LimitNotNumericAddressResponseError)
    )
  }

  def OffsetNotNumeric: AddressBySearchResponseContainer = {
    AddressBySearchResponseContainer(
      response = Error,
      status = BadRequestAddressResponseStatus,
      errors = Seq(OffsetNotNumericAddressResponseError)
    )
  }

  def LimitTooSmall: AddressBySearchResponseContainer = {
    AddressBySearchResponseContainer(
      response = Error,
      status = BadRequestAddressResponseStatus,
      errors = Seq(LimitTooSmallAddressResponseError)
    )
  }

  def OffsetTooSmall: AddressBySearchResponseContainer = {
    AddressBySearchResponseContainer(
      response = Error,
      status = BadRequestAddressResponseStatus,
      errors = Seq(OffsetTooSmallAddressResponseError)
    )
  }

  def LimitTooLarge: AddressBySearchResponseContainer = {
    AddressBySearchResponseContainer(
      response = Error,
      status = BadRequestAddressResponseStatus,
      errors = Seq(LimitTooLargeAddressResponseError)
    )
  }

  def OffsetTooLarge: AddressBySearchResponseContainer = {
    AddressBySearchResponseContainer(
      response = Error,
      status = BadRequestAddressResponseStatus,
      errors = Seq(OffsetTooLargeAddressResponseError)
    )
  }

  def UnsupportedFormat: AddressBySearchResponseContainer = {
    AddressBySearchResponseContainer(
      response = Error,
      status = BadRequestAddressResponseStatus,
      errors = Seq(FormatNotSupportedAddressResponseError)
    )
  }

  def EmptySearch: AddressBySearchResponseContainer = {
    AddressBySearchResponseContainer(
      response = Error,
      status = BadRequestAddressResponseStatus,
      errors = Seq(EmptyQueryAddressResponseError)
    )
  }

  def Error: AddressBySearchResponse = {
    AddressBySearchResponse(
      Seq.empty,
      addresses = Seq.empty,
      limit = 10,
      offset = 0,
      total = 0
    )
  }
}