package uk.gov.ons.addressIndex.server.controllers.general

import javax.inject.{Inject, Singleton}

import play.api.mvc.{Action, AnyContent, Controller}

/**
  * The main controller of the application.
  */
@Singleton
class ApplicationController @Inject() extends Controller {

  def index() : Action[AnyContent] = Action { implicit req =>
    Ok("hello world")
  }
}