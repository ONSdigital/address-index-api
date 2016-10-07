package addressIndex.controllers

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.mvc.{Action, AnyContent}
import scala.concurrent.Future

@Singleton
class AddressController @Inject()
  extends AddressIndexController {

  def addressQuery(
    format : String,
    input  : String
  ): Action[AnyContent] = Action async { implicit req =>
    Logger("address-index:AddressController").info("#addressQuery called")
    Future successful Ok
  }


  def uprnQuery(
    uprn   : String,
    format : String
  ): Action[AnyContent] = Action async { implicit req =>
    Logger("address-index:AddressController").info("#uprnQuery called")
    Future successful Ok
  }
}