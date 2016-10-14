package addressIndex.controllers.general

import javax.inject.{Inject, Singleton}
import addressIndex.controllers.AddressIndexController
import play.api.mvc.{Action, AnyContent}

@Singleton
class ApplicationController @Inject() extends AddressIndexController {

  def index(): Action[AnyContent] = Action { implicit req =>
    Ok("hi")
  }
}