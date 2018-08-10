package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import play.api.mvc.{RequestHeader, Result}
import uk.gov.ons.addressIndex.model.server.response._
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}
import uk.gov.ons.addressIndex.server.utils.impl.AddressLogMessage

import scala.util.Try

@Singleton
class BatchValidation @Inject()(implicit conf: ConfigModule, versionProvider: VersionModule)
  extends AddressValidation {

  // The batch does not use Futures for the validation so we have to override the address ones to return the
  // error without a Future wrapping.

  def validateBatchSource(implicit request: RequestHeader): Option[Result] = {

    val source = request.headers.get("Source").getOrElse(missing)

    checkSource(source) match {
      case `missing` =>
        log(AddressLogMessage(badRequestMessage = SourceMissingError.message))
        Some(super.jsonUnauthorized(SourceMissing))
      case `invalid` =>
        log(AddressLogMessage(badRequestMessage = SourceInvalidError.message))
        Some(jsonUnauthorized(SourceInvalid))
      case _ =>
        None
    }
  }

  def validateBatchKeyStatus(implicit request: RequestHeader): Option[Result] = {
    val apiKey = request.headers.get("authorization").getOrElse(missing)

    checkAPIkey(apiKey) match {
      case `missing` =>
        log(AddressLogMessage(badRequestMessage = ApiKeyMissingError.message))
        Some(jsonUnauthorized(KeyMissing))
      case `invalid` =>
        log(AddressLogMessage(badRequestMessage = ApiKeyInvalidError.message))
        Some(jsonUnauthorized(KeyInvalid))
      case _ =>
        None
    }
  }

  def validateBatchAddressLimit(limit: Option[String]): Option[Result] = {

    val defLimit: Int = conf.config.elasticSearch.defaultLimit
    val limval = limit.getOrElse(defLimit.toString)
    val limitInvalid = Try(limval.toInt).isFailure
    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)
    val maxLimit: Int = conf.config.elasticSearch.maximumLimit

    if (limitInvalid) {
      log(AddressLogMessage(badRequestMessage =LimitNotNumericAddressResponseError.message))
      Some(jsonBadRequest(LimitNotNumeric))
    } else if (limitInt < 1) {
      log(AddressLogMessage(badRequestMessage = LimitTooSmallAddressResponseError.message))
      Some(jsonBadRequest(LimitTooSmall))
    } else if (limitInt > maxLimit) {
      log(AddressLogMessage(badRequestMessage = LimitTooLargeAddressResponseError.message))
      Some(jsonBadRequest(LimitTooLarge))
    } else None
  }

  def validateBatchThreshold(matchthreshold: Option[String]): Option[Result] = {

    val defThreshold: Float = conf.config.elasticSearch.matchThreshold
    val threshval = matchthreshold.getOrElse(defThreshold.toString)
    val thresholdFloat = Try(threshval.toFloat).toOption.getOrElse(defThreshold)
    val thresholdNotInRange = !(thresholdFloat >= 0 && thresholdFloat <= 100)
    val thresholdInvalid = Try(threshval.toFloat).isFailure

    if (thresholdInvalid) {
      log(AddressLogMessage(badRequestMessage = ThresholdNotNumericAddressResponseError.message))
      Some(jsonBadRequest(ThresholdNotNumeric))
    } else if (thresholdNotInRange) {
      log(AddressLogMessage(badRequestMessage = ThresholdNotInRangeAddressResponseError.message))
      Some(jsonBadRequest(ThresholdNotInRange))
    } else None
  }

}

