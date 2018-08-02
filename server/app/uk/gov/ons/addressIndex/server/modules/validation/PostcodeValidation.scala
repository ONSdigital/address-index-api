package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Result
import uk.gov.ons.addressIndex.model.server.response._
import uk.gov.ons.addressIndex.server.modules.response.{Response, PostcodeResponse}
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}
import uk.gov.ons.addressIndex.server.utils.APILogging
import uk.gov.ons.addressIndex.server.utils.impl.{AddressLogMessage, AddressLogging}

import scala.concurrent.Future
import scala.util.Try

@Singleton
class PostcodeValidation @Inject()(implicit conf: ConfigModule, versionProvider: VersionModule )
  extends Validation with PostcodeResponse with APILogging[AddressLogMessage] {

  override def trace(message: AddressLogMessage): Unit = AddressLogging trace message
  override def log(message: AddressLogMessage): Unit = AddressLogging log message
  override def debug(message: AddressLogMessage): Unit = AddressLogging debug message

  def validatePostcodeLimit(limit: Option[String]): Option[Future[Result]] = {

    val defLimit: Int = conf.config.elasticSearch.defaultLimit
    val limval = limit.getOrElse(defLimit.toString)
    val limitInvalid = Try(limval.toInt).isFailure
    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)
    val maxLimit: Int = conf.config.elasticSearch.maximumLimit

    if (limitInvalid) {
      log(AddressLogMessage(badRequestMessage = LimitNotNumericPostcodeAddressResponseError.message))
      Some(futureJsonBadRequest(LimitNotNumericPostcode))
    } else if (limitInt < 1) {
      log(AddressLogMessage(badRequestMessage = LimitTooSmallPostcodeAddressResponseError.message))
      Some(futureJsonBadRequest(LimitTooSmallPostcode))
    } else if (limitInt > maxLimit) {
      log(AddressLogMessage(badRequestMessage = LimitTooLargePostcodeAddressResponseError.message))
      Some(futureJsonBadRequest(LimitTooLargePostcode))
    } else None

  }

  def validatePostcodeFilter(classificationfilter: Option[String]): Option[Future[Result]] = {

    val filterString: String = classificationfilter.getOrElse("")

    if (!filterString.isEmpty &&
      !filterString.matches("""\b(residential|commercial|C|c|C\w+|c\w+|L|l|L\w+|l\w+|M|m|M\w+|m\w+|O|o|O\w+|o\w+|P|p|P\w+|p\w+|R|r|R\w+|r\w+|U|u|U\w+|u\w+|X|x|X\w+|x\w+|Z|z|Z\w+|z\w+)\b.*""")) {
      log(AddressLogMessage(badRequestMessage = FilterInvalidError.message))
      Some(futureJsonBadRequest(PostcodeFilterInvalid))
    } else None
  }


  def validatePostcodeOffset(offset: Option[String]): Option[Future[Result]] = {
    val maxOffset: Int = conf.config.elasticSearch.maximumOffset
    val defOffset: Int = conf.config.elasticSearch.defaultOffset
    val offval = offset.getOrElse(defOffset.toString)
    val offsetInvalid = Try(offval.toInt).isFailure
    val offsetInt = Try(offval.toInt).toOption.getOrElse(defOffset)

    if (offsetInvalid) {
      log(AddressLogMessage(badRequestMessage = OffsetNotNumericPostcodeAddressResponseError.message))
      Some(futureJsonBadRequest(OffsetNotNumericPostcode))
    } else if (offsetInt < 0) {
      log(AddressLogMessage(badRequestMessage = OffsetTooSmallPostcodeAddressResponseError.message))
      Some(futureJsonBadRequest(OffsetTooSmallPostcode))
    } else if (offsetInt > maxOffset) {
      log(AddressLogMessage(badRequestMessage = OffsetTooLargePostcodeAddressResponseError.message))
      Some(futureJsonBadRequest(OffsetTooLargePostcode))
    } else None
  }

  def validatePostcode(postcode: String): Option[Future[Result]] = {
    if (postcode.isEmpty) {
      log(AddressLogMessage(badRequestMessage = EmptyQueryPostcodeAddressResponseError.message))
      Some(futureJsonBadRequest(EmptySearchPostcode))
    } else None
  }

}
