package uk.gov.ons.addressIndex.demoui.controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}

import play.{Logger}
import play.api.mvc.{Action, AnyContent, Controller}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.data.Forms._
import play.api.data._
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.demoui.model._
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import uk.gov.ons.addressIndex.model.{AddressIndexSearchRequest, PostcodeAddressFile}
import uk.gov.ons.addressIndex.model.server.response.AddressBySearchResponseContainer

@Singleton
class SingleMatchController @Inject()(
   conf : DemouiConfigModule,
   val messagesApi: MessagesApi,
   apiClient: AddressIndexClientInstance
)(implicit ec: ExecutionContext) extends Controller with I18nSupport {

  def showSingleMatchPage() : Action[AnyContent] = Action.async { implicit request =>
    Logger.info("SingleMatch: Rendering Single Match Page")
    val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
      singleSearchForm = SingleMatchController.form,
      warningMessage = None,
      addressBySearchResponse = None)
    Future.successful(
        Ok(viewToRender)
    )
  }

  def doMatch() : Action[AnyContent] = Action.async { implicit request =>
    val addressText = request.body.asFormUrlEncoded.get("address").mkString
    if (addressText.trim.isEmpty) {
      Logger.info("Single Match with Empty input address")
      val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
        singleSearchForm = SingleMatchController.form,
        warningMessage = Some(messagesApi("single.pleasesupply")),
        addressBySearchResponse = None)
      Future.successful(
        Ok(viewToRender)
      )
    } else {
      Future.successful(
        Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.SingleMatchController.doMatchWithInput(addressText))
      )
    }
  }

  def doMatchWithInput(input : String) : Action[AnyContent] = Action.async { implicit request =>
    val addressText = input
    if (addressText.trim.isEmpty) {
      Logger.info("Single Match with expected input address missing")
      val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
        singleSearchForm = SingleMatchController.form,
        warningMessage = Some(messagesApi("single.pleasesupply")),
        addressBySearchResponse = None)
        Future.successful(
          Ok(viewToRender)
      )
    } else {
      Logger.info("Single Match with supplied input address" + addressText)
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
        Ok(viewToRender)
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

