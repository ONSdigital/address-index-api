package uk.gov.ons.addressIndex.demoui.controllers

import javax.inject.{Inject, Singleton}
import play.Logger
import play.api.mvc.{Controller, AnyContent, Action, Result}
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import play.api.i18n.{I18nSupport, Messages, MessagesApi, Lang}

@Singleton
class ApplicationHomeController @Inject()(conf : DemouiConfigModule, val messagesApi: MessagesApi)(implicit ec : ExecutionContext) extends Controller with I18nSupport {

    def indexPage(language: Option[String]) : Action[AnyContent] = Action { implicit req =>
      Logger.info("ApplicationHome: Rendering Index page")
      // Get language from Config file rather than req.acceptLanguages
      val defaultLanguage = conf.config.defaultLanguage
      val lang = language.getOrElse(defaultLanguage)
      Logger.info("ApplicationHome: Language =  " + defaultLanguage)
      messagesApi.setLang(Ok(uk.gov.ons.addressIndex.demoui.views.html.index()),Lang(lang))
  }

}