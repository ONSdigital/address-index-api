package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import play.api.mvc.{RequestHeader, Result}
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.server.model.dao.QueryValues
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}

import scala.util.{Failure, Success, Try}

@Singleton
class BatchControllerValidation @Inject()(implicit conf: ConfigModule, versionProvider: VersionModule)
  extends AddressControllerValidation {

  // validEpochs is inherited from AddressControllerValidation
  val epochRegex: String = """\b(""" + validEpochs + """)\b.*"""


  def validateBatchSource(queryValues: QueryValues)(implicit request: RequestHeader): Option[Result] = {
    val source = request.headers.get("Source").getOrElse(missing)

    checkSource(source) match {
      case `missing` =>
        logger.systemLog(responsecode = "400",badRequestMessage = SourceMissingError.message)
        Some(super.jsonUnauthorized(SourceMissing(queryValues)))
      case `invalid` =>
        logger.systemLog(responsecode = "400",badRequestMessage = SourceInvalidError.message)
        Some(jsonUnauthorized(SourceInvalid(queryValues)))
      case _ => None
    }
  }

  def validateBatchKeyStatus(queryValues: QueryValues)(implicit request: RequestHeader): Option[Result] = {
    val apiKey = request.headers.get("authorization").getOrElse(missing)

    checkAPIkey(apiKey) match {
      case `missing` =>
        logger.systemLog(responsecode = "400",badRequestMessage = ApiKeyMissingError.message)
        Some(jsonUnauthorized(KeyMissing(queryValues)))
      case `invalid` =>
        logger.systemLog(responsecode = "400",badRequestMessage = ApiKeyInvalidError.message)
        Some(jsonUnauthorized(KeyInvalid(queryValues)))
      case _ => None
    }
  }

  // maximumLimit and defaultLimit are inherited from AddressValidation
  def validateBatchAddressLimit(limit: Option[String], queryValues: QueryValues): Option[Result] = {
    def inner(limit: Int): Option[Result] = limit match {
      case i if i < 0 =>
        logger.systemLog(responsecode = "400",badRequestMessage = LimitTooSmallAddressResponseError.message)
        Some(jsonBadRequest(LimitTooSmall(queryValues)))
      case i if maximumLimit < i =>
        logger.systemLog(responsecode = "400",badRequestMessage = LimitTooLargeAddressResponseError.message)
        Some(jsonBadRequest(LimitTooLarge(queryValues)))
      case _ => None
    }

    limit match {
      case Some(l) => Try(l.toInt) match {
        case Success(lInt) => inner(lInt)
        case Failure(_) =>
          logger.systemLog(responsecode = "400",badRequestMessage = LimitNotNumericAddressResponseError.message)
          Some(jsonBadRequest(LimitNotNumeric(queryValues)))
      }
      case None => inner(defaultLimit)
    }
  }

  // matchThreshold is inherited from AddressControllerValidation
  def validateBatchThreshold(threshold: Option[String], queryValues: QueryValues): Option[Result] = {
    def inner(threshold: Float): Option[Result] = threshold match {
      case t if !(0 <= t && t <= 100) =>
        logger.systemLog(responsecode = "400",badRequestMessage = ThresholdNotInRangeAddressResponseError.message)
        Some(jsonBadRequest(ThresholdNotInRange(queryValues)))
      case _ => None
    }

    threshold match {
      case Some(t) => Try(t.toFloat) match {
        case Success(tFloat) => inner(tFloat)
        case Failure(_) =>
          logger.systemLog(responsecode = "400",badRequestMessage = ThresholdNotNumericAddressResponseError.message)
          Some(jsonBadRequest(ThresholdNotNumeric(queryValues)))
      }
      case None => inner(matchThreshold)
    }
  }

  def validateBatchEpoch(epoch: Option[String], queryValues: QueryValues): Option[Result] = {
    epoch match {
      case None => None
      case Some(epochStr) if epochStr.matches(epochRegex) || epochStr.equals("current") || epochStr.equals("") => None
      case Some(_) =>
        logger.systemLog(responsecode = "400",badRequestMessage = EpochNotAvailableError.message)
        Some(jsonBadRequest(EpochInvalid(queryValues)))
    }
  }
}

