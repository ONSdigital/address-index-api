package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Result
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.postcode.AddressByPostcodeResponseContainer
import uk.gov.ons.addressIndex.server.model.dao.QueryValues
import uk.gov.ons.addressIndex.server.modules.response.PostcodeControllerResponse
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}

import scala.concurrent.Future
import scala.util.Try

@Singleton
class PostcodeControllerValidation @Inject()(implicit conf: ConfigModule, versionProvider: VersionModule)
  extends AddressValidation with PostcodeControllerResponse {

  override def LimitTooLargePostcode(queryValues: QueryValues): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues, LimitTooLargeAddressResponseErrorCustom)
  }

  override def OffsetTooLargePostcode(queryValues: QueryValues): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues, OffsetTooLargeAddressResponseErrorCustom)
  }

  def validatePostcodeLimit(limit: Option[String], queryValues: QueryValues): Option[Future[Result]] = {
    val defLimit: Int = conf.config.elasticSearch.defaultLimit
    val limVal = limit.getOrElse(defLimit.toString)
    val limitInvalid = Try(limVal.toInt).isFailure
    val limitInt = Try(limVal.toInt).toOption.getOrElse(defLimit)
    val maxLimit: Int = conf.config.elasticSearch.maximumLimit

    (limitInvalid, limitInt) match {
      case (true, _) =>
        logger.systemLog(badRequestMessage = LimitNotNumericAddressResponseError.message)
        Some(futureJsonBadRequest(LimitNotNumericPostcode(queryValues)))
      case (false, i) if i < 0 =>
        logger.systemLog(badRequestMessage = LimitTooSmallAddressResponseError.message)
        Some(futureJsonBadRequest(LimitTooSmallPostcode(queryValues)))
      case (false, i) if i > maxLimit =>
        logger.systemLog(badRequestMessage = LimitTooLargeAddressResponseErrorCustom.message)
        Some(futureJsonBadRequest(LimitTooLargePostcode(queryValues)))
      case _ => None
    }
  }

  def validatePostcodeFilter(classificationFilter: Option[String], queryValues: QueryValues): Option[Future[Result]] = {
    val postcodeFilterRegex = """\b(residential|commercial|C|c|C\w+|c\w+|L|l|L\w+|l\w+|M|m|M\w+|m\w+|O|o|O\w+|o\w+|P|p|P\w+|p\w+|R|r|R\w+|r\w+|U|u|U\w+|u\w+|X|x|X\w+|x\w+|Z|z|Z\w+|z\w+)\b.*"""
    val filterString: String = classificationFilter.getOrElse("")

    filterString match {
      case "" => None
      case s if s.contains("*") && s.contains(",") =>
        logger.systemLog(badRequestMessage = MixedFilterError.message)
        Some(futureJsonBadRequest(PostcodeMixedFilter(queryValues)))
      case s if !s.matches(postcodeFilterRegex) =>
        logger.systemLog(badRequestMessage = FilterInvalidError.message)
        Some(futureJsonBadRequest(PostcodeFilterInvalid(queryValues)))
      case _ => None
    }
  }

  def validatePostcodeOffset(offset: Option[String], queryValues: QueryValues): Option[Future[Result]] = {
    val maxOffset: Int = conf.config.elasticSearch.maximumOffset
    val defOffset: Int = conf.config.elasticSearch.defaultOffset
    val offVal = offset.getOrElse(defOffset.toString)
    val offsetInvalid = Try(offVal.toInt).isFailure
    val offsetInt = Try(offVal.toInt).toOption.getOrElse(defOffset)

    (offsetInvalid, offsetInt) match {
      case (true, _) =>
        logger.systemLog(badRequestMessage = OffsetNotNumericAddressResponseError.message)
        Some(futureJsonBadRequest(OffsetNotNumericPostcode(queryValues)))
      case (false, i) if i < 0 =>
        logger.systemLog(badRequestMessage = OffsetTooSmallAddressResponseError.message)
        Some(futureJsonBadRequest(OffsetTooSmallPostcode(queryValues)))
      case (false, i) if i > maxOffset =>
        logger.systemLog(badRequestMessage = OffsetTooLargeAddressResponseErrorCustom.message)
        Some(futureJsonBadRequest(OffsetTooLargePostcode(queryValues)))
      case _ => None
    }
  }

  def validatePostcode(postcode: String, queryValues: QueryValues): Option[Future[Result]] = {
    val postcodeRegex = "^(GIR 0AA)|((([A-Z][0-9]{1,2})|(([A-Z][A-HJ-Y][0-9]{1,2})|(([A-Z][0-9][A-Z])|([A-Z][A-HJ-Y][0-9]?[A-Z])))) ?[0-9][A-Z]{2})$"
    postcode match {
      case "" =>
        logger.systemLog(badRequestMessage = EmptyQueryPostcodeAddressResponseError.message)
        Some(futureJsonBadRequest(EmptySearchPostcode(queryValues)))
      case s if !s.toUpperCase().matches(postcodeRegex) =>
        logger.systemLog(badRequestMessage = postcode + ": " + InvalidPostcodeAddressResponseError.message)
        Some(futureJsonBadRequest(InvalidPostcode(queryValues)))
      case _ => None
    }
  }

  // set minimum string length from config
  val validEpochs: String = conf.config.elasticSearch.validEpochs
  val validEpochsMessage: String = validEpochs.replace("|test", "").replace("|", ", ")

  // override error message with named length
  object EpochNotAvailableErrorCustom extends AddressResponseError(
    code = 36,
    message = EpochNotAvailableError.message.concat(". Current available epochs are " + validEpochsMessage + ".")
  )

  override def PostcodeEpochInvalid(queryValues: QueryValues): AddressByPostcodeResponseContainer = {
    BadRequestPostcodeTemplate(queryValues, EpochNotAvailableErrorCustom)
  }

  def validateEpoch(queryValues: QueryValues): Option[Future[Result]] = {

    val epochVal: String = queryValues.epochOrDefault

    if (!epochVal.isEmpty) {
      if (!epochVal.matches("""\b(""" + validEpochs + """)\b.*""")) {
        logger.systemLog(badRequestMessage = EpochNotAvailableError.message, epoch = epochVal, postcode = queryValues.postcodeOrDefault)
        Some(futureJsonBadRequest(PostcodeEpochInvalid(queryValues)))
      } else None
    } else None

  }

}
