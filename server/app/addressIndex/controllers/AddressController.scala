package addressIndex.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent}
import scala.concurrent.Future

@Singleton
class AddressController @Inject()
  extends AddressIndexController {

  def addressQuery(
    format : String
  ): Action[AnyContent] = Action async { implicit req =>
    Future successful NotImplemented
  }


  def uprnQuery(
    uprn   : String,
    format : String
  ): Action[AnyContent] = Action async { implicit req =>
    Future successful NotImplemented
  }

}