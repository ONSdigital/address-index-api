package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Result
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.random.AddressByRandomResponseContainer
import uk.gov.ons.addressIndex.server.model.dao.QueryValues
import uk.gov.ons.addressIndex.server.modules.response.RandomControllerResponse
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Singleton
class RandomControllerValidation @Inject()(implicit conf: ConfigModule, versionProvider: VersionModule)
  extends AddressValidation with RandomControllerResponse {

  // override error message with named length
  object EpochNotAvailableErrorCustom extends AddressResponseError(
    code = 36,
    message = EpochNotAvailableError.message.concat(". Current available epochs are " + validEpochsMessage + ".")
  )

  override def RandomEpochInvalid(queryValues: QueryValues): AddressByRandomResponseContainer = {
    BadRequestRandomTemplate(queryValues, EpochNotAvailableErrorCustom)
  }

  override def LimitTooLargeRandom(queryValues: QueryValues): AddressByRandomResponseContainer = {
    BadRequestRandomTemplate(queryValues, LimitTooLargeAddressResponseErrorCustom)
  }

  def validateRandomLimit(limit: Option[String], queryValues: QueryValues): Option[Future[Result]] = {
    def inner(limit: Int): Option[Future[Result]] = limit match {
      case l if l < 1 =>
        logger.systemLog(badRequestMessage = LimitTooSmallAddressResponseError.message)
        Some(futureJsonBadRequest(LimitTooSmallRandom(queryValues)))
      case l if maximumLimit < l =>
        logger.systemLog(badRequestMessage = LimitTooLargeAddressResponseErrorCustom.message)
        Some(futureJsonBadRequest(LimitTooLargeRandom(queryValues)))
      case _ => None
    }

    limit match {
      case Some(l) => Try(l.toInt) match {
        case Success(lInt) => inner(lInt)
        case Failure(_) =>
          logger.systemLog(badRequestMessage = LimitNotNumericAddressResponseError.message)
          Some(futureJsonBadRequest(LimitNotNumericRandom(queryValues)))
      }
      case None => inner(defaultLimit)
    }
  }

  def validateRandomFilter(classificationFilter: Option[String], queryValues: QueryValues): Option[Future[Result]] = {
    val regexString: String = """\b(residential|commercial|[CcLlMmOoPpRrUuXxZz]\w*)\b.*"""
    //    val regexString: String = """\b(residential|commercial|C|c|C\w+|c\w+|L|l|L\w+|l\w+|M|m|M\w+|m\w+|O|o|O\w+|o\w+|P|p|P\w+|p\w+|R|r|R\w+|r\w+|U|u|U\w+|u\w+|X|x|X\w+|x\w+|Z|z|Z\w+|z\w+)\b.*"""

    classificationFilter.getOrElse("") match {
      case "" => None
      case s if s.contains("*") && s.contains(",") =>
        logger.systemLog(badRequestMessage = MixedFilterError.message)
        Some(futureJsonBadRequest(RandomMixedFilter(queryValues)))
      case s if !s.matches(regexString) =>
        logger.systemLog(badRequestMessage = FilterInvalidError.message)
        Some(futureJsonBadRequest(RandomFilterInvalid(queryValues)))
      case _ => None
    }
  }

  def validateEpoch(queryValues: QueryValues): Option[Future[Result]] =
    queryValues.epochOrDefault match {
      case "" => None
      case e if e.matches(validEpochsRegex) => None
      case _ =>
        logger.systemLog(badRequestMessage = EpochNotAvailableError.message)
        Some(futureJsonBadRequest(RandomEpochInvalid(queryValues)))
    }

}
