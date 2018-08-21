package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Result
import uk.gov.ons.addressIndex.model.server.response.address.UprnNotNumericAddressResponseError
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
}
