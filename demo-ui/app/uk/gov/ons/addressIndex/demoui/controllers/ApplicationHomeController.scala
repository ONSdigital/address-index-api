package uk.gov.ons.addressIndex.demoui.controllers

import javax.inject.{Inject, Singleton}
import play.Logger
import play.api.mvc.{Controller, AnyContent, Action, Result}
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import play.api.i18n.{I18nSupport, Messages, MessagesApi}

@Singleton
class ApplicationHomeController @Inject()(conf : DemouiConfigModule, val messagesApi: MessagesApi)(implicit ec : ExecutionContext) extends Controller with I18nSupport {

    def indexPage() : Action[AnyContent] = Action.async { implicit req =>
      Logger.info("ApplicationHome: Rendering Index page")
      Future.successful(
        Ok(uk.gov.ons.addressIndex.demoui.views.html.index())
      )
  }

}