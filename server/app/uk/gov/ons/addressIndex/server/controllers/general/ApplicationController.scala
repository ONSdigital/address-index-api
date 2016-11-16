package uk.gov.ons.addressIndex.server.controllers.general

import javax.inject.{Inject, Singleton}
import uk.gov.ons.addressIndex.server.controllers.AddressIndexController
import play.api.mvc.{Action, AnyContent}

/**
  * The main controller of the application.
  */
@Singleton
class ApplicationController @Inject() extends AddressIndexController {

  def index() : Action[AnyContent] = Action { implicit req =>
    Ok("hello world")
  }
}