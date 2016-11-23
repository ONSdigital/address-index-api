package uk.gov.ons.addressIndex.demoui.controllers

import javax.inject.{Inject, Singleton}

import play.{Configuration, Logger}
import play.api.mvc.{Action, AnyContent, Controller}

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.data.Forms._
import play.api.data._
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.demoui.model._
import uk.gov.ons.addressIndex.demoui.views


@Singleton
class SingleMatch @Inject()(val configurataion : Configuration, val messagesApi: MessagesApi)(implicit ec : ExecutionContext) extends Controller with I18nSupport {

  def showSingleMatchPage() : Action[AnyContent] = Action { implicit req =>
    Logger.info("SingleMatch: Rendering Single Match Page")
    // Get language from Config file rather than req.acceptLanguages
    val defaultLanguage = configurataion.getString("demoui.defaultLanguage")
    Logger.info("SingleMatch: Default Language =  " + defaultLanguage)
    val lang = req.getQueryString("lang").getOrElse(defaultLanguage)
    messagesApi.setLang(Ok(uk.gov.ons.addressIndex.demoui.views.html.singleMatch(SingleMatch.singleSearchForm)),Lang(lang))
  }

  def doMatch() = Action { implicit request =>
    val address = request.body.asFormUrlEncoded.get("address").mkString
    Ok("matching address not yet implemented - " + address)
  }

}

object SingleMatch {
  val singleSearchForm = Form(
    mapping(
      "address" -> optional(text)
    )(SingleSearchForm.apply)(SingleSearchForm.unapply)
  )
}

