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
class SingleMatch @Inject()(implicit ec : ExecutionContext, val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def showSingleMatchPage() : Action[AnyContent] = Action { implicit req =>
    Logger.info("Rendering Single Match Page")
    req.getQueryString("lang") match{
      case Some(lang) =>  messagesApi.setLang(Ok(uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
        SingleMatch.singleSearchForm)(messagesApi,Lang(lang))),Lang(lang))
      case None => messagesApi.setLang(Ok(uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
        SingleMatch.singleSearchForm)(messagesApi,Lang("en"))),Lang("en"))
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


  /**
  def doSingleMatch() = Action.async { implicit request =>
  val address  = request.body.asFormUrlEncoded.get("address").mkString
   val inputAddress = address
    if (inputAddress.trim.isEmpty) {
      logger.debug("Empty input address")
      Future.successful(Ok(
        views.html.singleMatch(
          None,
          SingleMatch.singleSearchForm,
          Some("Please provide an address for matching!"),
          onsFrontendConfiguration
        )
      )
      )
    } else {
      logger.debug("Calling Single match api")
      val result = addressApiClient.singleMatch(inputAddress)
      result.map(a => Ok(
        views.html.singleMatch(
          Some(a),
          SingleMatch.singleSearchForm,
          None,
          onsFrontendConfiguration
        )
      )
      )
    }
  }
}
**/

