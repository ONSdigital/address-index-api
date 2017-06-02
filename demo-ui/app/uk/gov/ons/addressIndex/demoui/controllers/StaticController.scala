package uk.gov.ons.addressIndex.demoui.controllers

import javax.inject.{Inject, Singleton}

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Controller}
import uk.gov.ons.addressIndex.demoui.modules.DemoUIAddressIndexVersionModule

@Singleton
class StaticController @Inject()(
  val messagesApi: MessagesApi,
  version: DemoUIAddressIndexVersionModule
 ) extends Controller with I18nSupport{

  def help: Action[AnyContent] = Action { implicit request =>
    Ok(
      uk.gov.ons.addressIndex.demoui.views.html.help(version)
    )
  }

}
