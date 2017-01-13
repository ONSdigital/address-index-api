package uk.gov.ons.addressIndex.server.modules

import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.server.response._
import uk.gov.ons.addressIndex.server.modules.Model.Pagination

trait AddressIndexCannedResponse {

  def searchContainerTemplate(
    tokens: Seq[CrfTokenResult],
    addresses: Seq[AddressResponseAddress],
    total: Int
  )(implicit p: Pagination): AddressBySearchResponseContainer = {
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
        limit = p.limit,
        offset = p.offset,
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

  private def BadRequestTemplate(errors: AddressResponseError*): AddressBySearchResponseContainer = {
    AddressBySearchResponseContainer(
      response = Error,
      status = BadRequestAddressResponseStatus,
      errors = errors
    )
  }

  def OffsetNotNumeric: AddressBySearchResponseContainer = {
    BadRequestTemplate(OffsetNotNumericAddressResponseError)
  }

  def LimitNotNumeric: AddressBySearchResponseContainer = {
    BadRequestTemplate(LimitNotNumericAddressResponseError)
  }

  def LimitTooSmall: AddressBySearchResponseContainer = {
    BadRequestTemplate(LimitTooSmallAddressResponseError)
  }

  def OffsetTooSmall: AddressBySearchResponseContainer = {
    BadRequestTemplate(OffsetTooSmallAddressResponseError)
  }

  def LimitTooLarge: AddressBySearchResponseContainer = {
    BadRequestTemplate(LimitTooLargeAddressResponseError)
  }

  def OffsetTooLarge: AddressBySearchResponseContainer = {
    BadRequestTemplate(OffsetTooLargeAddressResponseError)
  }

  def UnsupportedFormat: AddressBySearchResponseContainer = {
    BadRequestTemplate(FormatNotSupportedAddressResponseError)
  }

  def EmptySearch: AddressBySearchResponseContainer = {
    BadRequestTemplate(EmptyQueryAddressResponseError)
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