package uk.gov.ons.addressIndex.demoui.controllers

import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Controller}
import uk.gov.ons.addressIndex.demoui.model.formdata.LoginCredentials
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

/**
  * Simple controller for home page
  *
  * @param conf
  * @param messagesApi
  * @param ec
  */
@Singleton
class ApplicationHomeController @Inject()(conf: DemouiConfigModule, val messagesApi: MessagesApi)(implicit ec: ExecutionContext) extends Controller with I18nSupport {

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

  def indexPage(): Action[AnyContent] = Action.async { implicit req =>
    logger info ("ApplicationHome: Rendering Index page")
    Future.successful(
      Ok(uk.gov.ons.addressIndex.demoui.views.html.index())
    )
  }

  def login: Action[AnyContent] = Action {
    Ok(uk.gov.ons.addressIndex.demoui.views.html.forms.login.fieldset())
  }

  def doLogin: Action[AnyContent] = Action { implicit req =>
    println("TEST")
    val formValidationResult = loginForm.bindFromRequest.data
    (for {
      userName <- formValidationResult.get("userName") if userName.nonEmpty
      password <- formValidationResult.get("password") if userName.nonEmpty
    } yield {
      // there is userName and password values, both are filled
      println(userName)
      // Here you have access to userName and password values, if you want, you can do some validation

      /*
      Now you'll need to send a request to CI gateway
      https://www.playframework.com/documentation/2.5.x/ScalaWS
      val complexRequest: WSRequest =
        request.withHeaders("Accept" -> "application/json")
        .withRequestTimeout(10000.millis)
        .withQueryString("search" -> "play")


      This request will return a Future[WSResponse]
      val futureResponse: Future[WSResponse] = complexRequest.get()


      You can use Await.result(resultOfRequest, 10 seconds) on it as it will be simplier than manipulating Future structure

      val result = Await.result(futureRequest, Duration.INF)

      you will need to process that response (result) in any maner you want
      https://www.playframework.com/documentation/2.5.x/ScalaWS#Processing-the-Response

      To process it as a JSON you will need to declare a case class for an expdected response and add an implicit formatter to it
      (for an example check AddressResponse.scala in models project)



       */


      /*
      how  to store (and read) a session is written here https://www.playframework.com/documentation/2.5.x/ScalaSessionFlash

      pay attention that you will need to read this value on every other request here (the list is in routes file)
      it the value is absent, you should redirect the user to the login page:

      def index = Action { request =>
        request.session.get("key").map { user =>
          // DO REQUESTS TO THE API
       }.getOrElse {
   // REDIRECT TO LOGIN PAGE
      }
      }



      Now when you are at a request handling method and you have a key, you will need to make a request to the CI gateway the same
      way you do for the API (bridge API), but now you will also need to add a "key" parameter to it so
      that the CI gateway accept it

      Currently  (you will find this piece of code in the singlematchcontroller.scala:
      apiClient.addressQuery(
        AddressIndexSearchRequest(
          input = addressText,
          limit = limit,
          offset = offset,
          id = UUID.randomUUID
        )
      )

      Modified:

      apiClient.addressQuery(
        AddressIndexSearchRequest(
          input = addressText,
          limit = limit,
          offset = offset,
          id = UUID.randomUUID,
          key = keyYouGotFromSession
        )
      )

      DONT FORGET TO MODIFY THE URL THAT IS USED FOR THE REQUEST, currently it's api, it will need to be CI gateway

      The handling of the response of the CI gateway is the same as the actual one, no modification required

       */


      // if template is red, just re-run the app
//      Ok(uk.gov.ons.addressIndex.demoui.views.html.forms.login.fieldset()).withNewSession
      Ok(uk.gov.ons.addressIndex.demoui.views.html.index())


    }).getOrElse {
      // bad, data is not filled or not exist
      Ok(uk.gov.ons.addressIndex.demoui.views.html.forms.login.fieldset("Empty Username or Password"))
    }

  }

  //  def list = IsAuthenticated { username => implicit request =>
  //    val CA = CA.findUSer()
  //    Ok(uk.gov.ons.addressIndex.demoui.views.html.index())

}