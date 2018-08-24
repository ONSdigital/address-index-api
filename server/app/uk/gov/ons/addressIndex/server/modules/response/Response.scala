package uk.gov.ons.addressIndex.server.modules.response

import play.api.libs.json.{Json, Writes}
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, Ok, Unauthorized, _}
import uk.gov.ons.addressIndex.model.server.response.address._

import scala.concurrent.Future

trait Response {

  val dataVersion: String
  val apiVersion: String

  def StartDateInvalid: AddressBySearchResponseContainer = {
    BadRequestTemplate(StartDateInvalidResponseError)
  }

  def EndDateInvalid: AddressBySearchResponseContainer = {
    BadRequestTemplate(EndDateInvalidResponseError)
  }

  def BadRequestTemplate(errors: AddressResponseError*): AddressBySearchResponseContainer = {
    AddressBySearchResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = Error,
      status = BadRequestAddressResponseStatus,
      errors = errors
    )
  }

  def FailedRequestToEs: AddressBySearchResponseContainer = {
    AddressBySearchResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = Error,
      status = InternalServerErrorAddressResponseStatus,
      errors = Seq(FailedRequestToEsError)
    )
  }

  def FailedRequestToEsTooBusy: AddressBySearchResponseContainer = {
    AddressBySearchResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = Error,
      status = TooManyRequestsResponseStatus,
      errors = Seq(FailedRequestToEsError)
    )
  }

  def SourceMissing: AddressBySearchResponseContainer = {
    UnauthorizedRequestTemplate(SourceMissingError)
  }

  def SourceInvalid: AddressBySearchResponseContainer = {
    UnauthorizedRequestTemplate(SourceInvalidError)
  }

  def KeyMissing: AddressBySearchResponseContainer = {
    UnauthorizedRequestTemplate(ApiKeyMissingError)
  }

  def KeyInvalid: AddressBySearchResponseContainer = {
    UnauthorizedRequestTemplate(ApiKeyInvalidError)
  }

  private def UnauthorizedRequestTemplate(errors: AddressResponseError*): AddressBySearchResponseContainer = {
    AddressBySearchResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = Error,
      status = UnauthorizedRequestAddressResponseStatus,
      errors = errors
    )
  }

  def Error: AddressBySearchResponse = {
    AddressBySearchResponse(
      Map.empty,
      addresses = Seq.empty,
      filter = "",
      historical = true,
      rangekm = "",
      latitude = "",
      longitude = "",
      startDate = "",
      endDate = "",
      limit = 10,
      offset = 0,
      total = 0,
      sampleSize = 20,
      maxScore = 0f,
      matchthreshold = 5f,
      verbose = true
    )
  }

  /**
    * Use this instead of Future.successful(BadRequest(Json.toJson(toJsonable)))
    *
    * @param toJsonable an object which has an implicit Writes (PlayJson)
    * @param writes     implicit Writes
    * @tparam T type of the toJsonable
    * @return a Future BadRequest with a Json body
    */
  def futureJsonBadRequest[T](toJsonable: T)(implicit writes: Writes[T]): Future[Result] = {
    futureJson[T](
      status = BadRequest,
      toJsonable = toJsonable
    )
  }

  /**
    * Use this instead of Future.successful(BadRequest(Json.toJson(toJsonable)))
    *
    * @param toJsonable an object which has an implicit Writes (PlayJson)
    * @param writes     implicit Writes
    * @tparam T type of the toJsonable
    * @return a Future BadRequest with a Json body
    */
  def futureJsonUnauthorized[T](toJsonable: T)(implicit writes: Writes[T]): Future[Result] = {
    futureJson[T](
      status = Unauthorized,
      toJsonable = toJsonable
    )
  }

  /**
    * Helper for creating Future.successful(Status(Json.toJson(toJsonable)))
    *
    * @param status
    * @param toJsonable
    * @param writes
    * @tparam T
    * @return
    */
  def futureJson[T](status: Status, toJsonable: T)(implicit writes: Writes[T]): Future[Result] = {
    Future successful json(status, toJsonable)
  }

  /**
    * Helper for creating Status(Json.toJson(toJsonable))
    *
    * @param status
    * @param toJsonable
    * @param writes
    * @tparam T
    * @return
    */
  def json[T](status: Status, toJsonable: T)(implicit writes: Writes[T]): Result = {
    status(Json toJson toJsonable)
  }

  /**
    * Use this instead of BadRequest(Json.toJson(toJsonable))
    *
    * @param toJsonable an object which has an implicit Writes (PlayJson)
    * @param writes     implicit Writes
    * @tparam T type of the toJsonable
    * @return a BadRequest with a Json body
    */
  def jsonBadRequest[T](toJsonable: T)(implicit writes: Writes[T]): Result = {
    json[T](
      status = BadRequest,
      toJsonable = toJsonable
    )
  }

  /**
    * Use this instead of Future.successful(BadRequest(Json.toJson(toJsonable)))
    *
    * @param toJsonable an object which has an implicit Writes (PlayJson)
    * @param writes     implicit Writes
    * @tparam T type of the toJsonable
    * @return a Future BadRequest with a Json body
    */
  def jsonUnauthorized[T](toJsonable: T)(implicit writes: Writes[T]): Result = {
    json[T](
      status = Unauthorized,
      toJsonable = toJsonable
    )
  }

  /**
    * Use this instead of Future.successful(NotFound(Json.toJson(toJsonable)))
    *
    * @param toJsonable an object which has an implicit Writes (PlayJson)
    * @param writes     implicit Writes
    * @tparam T type of the toJsonable
    * @return a Future NotFound with a Json body
    */
  def futureJsonNotFound[T](toJsonable: T)(implicit writes: Writes[T]): Future[Result] = {
    futureJson[T](
      status = NotFound,
      toJsonable = toJsonable
    )
  }

  /**
    * Use this instead of NotFound(Json.toJson(toJsonable))
    *
    * @param toJsonable an object which has an implicit Writes (PlayJson)
    * @param writes     implicit Writes
    * @tparam T type of the toJsonable
    * @return a NotFound with a Json body
    */
  def jsonNotFound[T](toJsonable: T)(implicit writes: Writes[T]): Result = {
    json[T](
      status = NotFound,
      toJsonable = toJsonable
    )
  }

  /**
    * Use this instead of Future.successful(Ok(Json.toJson(toJsonable)))
    *
    * @param toJsonable an object which has an implicit Writes (PlayJson)
    * @param writes     implicit Writes
    * @tparam T type of the toJsonable
    * @return a Future Ok with a Json body
    */
  def futureJsonOk[T](toJsonable: T)(implicit writes: Writes[T]): Future[Result] = {
    futureJson[T](
      status = Ok,
      toJsonable = toJsonable
    )
  }

  /**
    * Use this instead of Ok(Json.toJson(toJsonable))
    *
    * @param toJsonable an object which has an implicit Writes (PlayJson)
    * @param writes     implicit Writes
    * @tparam T type of the toJsonable
    * @return a Ok with a Json body
    */
  def jsonOk[T](toJsonable: T)(implicit writes: Writes[T]): Result = {
    json[T](
      status = Ok,
      toJsonable = toJsonable
    )
  }

}
