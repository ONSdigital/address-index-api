package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Result
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.random.AddressByRandomResponseContainer
import uk.gov.ons.addressIndex.server.model.dao.{QueryValues, RequestValues}
import uk.gov.ons.addressIndex.server.modules.response.RandomControllerResponse
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}

import scala.concurrent.Future
import scala.util.matching.Regex
import scala.util.{Failure, Success, Try}

@Singleton
class RandomControllerValidation @Inject()(implicit conf: ConfigModule, versionProvider: VersionModule)
  extends AddressValidation with RandomControllerResponse {
  val randomFilterRegex: Regex = raw"""\b(residential|commercial|workplace|educational|[CcLlMmOoPpRrUuXxZz]\w*)\b.*""".r

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

  def validateRandomLimit(limit: Option[String], queryValues: QueryValues, requestValues: RequestValues): Option[Future[Result]] = {
    def inner(limit: Int): Option[Future[Result]] = limit match {
      case l if l < 1 =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = LimitTooSmallAddressResponseError.message)
        Some(futureJsonBadRequest(LimitTooSmallRandom(queryValues)))
      case l if maximumLimit < l =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = LimitTooLargeAddressResponseErrorCustom.message)
        Some(futureJsonBadRequest(LimitTooLargeRandom(queryValues)))
      case _ => None
    }

    limit match {
      case Some(l) => Try(l.toInt) match {
        case Success(lInt) => inner(lInt)
        case Failure(_) =>
          logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
            responsecode = "400",badRequestMessage = LimitNotNumericAddressResponseError.message)
          Some(futureJsonBadRequest(LimitNotNumericRandom(queryValues)))
      }
      case None => inner(defaultLimit)
    }
  }

  def validateRandomFilter(classificationFilter: Option[String], queryValues: QueryValues, requestValues: RequestValues): Option[Future[Result]] = classificationFilter match {
    case None => None
    case Some("") => None
    case Some(filter) => filter match {
      case f if f.contains("*") && f.contains(",") =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = MixedFilterError.message)
        Some(futureJsonBadRequest(RandomMixedFilter(queryValues)))
      case randomFilterRegex(_*) => None
      case _ =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = FilterInvalidError.message)
        Some(futureJsonBadRequest(RandomFilterInvalid(queryValues)))
    }
  }

  // validEpochsRegex is inherited from AddressControllerValidation
   def validateEpoch(queryValues: QueryValues, requestValues: RequestValues): Option[Future[Result]] =
    queryValues.epochOrDefault match {
      case "" => None
      case validEpochsRegex(_*) => None
      case _ =>
        logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
          responsecode = "400",badRequestMessage = EpochNotAvailableError.message)
        Some(futureJsonBadRequest(RandomEpochInvalid(queryValues)))
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

    if (eboostDouble > 10 || nboostDouble > 10 || sboostDouble > 10 || wboostDouble > 10 || eboostDouble < 0 || nboostDouble < 0 || sboostDouble < 0 || wboostDouble < 0) {
      logger.systemLog(ip=requestValues.ip,url=requestValues.url,endpoint=requestValues.endpoint,networkid=requestValues.networkid,
        responsecode = "400",badRequestMessage = CountryBoostsInvalidError.message)
      Some(futureJsonBadRequest(RandomCountryBoostsInvalid(queryValues)))
    } else None

  }

}
