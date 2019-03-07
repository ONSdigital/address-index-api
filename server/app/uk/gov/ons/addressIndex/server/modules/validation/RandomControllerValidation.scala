package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Result
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.random.AddressByRandomResponseContainer
import uk.gov.ons.addressIndex.server.model.dao.QueryValues
import uk.gov.ons.addressIndex.server.modules.response.RandomControllerResponse
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}

import scala.concurrent.Future
import scala.util.Try

@Singleton
class RandomControllerValidation @Inject()(implicit conf: ConfigModule, versionProvider: VersionModule)
  extends AddressValidation with RandomControllerResponse {

  override def LimitTooLargeRandom(queryValues: QueryValues): AddressByRandomResponseContainer = {
    BadRequestRandomTemplate(queryValues, LimitTooLargeAddressResponseErrorCustom)
  }

  def validateRandomLimit(limit: Option[String], queryValues: QueryValues): Option[Future[Result]] = {

    val defLimit: Int = conf.config.elasticSearch.defaultLimit
    val limval = limit.getOrElse(defLimit.toString)
    val limitInvalid = Try(limval.toInt).isFailure
    val limitInt = Try(limval.toInt).toOption.getOrElse(defLimit)
    val maxLimit: Int = conf.config.elasticSearch.maximumLimit

    if (limitInvalid) {
      logger.systemLog(badRequestMessage = LimitNotNumericAddressResponseError.message)
      Some(futureJsonBadRequest(LimitNotNumericRandom(queryValues)))
    } else if (limitInt < 1) {
      logger.systemLog(badRequestMessage = LimitTooSmallAddressResponseError.message)
      Some(futureJsonBadRequest(LimitTooSmallRandom(queryValues)))
    } else if (limitInt > maxLimit) {
      logger.systemLog(badRequestMessage = LimitTooLargeAddressResponseErrorCustom.message)
      Some(futureJsonBadRequest(LimitTooLargeRandom(queryValues)))
    } else None

  }

  def validateRandomFilter(classificationfilter: Option[String], queryValues: QueryValues): Option[Future[Result]] = {

    val filterString: String = classificationfilter.getOrElse("")

    if (!filterString.isEmpty) {
      if (filterString.contains("*") && filterString.contains(",")) {
        logger.systemLog(badRequestMessage = MixedFilterError.message)
        Some(futureJsonBadRequest(RandomMixedFilter(queryValues)))
      }
      else if (!filterString.matches("""\b(residential|commercial|C|c|C\w+|c\w+|L|l|L\w+|l\w+|M|m|M\w+|m\w+|O|o|O\w+|o\w+|P|p|P\w+|p\w+|R|r|R\w+|r\w+|U|u|U\w+|u\w+|X|x|X\w+|x\w+|Z|z|Z\w+|z\w+)\b.*""")) {
        logger.systemLog(badRequestMessage = FilterInvalidError.message)
        Some(futureJsonBadRequest(RandomFilterInvalid(queryValues)))
      } else None
    } else None

  }

  // set minimum string length from config
  val validEpochs: String = conf.config.elasticSearch.validEpochs
  val validEpochsMessage: String = validEpochs.replace("|test", "").replace("|", ", ")

  // override error message with named length
  object EpochNotAvailableErrorCustom extends AddressResponseError(
    code = 36,
    message = EpochNotAvailableError.message.concat(". Current available epochs are " + validEpochsMessage + ".")
  )

  override def RandomEpochInvalid(queryValues: QueryValues): AddressByRandomResponseContainer = {
    BadRequestRandomTemplate(queryValues, EpochNotAvailableErrorCustom)
  }

  def validateEpoch(queryValues: QueryValues): Option[Future[Result]] = {

    val epochVal: String = queryValues.epochOrDefault.toString
    val validEpochs: String = conf.config.elasticSearch.validEpochs

    if (!epochVal.isEmpty) {
      if (!epochVal.matches("""\b(""" + validEpochs + """)\b.*""")) {
        logger.systemLog(badRequestMessage = EpochNotAvailableError.message)
        Some(futureJsonBadRequest(RandomEpochInvalid(queryValues)))
      } else None
    } else None

  }

}
