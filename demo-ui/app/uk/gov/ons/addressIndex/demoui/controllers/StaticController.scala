package uk.gov.ons.addressIndex.demoui.controllers

import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.ons.addressIndex.demoui.modules.DemoUIAddressIndexVersionModule

@Singleton
class StaticController @Inject()(
  val controllerComponents: ControllerComponents,
  override val messagesApi: MessagesApi,
  version: DemoUIAddressIndexVersionModule
 ) extends BaseController with I18nSupport{
  val logger = Logger("ApplicationHomeController")
  def help: Action[AnyContent] = Action { implicit request =>
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

  def helpForgotPassword: Action[AnyContent] = Action { implicit request =>
    val chosenLang = messagesApi.preferred(request).lang.code
    if (chosenLang == "cy") {
      Ok(uk.gov.ons.addressIndex.demoui.views.html.helpContainer(content = "forgotPassword", version = version, userLang = "cy"))
    } else {
      Ok(uk.gov.ons.addressIndex.demoui.views.html.helpContainer(content = "forgotPassword", version = version, userLang = "en"))
    }
  }

  def devLanding: Action[AnyContent] = Action { implicit request =>
    val chosenLang = messagesApi.preferred(request).lang.code
    if (chosenLang == "cy") {
      Ok(uk.gov.ons.addressIndex.demoui.views.html.devContainer(content = "landing", version = version, userLang = "cy"))
    } else {
      Ok(uk.gov.ons.addressIndex.demoui.views.html.devContainer(content = "landing", version = version, userLang = "en"))
    }
  }

  def devTypeAhead: Action[AnyContent] = Action { implicit request =>
    val chosenLang = messagesApi.preferred(request).lang.code
    if (chosenLang == "cy") {
      Ok(uk.gov.ons.addressIndex.demoui.views.html.devContainer(content = "typeAhead", version = version, userLang = "cy"))
    } else {
      Ok(uk.gov.ons.addressIndex.demoui.views.html.devContainer(content = "typeAhead", version = version, userLang = "en"))
    }
  }

  def devVersion: Action[AnyContent] = Action { implicit request =>
    val chosenLang = messagesApi.preferred(request).lang.code
    if (chosenLang == "cy") {
      Ok(uk.gov.ons.addressIndex.demoui.views.html.devContainer(content = "version", version = version, userLang = "cy"))
    } else {
      Ok(uk.gov.ons.addressIndex.demoui.views.html.devContainer(content = "version", version = version, userLang = "en"))
    }
  }

  def devSingleMatch: Action[AnyContent] = Action { implicit request =>
    val chosenLang = messagesApi.preferred(request).lang.code
    if (chosenLang == "cy") {
      Ok(uk.gov.ons.addressIndex.demoui.views.html.devContainer(content = "single", version = version, userLang = "cy"))
    } else {
      Ok(uk.gov.ons.addressIndex.demoui.views.html.devContainer(content = "single", version = version, userLang = "en"))
    }
  }

  def devUprn: Action[AnyContent] = Action { implicit request =>
    val chosenLang = messagesApi.preferred(request).lang.code
    if (chosenLang == "cy") {
      Ok(uk.gov.ons.addressIndex.demoui.views.html.devContainer(content = "uprn", version = version, userLang = "cy"))
    } else {
      Ok(uk.gov.ons.addressIndex.demoui.views.html.devContainer(content = "uprn", version = version, userLang = "en"))
    }
  }

  def devPostcode: Action[AnyContent] = Action { implicit request =>
    val chosenLang = messagesApi.preferred(request).lang.code
    if (chosenLang == "cy") {
      Ok(uk.gov.ons.addressIndex.demoui.views.html.devContainer(content = "postcode", version = version, userLang = "cy"))
    } else {
      Ok(uk.gov.ons.addressIndex.demoui.views.html.devContainer(content = "postcode", version = version, userLang = "en"))
    }
  }

}
