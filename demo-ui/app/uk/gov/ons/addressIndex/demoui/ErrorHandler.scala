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

@Singleton
class ErrorHandler @Inject() (val messagesApi: MessagesApi,
                                  environment: Environment,
                                  conf : DemouiConfigModule,
                                  defaultHttpErrorHandler: DefaultHttpErrorHandler
                              ) extends HttpErrorHandler with I18nSupport {

  val logger = Logger("ErrorHandler")

  val mode : Mode = environment.mode

  // Boolean to determine whether or not to use custom error processing
  // Settings for each mode come from application.conf file
  val processError : Boolean = mode match {
    case Mode.Dev => conf.config.customErrorDev
    case Mode.Test => conf.config.customErrorTest
    case Mode.Prod => conf.config.customErrorProd
    case _ => false
  }
  Logger.info("custom error = " + processError)

   def onBadRequest(request: RequestHeader, error: String) = {
      logger error s"bad request: $error"
      BadRequest(uk.gov.ons.addressIndex.demoui.views.html.error(400, error))
  }

  def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    if (processError){
      logger error s"client error: $message"
      Future.successful(
        NotFound(uk.gov.ons.addressIndex.demoui.views.html.error(statusCode, message))
      )
    } else {
      defaultHttpErrorHandler.onClientError(request, statusCode, message)
    }
  }

  def onServerError(request: RequestHeader, exception: Throwable) = {
    if (processError){
      logger error s"server error: ${exception.getMessage}"
      Logger("onServerError").error(exception.getMessage)
      Future.successful(
        InternalServerError(uk.gov.ons.addressIndex.demoui.views.html.error(500, exception.getMessage))
      )
    } else {
      defaultHttpErrorHandler.onServerError(request, exception)
    }
  }
}
