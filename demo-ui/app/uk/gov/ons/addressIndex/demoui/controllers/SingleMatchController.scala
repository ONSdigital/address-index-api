package uk.gov.ons.addressIndex.demoui.controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}

import org.apache.commons.lang3.StringUtils
import play.api.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, Lang, Langs, MessagesApi}
import play.api.mvc._
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
  val controllerComponents: ControllerComponents,
   conf : DemouiConfigModule,
   override val messagesApi: MessagesApi,
   langs: Langs,
   apiClient: AddressIndexClientInstance,
   classHierarchy: ClassHierarchy,
   version: DemoUIAddressIndexVersionModule
)(implicit ec: ExecutionContext) extends BaseController with I18nSupport {

  implicit val lang: Lang = langs.availables.head

  val logger = Logger("SingleMatchController")
  val pageSize = conf.config.limit
  val maxOff = conf.config.maxOffset
  val maxPages = (maxOff + pageSize - 1) / pageSize
  val apiUrl = conf.config.apiURL.host + ":" + conf.config.apiURL.port + conf.config.apiURL.gatewayPath

  /**
    * Present empty form for user to input address
    *
    * @return result to view
    */
  def showSingleMatchPage(): Action[AnyContent] = Action.async { implicit request =>
  //  logger info ("SingleMatch: Rendering Single Match Page")
    val refererUrl = request.uri
    request.session.get("api-key").map { apiKey =>
    val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
      singleSearchForm = SingleMatchController.form,
      filter = None,
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
    }.getOrElse {
      Future.successful(Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login()).withSession("referer" -> refererUrl))
    }
  }

  /**
    * Accept posted form, deal with empty address or pass on to MatchWithInput
    *
    * @return result to view or redirect
    */
  def doMatch(): Action[AnyContent] = Action.async { implicit request =>
    val optAddress: Option[String] = Try(request.body.asFormUrlEncoded.get("address").mkString).toOption
    val addressText = optAddress.getOrElse("")
    val optFilter: Option[String] = Try(request.body.asFormUrlEncoded.get("filter").mkString).toOption
    val filterText = optFilter.getOrElse("")
    if (addressText.trim.isEmpty) {
      logger info ("Single Match with Empty input address")
      val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
        singleSearchForm = SingleMatchController.form,
        filter = None,
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
        Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.SingleMatchController.doGetUprn(addressText, filterText))
      )
    } else {
      Future.successful(
        Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.SingleMatchController.doMatchWithInput(addressText, filterText, Some(1)))
      )
    }
  }

  /**
    * Perform match by calling API with address string. Can be called directly via get or redirect from form
    *
    * @param input
    * @return result to view
    */
  def doMatchWithInput(input: String, filter: String, page: Option[Int]): Action[AnyContent] = Action.async { implicit request =>

    val refererUrl = request.uri
    request.session.get("api-key").map { apiKey =>
      val addressText = StringUtils.stripAccents(input)
      val filterText = StringUtils.stripAccents(filter)
      val limit = pageSize.toString()
      val pageNum = page.getOrElse(1)
      val offNum = (pageNum - 1) * pageSize
      val offset = offNum.toString
      if (addressText.trim.isEmpty) {
        logger info ("Single Match with expected input address missing")
        val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
          singleSearchForm = SingleMatchController.form,
          filter = None,
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
     //   logger info ("Single Match with supplied input address " + addressText)

        apiClient.addressQuery(
          AddressIndexSearchRequest(
            input = addressText,
            limit = limit,
            offset = offset,
            filter = filterText,
            rangekm = "",
            lat = "50.705948",
            lon = "-3.5091076",
            id = UUID.randomUUID,
            apiKey = apiKey
          )
        ) map { resp: AddressBySearchResponseContainer =>
          val filledForm = SingleMatchController.form.fill(SingleSearchForm(addressText,filterText))

          val nags = resp.response.addresses.flatMap(_.nag)
          val classCodes: Map[String, String] = nags.map(nag =>
            (nag.uprn, classHierarchy.analyseClassCode(nag.classificationCode))).toMap

          val warningMessage =
            if (resp.status.code == 200) None
            else Some(s"${resp.status.code} ${resp.status.message} : ${resp.errors.headOption.map(_.message).getOrElse("")}")


        val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
          singleSearchForm = filledForm,
          filter = None,
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
      Future.successful(Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login()).withSession("referer" -> refererUrl))
    }
  }

  /**
    * Perform match by calling API with address string. Can be called directly via get or redirect from form
    *
    * @param input
    * @return result to view
    */
  def doGetUprn(input : String, filter: String) : Action[AnyContent] = Action.async { implicit request =>
    val refererUrl = request.uri
    request.session.get("api-key").map { apiKey =>val addressText = StringUtils.stripAccents(input)
      val filterText = StringUtils.stripAccents(filter)
    if (addressText.trim.isEmpty) {
      logger info("UPRN with expected input address missing")
      val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
        singleSearchForm = SingleMatchController.form,
        filter = None,
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
   //   logger info("UPRN with supplied input address " + addressText)
      val numericUPRN = BigInt(addressText)
      apiClient.uprnQuery(
        AddressIndexUPRNRequest(
          uprn = numericUPRN,
          id = UUID.randomUUID,
            apiKey = apiKey
        )
      ) map { resp: AddressByUprnResponseContainer =>
        val filledForm = SingleMatchController.form.fill(SingleSearchForm(addressText,filterText))

          val nags = resp.response.address.flatMap(_.nag)
          val classCodes: Map[String, String] = nags.map(nag =>
            (nag.uprn, classHierarchy.analyseClassCode(nag.classificationCode))).toMap

          val warningMessage =
            if (resp.status.code == 200) None
            else Some(s"${resp.status.code} ${resp.status.message} : ${resp.errors.headOption.map(_.message).getOrElse("")}")


        val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.uprnResult(
          singleSearchForm = filledForm,
          filter = None,
          warningMessage = warningMessage,
          addressByUprnResponse = Some(resp.response),
          classification = Some(classCodes),
          version = version,
          isClerical = false
        )
        Ok(viewToRender)}
      }
    }.getOrElse {
      Future.successful(Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login()).withSession("referer" -> refererUrl))
    }
  }

  /**
    * Perform match by calling API with address string. Can be called directly via get or redirect from form
    *
    * @param input
    * @return result to view
    */
  def doGetResult(input : String) : Action[AnyContent] = Action.async { implicit request =>
    val refererUrl = request.uri
    request.session.get("api-key").map { apiKey =>val addressText = StringUtils.stripAccents(input)
      val filterText = ""
      if (addressText.trim.isEmpty) {
        logger info("Result with expected input address missing")
        val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.singleMatch(
          singleSearchForm = SingleMatchController.form,
          filter = None,
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
        //   logger info("UPRN with supplied input address " + addressText)
        val numericUPRN = BigInt(addressText)
        apiClient.uprnQuery(
          AddressIndexUPRNRequest(
            uprn = numericUPRN,
            id = UUID.randomUUID,
            apiKey = apiKey
          )
        ) map { resp: AddressByUprnResponseContainer =>
          val filledForm = SingleMatchController.form.fill(SingleSearchForm(addressText,filterText))

          val nags = resp.response.address.flatMap(_.nag)
          val classCodes: Map[String, String] = nags.map(nag =>
            (nag.uprn, classHierarchy.analyseClassCode(nag.classificationCode))).toMap

          val warningMessage =
            if (resp.status.code == 200) None
            else Some(s"${resp.status.code} ${resp.status.message} : ${resp.errors.headOption.map(_.message).getOrElse("")}")


          val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.result(
            singleSearchForm = filledForm,
            filter = None,
            warningMessage = warningMessage,
            addressByUprnResponse = Some(resp.response),
            classification = Some(classCodes),
            version = version,
            isClerical = false,
            apiUrl = apiUrl,
            apiKey = apiKey
          )
          Ok(viewToRender)}
      }
    }.getOrElse {
      Future.successful(Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login()).withSession("referer" -> refererUrl))
    }
  }


}


object SingleMatchController {
  val form = Form(
    mapping(
      "address" -> text,
      "filter" -> text
    )(SingleSearchForm.apply)(SingleSearchForm.unapply)
  )
}