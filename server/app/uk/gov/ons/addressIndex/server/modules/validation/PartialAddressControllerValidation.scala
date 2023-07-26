package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Result
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.partialaddress.AddressByPartialAddressResponseContainer
import uk.gov.ons.addressIndex.server.model.dao.{QueryValues, RequestValues}
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
  override def validateInput(input: String, queryValues: QueryValues, requestValues: RequestValues): Option[Future[Result]] =
    input match {
      case "" =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = EmptyQueryAddressResponseError.message)
        Some(futureJsonBadRequest(EmptySearch(queryValues)))
      case i if i.trim().replaceAll(",","").length < minimumTermLength =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = ShortQueryAddressResponseErrorCustom.message)
        Some(futureJsonBadRequest(ShortSearch(queryValues)))
      case _ => None
    }

  override def LimitTooLargePartial(queryValues: QueryValues): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues, LimitTooLargeAddressResponseErrorCustom)
  }

  override def OffsetTooLargePartial(queryValues: QueryValues): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues, OffsetTooLargeAddressResponseErrorCustom)
  }

  override def TimeoutTooLargePartial(queryValues: QueryValues): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues, TimeoutTooLargeAddressResponseErrorCustom)
  }

  override def PartialEpochInvalid(queryValues: QueryValues): AddressByPartialAddressResponseContainer = {
    BadRequestPartialTemplate(queryValues, EpochNotAvailableErrorCustom)
  }

  // defaultLimit and maximumLimit is inherited from AddressValidation
  def validatePartialLimit(limit: Option[String], queryValues: QueryValues, requestValues: RequestValues): Option[Future[Result]] = {
    def inner(limit: Int): Option[Future[Result]] = limit match {
      case l if l < 1 =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = LimitTooSmallAddressResponseError.message)
        Some(futureJsonBadRequest(LimitTooSmallPartial(queryValues)))
      case l if maximumLimit < l =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = LimitTooLargeAddressResponseErrorCustom.message)
        Some(futureJsonBadRequest(LimitTooLargePartial(queryValues)))
      case _ => None
    }

    limit match {
      case Some(l) => Try(l.toInt) match {
        case Success(lInt) => inner(lInt)
        case Failure(_) =>
          logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
            responsecode = "400",badRequestMessage = LimitNotNumericAddressResponseError.message)
          Some(futureJsonBadRequest(LimitNotNumericPartial(queryValues)))
      }
      case None => inner(defaultLimit)
    }
  }

  def validatePartialOffset(offset: Option[String], queryValues: QueryValues, requestValues: RequestValues): Option[Future[Result]] = {
    def inner(offset: Int): Option[Future[Result]] = offset match {
      case l if l < 0 =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = OffsetTooSmallAddressResponseError.message)
        Some(futureJsonBadRequest(OffsetTooSmallPartial(queryValues)))
      case l if maximumOffset < l =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = OffsetTooLargeAddressResponseErrorCustom.message)
        Some(futureJsonBadRequest(OffsetTooLargePartial(queryValues)))
      case _ => None
    }

    offset match {
      case Some(l) => Try(l.toInt) match {
        case Success(lInt) => inner(lInt)
        case Failure(_) =>
          logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
            responsecode = "400",badRequestMessage = OffsetNotNumericAddressResponseError.message)
          Some(futureJsonBadRequest(OffsetNotNumericPartial(queryValues)))
      }
      case None => inner(defaultOffset)
    }
  }

  def validatePartialTimeout(timeout: Option[String], queryValues: QueryValues, requestValues: RequestValues): Option[Future[Result]] = {
    def inner(timeout: Int): Option[Future[Result]] = timeout match {
      case l if l < 50 =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = TimeoutTooSmallAddressResponseError.message)
        Some(futureJsonBadRequest(TimeoutTooSmallPartial(queryValues)))
      case l if maximumTimeout < l =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = TimeoutTooLargeAddressResponseErrorCustom.message)
        Some(futureJsonBadRequest(TimeoutTooLargePartial(queryValues)))
      case _ => None
    }

    timeout match {
      case Some(l) => Try(l.toInt) match {
        case Success(lInt) => inner(lInt)
        case Failure(_) =>
          logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
            responsecode = "400",badRequestMessage = TimeoutNotNumericAddressResponseError.message)
          Some(futureJsonBadRequest(TimeoutNotNumericPartial(queryValues)))
      }
      case None => inner(defaultTimeout)
    }
  }

  // validEpochsRegex is inherited from AddressControllerValidation
  override def validateEpoch(queryValues: QueryValues, requestValues: RequestValues): Option[Future[Result]] =
    queryValues.epochOrDefault match {
      case "" => None
      case validEpochsRegex(_*) => None
      case _ =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = EpochNotAvailableError.message)
        Some(futureJsonBadRequest(PartialEpochInvalid(queryValues)))
    }

  override def validateBoosts(eboost: Option[String],nboost: Option[String],sboost: Option[String],wboost: Option[String],lboost: Option[String],mboost: Option[String],jboost: Option[String],queryValues: QueryValues, requestValues: RequestValues): Option[Future[Result]] = {
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

    if (eboostDouble > 10 || nboostDouble > 10 || sboostDouble > 10 || wboostDouble > 10 || eboostDouble < 0 || nboostDouble < 0 || sboostDouble < 0 || wboostDouble < 0) {
      logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
        responsecode = "400",badRequestMessage = CountryBoostsInvalidError.message)
      Some(futureJsonBadRequest(PartialCountryBoostsInvalid(queryValues)))
    } else None

  }

}
