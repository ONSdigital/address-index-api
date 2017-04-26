package uk.gov.ons.addressIndex.demoui.controllers

import javax.inject.{Inject, Singleton}

import play.api.{Environment, Logger, Mode}
import play.api.Mode.Mode
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{Format, Json}
import play.api.libs.ws.{WSAuthScheme, WSClient, WSRequest, WSResponse}
import play.api.mvc.{Action, AnyContent, Controller}
import uk.gov.ons.addressIndex.demoui.model.formdata.LoginCredentials
import uk.gov.ons.addressIndex.demoui.modules.{DemoUIAddressIndexVersionModule, DemouiConfigModule}
import uk.gov.ons.addressIndex.demoui.utils.GatewaySimulator

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.language.implicitConversions

/**
  * Simple controller for home page
  *
  * @param conf
  * @param messagesApi
  * @param ec
  */
@Singleton
class ApplicationHomeController @Inject()(conf: DemouiConfigModule, version: DemoUIAddressIndexVersionModule, val messagesApi: MessagesApi, environment: Environment, ws: WSClient)(implicit ec: ExecutionContext) extends Controller with I18nSupport {

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
      logger info ("ApplicationHome: Rendering Index page")
      Ok(uk.gov.ons.addressIndex.demoui.views.html.index(version))
    }.getOrElse {
      Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login())
    }

  }

  /**
    * Load login viewlet unless config says login is not required in the conf
    * @return
    */
  def login: Action[AnyContent] = Action {
    logger.info("loginRequired " + conf.config.loginRequired )
    if (!conf.config.loginRequired)
    {
      Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.home())
        .withSession("api-key" -> "")
    }
    else {
      Ok(uk.gov.ons.addressIndex.demoui.views.html.login("", version))
    }
  }

  /**
    * Redirect to the API gateway to perform login
    * @return
    */
  def doLogin: Action[AnyContent] = Action { implicit req =>

    val formValidationResult = loginForm.bindFromRequest.data
    (for {
      userName <- formValidationResult.get("userName") if userName.nonEmpty
      password <- formValidationResult.get("password") if userName.nonEmpty
    } yield {
    //  use fake gateway in dev or test
      val mode: Mode = environment.mode
      val realGateway : Boolean = mode match {
        case Mode.Dev => false
        case Mode.Test => false
        case Mode.Prod => true
      }
      if (realGateway) {

        val request: WSRequest = ws.url(conf.config.gatewayURL+"/ai/login")

        logger.info("about to run request")
        val complexRequest: WSRequest =
          request.withHeaders("Accept" -> "application/json")
            .withAuth(userName, password, WSAuthScheme.BASIC)
            .withRequestTimeout(10000.millis)
      //      .withQueryString("userName" -> userName, "password" -> password)

        val futureResponse: Future[WSResponse] = complexRequest.get()

        val result = Await.result(futureResponse, 10000.millis)
        logger.info("request run + result.status")

        if (result.status != OK) {
          val key = (result.json \ "key").as[String]

          Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.home())
            .withSession("api-key" -> key)
        } else Ok(uk.gov.ons.addressIndex.demoui.views.html.login("Authentication failed",version))

      } else
        {
          val fakeResponse = GatewaySimulator.getApiKey(userName,password)
          logger.info("fakeResponse = " + Json.toJson(fakeResponse))
          if (fakeResponse.errorCode == "") {
            val key = fakeResponse.key

            Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.home())
              .withSession("api-key" -> key)
          } else Ok(uk.gov.ons.addressIndex.demoui.views.html.login("Authentication failed",version))
        }
    }).getOrElse {
      // bad, data is not filled or not exist
      Ok(uk.gov.ons.addressIndex.demoui.views.html.login("Empty Username or Password",version))
    }
  }

}
