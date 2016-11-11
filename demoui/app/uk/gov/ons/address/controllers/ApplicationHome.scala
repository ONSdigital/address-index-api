package uk.gov.ons.address.controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}
import play.Logger
import play.api.libs.ws.{WSClient, WSResponse}
import play.api.mvc.{Controller, _}
import uk.gov.ons.address.conf.OnsFrontendConfiguration
import uk.gov.ons.address.views
import uk.gov.ons.addressIndex.client.AddressIndexClient
import uk.gov.ons.addressIndex.model.{AddressIndexUPRNRequest, PostcodeAddressFile}
import play.api.mvc.Action
import scala.concurrent.ExecutionContext

@Singleton
class AddressIndexClientInstance @Inject()(override val client : WSClient) extends AddressIndexClient {
  override def host : String = "http://localhost:9001"
}

@Singleton
class ApplicationHome @Inject()(
  configuation: OnsFrontendConfiguration
)(
  implicit ec : ExecutionContext
) extends Controller {

  def indexPage() : Action[AnyContent] = Action { implicit req =>
    Logger.info("Rendering Index page")
    Ok(views.html.index(configuation))
  }
}
