package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Result
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.postcode.AddressByPostcodeResponseContainer
import uk.gov.ons.addressIndex.server.model.dao.{QueryValues, RequestValues}
import uk.gov.ons.addressIndex.server.modules.response.PostcodeControllerResponse
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Singleton
class PostcodeControllerValidation @Inject()(implicit conf: ConfigModule, versionProvider: VersionModule)
  extends AddressValidation with PostcodeControllerResponse {

  // override error message with named length
  object EpochNotAvailableErrorCustom extends AddressResponseError(
    code = 36,
    message = EpochNotAvailableError.message.concat(". Current available epochs are " + validEpochsMessage + ".")
  )

  override def PostcodeEpochInvalid(queryValues: QueryValues): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues, EpochNotAvailableErrorCustom)
  }

  override def LimitTooLargePostcode(queryValues: QueryValues): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues, LimitTooLargeAddressResponseErrorCustom)
  }

  override def OffsetTooLargePostcode(queryValues: QueryValues): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues, OffsetTooLargeAddressResponseErrorCustom)
  }

  def validatePostcodeLimit(limit: Option[String], queryValues: QueryValues, requestValues: RequestValues): Option[Future[Result]] = {
    def inner(limit: Int): Option[Future[Result]] = limit match {
      case l if l < 1 =>
        logger.systemLog(responsecode = "400",badRequestMessage = LimitTooSmallAddressResponseError.message)
        Some(futureJsonBadRequest(LimitTooSmallPostcode(queryValues)))
      case l if maximumLimit < l =>
        logger.systemLog(responsecode = "400",badRequestMessage = LimitTooLargeAddressResponseErrorCustom.message)
        Some(futureJsonBadRequest(LimitTooLargePostcode(queryValues)))
      case _ => None
    }

    limit match {
      case Some(l) => Try(l.toInt) match {
        case Success(lInt) => inner(lInt)
        case Failure(_) =>
          logger.systemLog(responsecode = "400",badRequestMessage = LimitNotNumericAddressResponseError.message)
          Some(futureJsonBadRequest(LimitNotNumericPostcode(queryValues)))
      }
      case None => inner(defaultLimit)
    }
  }

  def validatePostcodeFilter(classificationFilter: Option[String], queryValues: QueryValues, requestValues: RequestValues): Option[Future[Result]] = {
    val postcodeFilterRegex: String = """\b(residential|commercial|workplace|educational|[CcLlMmOoPpRrUuXxZz]\w*)\b.*"""
    val filterString: String = classificationFilter.getOrElse("")

    filterString match {
      case "" => None
      case s if s.contains("*") && s.contains(",") =>
        logger.systemLog(responsecode = "400",badRequestMessage = MixedFilterError.message)
        Some(futureJsonBadRequest(PostcodeMixedFilter(queryValues)))
      case s if !s.matches(postcodeFilterRegex) =>
        logger.systemLog(responsecode = "400",badRequestMessage = FilterInvalidError.message)
        Some(futureJsonBadRequest(PostcodeFilterInvalid(queryValues)))
      case _ => None
    }
  }

  def validateBucketPattern(bucketPattern: String, queryValues: QueryValues, requestValues: RequestValues): Option[Future[Result]] = {
    bucketPattern match {
    case "*_*_*" => logger.systemLog(responsecode = "400",badRequestMessage = InvalidEQBucketError.message)
      Some(futureJsonBadRequest(EQBucketInvalid(queryValues)))
    case _ => None
    }
  }

  def validatePostcodeOffset(offset: Option[String], queryValues: QueryValues, requestValues: RequestValues): Option[Future[Result]] = {
    def inner(offset: Int): Option[Future[Result]] = offset match {
      case l if l < 0 =>
        logger.systemLog(responsecode = "400",badRequestMessage = OffsetTooSmallAddressResponseError.message)
        Some(futureJsonBadRequest(OffsetTooSmallPostcode(queryValues)))
      case l if maximumOffset < l =>
        logger.systemLog(responsecode = "400",badRequestMessage = OffsetTooLargeAddressResponseErrorCustom.message)
        Some(futureJsonBadRequest(OffsetTooLargePostcode(queryValues)))
      case _ => None
    }

    offset match {
      case Some(l) => Try(l.toInt) match {
        case Success(lInt) => inner(lInt)
        case Failure(_) =>
          logger.systemLog(responsecode = "400",badRequestMessage = OffsetNotNumericAddressResponseError.message)
          Some(futureJsonBadRequest(OffsetNotNumericPostcode(queryValues)))
      }
      case None => inner(defaultOffset)
    }
  }

  def validatePostcode(postcode: String, queryValues: QueryValues, requestValues: RequestValues): Option[Future[Result]] = {
    val postcodeRegex = "^(GIR 0AA)|((([A-Z][0-9]{1,2})|(([A-Z][A-HJ-Y][0-9]{1,2})|(([A-Z][0-9][A-Z])|([A-Z][A-HJ-Y][0-9]?[A-Z])))) ?[0-9][A-Z]{2})$"
    postcode match {
      case "" =>
        logger.systemLog(responsecode = "400",badRequestMessage = EmptyQueryPostcodeAddressResponseError.message)
        Some(futureJsonBadRequest(EmptySearchPostcode(queryValues)))
      case s if !s.toUpperCase().matches(postcodeRegex) =>
        logger.systemLog(responsecode = "400",badRequestMessage = postcode + ": " + InvalidPostcodeAddressResponseError.message)
        Some(futureJsonBadRequest(InvalidPostcode(queryValues)))
      case _ => None
    }
  }

  // validEpochsRegex is inherited from AddressControllerValidation
  def validateEpoch(queryValues: QueryValues, requestValues: RequestValues): Option[Future[Result]] =
    queryValues.epochOrDefault match {
      case "" => None
      case validEpochsRegex(_*) => None
      case e =>
        logger.systemLog(responsecode = "400",badRequestMessage = EpochNotAvailableError.message, epoch = e, postcode = queryValues.postcodeOrDefault)
        Some(futureJsonBadRequest(PostcodeEpochInvalid(queryValues)))
    }

}
