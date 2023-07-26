package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Result
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.server.model.dao.{QueryValues, RequestValues}
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
  def validateLocation(lat: Option[String], lon: Option[String], rangeKm: Option[String], queryValues: QueryValues, requestValues: RequestValues): Option[Future[Result]] = {
    (rangeKm, Try(lat.get.toDouble), Try(lon.get.toDouble)) match {
      case (None, _, _) => None
      case (Some(""), _, _) => None
      case (_, Failure(_), _) =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = LatitudeNotNumericAddressResponseError.message)
        Some(futureJsonBadRequest(LatitiudeNotNumeric(queryValues)))
      case (_, _, Failure(_)) =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = LongitudeNotNumericAddressResponseError.message)
        Some(futureJsonBadRequest(LongitudeNotNumeric(queryValues)))
      case (_, Success(latD), _) if latD > 60.9 =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = LatitudeTooFarNorthAddressResponseError.message)
        Some(futureJsonBadRequest(LatitudeTooFarNorth(queryValues)))
      case (_, Success(latD), _) if latD < 49.8 =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = LatitudeTooFarSouthAddressResponseError.message)
        Some(futureJsonBadRequest(LatitudeTooFarSouth(queryValues)))
      case (_, _, Success(lonD)) if lonD > 1.8 =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = LongitudeTooFarEastAddressResponseError.message)
        Some(futureJsonBadRequest(LongitudeTooFarEast(queryValues)))
      case (_, _, Success(lonD)) if lonD < -8.6 =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = LongitudeTooFarWestAddressResponseError.message)
        Some(futureJsonBadRequest(LongitudeTooFarWest(queryValues)))
      case (_, _, _) => None
    }
  }

  def validateAddressFilter(classificationFilter: Option[String], queryValues: QueryValues, requestValues: RequestValues): Option[Future[Result]] = classificationFilter match {
    case None => None
    case Some("") => None
    case Some(filter) => filter match {
      case f if f.contains("*") && f.contains(",") =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = MixedFilterError.message)
        Some(futureJsonBadRequest(AddressMixedFilter(queryValues)))
      case addressFilterRegex(_*) => None
      case _ =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = FilterInvalidError.message)
        Some(futureJsonBadRequest(AddressFilterInvalid(queryValues)))
    }
  }


  def validateRange(rangeKm: Option[String], queryValues: QueryValues, requestValues: RequestValues): Option[Future[Result]] = rangeKm match {
    case None => None
    case Some("") => None
    case Some(r) => Try(r.toDouble) match {
      case Failure(_) =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = RangeNotNumericAddressResponseError.message)
        Some(futureJsonBadRequest(RangeNotNumeric(queryValues)))
      case Success(_) => None
    }
  }

  def validateThreshold(threshold: Option[String], queryValues: QueryValues, requestValues: RequestValues): Option[Future[Result]] = {
    def inner(threshold: Float): Option[Future[Result]] = threshold match {
      case t if !(0 <= t && t <= 100) =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = ThresholdNotInRangeAddressResponseError.message)
        Some(futureJsonBadRequest(ThresholdNotInRange(queryValues)))
      case _ => None
    }

    threshold match {
      case Some("") => None
      case Some(t) => Try(t.toFloat) match {
        case Failure(_) =>
          logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
            responsecode = "400",badRequestMessage = ThresholdNotNumericAddressResponseError.message)
          Some(futureJsonBadRequest(ThresholdNotNumeric(queryValues)))
        case Success(tFloat) => inner(tFloat)
      }
      case None => inner(matchThreshold)
    }
  }

  // validEpochsRegex is inherited from AddressControllerValidation
  def validateEpoch(queryValues: QueryValues, requestValues: RequestValues): Option[Future[Result]] = {
    queryValues.epochOrDefault match {
      case "" => None
      case validEpochsRegex(_*) => None
      case _ =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = EpochNotAvailableError.message)
        Some(futureJsonBadRequest(EpochInvalid(queryValues)))
    }
  }

  def validateBoosts(eboost: Option[String],nboost: Option[String],sboost: Option[String],wboost: Option[String],lboost: Option[String],mboost: Option[String],jboost: Option[String],queryValues: QueryValues, requestValues: RequestValues): Option[Future[Result]] = {
    val eboostVal = if (eboost.getOrElse("1.0").isEmpty) "1.0" else eboost.getOrElse("1.0")
    val nboostVal = if (nboost.getOrElse("1.0").isEmpty) "1.0" else nboost.getOrElse("1.0")
    val sboostVal = if (sboost.getOrElse("1.0").isEmpty) "1.0" else sboost.getOrElse("1.0")
    val wboostVal = if (wboost.getOrElse("1.0").isEmpty) "1.0" else wboost.getOrElse("1.0")
    val lboostVal = if (lboost.getOrElse("1.0").isEmpty) "1.0" else nboost.getOrElse("1.0")
    val mboostVal = if (mboost.getOrElse("1.0").isEmpty) "1.0" else sboost.getOrElse("1.0")
    val jboostVal = if (jboost.getOrElse("1.0").isEmpty) "1.0" else wboost.getOrElse("1.0")

    val eboostDouble = Try(eboostVal.toDouble).toOption.getOrElse(99D)
    val nboostDouble = Try(nboostVal.toDouble).toOption.getOrElse(99D)
    val sboostDouble = Try(sboostVal.toDouble).toOption.getOrElse(99D)
    val wboostDouble = Try(wboostVal.toDouble).toOption.getOrElse(99D)
    val lboostDouble = Try(lboostVal.toDouble).toOption.getOrElse(99D)
    val mboostDouble = Try(mboostVal.toDouble).toOption.getOrElse(99D)
    val jboostDouble = Try(jboostVal.toDouble).toOption.getOrElse(99D)

    if (eboostDouble > 10 || nboostDouble > 10 || sboostDouble > 10 || wboostDouble > 10 || lboostDouble > 10 || mboostDouble > 10 || jboostDouble > 10 || eboostDouble < 0 || nboostDouble < 0 || sboostDouble < 0 || wboostDouble < 0 || lboostDouble < 0 || lboostDouble < 0 || jboostDouble < 0) {
      logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
        responsecode = "400",badRequestMessage = CountryBoostsInvalidError.message)
      Some(futureJsonBadRequest(CountryBoostsInvalid(queryValues)))
    } else None

  }

}
