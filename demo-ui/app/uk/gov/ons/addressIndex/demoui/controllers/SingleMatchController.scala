package uk.gov.ons.addressIndex.demoui.controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}
import play.api.Logger
import scala.language.implicitConversions
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.demoui.model._
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import uk.gov.ons.addressIndex.demoui.utils.ClassHierarchy
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.ons.addressIndex.model.{AddressIndexSearchRequest, PostcodeAddressFile}
import uk.gov.ons.addressIndex.model.server.response.Container

case class BulkRequest(inputs: Seq[String])
object BulkRequest {
  implicit lazy val fmts = Json.format[BulkRequest]
}

/**
  * Controller class for a single address to be matched
  * @param conf
  * @param messagesApi
  * @param apiClient
  * @param ec
  */
@Singleton
class SingleMatchController @Inject()(
   conf : DemouiConfigModule,
   val messagesApi: MessagesApi,
   apiClient: AddressIndexClientInstance,
   classHierarchy: ClassHierarchy
)(implicit ec: ExecutionContext) extends Controller with I18nSupport {

  val logger = Logger("SingleMatchController")

  /**
    * Present empty form for user to input address
    * @return result to view
    */
  def showSingleMatchPage() : Action[AnyContent] = Action.async { implicit request =>
    logger info("SingleMatch: Rendering Single Match Page")
    Future.successful(
      Ok(
        uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
          singleSearchForm = SingleSearchForm.form,
          warningMessage = None,
          addressBySearchResponse = None,
          classHierarchy = None
        )
      )
    )
  }

  /**
    * Accept posted form, deal with empty address or pass on to MatchWithInput
    * @return result to view or redirect
    */
  def doMatch() : Action[AnyContent] = Action.async { implicit request =>
    val addressText = Option(request.body.asFormUrlEncoded.get("address").mkString).getOrElse("")
    if (addressText.trim.isEmpty) {
      logger info("Single Match with Empty input address")
      Future.successful(
        Ok(
          uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
            singleSearchForm = SingleSearchForm.form,
            warningMessage = Some(messagesApi("single.pleasesupply")),
            addressBySearchResponse = None,
            classHierarchy = None
          )
        )
      )
    } else {
      Future.successful(
        Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.SingleMatchController.doMatchWithInput(addressText))
      )
    }
  }

  /**
    * Perform match by calling API with address string. Can be called directly via get or redirect from form
    * @param input
    * @return result to view
    */
  def doMatchWithInput(input : String) : Action[AnyContent] = Action.async { implicit request =>
    val addressText = input
    if (addressText.trim.isEmpty) {
      logger info("Single Match with expected input address missing")
      Future.successful(
        Ok(
          uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
            singleSearchForm = SingleSearchForm.form,
            warningMessage = Some(messagesApi("single.pleasesupply")),
            addressBySearchResponse = None,
            classHierarchy = None
          )
        )
      )
    } else {
      logger info("Single Match with supplied input address " + addressText)
      apiClient.addressQuery(
        AddressIndexSearchRequest(
          format = None,
          input = addressText,
          limit = "10",
          offset = "0",
          id = UUID.randomUUID
        )
      ) map { resp: Container =>
        Ok(
          uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
            singleSearchForm = SingleSearchForm.form.fill(SingleSearchForm(addressText)),
            warningMessage = None,
            addressBySearchResponse = resp.response,
            classHierarchy = Some(classHierarchy)
          )
        )
      }
    }
  }

  def doMatchWithBulk(): Action[BulkRequest] = Action.async(parse.json[BulkRequest]) { implicit request =>
    logger info "doMatchWithBulk"
    apiClient.addressQueriesBulkMimic(
      requests = request.body.inputs map { input =>
        AddressIndexSearchRequest(
          format = Some(PostcodeAddressFile("paf")),
          input = input,
          limit = "10",
          offset = "0",
          id = UUID.randomUUID
        )
      }
    ).map(resp => Ok(Json toJson resp))
  }
}