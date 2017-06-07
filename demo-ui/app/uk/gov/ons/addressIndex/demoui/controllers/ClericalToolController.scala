package uk.gov.ons.addressIndex.demoui.controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.demoui.model._
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import uk.gov.ons.addressIndex.demoui.utils.ClassHierarchy
import uk.gov.ons.addressIndex.model.server.response.{AddressBySearchResponseContainer, AddressByUprnResponseContainer}
import uk.gov.ons.addressIndex.model.{AddressIndexSearchRequest, AddressIndexUPRNRequest}
import uk.gov.ons.addressIndex.demoui.modules.DemoUIAddressIndexVersionModule

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
                                       classHierarchy: ClassHierarchy,
                                       version: DemoUIAddressIndexVersionModule
                                     )(implicit ec: ExecutionContext) extends Controller with I18nSupport {

  val logger = Logger("ClericalToolController")
  val pageSize = conf.config.limit
  val maxOff = conf.config.maxOffset
  val maxPages = (maxOff + pageSize - 1) / pageSize
  val apiUrl = conf.config.apiURL.host + ":" + conf.config.apiURL.port

  /**
    * Present empty form for user to input address
    *
    * @return result to view
    */
  def showSingleMatchPage(): Action[AnyContent] = Action.async { implicit request =>
    val refererUrl = request.uri
    request.session.get("api-key").map { apiKey =>
    logger info ("Clerial Tool: Rendering Single Match Page")
    val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.clericalTool(
      title = messagesApi("clerical.sfatext"),
      action = uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.doMatch,
      singleSearchForm = SingleMatchController.form,
      warningMessage = None,
      query = "",
      pageNum = 1,
      pageSize = pageSize,
      pageMax = maxPages,
      expandRow = -1,
      pagerAction = "clerical",
      addressBySearchResponse = None,
      classification = None,
      apiUrl = apiUrl,
      version = version,
      placeholder = messagesApi("clericalsearchform.placeholder")
    )
      Future.successful(Ok(viewToRender))
    }.getOrElse {
      Future.successful(Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login()).withSession("referer" -> refererUrl))
    }
  }

  /**
    * Accept posted form, deal with empty address or pass on to MatchWithInput
    *
    * @return result to view or redirect
    */
  def doMatch(): Action[AnyContent] = Action { implicit request =>
    val addressText = Try(request.body.asFormUrlEncoded.get("address").mkString).getOrElse("")
    if (addressText.trim.isEmpty) {
      logger.info("Clerical Tool with Empty input address")
      val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.clericalTool(
        title = messagesApi("clerical.sfatext"),
        action = uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.doMatch,
        singleSearchForm = SingleMatchController.form,
        warningMessage = Some(messagesApi("single.pleasesupply")),
        query = "",
        pageNum = 1,
        pageSize = pageSize,
        pageMax = maxPages,
        expandRow = -1,
        pagerAction = "clerical",
        addressBySearchResponse = None,
        classification = None,
        apiUrl = apiUrl,
        version = version,
        placeholder = messagesApi("clericalsearchform.placeholder")
      )
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
    val refererUrl = request.uri
    request.session.get("api-key").map { apiKey =>
 //     generateClericalView(input, page, expand, messagesApi("clerical.sfatext"), uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.doMatch, "clerical", messagesApi("clericalsearchform.placeholder"), apiKey)
      val addressText = input
      val expandr = expand.getOrElse(-1)
      val limit = pageSize.toString()
      val pageNum = page.getOrElse(1)
      val offNum = (pageNum - 1) * pageSize
      val offset = offNum.toString
      if (addressText.trim.isEmpty) {
        logger info ("Clerical Tool with expected input address missing")
        val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.clericalTool(
          title = messagesApi("clerical.sfatext"),
          action = uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.doMatch,
          singleSearchForm = SingleMatchController.form,
          warningMessage = Some(messagesApi("single.pleasesupply")),
          query = "",
          pageNum = 1,
          pageSize = pageSize,
          pageMax = maxPages,
          expandRow = -1,
          pagerAction = "clerical",
          addressBySearchResponse = None,
          classification = None,
          apiUrl = apiUrl,
          version = version,
          placeholder = messagesApi("clericalsearchform.placeholder")
        )

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


          Ok(uk.gov.ons.addressIndex.demoui.views.html.clericalTool(
            title = messagesApi("clerical.sfatext"),
            action = uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.doMatch,
            singleSearchForm = filledForm,
            warningMessage = warningMessage,
            query = "",
            pageNum = pageNum,
            pageSize = pageSize,
            pageMax = maxPages,
            expandRow = expandr,
            pagerAction = "clerical",
            addressBySearchResponse = Some(resp.response),
            classification = Some(classCodes),
            apiUrl = apiUrl,
            version = version,
            placeholder = messagesApi("debugsearchform.placeholder")
          ))
        }
      }
    }.getOrElse {
      Future.successful(Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login()).withSession("referer" -> refererUrl))
    }
  }

  /** This shared method can't be used for now as it breaks Welsh langugage - might be able to reinstate later
  private def generateClericalView(input: String, page: Option[Int], expand: Option[Int], title: String, action: Call,
    pagerAction: String, placeholder: String, apiKey: String, query: String = ""): Future[Result] = {
    val addressText = input
    val expandr = expand.getOrElse(-1)
    val limit = pageSize.toString()
    val pageNum = page.getOrElse(1)
    val offNum = (pageNum - 1) * pageSize
    val offset = offNum.toString
    if (addressText.trim.isEmpty) {
      logger info ("Clerical Tool with expected input address missing")
      val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.clericalTool(
        title = title,
        action = action,
        singleSearchForm = SingleMatchController.form,
        warningMessage = Some(messagesApi("single.pleasesupply")),
        query = "",
        pageNum = 1,
        pageSize = pageSize,
        pageMax = maxPages,
        expandRow = -1,
        pagerAction = pagerAction,
        addressBySearchResponse = None,
        classification = None,
        apiUrl = apiUrl,
        version = version,
        placeholder = placeholder
      )

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


        Ok(uk.gov.ons.addressIndex.demoui.views.html.clericalTool(
          title = title,
          action = action,
          singleSearchForm = filledForm,
          warningMessage = warningMessage,
          query = query,
          pageNum = pageNum,
          pageSize = pageSize,
          pageMax = maxPages,
          expandRow = expandr,
          pagerAction = pagerAction,
          addressBySearchResponse = Some(resp.response),
          classification = Some(classCodes),
          apiUrl = apiUrl,
          version = version,
          placeholder = messagesApi("debugsearchform.placeholder")
        ))
      }
    }
  }
*/
  /**
    * Perform match by calling API with address string. Can be called directly via get or redirect from form
    * @param input
    * @return result to view
    */
  def doUprnWithInput(input : Long) : Action[AnyContent] = Action.async { implicit request =>
    val refererUrl = request.uri
    request.session.get("api-key").map { apiKey =>
      logger info("UPRN with supplied input address " + input)
      apiClient.uprnQuery(
        AddressIndexUPRNRequest(
          uprn = input,
          id = UUID.randomUUID,
          apiKey = apiKey
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
          classification = Some(classCodes),
          version = version,
          isClerical = true,
          apiUrl = apiUrl
        )
        Ok(viewToRender)
      }
    }.getOrElse {
      Future.successful(Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login()).withSession("referer" -> refererUrl))
    }
  }

  def showQuery(): Action[AnyContent] = Action.async {implicit request =>
    val refererUrl = request.uri
    request.session.get("api-key").map { apiKey =>
    val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.clericalTool(
      title = messagesApi("debug.sfatext"),
      action = uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.doShowQuery,
      singleSearchForm = SingleMatchController.form,
      warningMessage = None,
      query = "",
      pageNum = 1,
      pageSize = pageSize,
      pageMax = maxPages,
      expandRow = -1,
      pagerAction = "debug",
      addressBySearchResponse = None,
      classification = None,
      apiUrl = apiUrl,
      version = version,
      placeholder = messagesApi("debugsearchform.placeholder")
    )
      Future.successful(
        Ok(viewToRender)
      )
    }.getOrElse {
      Future.successful(Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login()).withSession("referer" -> refererUrl))
    }
  }

  def doShowQuery(): Action[AnyContent] = Action { implicit request =>

    val input: String = Try(request.body.asFormUrlEncoded.get("address").mkString).getOrElse("")
    Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.showQueryWithInput(input, Some(1), Some(-1)))

  }

  def showQueryWithInput(input: String, page: Option[Int], expand: Option[Int]): Action[AnyContent] = Action.async { implicit request =>
    val refererUrl = request.uri
    request.session.get("api-key").map { apiKey =>
      apiClient.showQuery(input, apiKey).flatMap{ query =>
   //     generateClericalView(input, page, expand, messagesApi("debug.sfatext"),  uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.doShowQuery, "debug", messagesApi("debugsearchform.placeholder"), apiKey, query)
        val addressText = input
        val expandr = expand.getOrElse(-1)
        val limit = pageSize.toString()
        val pageNum = page.getOrElse(1)
        val offNum = (pageNum - 1) * pageSize
        val offset = offNum.toString
        if (addressText.trim.isEmpty) {
          logger info ("Clerical Tool with expected input address missing")
          val viewToRender = uk.gov.ons.addressIndex.demoui.views.html.clericalTool(
            title = messagesApi("debug.sfatext"),
            action = uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.doShowQuery,
            singleSearchForm = SingleMatchController.form,
            warningMessage = Some(messagesApi("single.pleasesupply")),
            query = "",
            pageNum = 1,
            pageSize = pageSize,
            pageMax = maxPages,
            expandRow = -1,
            pagerAction = "debug",
            addressBySearchResponse = None,
            classification = None,
            apiUrl = apiUrl,
            version = version,
            placeholder = messagesApi("debugsearchform.placeholder")
          )

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


            Ok(uk.gov.ons.addressIndex.demoui.views.html.clericalTool(
              title = messagesApi("debug.sfatext"),
              action = uk.gov.ons.addressIndex.demoui.controllers.routes.ClericalToolController.doShowQuery,
              singleSearchForm = filledForm,
              warningMessage = warningMessage,
              query = query,
              pageNum = pageNum,
              pageSize = pageSize,
              pageMax = maxPages,
              expandRow = expandr,
              pagerAction = "debug",
              addressBySearchResponse = Some(resp.response),
              classification = Some(classCodes),
              apiUrl = apiUrl,
              version = version,
              placeholder = messagesApi("debugsearchform.placeholder")
            ))
          }
        }
      }
    }.getOrElse {
      Future.successful(Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login()).withSession("referer" -> refererUrl))
    }
  }
}

object ClericalToolController {
  val form = Form(
    mapping(
      "address" -> text
    )(SingleSearchForm.apply)(SingleSearchForm.unapply)
  )
}
