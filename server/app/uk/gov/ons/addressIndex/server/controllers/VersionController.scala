package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import uk.gov.ons.addressIndex.server.modules._
import uk.gov.ons.addressIndex.model.server.response.AddressResponseVersion
import uk.gov.ons.addressIndex.server.modules.response.AddressIndexResponse

import scala.concurrent.ExecutionContext

/**
  * Returns version information - could later become general purpose info
  * @param conf
  * @param versionProvider
  * @param ec
  */
@Singleton
class VersionController @Inject()(
  val controllerComponents: ControllerComponents,
  conf: ConfigModule,
  versionProvider: VersionModule
)(implicit ec: ExecutionContext) extends PlayHelperController(versionProvider) with AddressIndexResponse {

  lazy val logger = Logger("address-index-server:AddressController")

  // lazy to avoid application crash at startup if ES is down
  lazy val versionResults = new AddressResponseVersion(apiVersion, dataVersion)

  def versionQuery(): Action[AnyContent] = Action { implicit req =>
    Ok(Json.toJson(versionResults))
  }

}
