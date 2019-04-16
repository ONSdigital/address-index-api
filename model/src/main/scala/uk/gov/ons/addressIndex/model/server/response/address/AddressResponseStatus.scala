package uk.gov.ons.addressIndex.model.server.response.address

import play.api.http.Status
import play.api.libs.json.{Format, Json}

/**
  * Contains response status
  *
  * @param code    http code
  * @param message response description
  */
case class AddressResponseStatus(code: Int,
                                 message: String)

object AddressResponseStatus {
  implicit lazy val addressResponseStatusFormat: Format[AddressResponseStatus] = Json.format[AddressResponseStatus]
}

object OkAddressResponseStatus extends AddressResponseStatus(
  code = Status.OK,
  message = "Ok"
)


object NotFoundAddressResponseStatus extends AddressResponseStatus(
  code = Status.NOT_FOUND,
  message = "Not found"
)

object BadRequestAddressResponseStatus extends AddressResponseStatus(
  code = Status.BAD_REQUEST,
  message = "Bad request"
)

object UnauthorizedRequestAddressResponseStatus extends AddressResponseStatus(
  code = Status.UNAUTHORIZED,
  message = "Unauthorized"
)

object InternalServerErrorAddressResponseStatus extends AddressResponseStatus(
  code = Status.INTERNAL_SERVER_ERROR,
  message = "Internal server error"
)

object TooManyRequestsResponseStatus extends AddressResponseStatus(
  code = Status.TOO_MANY_REQUESTS,
  message = "Too many requests (unexpected errors can also trigger the circuit breaker, see message detail)"
)
