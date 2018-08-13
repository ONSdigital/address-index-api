package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.ons.addressIndex.model.server.response.AddressResponseVersion
import uk.gov.ons.addressIndex.server.modules._
import uk.gov.ons.addressIndex.server.modules.response.AddressIndexResponse

import scala.concurrent.ExecutionContext

/**
  * Returns version information - could later become general purpose info
  */
@Singleton
class VersionController @Inject()(
  val controllerComponents: ControllerComponents,
  conf: ConfigModule,
  versionProvider: VersionModule
)(implicit ec: ExecutionContext) extends PlayHelperController(versionProvider) with AddressIndexResponse {

  // lazy to avoid application crash at startup if ES is down
  lazy val versionResults = new AddressResponseVersion(apiVersion, dataVersion)

  def versionQuery(): Action[AnyContent] = Action { implicit req =>
    Ok(Json.toJson(versionResults))
  }

}
