package uk.gov.ons.addressIndex.demoui.controllers

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.mvc.{Action, AnyContent, Controller}
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import play.api.i18n.{I18nSupport, MessagesApi}

/**
  * Simple controller for home page
  * @param conf
  * @param messagesApi
  * @param ec
  */
@Singleton
class ApplicationHomeController @Inject()(conf : DemouiConfigModule, val messagesApi: MessagesApi)(implicit ec : ExecutionContext) extends Controller with I18nSupport {

  val logger = Logger("ApplicationHomeController")

  /**
    * Render index page
    * @return result to view
    */
  def indexPage() : Action[AnyContent] = Action.async { implicit req =>
    logger info("ApplicationHome: Rendering Index page")
    Future.successful(
      Ok(uk.gov.ons.addressIndex.demoui.views.html.index())
    )
  }

}