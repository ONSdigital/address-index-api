package uk.gov.ons.addressIndex.demoui

import play.api._
import play.api.http.{DefaultHttpErrorHandler, HttpErrorHandler}
import play.api.i18n._
import play.api.mvc._
import javax.inject.{Inject, Singleton}
import play.api.Mode
import play.api.mvc.Results._
import uk.gov.ons.addressIndex.demoui.modules.{DemoUIAddressIndexVersionModule, DemouiConfigModule}

import scala.concurrent._

/**
  * Error Handler class for Demo UI - creates tidy error pages
  * and allows default error handler to be used instead as required
  *
  * @param messagesApi
  * @param environment
  * @param conf
  * @param defaultHttpErrorHandler
  */
@Singleton
class ErrorHandler @Inject()(langs: Langs,
                             val messagesApi: MessagesApi,
                             environment: Environment,
                             conf: DemouiConfigModule,
                             defaultHttpErrorHandler: DefaultHttpErrorHandler,
                             version: DemoUIAddressIndexVersionModule
                            ) extends HttpErrorHandler with I18nSupport {

  val logger = Logger("ErrorHandler")

  implicit val lang: Lang = langs.availables.head
  implicit val messages: MessagesImpl = MessagesImpl(lang, messagesApi)

  val mode: Mode = environment.mode

  // Boolean to determine whether or not to use custom error processing
  // Settings for each mode come from application.conf file
  val processError: Boolean = mode match {
    case Mode.Dev => conf.config.customErrorDev
    case Mode.Test => conf.config.customErrorTest
    case Mode.Prod => conf.config.customErrorProd
  }
  logger info ("custom error = " + processError)

  /**
    * Handle bad request errors which are not sent to an explicit view in the controller
    * For example, when a route is found, but it was not possible to bind the request parameters
    *
    * @param request
    * @param error
    * @return
    */
  def onBadRequest(request: RequestHeader, error: String): Result = {
    logger info ("request header = " + request.toString())
    logger error s"bad request: 400 $error"
    BadRequest(uk.gov.ons.addressIndex.demoui.views.html.error(400, error, version))
  }

  /**
    * Handle client error such as 404
    *
    * @param request
    * @param statusCode
    * @param message
    * @return
    */
  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    if (processError) {
      logger error s"client error: $statusCode $message"
      val response = if (statusCode == 404) {
        NotFound(uk.gov.ons.addressIndex.demoui.views.html.error(statusCode, message, version))
      } else {
        Ok(uk.gov.ons.addressIndex.demoui.views.html.error(statusCode, message, version))
      }
      Future.successful(response)
    } else {
      defaultHttpErrorHandler.onClientError(request, statusCode, message)
    }
  }

  /**
    * Handle server error such as 500
    *
    * @param request
    * @param exception
    * @return
    */
  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    if (processError) {
      logger error s"server error: 500 ${exception.getMessage}"
      Future.successful(
        InternalServerError(uk.gov.ons.addressIndex.demoui.views.html.error(500, exception.getMessage, version))
      )
    } else {
      defaultHttpErrorHandler.onServerError(request, exception)
    }
  }
}
