package uk.gov.ons.addressIndex.controllers

import javax.inject.{Inject, Singleton}
import play.Logger
import play.api.mvc.{Controller, _}
import play.api.mvc.Action
import scala.concurrent.ExecutionContext

@Singleton
class ApplicationHome @Inject()(implicit ec : ExecutionContext) extends Controller {

  def indexPage() : Action[AnyContent] = Action { implicit req =>
    Logger.info("Rendering Index page")
    Ok
  }
}