package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import uk.gov.ons.addressIndex.server.modules._
import uk.gov.ons.addressIndex.model.server.response.AddressResponseVersion
import scala.concurrent.ExecutionContext

/**
  * Returns version information - could later become general purpose info
  * @param conf
  * @param versionProvider
  * @param ec
  */
@Singleton
class VersionController @Inject()(
  conf: ConfigModule,
  versionProvider: VersionModule
)(implicit ec: ExecutionContext) extends PlayHelperController with AddressIndexCannedResponse {

  val logger = Logger("address-index-server:AddressController")

  override val apiVersion: String = versionProvider.apiVersion
  override val dataVersion: String = versionProvider.dataVersion

  val versionResults = new AddressResponseVersion(apiVersion, dataVersion)

  def versionQuery(): Action[AnyContent] = Action { implicit req =>
    Ok(Json.toJson(versionResults))
  }

}
