package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Result
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.server.modules.response.AddressControllerResponse
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}

import scala.concurrent.Future
import scala.util.Try

@Singleton
class AddressControllerValidation @Inject()(implicit conf: ConfigModule, versionProvider: VersionModule)
  extends AddressValidation with AddressControllerResponse {

  // get the defaults and maxima for the paging parameters from the config

  def validateLocation(lat: Option[String], lon: Option[String], rangekm: Option[String]): Option[Future[Result]] = {

    val latVal: String = lat.getOrElse("")
    val lonVal: String = lon.getOrElse("")
    val rangeVal: String = rangekm.getOrElse("")

    val latInvalid: Boolean = if (rangeVal.equals("")) false else Try(latVal.toDouble).isFailure
    val lonInvalid: Boolean = if (rangeVal.equals("")) false else Try(lonVal.toDouble).isFailure

    val latTooFarNorth: Boolean = if (rangeVal.equals("")) false else Try(latVal.toDouble).getOrElse(50D) > 60.9
    val latTooFarSouth: Boolean = if (rangeVal.equals("")) false else Try(latVal.toDouble).getOrElse(50D) < 49.8
    val lonTooFarEast: Boolean = if (rangeVal.equals("")) false else Try(lonVal.toDouble).getOrElse(0D) > 1.8
    val lonTooFarWest: Boolean = if (rangeVal.equals("")) false else Try(lonVal.toDouble).getOrElse(0D) < -8.6

    if (latInvalid) {
      logger.systemLog(badRequestMessage = LatitudeNotNumericAddressResponseError.message)
      Some(futureJsonBadRequest(LatitiudeNotNumeric))
    } else if (lonInvalid) {
      logger.systemLog(badRequestMessage = LongitudeNotNumericAddressResponseError.message)
      Some(futureJsonBadRequest(LongitudeNotNumeric))
    } else if (latTooFarNorth) {
      logger.systemLog(badRequestMessage = LatitudeTooFarNorthAddressResponseError.message)
      Some(futureJsonBadRequest(LatitudeTooFarNorth))
    } else if (latTooFarSouth) {
      logger.systemLog(badRequestMessage = LatitudeTooFarSouthAddressResponseError.message)
      Some(futureJsonBadRequest(LatitudeTooFarSouth))
    } else if (lonTooFarEast) {
      logger.systemLog(badRequestMessage = LongitudeTooFarEastAddressResponseError.message)
      Some(futureJsonBadRequest(LongitudeTooFarEast))
    } else if (lonTooFarWest) {
      logger.systemLog(badRequestMessage = LongitudeTooFarWestAddressResponseError.message)
      Some(futureJsonBadRequest(LongitudeTooFarWest))
    } else None
  }

  def validateAddressFilter(classificationfilter: Option[String]): Option[Future[Result]] = {

    val filterString: String = classificationfilter.getOrElse("")

    if (!filterString.isEmpty){
      if (filterString.contains("*") && filterString.contains(",")){
        logger.systemLog(badRequestMessage = MixedFilterError.message)
        Some(futureJsonBadRequest(AddressMixedFilter))
      }
      else if (!filterString.matches("""\b(residential|commercial|C|c|C\w+|c\w+|L|l|L\w+|l\w+|M|m|M\w+|m\w+|O|o|O\w+|o\w+|P|p|P\w+|p\w+|R|r|R\w+|r\w+|U|u|U\w+|u\w+|X|x|X\w+|x\w+|Z|z|Z\w+|z\w+)\b.*""")) {
        logger.systemLog(badRequestMessage = FilterInvalidError.message)
        Some(futureJsonBadRequest(AddressFilterInvalid))
      } else None
    } else None

  }

  def validateRange(rangekm: Option[String]): Option[Future[Result]] = {
    val rangeVal: String = rangekm.getOrElse("")
    val rangeInvalid: Boolean = if (rangeVal.equals("")) false else Try(rangeVal.toDouble).isFailure

    if (rangeInvalid) {
      logger.systemLog(badRequestMessage = RangeNotNumericAddressResponseError.message)
      Some(futureJsonBadRequest(RangeNotNumeric))
    } else None
  }

  def validateThreshold(matchthreshold: Option[String]): Option[Future[Result]] = {

    val defThreshold: Float = conf.config.elasticSearch.matchThreshold
    val threshval = matchthreshold.getOrElse(defThreshold.toString)
    val thresholdFloat = Try(threshval.toFloat).toOption.getOrElse(defThreshold)
    val thresholdNotInRange = !(thresholdFloat >= 0 && thresholdFloat <= 100)
    val thresholdInvalid = Try(threshval.toFloat).isFailure

    if (thresholdInvalid) {
      logger.systemLog(badRequestMessage = ThresholdNotNumericAddressResponseError.message)
      Some(futureJsonBadRequest(ThresholdNotNumeric))
    } else if (thresholdNotInRange) {
      logger.systemLog(badRequestMessage = ThresholdNotInRangeAddressResponseError.message)
      Some(futureJsonBadRequest(ThresholdNotInRange))
    } else None
  }

  // set minimum string length from config
  val validEpochs = conf.config.elasticSearch.validEpochs
  val validEpochsMessage = validEpochs.replace("|test","").replace("|", ", ")

  // override error message with named length
  object EpochNotAvailableErrorCustom extends AddressResponseError(
    code = 36,
    message = EpochNotAvailableError.message.concat(". Current available epochs are " + validEpochsMessage + ".")
  )

  override def EpochInvalid: AddressBySearchResponseContainer = {
    BadRequestTemplate(EpochNotAvailableErrorCustom)
  }

  def validateEpoch(epoch: Option[String]): Option[Future[Result]] = {

    val epochVal: String = epoch.getOrElse("")
    val validEpochs: String = conf.config.elasticSearch.validEpochs

    if (!epochVal.isEmpty){
      if (!epochVal.matches("""\b("""+ validEpochs + """)\b.*""")) {
        logger.systemLog(badRequestMessage = EpochNotAvailableError.message)
        Some(futureJsonBadRequest(EpochInvalid))
      } else None
    } else None

  }

}
