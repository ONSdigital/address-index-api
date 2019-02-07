import javax.inject._
import play.api.http.{DefaultHttpErrorHandler}
import play.api._
import play.api.libs.json.{Format, Json}
import play.api.mvc._
import play.api.mvc.Results._
import play.api.routing.Router

import scala.concurrent._

@Singleton
class ErrorHandler @Inject() (
  env: Environment,
  config: Configuration,
  sourceMapper: OptionalSourceMapper,
  router: Provider[Router]
) extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

  // initially override client only - may add onProdServerError and/or onForbidden later

  override def onClientError(request: RequestHeader, statusCode: Int, message: String) = {
    val newMessage = if (message == "" && statusCode == 404) "Path not found" else message
    Future.successful(
      Status(statusCode)(Json.toJson(ClientErrorContainer(response="Error calling API", status=ClientResponseStatus(statusCode, newMessage))))
    )
  }

  case class ClientErrorContainer(
    response: String,
    status: ClientResponseStatus

  )

  object ClientErrorContainer {
    implicit lazy val ClientErrorContainerFormat: Format[ClientErrorContainer] = Json.format[ClientErrorContainer]
  }

  case class ClientResponseStatus (
    code: Int,
    message: String
  )

  object ClientResponseStatus {
    implicit lazy val ClientResponseStatusFormat: Format[ClientResponseStatus] = Json.format[ClientResponseStatus]
  }


}
