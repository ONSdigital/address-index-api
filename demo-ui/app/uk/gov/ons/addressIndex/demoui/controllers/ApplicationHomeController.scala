package uk.gov.ons.addressIndex.demoui.controllers

import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.ws.{WSClient, WSRequest, WSResponse}
import play.api.mvc.{Action, AnyContent, Controller}
import uk.gov.ons.addressIndex.demoui.model.formdata.LoginCredentials
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule

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
class ApplicationHomeController @Inject()(conf: DemouiConfigModule, val messagesApi: MessagesApi, ws: WSClient)(implicit ec: ExecutionContext) extends Controller with I18nSupport {

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

  def indexPage(): Action[AnyContent] = Action { implicit req =>
    req.session.get("api-key").map { apiKey =>
      logger info ("ApplicationHome: Rendering Index page")
      Ok(uk.gov.ons.addressIndex.demoui.views.html.index())
    }.getOrElse {
      Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.login())
    }

  }

  def login: Action[AnyContent] = Action {
    Ok(uk.gov.ons.addressIndex.demoui.views.html.forms.login.fieldset())
  }

  def doLogin: Action[AnyContent] = Action { implicit req =>
//    println("TEST")
    val formValidationResult = loginForm.bindFromRequest.data
    (for {
      userName <- formValidationResult.get("userName") if userName.nonEmpty
      password <- formValidationResult.get("password") if userName.nonEmpty
    } yield {
//      println(userName)

      val prod = false // Add to config param

      if (prod) {
        val request: WSRequest = ws.url("http://localhost:9443/login")

        val complexRequest: WSRequest =
          request.withHeaders("Accept" -> "application/json")
            .withRequestTimeout(10000.millis)
            .withQueryString("userName" -> userName, "password" -> password)

        val futureResponse: Future[WSResponse] = complexRequest.get()

        val result = Await.result(futureResponse, 10000.millis)

        if (result.status != OK) {
          val key = (result.json \ "key").as[String]

          Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.indexPage())
            .withSession("api-key" -> key)
        } else Ok(uk.gov.ons.addressIndex.demoui.views.html.forms.login.fieldset("Authentication failed"))


      } else Redirect(uk.gov.ons.addressIndex.demoui.controllers.routes.ApplicationHomeController.indexPage())
        .withSession("api-key" -> "")

    }).getOrElse {
      // bad, data is not filled or not exist
      Ok(uk.gov.ons.addressIndex.demoui.views.html.forms.login.fieldset("Empty Username or Password"))
    }
  }
}
/* Responses

password <-!CapGateway23
user <- omidii
{
"Status": 401,
"ErrorCode": "Authentication Error",
"ErrorMessage": "Authentication failed. Invalid username or password."
}

{
"Status": 200,
"key": "ea15dc73-1991-46db-8c02-e3c483bf9e3e"
}*/