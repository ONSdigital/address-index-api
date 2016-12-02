package uk.gov.ons.addressIndex.demoui

import play.api.http.HttpErrorHandler
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent._
import javax.inject.Singleton
import play.api.Logger

@Singleton
class ErrorHandler extends HttpErrorHandler {

  val logger = Logger("ErrorHandler")

  def onBadRequest(request: RequestHeader, error: String) = {
    logger error s"bad request: $error"
    BadRequest("Bad Request: " + error)
  }

  def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    logger error s"client error: $message"
    Future.successful(
         NotFound
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable) = {
    logger error s"server error: ${exception.getMessage}"
    Logger("onServerError").error(exception.getMessage)
    Future.successful(
      InternalServerError
    )
  }
}
