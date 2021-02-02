package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Result
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.partialaddress.AddressByPartialAddressResponseContainer
import uk.gov.ons.addressIndex.server.model.dao.QueryValues
import uk.gov.ons.addressIndex.server.modules.response.PartialAddressControllerResponse
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Singleton
class PartialAddressControllerValidation @Inject()(implicit conf: ConfigModule, versionProvider: VersionModule)
  extends AddressControllerValidation with PartialAddressControllerResponse {

  // set minimum string length from config
  val minimumTermLength: Int = conf.config.elasticSearch.minimumPartial

  // override error message with named length
  object ShortQueryAddressResponseErrorCustom extends AddressResponseError(
    code = 33,
    message = ShortQueryAddressResponseError.message.replace("*", minimumTermLength.toString)
  )

  override def ShortSearch(queryValues: QueryValues): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues, ShortQueryAddressResponseErrorCustom)
  }

  // minimum length only for partial so override
  override def validateInput(input: String, queryValues: QueryValues): Option[Future[Result]] =
    input match {
      case "" =>
        logger.systemLog(badRequestMessage = EmptyQueryAddressResponseError.message)
        Some(futureJsonBadRequest(EmptySearch(queryValues)))
      case i if i.trim().replaceAll(",","").length < minimumTermLength =>
        logger.systemLog(badRequestMessage = ShortQueryAddressResponseErrorCustom.message)
        Some(futureJsonBadRequest(ShortSearch(queryValues)))
      case _ => None
    }

  override def LimitTooLargePartial(queryValues: QueryValues): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues, LimitTooLargeAddressResponseErrorCustom)
  }

  override def OffsetTooLargePartial(queryValues: QueryValues): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues, OffsetTooLargeAddressResponseErrorCustom)
  }

  override def PartialEpochInvalid(queryValues: QueryValues): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues, EpochNotAvailableErrorCustom)
  }

  // defaultLimit and maximumLimit is inherited from AddressValidation
  def validatePartialLimit(limit: Option[String], queryValues: QueryValues): Option[Future[Result]] = {
    def inner(limit: Int): Option[Future[Result]] = limit match {
      case l if l < 1 =>
        logger.systemLog(badRequestMessage = LimitTooSmallAddressResponseError.message)
        Some(futureJsonBadRequest(LimitTooSmallPartial(queryValues)))
      case l if maximumLimit < l =>
        logger.systemLog(badRequestMessage = LimitTooLargeAddressResponseErrorCustom.message)
        Some(futureJsonBadRequest(LimitTooLargePartial(queryValues)))
      case _ => None
    }

    limit match {
      case Some(l) => Try(l.toInt) match {
        case Success(lInt) => inner(lInt)
        case Failure(_) =>
          logger.systemLog(badRequestMessage = LimitNotNumericAddressResponseError.message)
          Some(futureJsonBadRequest(LimitNotNumericPartial(queryValues)))
      }
      case None => inner(defaultLimit)
    }
  }

  def validatePartialOffset(offset: Option[String], queryValues: QueryValues): Option[Future[Result]] = {
    def inner(offset: Int): Option[Future[Result]] = offset match {
      case l if l < 0 =>
        logger.systemLog(badRequestMessage = OffsetTooSmallAddressResponseError.message)
        Some(futureJsonBadRequest(OffsetTooSmallPartial(queryValues)))
      case l if maximumOffset < l =>
        logger.systemLog(badRequestMessage = OffsetTooLargeAddressResponseErrorCustom.message)
        Some(futureJsonBadRequest(OffsetTooLargePartial(queryValues)))
      case _ => None
    }

    offset match {
      case Some(l) => Try(l.toInt) match {
        case Success(lInt) => inner(lInt)
        case Failure(_) =>
          logger.systemLog(badRequestMessage = OffsetNotNumericAddressResponseError.message)
          Some(futureJsonBadRequest(OffsetNotNumericPartial(queryValues)))
      }
      case None => inner(defaultOffset)
    }
  }

  // validEpochsRegex is inherited from AddressControllerValidation
  override def validateEpoch(queryValues: QueryValues): Option[Future[Result]] =
    queryValues.epochOrDefault match {
      case "" => None
      case validEpochsRegex(_*) => None
      case _ =>
        logger.systemLog(badRequestMessage = EpochNotAvailableError.message)
        Some(futureJsonBadRequest(PartialEpochInvalid(queryValues)))
    }

  override def validateFromSource(queryValues: QueryValues): Option[Future[Result]] = {
    queryValues.fromsource match {
      case Some("nionly") => None
      case Some("ewonly") => None
      case Some("niboost") => None
      case Some("ewboost") => None
      case Some("all") => None
      case _ =>
        logger.systemLog(badRequestMessage = FromSourceInvalidError.message)
        Some(futureJsonBadRequest(PartialFromSourceInvalid(queryValues)))
    }
  }

  override def validateBoosts(eboost: Option[String],nboost: Option[String],sboost: Option[String],wboost: Option[String],queryValues: QueryValues): Option[Future[Result]] = {
    val eboostVal = {if (eboost.getOrElse("1.0").isEmpty) "1.0" else eboost.getOrElse("1.0")}
    val nboostVal = {if (nboost.getOrElse("1.0").isEmpty) "1.0" else nboost.getOrElse("1.0")}
    val sboostVal = {if (sboost.getOrElse("1.0").isEmpty) "1.0" else sboost.getOrElse("1.0")}
    val wboostVal = {if (wboost.getOrElse("1.0").isEmpty) "1.0" else wboost.getOrElse("1.0")}

    val eboostDouble = Try(eboostVal.toDouble).toOption.getOrElse(99D)
    val nboostDouble = Try(nboostVal.toDouble).toOption.getOrElse(99D)
    val sboostDouble = Try(sboostVal.toDouble).toOption.getOrElse(99D)
    val wboostDouble = Try(wboostVal.toDouble).toOption.getOrElse(99D)

    if (eboostDouble > 10 || nboostDouble > 10 || sboostDouble > 10 || wboostDouble > 10 || eboostDouble < 0 || nboostDouble < 0 || sboostDouble < 0 || wboostDouble < 0) {
      logger.systemLog(badRequestMessage = CountryBoostsInvalidError.message)
      Some(futureJsonBadRequest(PartialCountryBoostsInvalid(queryValues)))
    } else None

  }

}
