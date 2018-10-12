package uk.gov.ons.addressIndex.server.modules.validation

import javax.inject.{Inject, Singleton}
import play.api.mvc.Result
import uk.gov.ons.addressIndex.model.server.response.address.{AddressBySearchResponseContainer, AddressResponseError, EmptyQueryAddressResponseError, ShortQueryAddressResponseError}
import uk.gov.ons.addressIndex.server.modules.response.PartialAddressControllerResponse
import uk.gov.ons.addressIndex.server.modules.{ConfigModule, VersionModule}

import scala.concurrent.Future

@Singleton
class PartialAddressControllerValidation @Inject()(implicit conf: ConfigModule, versionProvider: VersionModule)
  extends AddressValidation with PartialAddressControllerResponse {


  // set minimum string length from config
  val minimumTermLength = conf.config.elasticSearch.minimumPartial

  // override error message with named length
  object ShortQueryAddressResponseErrorCustom extends AddressResponseError(
    code = 39,
    message = "Partial address string too short, minimum " + minimumTermLength + " characters"
  )

  override def ShortSearch: AddressBySearchResponseContainer = {
    BadRequestTemplate(ShortQueryAddressResponseErrorCustom)
  }

  // minimum length only for partial so override
  override def validateInput(input: String): Option[Future[Result]] = {
    if (input.isEmpty) {
      logger.systemLog(badRequestMessage = EmptyQueryAddressResponseError.message)
      Some(futureJsonBadRequest(EmptySearch))
    } else if (input.length < minimumTermLength) {
      logger.systemLog(badRequestMessage = ShortQueryAddressResponseErrorCustom.message)
      Some(futureJsonBadRequest(ShortSearch))
    } else None
  }
  
}
