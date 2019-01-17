package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Result
import uk.gov.ons.addressIndex.model.server.response.address.{EpochNotAvailableError, UprnNotNumericAddressResponseError}
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
