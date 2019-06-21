package uk.gov.ons.addressIndex.demoui.controllers

import javax.inject.{Inject, Singleton}
import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.ons.addressIndex.demoui.views
import uk.gov.ons.addressIndex.demoui.modules.{DemoUIAddressIndexVersionModule, DemouiConfigModule}

@Singleton
class StaticController @Inject()(val controllerComponents: ControllerComponents,
                                 override val messagesApi: MessagesApi,
                                 version: DemoUIAddressIndexVersionModule,
                                 conf: DemouiConfigModule
                                ) extends BaseController with I18nSupport {
  val logger = Logger("ApplicationHomeController")

  def getChosenLang(implicit request: RequestHeader): String = messagesApi.preferred(request).lang.code match {
    case "cy" => "cy"
    case _ => "en"
  }

  def help: Action[AnyContent] = Action { implicit request =>
    if (getChosenLang == "cy") {
      Ok(views.html.helpcy(version))
    } else {
      Ok(views.html.help(version))
    }
  }

  def helpForgotPassword: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.helpContainer(content = "forgotPassword", version = version, userLang = getChosenLang))
  }

  def devLanding: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.devContainer(content = "landing", version = version, userLang = getChosenLang, conf = conf))
  }

  def devTypeAhead: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.devContainer(content = "typeAhead", version = version, userLang = getChosenLang, conf = conf))
  }

  def devVersion: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.devContainer(content = "version", version = version, userLang = getChosenLang, conf = conf))
  }

  def devSingleMatch: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.devContainer(content = "single", version = version, userLang = getChosenLang, conf = conf))
  }

  def devUprn: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.devContainer(content = "uprn", version = version, userLang = getChosenLang, conf = conf))
  }

  def devPostcode: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.devContainer(content = "postcode", version = version, userLang = getChosenLang, conf = conf))
  }
}
