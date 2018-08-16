package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import uk.gov.ons.addressIndex.server.modules.response.PartialAddressControllerResponse
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}

@Singleton
class PartialAddressControllerValidation @Inject()(implicit conf: ConfigModule, versionProvider: VersionModule)
  extends AddressValidation with PartialAddressControllerResponse {

  // Nothing here as validation methods are reused from [AddressValidation]
  
}
