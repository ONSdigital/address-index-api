package uk.gov.ons.addressIndex.demoui.controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}

import play.{Configuration, Logger}
import play.api.mvc.{Action, AnyContent, Controller}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import play.api.i18n.{I18nSupport, Lang, Messages, MessagesApi}
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.demoui.model._
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import uk.gov.ons.addressIndex.model.{AddressIndexSearchRequest, PostcodeAddressFile}
import uk.gov.ons.addressIndex.model.AddressScheme._
import uk.gov.ons.addressIndex.model.server.response.AddressBySearchResponseContainer

@Singleton
class SingleMatch @Inject()(
   conf : DemouiConfigModule,
   val messagesApi: MessagesApi,
   apiClient: AddressIndexClientInstance
)(implicit ec: ExecutionContext) extends Controller with I18nSupport {

  def showSingleMatchPage() : Action[AnyContent] = Action { implicit req =>
    Logger.info("SingleMatch: Rendering Single Match Page")
    // Get language from Config file rather than req.acceptLanguages
    val defaultLanguage = conf.config.defaultLanguage
    Logger.info("SingleMatch: Default Language =  " + defaultLanguage)
    val lang = req.getQueryString("lang").getOrElse(defaultLanguage)
    messagesApi.setLang(Ok(uk.gov.ons.addressIndex.demoui.views.html.singleMatch(SingleMatch.form,
      None,
      None)),
    Lang(lang))
  }

  def doMatch() : Action[AnyContent] = Action.async { implicit request =>
    val addressText = request.body.asFormUrlEncoded.get("address").mkString
    val defaultLanguage = conf.config.defaultLanguage
    val lang = request.getQueryString("lang").getOrElse(defaultLanguage)
    if (addressText.trim.isEmpty) {
      Logger.debug("Empty input address")
      Future.successful(
        messagesApi.setLang(Ok(uk.gov.ons.addressIndex.demoui.views.html.singleMatch(SingleMatch.form,
          Some(messagesApi("single.pleasesupply")),
          None)),
          Lang(lang))
      )
    } else {
      Future.successful(
        Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.SingleMatch.doMatchQS(addressText))
      )
    }
  }

  def doMatchQS(input : String) : Action[AnyContent] = Action.async { implicit request =>
    val addressText = input
    val defaultLanguage = conf.config.defaultLanguage
    val lang = request.getQueryString("lang").getOrElse(defaultLanguage)
     if (addressText.trim.isEmpty) {
      Logger.debug("Empty input address")
      Future.successful(
        messagesApi.setLang(Ok(uk.gov.ons.addressIndex.demoui.views.html.singleMatch(SingleMatch.form,
          Some(messagesApi("single.pleasesupply")),
          None)),
        Lang(lang))
      )
    } else {
      apiClient.addressQuery(
        AddressIndexSearchRequest(
          format = PostcodeAddressFile("paf"),
          input = addressText,
          id = UUID.randomUUID
        )
      ) map { resp: AddressBySearchResponseContainer =>
        messagesApi.setLang(Ok(uk.gov.ons.addressIndex.demoui.views.html.singleMatch(SingleMatch.form.fill(new SingleSearchForm(addressText,"paf")),
          None,
          Some(resp.response))),
        Lang(lang))
      }
    }
  }
}



object SingleMatch {
  val form = Form(
    mapping(
      "address" -> text,
      "format" -> text
    )(SingleSearchForm.apply)(SingleSearchForm.unapply)
  )
}

