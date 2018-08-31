package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import uk.gov.ons.addressIndex.server.modules.response.AddressControllerResponse
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}

@Singleton
class CodelistControllerValidation @Inject()(implicit conf: ConfigModule, versionProvider: VersionModule)
  extends Validation with AddressControllerResponse {

}
