package uk.gov.ons.addressIndex.demoui.controllers

import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi, Lang, Langs}
import play.api.mvc.{Action, AnyContent, Controller}
import uk.gov.ons.addressIndex.demoui.modules.DemoUIAddressIndexVersionModule

@Singleton
class StaticController @Inject()(
  val messagesApi: MessagesApi,
  version: DemoUIAddressIndexVersionModule
 ) extends Controller with I18nSupport{
  val logger = Logger("ApplicationHomeController")
  def help: Action[AnyContent] = Action { implicit request =>
   // logger.info("language = " + langs.availables(0).code)
    val chosenLang = messagesApi.preferred(request).lang.code
    if (chosenLang == "cy") {
      Ok(
        uk.gov.ons.addressIndex.demoui.views.html.helpcy(version)
      )
    } else {
      Ok(
        uk.gov.ons.addressIndex.demoui.views.html.help(version)
      )
    }
  }

}
