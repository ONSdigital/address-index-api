package uk.gov.ons.addressIndex.demoui.controllers.api

import java.util.UUID
import play.api.mvc.{Action, AnyContent, Controller}
import javax.inject.{Inject, Singleton}
import play.Logger
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.model.{AddressIndexSearchRequest, PostcodeAddressFile}
import scala.concurrent.ExecutionContext

@Singleton
class AddressIndexController @Inject()(aiClient: AddressIndexClientInstance)(implicit ec : ExecutionContext) extends Controller {

  def addressQuery(q : String) : Action[AnyContent] = Action.async { implicit request =>
    aiClient.addressQuery(
      AddressIndexSearchRequest(
        format = PostcodeAddressFile("paf"),
        input = q,
        id = UUID.randomUUID
      )
    ) map (x => Ok(x.body))
  }
}