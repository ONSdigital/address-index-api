package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import play.api.mvc.{RequestHeader, Result}
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}

import scala.util.Try

@Singleton
class BatchControllerValidation @Inject()(implicit conf: ConfigModule, versionProvider: VersionModule)
  extends AddressControllerValidation {

  // The batch does not use Futures for the validation so we have to override the address ones to return the
  // error without a Future wrapping.

//  def validateBatchStartDate(startDate: String) : Option[Result] = {
//    if (super.invalidDate(startDate)) {
//      logger.systemLog(badRequestMessage = StartDateInvalidResponseError.message)
//      Some(jsonBadRequest(StartDateInvalid))
//    } else None
//  }
//
//  def validateBatchEndDate(endDate: String) : Option[Result] = {
//    if (super.invalidDate(endDate)) {
//      logger.systemLog(badRequestMessage = EndDateInvalidResponseError.message)
//      Some(jsonBadRequest(EndDateInvalid))
//    } else None
//  }

  def validateBatchSource(queryValues: Map[String,Any])(implicit request: RequestHeader): Option[Result] = {

    val source = request.headers.get("Source").getOrElse(missing)

    checkSource(source) match {
      case `missing` =>
        logger.systemLog(badRequestMessage = SourceMissingError.message)
        Some(super.jsonUnauthorized(SourceMissing(queryValues)))
      case `invalid` =>
        logger.systemLog(badRequestMessage = SourceInvalidError.message)
        Some(jsonUnauthorized(SourceInvalid(queryValues)))
      case _ =>
        None
    }
  }

  def validateBatchKeyStatus(queryValues: Map[String,Any])(implicit request: RequestHeader): Option[Result] = {
    val apiKey = request.headers.get("authorization").getOrElse(missing)

    checkAPIkey(apiKey) match {
      case `missing` =>
        logger.systemLog(badRequestMessage = ApiKeyMissingError.message)
        Some(jsonUnauthorized(KeyMissing(queryValues)))
      case `invalid` =>
        logger.systemLog(badRequestMessage = ApiKeyInvalidError.message)
        Some(jsonUnauthorized(KeyInvalid(queryValues)))
      case _ =>
        None
    }
  }

  def validateBatchAddressLimit(limit: Option[String], queryValues: Map[String,Any]): Option[Result] = {

    val defLimit: Int = conf.config.elasticSearch.defaultLimit
    val limval = limit.getOrElse(defLimit.toString)
    val limitInvalid = Try(limval.toInt).isFailure
    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)
    val maxLimit: Int = conf.config.elasticSearch.maximumLimit

    if (limitInvalid) {
      logger.systemLog(badRequestMessage =LimitNotNumericAddressResponseError.message)
      Some(jsonBadRequest(LimitNotNumeric(queryValues)))
    } else if (limitInt < 1) {
      logger.systemLog(badRequestMessage = LimitTooSmallAddressResponseError.message)
      Some(jsonBadRequest(LimitTooSmall(queryValues)))
    } else if (limitInt > maxLimit) {
      logger.systemLog(badRequestMessage = LimitTooLargeAddressResponseError.message)
      Some(jsonBadRequest(LimitTooLarge(queryValues)))
    } else None
  }

  def validateBatchThreshold(matchthreshold: Option[String], queryValues: Map[String,Any]): Option[Result] = {

    val defThreshold: Float = conf.config.elasticSearch.matchThreshold
    val threshval = matchthreshold.getOrElse(defThreshold.toString)
    val thresholdFloat = Try(threshval.toFloat).toOption.getOrElse(defThreshold)
    val thresholdNotInRange = !(thresholdFloat >= 0 && thresholdFloat <= 100)
    val thresholdInvalid = Try(threshval.toFloat).isFailure

    if (thresholdInvalid) {
      logger.systemLog(badRequestMessage = ThresholdNotNumericAddressResponseError.message)
      Some(jsonBadRequest(ThresholdNotNumeric(queryValues)))
    } else if (thresholdNotInRange) {
      logger.systemLog(badRequestMessage = ThresholdNotInRangeAddressResponseError.message)
      Some(jsonBadRequest(ThresholdNotInRange(queryValues)))
    } else None
  }

  def validateBatchEpoch(epoch: Option[String], queryValues: Map[String,Any]): Option[Result] = {

    val epochVal: String = epoch.getOrElse("")
    val validEpochs: String = conf.config.elasticSearch.validEpochs

    if (!epochVal.isEmpty){
      if (!epochVal.matches("""\b("""+ validEpochs + """)\b.*""")) {
        logger.systemLog(badRequestMessage = EpochNotAvailableError.message)
        Some(jsonBadRequest(EpochInvalid(queryValues)))
      } else None
    } else None

  }
}

