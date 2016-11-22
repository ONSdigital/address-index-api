package uk.gov.ons.addressIndex.demoui.controllers

import javax.inject.{Inject, Singleton}
import play.Logger
import play.api.mvc.{Controller, _}
import play.api.mvc.Action
import scala.concurrent.ExecutionContext
import scala.language.implicitConversions
import play.api.i18n.{I18nSupport, Messages, MessagesApi, Lang}

@Singleton
class ApplicationHome @Inject()(val messagesApi: MessagesApi)(implicit ec : ExecutionContext) extends Controller with I18nSupport {

    def indexPage() : Action[AnyContent] = Action { implicit req =>
      Logger.info("Rendering Index page")
      req.getQueryString("lang") match{
        case Some(lang) =>  messagesApi.setLang(Ok(uk.gov.ons.addressIndex.demoui.views.html.index()),Lang(lang))
        case None => messagesApi.setLang(Ok(uk.gov.ons.addressIndex.demoui.views.html.index()),Lang("en"))
    }
  }

}