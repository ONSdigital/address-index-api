package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Result
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.uprn.AddressByUprnResponseContainer
import uk.gov.ons.addressIndex.server.modules.response.UPRNControllerResponse
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}

import scala.concurrent.Future
import scala.util.Try

@Singleton
class UPRNControllerValidation @Inject()(implicit conf: ConfigModule, versionProvider: VersionModule)
  extends Validation with UPRNControllerResponse {

  def validateUprn(uprn: String): Option[Future[Result]] = {
    val uprnInvalid = Try(uprn.toLong).isFailure
    if (uprnInvalid) {
      logger.systemLog(badRequestMessage = UprnNotNumericAddressResponseError.message)
      Some(futureJsonBadRequest(UprnNotNumeric))
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

  override def UprnEpochInvalid: AddressByUprnResponseContainer = {
    BadRequestUprnTemplate(EpochNotAvailableErrorCustom)
  }

  def validateEpoch(epoch: Option[String]): Option[Future[Result]] = {

    val epochVal: String = epoch.getOrElse("")
    val validEpochs: String = conf.config.elasticSearch.validEpochs

    if (!epochVal.isEmpty){
      if (!epochVal.matches("""\b("""+ validEpochs + """)\b.*""")) {
        logger.systemLog(badRequestMessage = EpochNotAvailableError.message)
        Some(futureJsonBadRequest(UprnEpochInvalid))
      } else None
    } else None

  }
}
