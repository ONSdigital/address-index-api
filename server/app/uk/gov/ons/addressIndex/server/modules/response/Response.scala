package uk.gov.ons.addressIndex.server.modules.response

import play.api.libs.json.{Json, Writes}
import play.api.mvc.Result
import play.api.mvc.Results.{BadRequest, Ok, Unauthorized, _}
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.server.model.dao.QueryValues

import scala.concurrent.Future

trait Response {

  val dataVersion: String
  val apiVersion: String

  def BadRequestTemplate(queryValues: QueryValues, errors: AddressResponseError*): AddressBySearchResponseContainer = {
    AddressBySearchResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = Error(queryValues),
      status = BadRequestAddressResponseStatus,
      errors = errors
    )
  }

  def FailedRequestToEs(detail: String, queryValues: QueryValues): AddressBySearchResponseContainer = {
    val enhancedError = new AddressResponseError(FailedRequestToEsError.code, FailedRequestToEsError.message.replace("see logs", detail))
    AddressBySearchResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = Error(queryValues),
      status = InternalServerErrorAddressResponseStatus,
      errors = Seq(enhancedError)
    )
  }

  def FailedRequestToEsTooBusy(detail: String, queryValues: QueryValues): AddressBySearchResponseContainer = {
    val enhancedError = new AddressResponseError(FailedRequestToEsError.code, FailedRequestToEsError.message.replace("see logs", detail))
    AddressBySearchResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = Error(queryValues),
      status = TooManyRequestsResponseStatus,
      errors = Seq(enhancedError)
    )
  }

  def SourceMissing(queryValues: QueryValues): AddressBySearchResponseContainer = {
    UnauthorizedRequestTemplate(queryValues, SourceMissingError)
  }

  def SourceInvalid(queryValues: QueryValues): AddressBySearchResponseContainer = {
    UnauthorizedRequestTemplate(queryValues, SourceInvalidError)
  }

  def KeyMissing(queryValues: QueryValues): AddressBySearchResponseContainer = {
    UnauthorizedRequestTemplate(queryValues, ApiKeyMissingError)
  }

  def KeyInvalid(queryValues: QueryValues): AddressBySearchResponseContainer = {
    UnauthorizedRequestTemplate(queryValues, ApiKeyInvalidError)
  }

  private def UnauthorizedRequestTemplate(queryValues: QueryValues, errors: AddressResponseError*): AddressBySearchResponseContainer = {
    AddressBySearchResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = Error(queryValues),
      status = UnauthorizedRequestAddressResponseStatus,
      errors = errors
    )
  }

  def Error(queryValues: QueryValues): AddressBySearchResponse = {
    AddressBySearchResponse(
      Map.empty,
      addresses = Seq.empty,
      filter = queryValues.filterOrDefault,
      historical = queryValues.historicalOrDefault,
      epoch = queryValues.epochOrDefault,
      rangekm = queryValues.rangeKMOrDefault.toString,
      latitude = queryValues.latitudeOrDefault,
      longitude = queryValues.longitudeOrDefault,
      limit = queryValues.limitOrDefault,
      offset = queryValues.offsetOrDefault,
      total = 0,
      sampleSize = 20,
      maxScore = 0f,
      matchthreshold = 5f,
      verbose = queryValues.verboseOrDefault,
      fromsource = queryValues.fromSourceOrDefault
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
    * @param status     Status
    * @param toJsonable Able to convert to JSON
    * @param writes     Writes param
    * @tparam T T param
    * @return
    */
  def futureJson[T](status: Status, toJsonable: T)(implicit writes: Writes[T]): Future[Result] = {
    Future successful json(status, toJsonable)
  }

  /**
    * Helper for creating Status(Json.toJson(toJsonable))
    *
    * @param status     Status
    * @param toJsonable Able to convert to JSON
    * @param writes     Writes param
    * @tparam T T param
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
