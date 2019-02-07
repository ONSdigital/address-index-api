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

  override def LimitTooLargePostcode(queryValues: Map[String,Any]): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues,LimitTooLargeAddressResponseErrorCustom)
  }

  override def OffsetTooLargePostcode(queryValues: Map[String,Any]): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues,OffsetTooLargeAddressResponseErrorCustom)
  }

  def validatePostcodeLimit(limit: Option[String], queryValues: Map[String,Any]): Option[Future[Result]] = {

    val defLimit: Int = conf.config.elasticSearch.defaultLimit
    val limval = limit.getOrElse(defLimit.toString)
    val limitInvalid = Try(limval.toInt).isFailure
    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)
    val maxLimit: Int = conf.config.elasticSearch.maximumLimit

    if (limitInvalid) {
      logger.systemLog(badRequestMessage = LimitNotNumericAddressResponseError.message)
      Some(futureJsonBadRequest(LimitNotNumericPostcode(queryValues)))
    } else if (limitInt < 1) {
      logger.systemLog(badRequestMessage = LimitTooSmallAddressResponseError.message)
      Some(futureJsonBadRequest(LimitTooSmallPostcode(queryValues)))
    } else if (limitInt > maxLimit) {
      logger.systemLog(badRequestMessage = LimitTooLargeAddressResponseErrorCustom.message)
      Some(futureJsonBadRequest(LimitTooLargePostcode(queryValues)))
    } else None

  }

  def validatePostcodeFilter(classificationfilter: Option[String], queryValues: Map[String,Any]): Option[Future[Result]] = {

    val filterString: String = classificationfilter.getOrElse("")

    if (!filterString.isEmpty){
      if (filterString.contains("*") && filterString.contains(",")){
        logger.systemLog(badRequestMessage = MixedFilterError.message)
        Some(futureJsonBadRequest(PostcodeMixedFilter(queryValues)))
      }
      else if (!filterString.matches("""\b(residential|commercial|C|c|C\w+|c\w+|L|l|L\w+|l\w+|M|m|M\w+|m\w+|O|o|O\w+|o\w+|P|p|P\w+|p\w+|R|r|R\w+|r\w+|U|u|U\w+|u\w+|X|x|X\w+|x\w+|Z|z|Z\w+|z\w+)\b.*""")) {
        logger.systemLog(badRequestMessage = FilterInvalidError.message)
        Some(futureJsonBadRequest(PostcodeFilterInvalid(queryValues)))
      } else None
    } else None

  }

  def validatePostcodeOffset(offset: Option[String], queryValues: Map[String,Any]): Option[Future[Result]] = {
    val maxOffset: Int = conf.config.elasticSearch.maximumOffset
    val defOffset: Int = conf.config.elasticSearch.defaultOffset
    val offval = offset.getOrElse(defOffset.toString)
    val offsetInvalid = Try(offval.toInt).isFailure
    val offsetInt = Try(offval.toInt).toOption.getOrElse(defOffset)

    if (offsetInvalid) {
      logger.systemLog(badRequestMessage = OffsetNotNumericAddressResponseError.message)
      Some(futureJsonBadRequest(OffsetNotNumericPostcode(queryValues)))
    } else if (offsetInt < 0) {
      logger.systemLog(badRequestMessage = OffsetTooSmallAddressResponseError.message)
      Some(futureJsonBadRequest(OffsetTooSmallPostcode(queryValues)))
    } else if (offsetInt > maxOffset) {
      logger.systemLog(badRequestMessage = OffsetTooLargeAddressResponseErrorCustom.message)
      Some(futureJsonBadRequest(OffsetTooLargePostcode(queryValues)))
    } else None
  }

  def validatePostcode(postcode: String, queryValues: Map[String,Any]): Option[Future[Result]] = {
    if (postcode.isEmpty) {
      logger.systemLog(badRequestMessage = EmptyQueryPostcodeAddressResponseError.message)
      Some(futureJsonBadRequest(EmptySearchPostcode(queryValues)))
    } else if (!postcode.toUpperCase().matches("^(GIR 0AA)|((([A-Z][0-9]{1,2})|(([A-Z][A-HJ-Y][0-9]{1,2})|(([A-Z][0-9][A-Z])|([A-Z][A-HJ-Y][0-9]?[A-Z])))) ?[0-9][A-Z]{2})$")) {
       logger.systemLog(badRequestMessage = postcode + ": " + InvalidPostcodeAddressResponseError.message)
       Some(futureJsonBadRequest(InvalidPostcode(queryValues)))
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

  override def PostcodeEpochInvalid(queryValues: Map[String,Any]): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues,EpochNotAvailableErrorCustom)
  }

  def validateEpoch(queryValues: Map[String,Any]): Option[Future[Result]] = {

    val epochVal: String = queryValues("epoch").toString

    if (!epochVal.isEmpty){
      if (!epochVal.matches("""\b("""+ validEpochs + """)\b.*""")) {
        logger.systemLog(badRequestMessage = EpochNotAvailableError.message, epoch=epochVal, postcode=queryValues("postcode").toString)
        Some(futureJsonBadRequest(PostcodeEpochInvalid(queryValues)))
      } else None
    } else None

  }

}
