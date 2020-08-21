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
  private val addressFilterRegex = raw"""\b(residential|commercial|workplace|educational|[CcLlMmOoPpRrUuXxZz]\w*)\b.*""".r

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
    (rangeKm, Try(lat.get.toDouble), Try(lon.get.toDouble)) match {
      case (None, _, _) => None
      case (Some(""), _, _) => None
      case (_, Failure(_), _) =>
        logger.systemLog(badRequestMessage = LatitudeNotNumericAddressResponseError.message)
        Some(futureJsonBadRequest(LatitiudeNotNumeric(queryValues)))
      case (_, _, Failure(_)) =>
        logger.systemLog(badRequestMessage = LongitudeNotNumericAddressResponseError.message)
        Some(futureJsonBadRequest(LongitudeNotNumeric(queryValues)))
      case (_, Success(latD), _) if latD > 60.9 =>
        logger.systemLog(badRequestMessage = LatitudeTooFarNorthAddressResponseError.message)
        Some(futureJsonBadRequest(LatitudeTooFarNorth(queryValues)))
      case (_, Success(latD), _) if latD < 49.8 =>
        logger.systemLog(badRequestMessage = LatitudeTooFarSouthAddressResponseError.message)
        Some(futureJsonBadRequest(LatitudeTooFarSouth(queryValues)))
      case (_, _, Success(lonD)) if lonD > 1.8 =>
        logger.systemLog(badRequestMessage = LongitudeTooFarEastAddressResponseError.message)
        Some(futureJsonBadRequest(LongitudeTooFarEast(queryValues)))
      case (_, _, Success(lonD)) if lonD < -8.6 =>
        logger.systemLog(badRequestMessage = LongitudeTooFarWestAddressResponseError.message)
        Some(futureJsonBadRequest(LongitudeTooFarWest(queryValues)))
      case (_, _, _) => None
    }
  }

  def validateAddressFilter(classificationFilter: Option[String], queryValues: QueryValues): Option[Future[Result]] = classificationFilter match {
    case None => None
    case Some("") => None
    case Some(filter) => filter match {
      case f if f.contains("*") && f.contains(",") =>
        logger.systemLog(badRequestMessage = MixedFilterError.message)
        Some(futureJsonBadRequest(AddressMixedFilter(queryValues)))
      case addressFilterRegex(_*) => None
      case _ =>
        logger.systemLog(badRequestMessage = FilterInvalidError.message)
        Some(futureJsonBadRequest(AddressFilterInvalid(queryValues)))
    }
  }


  def validateRange(rangeKm: Option[String], queryValues: QueryValues): Option[Future[Result]] = rangeKm match {
    case None => None
    case Some("") => None
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
      case Some("") => None
      case Some(t) => Try(t.toFloat) match {
        case Failure(_) =>
          logger.systemLog(badRequestMessage = ThresholdNotNumericAddressResponseError.message)
          Some(futureJsonBadRequest(ThresholdNotNumeric(queryValues)))
        case Success(tFloat) => inner(tFloat)
      }
      case None => inner(matchThreshold)
    }
  }

  // validEpochsRegex is inherited from AddressControllerValidation
  def validateEpoch(queryValues: QueryValues): Option[Future[Result]] = {
    queryValues.epochOrDefault match {
      case "" => None
      case validEpochsRegex(_*) => None
      case _ =>
        logger.systemLog(badRequestMessage = EpochNotAvailableError.message)
        Some(futureJsonBadRequest(EpochInvalid(queryValues)))
    }
  }

  def validateFromSource(queryValues: QueryValues): Option[Future[Result]] = {
    queryValues.fromsource match {
      case Some("nionly") => None
      case Some("ewonly") => None
      case Some("niboost") => None
      case Some("ewboost") => None
      case Some("all") => None
      case _ =>
        logger.systemLog(badRequestMessage = FromSourceInvalidError.message)
        Some(futureJsonBadRequest(FromSourceInvalid(queryValues)))
    }
  }
}
