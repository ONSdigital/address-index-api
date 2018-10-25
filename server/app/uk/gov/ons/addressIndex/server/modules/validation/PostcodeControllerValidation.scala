package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Result
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.postcode.AddressByPostcodeResponseContainer
import uk.gov.ons.addressIndex.server.modules.response.PostcodeControllerResponse
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}

import scala.concurrent.Future
import scala.util.Try

@Singleton
class PostcodeControllerValidation @Inject()(implicit conf: ConfigModule, versionProvider: VersionModule )
  extends AddressValidation with PostcodeControllerResponse {

  override def LimitTooLargePostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(LimitTooLargeAddressResponseErrorCustom)
  }

  override def OffsetTooLargePostcode: AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(OffsetTooLargeAddressResponseErrorCustom)
  }

  def validatePostcodeLimit(limit: Option[String]): Option[Future[Result]] = {

    val defLimit: Int = conf.config.elasticSearch.defaultLimit
    val limval = limit.getOrElse(defLimit.toString)
    val limitInvalid = Try(limval.toInt).isFailure
    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)
    val maxLimit: Int = conf.config.elasticSearch.maximumLimit

    if (limitInvalid) {
      logger.systemLog(badRequestMessage = LimitNotNumericAddressResponseError.message)
      Some(futureJsonBadRequest(LimitNotNumericPostcode))
    } else if (limitInt < 1) {
      logger.systemLog(badRequestMessage = LimitTooSmallAddressResponseError.message)
      Some(futureJsonBadRequest(LimitTooSmallPostcode))
    } else if (limitInt > maxLimit) {
      logger.systemLog(badRequestMessage = LimitTooLargeAddressResponseErrorCustom.message)
      Some(futureJsonBadRequest(LimitTooLargePostcode))
    } else None

  }

  def validatePostcodeFilter(classificationfilter: Option[String]): Option[Future[Result]] = {

    val filterString: String = classificationfilter.getOrElse("")

    if (!filterString.isEmpty &&
      !filterString.matches("""\b(residential|commercial|C|c|C\w+|c\w+|L|l|L\w+|l\w+|M|m|M\w+|m\w+|O|o|O\w+|o\w+|P|p|P\w+|p\w+|R|r|R\w+|r\w+|U|u|U\w+|u\w+|X|x|X\w+|x\w+|Z|z|Z\w+|z\w+)\b.*""")) {
      logger.systemLog(badRequestMessage = FilterInvalidError.message)
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
      logger.systemLog(badRequestMessage = OffsetNotNumericAddressResponseError.message)
      Some(futureJsonBadRequest(OffsetNotNumericPostcode))
    } else if (offsetInt < 0) {
      logger.systemLog(badRequestMessage = OffsetTooSmallAddressResponseError.message)
      Some(futureJsonBadRequest(OffsetTooSmallPostcode))
    } else if (offsetInt > maxOffset) {
      logger.systemLog(badRequestMessage = OffsetTooLargeAddressResponseErrorCustom.message)
      Some(futureJsonBadRequest(OffsetTooLargePostcode))
    } else None
  }

  def validatePostcode(postcode: String): Option[Future[Result]] = {
    if (postcode.isEmpty) {
      logger.systemLog(badRequestMessage = EmptyQueryPostcodeAddressResponseError.message)
      Some(futureJsonBadRequest(EmptySearchPostcode))
    } else None
  }

}
