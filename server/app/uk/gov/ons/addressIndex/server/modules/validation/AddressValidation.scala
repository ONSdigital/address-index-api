package uk.gov.ons.addressIndex.server.modules.validation

import play.api.mvc.Result
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.server.model.dao.{QueryValues, RequestValues}
import uk.gov.ons.addressIndex.server.modules.response.AddressResponse
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}

import scala.concurrent.Future
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

abstract class AddressValidation(implicit conf: ConfigModule, versionProvider: VersionModule)
  extends Validation with AddressResponse {

  val defaultLimit: Int = conf.config.elasticSearch.defaultLimit
  // get maxima length from config
  val maximumLimit: Int = conf.config.elasticSearch.maximumLimitPostcode
  val defaultOffset: Int = conf.config.elasticSearch.defaultOffset
  val maximumOffset: Int = conf.config.elasticSearch.maximumOffsetPostcode
  val defaultTimeout: Int = conf.config.elasticSearch.defaultTimeoutPartial
  val maximumTimeout: Int = conf.config.elasticSearch.maximumTimeoutPartial

  // set minimum string length from config
  val validEpochs: String = conf.config.elasticSearch.validEpochs
  val validEpochsMessage: String = validEpochs.replace("|test", "").replace("|", ", ")
  val validEpochsRegex: Regex = ("""\b(""" + validEpochs + """)\b.*""").r

  // override error messages with maxima
  object LimitTooLargeAddressResponseErrorCustom extends AddressResponseError(
    code = 8,
    message = LimitTooLargeAddressResponseError.message.replace("*", maximumLimit.toString)
  )

  object OffsetTooLargeAddressResponseErrorCustom extends AddressResponseError(
    code = 9,
    message = OffsetTooLargeAddressResponseError.message.replace("*", maximumOffset.toString)
  )

  object TimeoutTooLargeAddressResponseErrorCustom extends AddressResponseError(
    code = 45,
    message = TimeoutTooLargeAddressResponseError.message.replace("*", maximumTimeout.toString)
  )

  override def LimitTooLarge(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, LimitTooLargeAddressResponseErrorCustom)
  }

  override def OffsetTooLarge(queryValues: QueryValues): AddressBySearchResponseContainer = {
    BadRequestTemplate(queryValues, OffsetTooLargeAddressResponseErrorCustom)
  }

  def validateLimit(limit: Option[String], queryValues: QueryValues, requestValues: RequestValues): Option[Future[Result]] = {
    def inner(limit: Int): Option[Future[Result]] = limit match {
      case l if l < 1 =>
        logger.systemLog(responsecode = "400",badRequestMessage = LimitTooSmallAddressResponseError.message)
        Some(futureJsonBadRequest(LimitTooSmall(queryValues)))
      case l if maximumLimit < l =>
        logger.systemLog(responsecode = "400",badRequestMessage = LimitTooLargeAddressResponseErrorCustom.message)
        Some(futureJsonBadRequest(LimitTooLarge(queryValues)))
      case _ => None
    }

    limit match {
      case Some("") => None
      case Some(l) => Try(l.toInt) match {
        case Success(lInt) => inner(lInt)
        case Failure(_) =>
          logger.systemLog(responsecode = "400",badRequestMessage = LimitNotNumericAddressResponseError.message)
          Some(futureJsonBadRequest(LimitNotNumeric(queryValues)))
      }
      case None => inner(defaultLimit)
    }
  }

  def validateOffset(offset: Option[String], queryValues: QueryValues, requestValues: RequestValues): Option[Future[Result]] = {
    def inner(offset: Int): Option[Future[Result]] = offset match {
      case o if o < 0 =>
        logger.systemLog(responsecode = "400",badRequestMessage = OffsetTooSmallAddressResponseError.message)
        Some(futureJsonBadRequest(OffsetTooSmall(queryValues)))
      case o if maximumOffset < o =>
        logger.systemLog(responsecode = "400",badRequestMessage = OffsetTooLargeAddressResponseErrorCustom.message)
        Some(futureJsonBadRequest(OffsetTooLarge(queryValues)))
      case _ => None
    }

    offset match {
      case Some("") => None
      case Some(o) => Try(o.toInt) match {
        case Success(oInt) => inner(oInt)
        case Failure(_) =>
          logger.systemLog(responsecode = "400",badRequestMessage = OffsetNotNumericAddressResponseError.message)
          Some(futureJsonBadRequest(OffsetNotNumeric(queryValues)))
      }
      case None => inner(defaultOffset)
    }
  }

  def validateInput(input: String, queryValues: QueryValues, requestValues: RequestValues): Option[Future[Result]] = {
    val inputEmpty: Boolean = input.isEmpty
    val withRange: Boolean = queryValues.rangeKMOrDefault != "" && queryValues.latitudeOrDefault != "" && queryValues.longitudeOrDefault != "" && queryValues.filterOrDefault != ""
    val withEmptyRange: Boolean = queryValues.rangeKMOrDefault == "" && queryValues.latitudeOrDefault == "" && queryValues.longitudeOrDefault == "" && queryValues.filterOrDefault == ""
    if (inputEmpty && withEmptyRange) {
      logger.systemLog(responsecode = "400",badRequestMessage = EmptyQueryAddressResponseError.message)
      Some(futureJsonBadRequest(EmptySearch(queryValues)))
    } else if (inputEmpty && !withEmptyRange && !withRange) {
      logger.systemLog(responsecode = "400",badRequestMessage = EmptyRadiusQueryAddressResponseError.message)
      Some(futureJsonBadRequest(EmptyRadiusSearch(queryValues)))
    }else None
  }
}
