package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Result
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.partialaddress.AddressByPartialAddressResponseContainer
import uk.gov.ons.addressIndex.server.modules.response.PartialAddressControllerResponse
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}

import scala.concurrent.Future
import scala.util.Try

@Singleton
class PartialAddressControllerValidation @Inject()(implicit conf: ConfigModule, versionProvider: VersionModule)
  extends AddressControllerValidation with PartialAddressControllerResponse {


  // set minimum string length from config
  val minimumTermLength = conf.config.elasticSearch.minimumPartial

  // override error message with named length
  object ShortQueryAddressResponseErrorCustom extends AddressResponseError(
    code = 34,
    message = ShortQueryAddressResponseError.message.replace("*",minimumTermLength.toString)
  )

  override def ShortSearch: AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(ShortQueryAddressResponseErrorCustom)
  }

  // minimum length only for partial so override
  override def validateInput(input: String): Option[Future[Result]] = {
    if (input.isEmpty) {
      logger.systemLog(badRequestMessage = EmptyQueryAddressResponseError.message)
      Some(futureJsonBadRequest(EmptySearch))
    } else if (input.length < minimumTermLength) {
      logger.systemLog(badRequestMessage = ShortQueryAddressResponseErrorCustom.message)
      Some(futureJsonBadRequest(ShortSearch))
    } else None
  }

  override def LimitTooLargePartial: AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(LimitTooLargeAddressResponseErrorCustom)
  }

  override def OffsetTooLargePartial: AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(OffsetTooLargeAddressResponseErrorCustom)
  }

  def validatePartialLimit(limit: Option[String]): Option[Future[Result]] = {

    val defLimit: Int = conf.config.elasticSearch.defaultLimit
    val limval = limit.getOrElse(defLimit.toString)
    val limitInvalid = Try(limval.toInt).isFailure
    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)
    val maxLimit: Int = conf.config.elasticSearch.maximumLimit

    if (limitInvalid) {
      logger.systemLog(badRequestMessage = LimitNotNumericAddressResponseError.message)
      Some(futureJsonBadRequest(LimitNotNumericPartial))
    } else if (limitInt < 1) {
      logger.systemLog(badRequestMessage = LimitTooSmallAddressResponseError.message)
      Some(futureJsonBadRequest(LimitTooSmallPartial))
    } else if (limitInt > maxLimit) {
      logger.systemLog(badRequestMessage = LimitTooLargeAddressResponseErrorCustom.message)
      Some(futureJsonBadRequest(LimitTooLargePartial))
    } else None

  }

  def validatePartialOffset(offset: Option[String]): Option[Future[Result]] = {
    val maxOffset: Int = conf.config.elasticSearch.maximumOffset
    val defOffset: Int = conf.config.elasticSearch.defaultOffset
    val offval = offset.getOrElse(defOffset.toString)
    val offsetInvalid = Try(offval.toInt).isFailure
    val offsetInt = Try(offval.toInt).toOption.getOrElse(defOffset)

    if (offsetInvalid) {
      logger.systemLog(badRequestMessage = OffsetNotNumericAddressResponseError.message)
      Some(futureJsonBadRequest(OffsetNotNumericPartial))
    } else if (offsetInt < 0) {
      logger.systemLog(badRequestMessage = OffsetTooSmallAddressResponseError.message)
      Some(futureJsonBadRequest(OffsetTooSmallPartial))
    } else if (offsetInt > maxOffset) {
      logger.systemLog(badRequestMessage = OffsetTooLargeAddressResponseErrorCustom.message)
      Some(futureJsonBadRequest(OffsetTooLargePartial))
    } else None
  }


}
