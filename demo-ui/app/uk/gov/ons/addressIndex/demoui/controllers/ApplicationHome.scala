package uk.gov.ons.addressIndex.demoui.controllers

import javax.inject.{Inject, Singleton}
import play.Logger
import play.api.mvc.{Controller, AnyContent, Action}
import play.Configuration
import scala.concurrent.ExecutionContext
import scala.language.implicitConversions
import play.api.i18n.{I18nSupport, Messages, MessagesApi, Lang}

@Singleton
class ApplicationHome @Inject()(val configurataion : Configuration, val messagesApi: MessagesApi)(implicit ec : ExecutionContext) extends Controller with I18nSupport {

    def indexPage() : Action[AnyContent] = Action { implicit req =>
      Logger.info("ApplicationHome: Rendering Index page")
      // Get language from Config file rather than req.acceptLanguages
      val defaultLanguage = configurataion.getString("demoui.defaultLanguage")
        Logger.info("ApplicationHome: Default Language =  " + defaultLanguage)
      val lang = req.getQueryString("lang").getOrElse(defaultLanguage)
      messagesApi.setLang(Ok(uk.gov.ons.addressIndex.demoui.views.html.index()),Lang(lang))
  }

}