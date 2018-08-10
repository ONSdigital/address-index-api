package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}
import uk.gov.ons.addressIndex.server.modules.response.AddressIndexResponse
import uk.gov.ons.addressIndex.server.utils.APILogging
import uk.gov.ons.addressIndex.server.utils.impl.{AddressLogMessage, AddressLogging}

@Singleton
class CodelistValidation @Inject()(implicit conf: ConfigModule, versionProvider: VersionModule)
  extends Validation with AddressIndexResponse with APILogging[AddressLogMessage] {

  override def trace(message: AddressLogMessage): Unit = AddressLogging trace message
  override def log(message: AddressLogMessage): Unit = AddressLogging log message
  override def debug(message: AddressLogMessage): Unit = AddressLogging debug message

}
