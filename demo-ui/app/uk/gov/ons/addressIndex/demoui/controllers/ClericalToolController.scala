package uk.gov.ons.addressIndex.demoui.controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, Controller}
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.demoui.model._
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import uk.gov.ons.addressIndex.demoui.utils.ClassHierarchy
import uk.gov.ons.addressIndex.model.server.response.{AddressBySearchResponseContainer, AddressByUprnResponseContainer}
import uk.gov.ons.addressIndex.model.{AddressIndexSearchRequest, AddressIndexUPRNRequest}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.Try

/**
  * Controller class for a single address to be matched
  * @param conf
  * @param messagesApi
  * @param apiClient
  * @param ec
  */
@Singleton
class ClericalToolController @Inject()(
                                       conf : DemouiConfigModule,
                                       val messagesApi: MessagesApi,
                                       apiClient: AddressIndexClientInstance,
                                       classHierarchy: ClassHierarchy
                                     )(implicit ec: ExecutionContext) extends Controller with I18nSupport {

  val logger = Logger("ClericalToolController")
  val pageSize = conf.config.limit
  val maxOff = conf.config.maxOffset
  val maxPages = (maxOff + pageSize - 1) / pageSize

  /**
    * Present empty form for user to input address
    *
    * @return result to view
    */
  def showSingleMatchPage(): Action[AnyContent] = Action.async { implicit request =>
    logger info ("Clerial Tool: Rendering Single Match Page")
    val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.clericalTool(
      singleSearchForm = SingleMatchController.form,
      warningMessage = None,
      pageNum = 1,
      pageSize = pageSize,
      pageMax = maxPages,
      expandRow = -1,
      addressBySearchResponse = None,
      classification = None)
    Future.successful(
      Ok(viewToRender)
    )
  }

  /**
    * Accept posted form, deal with empty address or pass on to MatchWithInput
    *
    * @return result to view or redirect
    */
  def doMatch(): Action[AnyContent] = Action { implicit request =>
    val optAddress: Option[String] = Try(request.body.asFormUrlEncoded.get("address").mkString).toOption
    val addressText = optAddress.getOrElse("")
    if (addressText.trim.isEmpty) {
      logger info ("Clerical Tool with Empty input address")
      val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.clericalTool(
        singleSearchForm = SingleMatchController.form,
        warningMessage = Some(messagesApi("single.pleasesupply")),
        pageNum = 1,
        pageSize = pageSize,
        pageMax = maxPages,
        expandRow = -1,
        addressBySearchResponse = None,
        classification = None)
        Ok(viewToRender)
    } else if (Try(addressText.toLong).isSuccess) {
        Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.doUprnWithInput(addressText.toLong))
    } else {
        Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.doMatchWithInput(addressText, Some(1), Some(-1)))
    }
  }

  /**
    * Perform match by calling API with address string. Can be called directly via get or redirect from form
    *
    * @param input
    * @return result to view
    */
  def doMatchWithInput(input: String, page: Option[Int], expand: Option[Int]): Action[AnyContent] = Action.async { implicit request =>
    val addressText = input
    val expandr = expand.getOrElse(-1)
    logger info ("expand param = " + expandr)
    val limit = pageSize.toString()
    logger info ("Limit param = " + limit)
    val pageNum = page.getOrElse(1)
    val offNum = (pageNum - 1) * pageSize
    val offset = offNum.toString
    logger info ("Offset param = " + offset)
    logger info ("Max pages = " + maxPages)
    if (addressText.trim.isEmpty) {
      logger info ("Clerical Tool with expected input address missing")
      val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.clericalTool(
        singleSearchForm = SingleMatchController.form,
        warningMessage = Some(messagesApi("single.pleasesupply")),
        pageNum = 1,
        pageSize = pageSize,
        pageMax = maxPages,
        expandRow = -1,
        addressBySearchResponse = None,
        classification = None)
      Future.successful(
        Ok(viewToRender)
      )
    } else {
      logger info ("Clerical Tool with supplied input address " + addressText)
      apiClient.addressQuery(
        AddressIndexSearchRequest(
          input = addressText,
          limit = limit,
          offset = offset,
          id = UUID.randomUUID
        )
      ) map { resp: AddressBySearchResponseContainer =>
        val filledForm = SingleMatchController.form.fill(SingleSearchForm(addressText))

        val nags = resp.response.addresses.flatMap(_.nag)
        val classCodes: Map[String, String] = nags.map(nag =>
          (nag.uprn, classHierarchy.analyseClassCode(nag.classificationCode))).toMap

        val warningMessage =
          if (resp.status.code == 200) None
          else Some(s"${resp.status.code} ${resp.status.message} : ${resp.errors.headOption.map(_.message).getOrElse("")}")


        val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.clericalTool(
          singleSearchForm = filledForm,
          warningMessage = warningMessage,
          pageNum = pageNum,
          pageSize = pageSize,
          pageMax = maxPages,
          expandRow = expandr,
          addressBySearchResponse = Some(resp.response),
          classification = Some(classCodes))
        Ok(viewToRender)
      }
    }
  }

  /**
    * Perform match by calling API with address string. Can be called directly via get or redirect from form
    * @param input
    * @return result to view
    */
  def doUprnWithInput(input : Long) : Action[AnyContent] = Action.async { implicit request =>
      logger info("UPRN with supplied input address " + input)
      apiClient.uprnQuery(
        AddressIndexUPRNRequest(
          uprn = input,
          id = UUID.randomUUID
        )
      ) map { resp: AddressByUprnResponseContainer =>
        val filledForm = SingleMatchController.form.fill(SingleSearchForm(input.toString))

        val nags = resp.response.address.flatMap(_.nag)
        val classCodes: Map[String, String] = nags.map(nag =>
          (nag.uprn , classHierarchy.analyseClassCode(nag.classificationCode))).toMap

        val warningMessage =
          if (resp.status.code == 200) None
          else Some(s"${resp.status.code} ${resp.status.message} : ${resp.errors.headOption.map(_.message).getOrElse("")}")


        val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.uprnResult(
          singleSearchForm = filledForm,
          warningMessage = warningMessage,
          addressByUprnResponse = Some(resp.response),
          classification = Some(classCodes))
        Ok(viewToRender)
      }
  }

  def showQuery(): Action[AnyContent] = Action {implicit request =>

    val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.queryDebug(
      singleSearchForm = SingleMatchController.form,
      warningMessage = None,
      query = ""
    )

    Ok(viewToRender)

   }

  def doShowQuery(): Action[AnyContent] = Action { implicit request =>

    val input: String = Try(request.body.asFormUrlEncoded.get("address").mkString).getOrElse("")
    Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.showQueryWithInput(input))

  }

  def showQueryWithInput(input: String): Action[AnyContent] = Action.async { implicit request =>

    apiClient.showQuery(input).map{ query =>

      val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.queryDebug(
        singleSearchForm = SingleMatchController.form.fill(SingleSearchForm(input)),
        warningMessage = None,
        query = query
      )

      Ok(viewToRender)
    }
  }
}

object ClericalToolController {
  val form = Form(
    mapping(
      "address" -> text,
      "format" -> text
    )(SingleSearchForm.apply)(SingleSearchForm.unapply)
  )
}
