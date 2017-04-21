package uk.gov.ons.addressIndex.demoui.controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}

import org.apache.commons.lang3.StringUtils
import play.api.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Controller}
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.demoui.model._
import uk.gov.ons.addressIndex.demoui.modules.{DemoUIAddressIndexVersionModule, DemouiConfigModule}
import uk.gov.ons.addressIndex.demoui.utils.ClassHierarchy
import uk.gov.ons.addressIndex.model.{AddressIndexSearchRequest, AddressIndexUPRNRequest}
import uk.gov.ons.addressIndex.model.server.response.{AddressBySearchResponseContainer, AddressByUprnResponseContainer}

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
class SingleMatchController @Inject()(
   conf : DemouiConfigModule,
   val messagesApi: MessagesApi,
   apiClient: AddressIndexClientInstance,
   classHierarchy: ClassHierarchy,
   version: DemoUIAddressIndexVersionModule
)(implicit ec: ExecutionContext) extends Controller with I18nSupport {

  val logger = Logger("SingleMatchController")
  val pageSize = conf.config.limit
  val maxOff = conf.config.maxOffset
  val maxPages = (maxOff + pageSize - 1) / pageSize

  /**
    * Present empty form for user to input address
    *
    * @return result to view
    */
  def showSingleMatchPage(): Action[AnyContent] = Action.async { implicit request =>
    logger info ("SingleMatch: Rendering Single Match Page")
    val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
      singleSearchForm = SingleMatchController.form,
      warningMessage = None,
      pageNum = 1,
      pageSize = pageSize,
      pageMax = maxPages,
      addressBySearchResponse = None,
      classification = None,
      version = version)
    Future.successful(
      Ok(viewToRender)
    )
  }

  /**
    * Accept posted form, deal with empty address or pass on to MatchWithInput
    *
    * @return result to view or redirect
    */
  def doMatch(): Action[AnyContent] = Action.async { implicit request =>
    val optAddress: Option[String] = Try(request.body.asFormUrlEncoded.get("address").mkString).toOption
    val addressText = optAddress.getOrElse("")
    if (addressText.trim.isEmpty) {
      logger info ("Single Match with Empty input address")
      val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
        singleSearchForm = SingleMatchController.form,
        warningMessage = Some(messagesApi("single.pleasesupply")),
        pageNum = 1,
        pageSize = pageSize,
        pageMax = maxPages,
        addressBySearchResponse = None,
        classification = None,
        version = version)
      Future.successful(
        Ok(viewToRender)
      )
    } else if (Try(addressText.toLong).isSuccess) {
      Future.successful(
        Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.SingleMatchController.doGetUprn(addressText))
      )
    } else {
      Future.successful(
        Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.SingleMatchController.doMatchWithInput(addressText, Some(1)))
      )
    }
  }

  /**
    * Perform match by calling API with address string. Can be called directly via get or redirect from form
    *
    * @param input
    * @return result to view
    */
  def doMatchWithInput(input: String, page: Option[Int]): Action[AnyContent] = Action.async { implicit request =>

    request.session.get("api-key").map { apiKey =>
      val addressText = StringUtils.stripAccents(input)
      val limit = pageSize.toString()
      logger info ("Limit param = " + limit)
      val pageNum = page.getOrElse(1)
      val offNum = (pageNum - 1) * pageSize
      val offset = offNum.toString
      logger info ("Offset param = " + offset)
      logger info ("Max pages = " + maxPages)
      if (addressText.trim.isEmpty) {
        logger info ("Single Match with expected input address missing")
        val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
          singleSearchForm = SingleMatchController.form,
          warningMessage = Some(messagesApi("single.pleasesupply")),
          pageNum = 1,
          pageSize = pageSize,
          pageMax = maxPages,
          addressBySearchResponse = None,
          classification = None,
        version = version)
        Future.successful(
          Ok(viewToRender)
        )
      } else {
        logger info ("Single Match with supplied input address " + addressText)

        apiClient.addressQuery(
          AddressIndexSearchRequest(
            input = addressText,
            limit = limit,
            offset = offset,
            id = UUID.randomUUID,
            apiKey = apiKey
          )
        ) map { resp: AddressBySearchResponseContainer =>
          val filledForm = SingleMatchController.form.fill(SingleSearchForm(addressText))

          val nags = resp.response.addresses.flatMap(_.nag)
          val classCodes: Map[String, String] = nags.map(nag =>
            (nag.uprn, classHierarchy.analyseClassCode(nag.classificationCode))).toMap

          val warningMessage =
            if (resp.status.code == 200) None
            else Some(s"${resp.status.code} ${resp.status.message} : ${resp.errors.headOption.map(_.message).getOrElse("")}")


        val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
          singleSearchForm = filledForm,
          warningMessage = warningMessage,
          pageNum = pageNum,
          pageSize = pageSize,
          pageMax = maxPages,
          addressBySearchResponse = Some(resp.response),
          classification = Some(classCodes),
        version = version)
          Ok(viewToRender)
        }
      }
    }.getOrElse {
      Future.successful(Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login()))
    }
  }

  /**
    * Perform match by calling API with address string. Can be called directly via get or redirect from form
    *
    * @param input
    * @return result to view
    */
  def doGetUprn(input : String) : Action[AnyContent] = Action.async { implicit request =>
    request.session.get("api-key").map { apiKey =>val addressText = StringUtils.stripAccents(input)
    if (addressText.trim.isEmpty) {
      logger info("UPRN with expected input address missing")
      val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
        singleSearchForm = SingleMatchController.form,
        warningMessage = Some(messagesApi("single.pleasesupply")),
        pageNum = 1,
        pageSize = pageSize,
        pageMax = maxPages,
        addressBySearchResponse = None,
        classification = None,
      version = version)
      Future.successful(
        Ok(viewToRender)
      )
    } else {
      logger info("UPRN with supplied input address " + addressText)
      val numericUPRN = BigInt(addressText)
      apiClient.uprnQuery(
        AddressIndexUPRNRequest(
          uprn = numericUPRN,
          id = UUID.randomUUID,
            apiKey = apiKey
        )
      ) map { resp: AddressByUprnResponseContainer =>
        val filledForm = SingleMatchController.form.fill(SingleSearchForm(addressText))

          val nags = resp.response.address.flatMap(_.nag)
          val classCodes: Map[String, String] = nags.map(nag =>
            (nag.uprn, classHierarchy.analyseClassCode(nag.classificationCode))).toMap

          val warningMessage =
            if (resp.status.code == 200) None
            else Some(s"${resp.status.code} ${resp.status.message} : ${resp.errors.headOption.map(_.message).getOrElse("")}")


        val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.uprnResult(
          singleSearchForm = filledForm,
          warningMessage = warningMessage,
          addressByUprnResponse = Some(resp.response),
          classification = Some(classCodes),
        version = version)
        Ok(viewToRender)}
      }
    }.getOrElse {
      Future.successful(Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login()))
    }
  }
}


object SingleMatchController {
  val form = Form(
    mapping(
      "address" -> text
    )(SingleSearchForm.apply)(SingleSearchForm.unapply)
  )
}