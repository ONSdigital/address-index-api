package uk.gov.ons.addressIndex.demoui.controllers

import javax.inject.{Inject, Singleton}
import play.Logger
import play.api.mvc.{Controller, _}
import play.api.mvc.Action
import scala.concurrent.ExecutionContext
import scala.language.implicitConversions
import play.api.i18n.{I18nSupport, Messages, MessagesApi, Lang}
import play.api.data.Forms._
import play.api.data._
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.demoui.model._
import uk.gov.ons.addressIndex.demoui.views


@Singleton
class SingleMatch @Inject() (val messagesApi: MessagesApi)(implicit ec : ExecutionContext) extends Controller with I18nSupport {

  def showSingleMatchPage() : Action[AnyContent] = Action { implicit req =>
    Logger.info("Rendering Single Match Page")
    req.getQueryString("lang") match{
      case Some(lang) =>  messagesApi.setLang(Ok(uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
        SingleMatch.singleSearchForm)),Lang(lang))
      case None => messagesApi.setLang(Ok(uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
        SingleMatch.singleSearchForm)),Lang("en"))
    }
  }

  def doSingleMatch() = Action { implicit request =>
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

