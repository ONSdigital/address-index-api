package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.ons.addressIndex.model.server.response.address.AddressResponseVersion
import uk.gov.ons.addressIndex.server.modules.EpochOptionsModule
import uk.gov.ons.addressIndex.server.modules.response.AddressControllerResponse

/**
  * Returns Available Epoch List
  */
@Singleton
class EpochOptionsController @Inject()(val controllerComponents: ControllerComponents,
                                       versionProvider: EpochOptionsModule
                                      ) extends PlayHelperControllerEpochVersion(versionProvider) with AddressControllerResponse {

  // lazy to avoid application crash at startup if ES is down
  lazy val versionResults = new AddressResponseVersion(apiVersion, dataVersion)

  def epochQuery(): Action[AnyContent] = Action {
    Ok(Json.toJson(versionResults))
  }

}
