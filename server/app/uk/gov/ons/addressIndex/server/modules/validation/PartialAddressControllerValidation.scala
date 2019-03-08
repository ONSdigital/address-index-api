package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Result
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.partialaddress.AddressByPartialAddressResponseContainer
import uk.gov.ons.addressIndex.server.model.dao.QueryValues
import uk.gov.ons.addressIndex.server.modules.response.PartialAddressControllerResponse
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}

import scala.concurrent.Future
import scala.util.Try

@Singleton
class PartialAddressControllerValidation @Inject()(implicit conf: ConfigModule, versionProvider: VersionModule)
  extends AddressControllerValidation with PartialAddressControllerResponse {

  // set minimum string length from config
  val minimumTermLength: Int = conf.config.elasticSearch.minimumPartial

  // override error message with named length
  object ShortQueryAddressResponseErrorCustom extends AddressResponseError(
    code = 33,
    message = ShortQueryAddressResponseError.message.replace("*", minimumTermLength.toString)
  )

  override def ShortSearch(queryValues: QueryValues): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues, ShortQueryAddressResponseErrorCustom)
  }

  // minimum length only for partial so override
  override def validateInput(input: String, queryValues: QueryValues): Option[Future[Result]] = {
    if (input.isEmpty) {
      logger.systemLog(badRequestMessage = EmptyQueryAddressResponseError.message)
      Some(futureJsonBadRequest(EmptySearch(queryValues)))
    } else if (input.length < minimumTermLength) {
      logger.systemLog(badRequestMessage = ShortQueryAddressResponseErrorCustom.message)
      Some(futureJsonBadRequest(ShortSearch(queryValues)))
    } else None
  }

  override def LimitTooLargePartial(queryValues: QueryValues): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues, LimitTooLargeAddressResponseErrorCustom)
  }

  override def OffsetTooLargePartial(queryValues: QueryValues): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues, OffsetTooLargeAddressResponseErrorCustom)
  }

  def validatePartialLimit(limit: Option[String], queryValues: QueryValues): Option[Future[Result]] = {

    val defLimit: Int = conf.config.elasticSearch.defaultLimit
    val limval = limit.getOrElse(defLimit.toString)
    val limitInvalid = Try(limval.toInt).isFailure
    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)
    val maxLimit: Int = conf.config.elasticSearch.maximumLimit

    if (limitInvalid) {
      logger.systemLog(badRequestMessage = LimitNotNumericAddressResponseError.message)
      Some(futureJsonBadRequest(LimitNotNumericPartial(queryValues)))
    } else if (limitInt < 1) {
      logger.systemLog(badRequestMessage = LimitTooSmallAddressResponseError.message)
      Some(futureJsonBadRequest(LimitTooSmallPartial(queryValues)))
    } else if (limitInt > maxLimit) {
      logger.systemLog(badRequestMessage = LimitTooLargeAddressResponseErrorCustom.message)
      Some(futureJsonBadRequest(LimitTooLargePartial(queryValues)))
    } else None

  }

  def validatePartialOffset(offset: Option[String], queryValues: QueryValues): Option[Future[Result]] = {
    val maxOffset: Int = conf.config.elasticSearch.maximumOffset
    val defOffset: Int = conf.config.elasticSearch.defaultOffset
    val offval = offset.getOrElse(defOffset.toString)
    val offsetInvalid = Try(offval.toInt).isFailure
    val offsetInt = Try(offval.toInt).toOption.getOrElse(defOffset)

    (offsetInvalid, offsetInt) match {
      case (true, _) =>
        logger.systemLog(badRequestMessage = OffsetNotNumericAddressResponseError.message)
        Some(futureJsonBadRequest(OffsetNotNumericPartial(queryValues)))
      case (false, i) if i < 0 =>
        logger.systemLog(badRequestMessage = OffsetTooSmallAddressResponseError.message)
        Some(futureJsonBadRequest(OffsetTooSmallPartial(queryValues)))
      case (false, i) if i > maxOffset =>
        logger.systemLog(badRequestMessage = OffsetTooLargeAddressResponseErrorCustom.message)
        Some(futureJsonBadRequest(OffsetTooLargePartial(queryValues)))
      case _ => None
    }
  }

  override def PartialEpochInvalid(queryValues: QueryValues): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues, EpochNotAvailableErrorCustom)
  }

  override def validateEpoch(queryValues: QueryValues): Option[Future[Result]] = {

    val epochVal: String = queryValues.epochOrDefault

    if (!epochVal.isEmpty) {
      if (!epochVal.matches("""\b(""" + validEpochs + """)\b.*""")) {
        logger.systemLog(badRequestMessage = EpochNotAvailableError.message)
        Some(futureJsonBadRequest(PartialEpochInvalid(queryValues)))
      } else None
    } else None

  }

}
