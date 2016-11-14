package uk.gov.ons.address

import play.api.http.HttpErrorHandler
import play.api.mvc._
import play.api.mvc.Results._

import scala.concurrent._
import javax.inject.Singleton

import play.api.Logger

@Singleton
class ErrorHandler extends HttpErrorHandler {
  // called when a route is found, but it was not possible to bind the request parameters
  def onBadRequest (request: RequestHeader, error: String) = {
    BadRequest("Bad Request: " + error)
  }

  def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    Future.successful(
      NotFound(views.html.ClientError(request,statusCode))
    )
  }

  def onServerError(request: RequestHeader, exception: Throwable) = {
    exception.printStackTrace()
    Logger("onServerError").error(exception.getMessage)
    Future.successful(
      InternalServerError(views.html.ServerError(exception))
    )
  }
}
