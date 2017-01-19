package uk.gov.ons.addressIndex.server.modules

import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.server.response.Model.HybridResponse
import uk.gov.ons.addressIndex.model.server.response._
import uk.gov.ons.addressIndex.server.modules.Model.Pagination

trait AddressIndexCannedResponse {

  def searchContainerTemplate(
    tokens: Seq[CrfTokenResult],
    addresses: Seq[HybridResponse],
    total: Int
  )(implicit p: Pagination): Container = {
    Container(
      response = Results(
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
      errors = Seq(NotFoundError$)
    )
  }

  def UnsupportedFormatUprn: AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      response = AddressByUprnResponse(
        address = None
      ),
      status = BadRequestAddressResponseStatus,
      errors = Seq(FormatNotSupportedError$)
    )
  }

  private def BadRequestTemplate(errors: Error*): Container = {
    Container(
      response = Error,
      status = BadRequestAddressResponseStatus,
      errors = errors
    )
  }

  def OffsetNotNumeric: Container = {
    BadRequestTemplate(OffsetNotNumericError$)
  }

  def LimitNotNumeric: Container = {
    BadRequestTemplate(LimitNotNumericError$)
  }

  def LimitTooSmall: Container = {
    BadRequestTemplate(LimitTooSmallError$)
  }

  def OffsetTooSmall: Container = {
    BadRequestTemplate(OffsetTooSmallError$)
  }

  def LimitTooLarge: Container = {
    BadRequestTemplate(LimitTooLargeError$)
  }

  def OffsetTooLarge: Container = {
    BadRequestTemplate(OffsetTooLargeError$)
  }

  def UnsupportedFormat: Container = {
    BadRequestTemplate(FormatNotSupportedError$)
  }

  def EmptySearch: Container = {
    BadRequestTemplate(EmptyQueryError$)
  }

  def Error: Results = {
    Results(
      Seq.empty,
      addresses = Seq.empty,
      limit = 10,
      offset = 0,
      total = 0
    )
  }
}