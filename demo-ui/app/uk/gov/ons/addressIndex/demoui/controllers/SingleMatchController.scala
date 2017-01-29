package uk.gov.ons.addressIndex.demoui.controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.demoui.model._
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import uk.gov.ons.addressIndex.demoui.utils.ClassHierarchy
import uk.gov.ons.addressIndex.model.server.response.AddressBySearchResponseContainer
import uk.gov.ons.addressIndex.model.AddressIndexSearchRequest

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.Try

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
  val pageSize = conf.config.limit
  val maxOff = conf.config.maxOffset
  val maxPages = (maxOff + pageSize - 1) / pageSize

  /**
    * Present empty form for user to input address
    * @return result to view
    */
  def showSingleMatchPage() : Action[AnyContent] = Action.async { implicit request =>
    logger info("SingleMatch: Rendering Single Match Page")
    val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
      singleSearchForm = SingleMatchController.form,
      warningMessage = None,
      addressFormat = "paf",
      pageNum = 1,
      pageSize = pageSize,
      pageMax = maxPages,
      addressBySearchResponse = None,
      classification = None)
    Future.successful(
        Ok(viewToRender)
    )
  }

  /**
    * Accept posted form, deal with empty address or pass on to MatchWithInput
    * @return result to view or redirect
    */
  def doMatch() : Action[AnyContent] = Action.async { implicit request =>
    val addressText = Option(request.body.asFormUrlEncoded.get("address").mkString).getOrElse("")
    val optFormat: Option[String] = Try(request.body.asFormUrlEncoded.get("format").mkString).toOption
    val addressFormat = optFormat.getOrElse("paf")
    logger info("Single Match with address format = " + addressFormat)
    if (addressText.trim.isEmpty) {
      logger info("Single Match with Empty input address")
      val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
        singleSearchForm = SingleMatchController.form,
        warningMessage = Some(messagesApi("single.pleasesupply")),
        addressFormat = addressFormat,
        pageNum = 1,
        pageSize = pageSize,
        pageMax = maxPages,
        addressBySearchResponse = None,
        classification = None)
      Future.successful(
        Ok(viewToRender)
      )
    } else {
      Future.successful(
        Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.SingleMatchController.doMatchWithInput(addressText,addressFormat, Some(1)))
      )
    }
  }

  /**
    * Perform match by calling API with address string. Can be called directly via get or redirect from form
    * @param input
    * @return result to view
    */
  def doMatchWithInput(input : String, formatText: String, page: Option[Int]) : Action[AnyContent] = Action.async { implicit request =>
    val addressText = input
    val limit = pageSize.toString()
    logger info("Limit param = " + limit)
    val pageNum = page.getOrElse(1)
    val offNum = (pageNum - 1) * pageSize
    val offset = offNum.toString
    logger info("Offset param = " + offset)
    logger info("Max pages = " + maxPages)
    if (addressText.trim.isEmpty) {
      logger info("Single Match with expected input address missing")
      val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
        singleSearchForm = SingleMatchController.form,
        warningMessage = Some(messagesApi("single.pleasesupply")),
        addressFormat = formatText,
        pageNum = 1,
        pageSize = pageSize,
        pageMax = maxPages,
        addressBySearchResponse = None,
        classification = None)
        Future.successful(
          Ok(viewToRender)
      )
    } else {
      logger info("Single Match with supplied input address " + addressText)
      apiClient.addressQuery(
        AddressIndexSearchRequest(
          input = addressText,
          limit = limit,
          offset = offset,
          id = UUID.randomUUID
        )
      ) map { resp: AddressBySearchResponseContainer =>
        val filledForm = SingleMatchController.form.fill(SingleSearchForm(addressText,formatText))

        val nags = resp.response.addresses.flatMap(_.nag)
        val classCodes: Map[String, String] = nags.map(nag =>
          (nag.uprn , classHierarchy.analyseClassCode(nag.classificationCode))).toMap

        val warningMessage =
          if (resp.status.code == 200) None
          else Some(s"${resp.status.code} ${resp.status.message} : ${resp.errors.headOption.mkString}")


        val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
          singleSearchForm = filledForm,
          warningMessage = warningMessage,
          addressFormat = formatText,
          pageNum = pageNum,
          pageSize = pageSize,
          pageMax = maxPages,
          addressBySearchResponse = Some(resp.response),
          classification = Some(classCodes))
        Ok(viewToRender)
      }
    }
  }

  def doMatchWithBulk(): Action[BulkRequest] = Action.async(parse.json[BulkRequest]) { implicit request =>
    logger info "doMatchWithBulk"
    apiClient.addressQueriesBulkMimic(
      requests = request.body.inputs map { input =>
        AddressIndexSearchRequest(
          input = input,
          limit = "10",
          offset = "0",
          id = UUID.randomUUID
        )
      }
    ).map(resp => Ok(Json toJson resp))
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