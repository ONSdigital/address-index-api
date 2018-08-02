package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Result
import uk.gov.ons.addressIndex.model.server.response.UprnNotNumericAddressResponseError
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}
import uk.gov.ons.addressIndex.server.modules.response.{AddressIndexResponse, UPRNResponse}
import uk.gov.ons.addressIndex.server.utils.APILogging
import uk.gov.ons.addressIndex.server.utils.impl.{AddressLogMessage, AddressLogging}

import scala.concurrent.Future
import scala.util.Try

@Singleton
class UPRNValidation @Inject()(implicit conf: ConfigModule, versionProvider: VersionModule)
  extends Validation with UPRNResponse with APILogging[AddressLogMessage] {

  override def trace(message: AddressLogMessage): Unit = AddressLogging trace message
  override def log(message: AddressLogMessage): Unit = AddressLogging log message
  override def debug(message: AddressLogMessage): Unit = AddressLogging debug message

  def validateUprn(uprn: String): Option[Future[Result]] = {
    val uprnInvalid = Try(uprn.toLong).isFailure
    if (uprnInvalid) {
      log(AddressLogMessage(badRequestMessage = UprnNotNumericAddressResponseError.message))
      Some(futureJsonBadRequest(UprnNotNumeric))
    } else None
  }
}
