package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Result
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.server.model.dao.QueryValues
import uk.gov.ons.addressIndex.server.modules.response.AddressControllerResponse
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Singleton
class AddressControllerValidation @Inject()(implicit conf: ConfigModule, versionProvider: VersionModule)
  extends AddressValidation with AddressControllerResponse {

  val matchThreshold: Float = conf.config.elasticSearch.matchThreshold

  // override error message with named length
  object EpochNotAvailableErrorCustom extends AddressResponseError(
    code = 36,
    message = EpochNotAvailableError.message.concat(". Current available epochs are " + validEpochsMessage + ".")
  )

  override def EpochInvalid(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, EpochNotAvailableErrorCustom)
  }

  // get the defaults and maxima for the paging parameters from the config
  def validateLocation(lat: Option[String], lon: Option[String], rangeKm: Option[String], queryValues: QueryValues): Option[Future[Result]] = {
    val latVal: String = lat.getOrElse("")
    val lonVal: String = lon.getOrElse("")
    val rangeVal: String = rangeKm.getOrElse("")

    rangeVal match {
      case "" => None
      case _ =>
        (Try(latVal.toDouble), Try(lonVal.toDouble)) match {
          case (Failure(_), _) =>
            logger.systemLog(badRequestMessage = LatitudeNotNumericAddressResponseError.message)
            Some(futureJsonBadRequest(LatitiudeNotNumeric(queryValues)))
          case (_, Failure(_)) =>
            logger.systemLog(badRequestMessage = LongitudeNotNumericAddressResponseError.message)
            Some(futureJsonBadRequest(LongitudeNotNumeric(queryValues)))
          case (Success(latDbl), Success(_)) if latDbl > 60.9 =>
            logger.systemLog(badRequestMessage = LatitudeTooFarNorthAddressResponseError.message)
            Some(futureJsonBadRequest(LatitudeTooFarNorth(queryValues)))
          case (Success(latDbl), Success(_)) if latDbl < 49.8 =>
            logger.systemLog(badRequestMessage = LatitudeTooFarSouthAddressResponseError.message)
            Some(futureJsonBadRequest(LatitudeTooFarSouth(queryValues)))
          case (Success(_), Success(lonDbl)) if lonDbl > 1.8 =>
            logger.systemLog(badRequestMessage = LongitudeTooFarEastAddressResponseError.message)
            Some(futureJsonBadRequest(LongitudeTooFarEast(queryValues)))
          case (Success(_), Success(lonDbl)) if lonDbl < -8.6 =>
            logger.systemLog(badRequestMessage = LongitudeTooFarWestAddressResponseError.message)
            Some(futureJsonBadRequest(LongitudeTooFarWest(queryValues)))
          case (Success(_), Success(_)) => None
        }
    }
  }

  def validateAddressFilter(classificationFilter: Option[String], queryValues: QueryValues): Option[Future[Result]] = {
    val regexString: String = """\b(residential|commercial|[CcLlMmOoPpRrUuXxZz]\w*)\b.*"""
    //    val regexString: String = """\b(residential|commercial|C|c|C\w+|c\w+|L|l|L\w+|l\w+|M|m|M\w+|m\w+|O|o|O\w+|o\w+|P|p|P\w+|p\w+|R|r|R\w+|r\w+|U|u|U\w+|u\w+|X|x|X\w+|x\w+|Z|z|Z\w+|z\w+)\b.*"""

    classificationFilter.getOrElse("") match {
      case "" => None
      case s if s.contains("*") && s.contains(",") =>
        logger.systemLog(badRequestMessage = MixedFilterError.message)
        Some(futureJsonBadRequest(AddressMixedFilter(queryValues)))
      case s if !s.matches(regexString) =>
        logger.systemLog(badRequestMessage = FilterInvalidError.message)
        Some(futureJsonBadRequest(AddressFilterInvalid(queryValues)))
      case _ => None
    }
  }

  def validateRange(rangeKm: Option[String], queryValues: QueryValues): Option[Future[Result]] = rangeKm match {
    case None => None
    case Some(r) => Try(r.toDouble) match {
      case Failure(_) =>
        logger.systemLog(badRequestMessage = RangeNotNumericAddressResponseError.message)
        Some(futureJsonBadRequest(RangeNotNumeric(queryValues)))
      case Success(_) => None
    }
  }

  def validateThreshold(threshold: Option[String], queryValues: QueryValues): Option[Future[Result]] = {
    def inner(threshold: Float): Option[Future[Result]] = threshold match {
      case t if !(0 <= t && t <= 100) =>
        logger.systemLog(badRequestMessage = ThresholdNotInRangeAddressResponseError.message)
        Some(futureJsonBadRequest(ThresholdNotInRange(queryValues)))
      case _ => None
    }

    threshold match {
      case Some(t) => Try(t.toFloat) match {
        case Failure(_) =>
          logger.systemLog(badRequestMessage = ThresholdNotNumericAddressResponseError.message)
          Some(futureJsonBadRequest(ThresholdNotNumeric(queryValues)))
        case Success(tFloat) => inner(tFloat)
      }
      case None => inner(matchThreshold)
    }
  }

  def validateEpoch(queryValues: QueryValues): Option[Future[Result]] = {
    queryValues.epochOrDefault match {
      case "" => None
      case e if e.matches(validEpochsRegex) => None
      case _ =>
        logger.systemLog(badRequestMessage = EpochNotAvailableError.message)
        Some(futureJsonBadRequest(EpochInvalid(queryValues)))
    }
  }
}
