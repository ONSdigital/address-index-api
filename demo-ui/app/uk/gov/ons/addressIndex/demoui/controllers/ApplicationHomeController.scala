package uk.gov.ons.addressIndex.demoui.controllers

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.ws.{WSAuthScheme, WSClient, WSRequest, WSResponse}
import play.api.mvc._
import play.api.{Environment, Logger, Mode}
import uk.gov.ons.addressIndex.demoui.model.formdata.LoginCredentials
import uk.gov.ons.addressIndex.demoui.modules.{DemoUIAddressIndexVersionModule, DemouiConfigModule}
import uk.gov.ons.addressIndex.demoui.utils.GatewaySimulator

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Simple controller for home page
  *
  * @param controllerComponents
  * @param conf
  * @param version
  * @param messagesApi
  * @param environment
  * @param ws
  */
@Singleton
class ApplicationHomeController @Inject()(val controllerComponents: ControllerComponents,
                                          conf: DemouiConfigModule,
                                          version: DemoUIAddressIndexVersionModule,
                                          override val messagesApi: MessagesApi,
                                          environment: Environment,
                                          ws: WSClient
                                         ) extends BaseController with I18nSupport {
  val logger = Logger("ApplicationHomeController")

  val loginForm = Form(
    mapping(
      "userName" -> nonEmptyText(minLength = 5, maxLength = 10),
      "password" -> nonEmptyText
    )(LoginCredentials.apply)(LoginCredentials.unapply)
  )

  /**
    * Render index page
    *
    * @return result to view
    */
  def home(): Action[AnyContent] = Action { implicit req =>
    req.session.get("api-key").map { apiKey =>
      // logger info ("ApplicationHome: Rendering Index page")
      Ok(uk.gov.ons.addressIndex.demoui.views.html.index(version))
    }.getOrElse {
      Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login())
    }
  }

  /**
    * Load login viewlet unless config says login is not required in the conf
    *
    * @return
    */
  def login: Action[AnyContent] = Action { implicit req =>
    // logger.info("Login Required =  " + conf.config.loginRequired )
    if (conf.config.loginRequired) {
      Ok(uk.gov.ons.addressIndex.demoui.views.html.login("", "", version))
    } else {
      Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.SingleMatchController.showSingleMatchPage())
        .withSession("api-key" -> "")
    }
  }

  private def invalidLoginResponse(implicit messages: Messages): Result =
    Ok(uk.gov.ons.addressIndex.demoui.views.html.login("Invalid username or password", "Please try again", version))

  private def emptyLoginResponse(implicit messages: Messages): Result =
    Ok(uk.gov.ons.addressIndex.demoui.views.html.login("Empty username or password", "Please try again", version))

  /**
    * Redirect to the API gateway to perform login
    *
    * @return
    */
  def doLogin: Action[AnyContent] = Action { implicit req =>
    val formValidationResult = loginForm.bindFromRequest.data
    (for {
      userName <- formValidationResult.get("userName") if userName.nonEmpty
      password <- formValidationResult.get("password") if userName.nonEmpty
    } yield {
      //  use fake gateway in dev or test (and prod until connectivity problem fixed)
      val mode: Mode = environment.mode
      val realGateway: Boolean = mode match {
        case Mode.Dev => conf.config.realGatewayDev
        case Mode.Test => conf.config.realGatewayTest
        case Mode.Prod => conf.config.realGatewayProd
      }

      if (realGateway) {
        // val request: WSRequest = ws.url(conf.config.gatewayURL+"/ai/login")
        val request: WSRequest = ws.url(conf.config.gatewayURL + "/ai/v1/ui/login")
        logger.info("attempting to login via gateway")
        val complexRequest: WSRequest =
          request.withHttpHeaders("Accept" -> "application/json")
            .withAuth(userName, password, WSAuthScheme.BASIC)
            .withRequestTimeout(10000.millis)

        val futureResponse: Future[WSResponse] = complexRequest.get()
        val result = Await.result(futureResponse, 10000.millis)

        // Any response other than a 200 is assumed to be an authentication failure (e.g. 401)
        if (result.status == OK) {
          val key = userName + "_" + (result.json \ "key").as[String]
          Redirect(Call("GET", req.session.get("referer").getOrElse(default = "/addresses")))
            .withSession("api-key" -> key)
        } else invalidLoginResponse
      } else {
        val fakeResponse = GatewaySimulator.getApiKey(userName, password)
        if (fakeResponse.errorCode.isEmpty) {
          val key = userName + "_" + fakeResponse.key
          Redirect(
            Call("GET", req.session.get("referer").getOrElse(default = "/addresses"))
          ).withSession("api-key" -> key)
        } else invalidLoginResponse
      }
    }).getOrElse {
      // bad, data is not filled or not exist
      emptyLoginResponse
    }
  }

}
