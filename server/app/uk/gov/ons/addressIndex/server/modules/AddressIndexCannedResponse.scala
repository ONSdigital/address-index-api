package uk.gov.ons.addressIndex.server.modules

import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.server.response._

trait AddressIndexCannedResponse {

  def Limit: Int = 10

  def searchContainerTemplate(
    tokens: Seq[CrfTokenResult],
    addresses: Seq[AddressResponseAddress],
    total: Int
  ): AddressBySearchResponseContainer = {
    AddressBySearchResponseContainer(
      response = AddressBySearchResponse(
        tokens = tokens
          .groupBy(_.label).map { case (tkn, seqTknRslt) =>
            tkn -> seqTknRslt.map(_.value).mkString(" ")
          }.map { case (tkn, input) =>
            CrfTokenResult(tkn, input)
          }.toSeq,
        addresses = addresses,
        limit = Limit,
        offset = 0,
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
      limit = Limit,
      offset = 0,
      total = 0
    )
  }
}