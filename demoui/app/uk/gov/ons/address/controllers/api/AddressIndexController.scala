package uk.gov.ons.address.controllers.api

import java.util.UUID

import play.api.mvc.{Action, AnyContent, Controller}
import javax.inject.{Inject, Singleton}

import play.Logger
import uk.gov.ons.address.controllers.AddressIndexClientInstance
import uk.gov.ons.addressIndex.model.{AddressIndexSearchRequest, AddressScheme, PostcodeAddressFile}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class AddressIndexController @Inject()(aiClient: AddressIndexClientInstance)(implicit ec : ExecutionContext) extends Controller {

  def addressQuery(q : String) : Action[AnyContent] = Action.async { implicit request =>
    Logger.info(s"address query called with q: $q")
    aiClient.addressQuery(
      AddressIndexSearchRequest(
        format = PostcodeAddressFile("paf"),
        input = q,
        id = UUID.randomUUID
      )
    ).map{x => Logger.info("got response"); Ok(x.body)}
  }
}