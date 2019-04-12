package uk.gov.ons.addressIndex.server.controllers.general

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}

/**
  * The main controller of the application.
  */
@Singleton
class ApplicationController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {
  def index(): Action[AnyContent] = Action { implicit req =>
    Ok("hello world")
  }
}