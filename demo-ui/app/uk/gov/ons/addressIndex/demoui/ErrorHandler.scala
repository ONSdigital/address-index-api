package uk.gov.ons.addressIndex.demoui

import play.api.http.HttpErrorHandler
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc._
import play.api._
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import scala.language.implicitConversions
import scala.concurrent._
import javax.inject.{Inject, Singleton}

import play.api.mvc.Results._
import play.api.Mode.Mode
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule

/**
  * Error Handler class for Demo UI - creates tidy error pages
  * and allows default error handler to be used instead as required
  * @param messagesApi
  * @param environment
  * @param conf
  * @param defaultHttpErrorHandler
  */
@Singleton
class ErrorHandler @Inject() (
  val messagesApi: MessagesApi,
  environment: Environment,
  conf : DemouiConfigModule,
  defaultHttpErrorHandler: DefaultHttpErrorHandler
) extends HttpErrorHandler with I18nSupport {

  val logger = Logger("ErrorHandler")

  val mode: Mode = environment.mode

  // Boolean to determine whether or not to use custom error processing
  // Settings for each mode come from application.conf file
  val processError : Boolean = mode match {
    case Mode.Dev => conf.config.customErrorProd
    case Mode.Test => conf.config.customErrorTest
    case Mode.Prod => conf.config.customErrorProd
  }
  logger info("custom error = " + processError)

  /**
    * Handle bad request errors which are not sent to an explicit view in the controller
    *  For example, when a route is found, but it was not possible to bind the request parameters
    * @param request
    * @param error
    * @return
    */
  def onBadRequest(request: RequestHeader, error: String): Result = {
    logger error s"bad request: 400 $error"
    BadRequest(uk.gov.ons.addressIndex.demoui.views.html.error(400, error))
  }

  /**
    * Handle client error such as 404
    * @param request
    * @param statusCode
    * @param message
    * @return
    */
  def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    if (processError) {
      logger error s"client error: $statusCode $message"
      val response = if (statusCode == 404) {
        NotFound(uk.gov.ons.addressIndex.demoui.views.html.error(statusCode, message))
      } else {
        Ok(uk.gov.ons.addressIndex.demoui.views.html.error(statusCode, message))
      }
      Future.successful(response)
    } else {
      defaultHttpErrorHandler.onClientError(request, statusCode, message)
    }
  }

  /**
    * Handle server error such as 500
    * @param request
    * @param exception
    * @return
    */
  def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    if (processError) {
      logger error s"server error: 500 ${exception.getMessage}"
      Future.successful(
        InternalServerError(uk.gov.ons.addressIndex.demoui.views.html.error(500, exception.getMessage))
      )
    } else {
      defaultHttpErrorHandler.onServerError(request, exception)
    }
  }
}
