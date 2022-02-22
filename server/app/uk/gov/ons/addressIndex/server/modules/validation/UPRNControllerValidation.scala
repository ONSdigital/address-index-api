package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Result
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.uprn.AddressByUprnResponseContainer
import uk.gov.ons.addressIndex.server.model.dao.QueryValues
import uk.gov.ons.addressIndex.server.modules.response.UPRNControllerResponse
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

@Singleton
class UPRNControllerValidation @Inject()(implicit conf: ConfigModule, versionProvider: VersionModule)
  extends Validation with UPRNControllerResponse {

  // set minimum string length from config
  val validEpochs: String = conf.config.elasticSearch.validEpochs
  val validEpochsMessage: String = validEpochs.replace("|test", "").replace("|", ", ")
  val validEpochsRegex: String = """\b(""" + validEpochs + """)\b.*"""
  val validAddressTypes: Seq[String] = Seq("paf", "welshpaf", "nag", "welshnag", "nisra")

  // override error message with named length
  object EpochNotAvailableErrorCustom extends AddressResponseError(
    code = 36,
    message = EpochNotAvailableError.message.concat(". Current available epochs are " + validEpochsMessage + ".")
  )

  override def UprnEpochInvalid(queryValues: QueryValues): AddressByUprnResponseContainer = {
    BadRequestUprnTemplate(queryValues, EpochNotAvailableErrorCustom)
  }

  def validateUprns(uprns: List[String], queryValues: QueryValues): Option[Future[Result]] = {
//    Try(uprns.head.toLong) match {
//      case Success(_) => None
//      case Failure(_) =>
//        logger.systemLog(badRequestMessage = UprnNotNumericAddressResponseError.message)
//        Some(futureJsonBadRequest(UprnNotNumeric(queryValues)))
//    }
    None
  }

  def validateUprn(uprn: String, queryValues: QueryValues): Option[Future[Result]] = {
    Try(uprn.toLong) match {
      case Success(_) => None
      case Failure(_) =>
        logger.systemLog(badRequestMessage = UprnNotNumericAddressResponseError.message)
        Some(futureJsonBadRequest(UprnNotNumeric(queryValues)))
    }
  }


  def validateAddressType(addressType: String, queryValues: QueryValues): Option[Future[Result]] = {

    if (validAddressTypes.contains(addressType)) {
      None
    } else {
      logger.systemLog(badRequestMessage = InvalidAddressTypeAddressResponseError.message)
      Some(futureJsonBadRequest(AddressTypeInvalid(queryValues)))
    }
  }

  def validateEpoch(queryValues: QueryValues): Option[Future[Result]] =
    queryValues.epochOrDefault match {
      case "" => None
      case e if e.matches(validEpochsRegex) => None
      case _ =>
        logger.systemLog(badRequestMessage = EpochNotAvailableError.message)
        Some(futureJsonBadRequest(UprnEpochInvalid(queryValues)))
    }
}
