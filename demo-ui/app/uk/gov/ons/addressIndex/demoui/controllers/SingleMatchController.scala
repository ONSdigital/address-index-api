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
class SingleMatchController @Inject()(
   conf : DemouiConfigModule,
   val messagesApi: MessagesApi,
   apiClient: AddressIndexClientInstance
)(implicit ec: ExecutionContext) extends Controller with I18nSupport {

  def showSingleMatchPage() : Action[AnyContent] = Action.async { implicit request =>
    Logger.info("SingleMatch: Rendering Single Match Page")
    // Get language from Config file rather than req.acceptLanguages
    val defaultLanguage = conf.config.defaultLanguage
    val lang = request.session.get("userlang").getOrElse {defaultLanguage}
    val qlang = request.getQueryString("lang").getOrElse(lang)
    Logger.info("SingleMatch: Language =  " + qlang)
    Logger.debug("Empty input address")
    val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
      singleSearchForm = SingleMatchController.form,
      warningMessage = None,
      addressBySearchResponse = None)
    Future.successful(
      messagesApi.setLang(Ok(viewToRender), Lang(qlang)).withSession("userlang" -> qlang)
    )
  }

  def doMatch() : Action[AnyContent] = Action.async { implicit request =>
    // todo try (parse.tolerantJson)
    val addressText = request.body.asFormUrlEncoded.get("address").mkString
    val defaultLanguage = conf.config.defaultLanguage
    val lang = request.session.get("userlang").getOrElse {defaultLanguage}
    val qlang = request.getQueryString("lang").getOrElse(lang)
    Logger.info("SingleMatch: Language =  " + qlang)
        if (addressText.trim.isEmpty) {
      Logger.debug("Empty input address")
      val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
        singleSearchForm = SingleMatchController.form,
        warningMessage = Some(messagesApi("single.pleasesupply")),
        addressBySearchResponse = None)
      Future.successful(
        messagesApi.setLang(Ok(viewToRender), Lang(qlang)).withSession("userlang" -> qlang)
      )
    } else {
      Future.successful(
        Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.SingleMatchController.doMatchWithInput(addressText))
      )
    }
  }

  def doMatchWithInput(input : String) : Action[AnyContent] = Action.async { implicit request =>
    val addressText = input
    val defaultLanguage = conf.config.defaultLanguage
    val lang = request.session.get("userlang").getOrElse {defaultLanguage}
    val qlang = request.getQueryString("lang").getOrElse(lang)
    Logger.info("SingleMatch: Language =  " + qlang)
    if (addressText.trim.isEmpty) {
      Logger.debug("Empty input address")
      val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
        singleSearchForm = SingleMatchController.form,
        warningMessage = Some(messagesApi("single.pleasesupply")),
        addressBySearchResponse = None)
        Future.successful(
          messagesApi.setLang(Ok(viewToRender), Lang(qlang)).withSession("userlang" -> qlang)
      )
    } else {
      apiClient.addressQuery(
        AddressIndexSearchRequest(
          format = PostcodeAddressFile("paf"),
          input = addressText,
          id = UUID.randomUUID
        )
      ) map { resp: AddressBySearchResponseContainer =>
        val filledForm = SingleMatchController.form.fill(SingleSearchForm(addressText,"paf"))
        val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
          singleSearchForm = filledForm,
          warningMessage = None,
          addressBySearchResponse = Some(resp.response))
        messagesApi.setLang(Ok(viewToRender), Lang(qlang)).withSession("userlang" -> qlang)
      }
    }
  }
}



object SingleMatchController {
  val form = Form(
    mapping(
      "address" -> text,
      "format" -> text
    )(SingleSearchForm.apply)(SingleSearchForm.unapply)
  )
}

