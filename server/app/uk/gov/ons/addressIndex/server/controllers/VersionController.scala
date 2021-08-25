package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.ons.addressIndex.model.server.response.address.AddressResponseVersion
import uk.gov.ons.addressIndex.server.modules.VersionModule
import uk.gov.ons.addressIndex.server.modules.response.AddressControllerResponse

/**
  * Returns version information - could later become general purpose info
  */
@Singleton
class VersionController @Inject()(val controllerComponents: ControllerComponents,
                                  versionProvider: VersionModule
                                 ) extends PlayHelperController(versionProvider) with AddressControllerResponse {

  // lazy to avoid application crash at startup if ES is down
  lazy val versionResults = new AddressResponseVersion(apiVersion, dataVersion)

  def versionQuery(): Action[AnyContent] = Action {
    Ok(Json.toJson(versionResults))
  }

}
