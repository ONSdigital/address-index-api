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
      response = Some(
        Results(
          tokens = tokens
            .groupBy(_.label).map { case (tkn, seqTknRslt) =>
              CrfTokenResult(
                value = tkn,
                label = seqTknRslt.map(_.value).mkString(" ")
              )
            }.toSeq,
          addresses = Some(addresses),
          limit = p.limit,
          offset = p.offset,
          total = addresses.size
        )
      ),
      status = Status.Ok
    )
  }

  def searchUprnContainerTemplate(optAddresses: Option[AddressInformation]): Container = {
    Container(
      status = Status.Ok
    )
  }

  def NoAddressFoundUprn: Container = {
    Container(
      status = Status.BadRequest,
      errors = Some(Seq(Error.NotFound))
    )
  }

  def UnsupportedFormatUprn: Container = {
    Container(
      status = Status.BadRequest,
      errors = Some(Seq(Error.FormatNotSupported))
    )
  }

  private def BadRequestTemplate(errors: Error*): Container = {
    Container(
      response = None,
      status = Status.BadRequest,
      errors = Some(errors)
    )
  }

  def OffsetNotNumeric: Container = {
    BadRequestTemplate(Error.OffsetNotNumeric)
  }

  def LimitNotNumeric: Container = {
    BadRequestTemplate(Error.LimitNotNumeric)
  }

  def LimitTooSmall: Container = {
    BadRequestTemplate(Error.LimitTooSmall)
  }

  def OffsetTooSmall: Container = {
    BadRequestTemplate(Error.OffsetTooSmall)
  }

  def LimitTooLarge: Container = {
    BadRequestTemplate(Error.LimitTooLarge)
  }

  def OffsetTooLarge: Container = {
    BadRequestTemplate(Error.OffsetTooLarge)
  }

  def UnsupportedFormat: Container = {
    BadRequestTemplate(Error.FormatNotSupported)
  }

  def EmptySearch: Container = {
    BadRequestTemplate(Error.EmptyQuery)
  }

  def ErrorResults: Results = {
    Results(
      Seq.empty,
      addresses = Seq.empty,
      limit = 10,
      offset = 0,
      total = 0
    )
  }

  object Error {
    object EmptyQuery extends Error(
      code = 1,
      message = "Empty query"
    )

    object FormatNotSupported extends Error(
      code = 2,
      message = "Address format is not supported"
    )

    object NotFound extends Error(
      code = 3,
      message = "UPRN request didn't yield a result"
    )

    object LimitNotNumeric extends Error(
      code = 4,
      message = "Limit parameter not numeric"
    )

    object OffsetNotNumeric extends Error(
      code = 5,
      message = "Offset parameter not numeric"
    )

    object LimitTooSmall extends Error(
      code = 6,
      message = "Limit parameter too small, minimum = 1"
    )

    object OffsetTooSmall extends Error(
      code = 7,
      message = "Offset parameter too small, minimum = 0"
    )

    object LimitTooLarge extends Error(
      code = 8,
      message = "Limit parameter too large (maximum configurable)"
    )

    object OffsetTooLarge extends Error(
      code = 9,
      message = "Offset parameter too large (maximum configurable)"
    )
  }

  object Status {
    object Ok extends Status(
      code = play.api.http.Status.OK,
      message = "Ok"
    )

    object NotFound extends Status(
      code = play.api.http.Status.NOT_FOUND,
      message = "Not Found"
    )

    object BadRequest extends Status(
      code = play.api.http.Status.BAD_REQUEST,
      message = "Bad request"
    )
  }
}