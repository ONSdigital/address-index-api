package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Result
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.random.AddressByRandomResponseContainer
import uk.gov.ons.addressIndex.server.model.dao.QueryValues
import uk.gov.ons.addressIndex.server.modules.response.RandomControllerResponse
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}

import scala.concurrent.Future
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

@Singleton
class RandomControllerValidation @Inject()(implicit conf: ConfigModule, versionProvider: VersionModule)
  extends AddressValidation with RandomControllerResponse {
  val randomFilterRegex: Regex = raw"""\b(residential|commercial|[CcLlMmOoPpRrUuXxZz]\w*)\b.*""".r

 //  override error message with named valid epochs
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

  def validateRandomFilter(classificationFilter: Option[String], queryValues: QueryValues): Option[Future[Result]] = classificationFilter match {
    case None => None
    case Some("") => None
    case Some(filter) => filter match {
      case f if f.contains("*") && f.contains(",") =>
        logger.systemLog(badRequestMessage = MixedFilterError.message)
        Some(futureJsonBadRequest(RandomMixedFilter(queryValues)))
      case randomFilterRegex(_*) => None
      case _ =>
        logger.systemLog(badRequestMessage = FilterInvalidError.message)
        Some(futureJsonBadRequest(RandomFilterInvalid(queryValues)))
    }
  }

  // validEpochsRegex is inherited from AddressControllerValidation
   def validateEpoch(queryValues: QueryValues): Option[Future[Result]] =
    queryValues.epochOrDefault match {
      case "" => None
      case validEpochsRegex(_*) => None
      case _ =>
        logger.systemLog(badRequestMessage = EpochNotAvailableError.message)
        Some(futureJsonBadRequest(RandomEpochInvalid(queryValues)))
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
        Some(futureJsonBadRequest(RandomFromSourceInvalid(queryValues)))
    }
  }

}
