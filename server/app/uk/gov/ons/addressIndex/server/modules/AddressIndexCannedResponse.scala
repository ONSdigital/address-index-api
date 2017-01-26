package uk.gov.ons.addressIndex.server.modules

import uk.gov.ons.addressIndex.model.server.response._

trait AddressIndexCannedResponse {

  def searchUprnContainerTemplate(optAddresses: Option[AddressInformation]): Container = {
    Container(
      status = PredefStatus.Ok
    )
  }

  def NoAddressFoundUprn: Container = {
    Container(
      status = PredefStatus.BadRequest,
      errors = Some(Seq(PredefError.NotFound))
    )
  }

  def UnsupportedFormatUprn: Container = {
    Container(
      status = PredefStatus.BadRequest,
      errors = Some(Seq(PredefError.FormatNotSupported))
    )
  }

  private def BadRequestTemplate(errors: Error*): Container = {
    Container(
      response = None,
      status = PredefStatus.BadRequest,
      errors = Some(errors)
    )
  }

  def OffsetNotNumeric: Container = {
    BadRequestTemplate(PredefError.OffsetNotNumeric)
  }

  def LimitNotNumeric: Container = {
    BadRequestTemplate(PredefError.LimitNotNumeric)
  }

  def LimitTooSmall: Container = {
    BadRequestTemplate(PredefError.LimitTooSmall)
  }

  def OffsetTooSmall: Container = {
    BadRequestTemplate(PredefError.OffsetTooSmall)
  }

  def LimitTooLarge: Container = {
    BadRequestTemplate(PredefError.LimitTooLarge)
  }

  def OffsetTooLarge: Container = {
    BadRequestTemplate(PredefError.OffsetTooLarge)
  }

  def UnsupportedFormat: Container = {
    BadRequestTemplate(PredefError.FormatNotSupported)
  }

  def EmptySearch: Container = {
    BadRequestTemplate(PredefError.EmptyQuery)
  }

  def ErrorResults: Results = {
    Results(
      Seq.empty,
      addresses = None,
      limit = 10,
      offset = 0,
      total = 0
    )
  }

  object PredefError {
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

  object PredefStatus {
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