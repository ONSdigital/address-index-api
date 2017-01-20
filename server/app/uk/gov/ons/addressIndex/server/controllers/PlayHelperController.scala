package uk.gov.ons.addressIndex.server.controllers

import akka.event.slf4j.Logger
import play.api.libs.json.{Json, Writes}
import play.api.mvc.{Controller, Result}
import scala.concurrent.Future

trait PlayHelperController extends Controller {

  private val logger = Logger("Response")

  /**
    * Use this instead of Future.successful(BadRequest(Json.toJson(toJsonable)))
    *
    * @param toJsonable an object which has an implicit Writes (PlayJson)
    * @param writes implicit Writes
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
    * Use this instead of BadRequest(Json.toJson(toJsonable))
    *
    * @param toJsonable an object which has an implicit Writes (PlayJson)
    * @param writes implicit Writes
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
    * Use this instead of Future.successful(NotFound(Json.toJson(toJsonable)))
    *
    * @param toJsonable an object which has an implicit Writes (PlayJson)
    * @param writes implicit Writes
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
    * @param writes implicit Writes
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
    * @param writes implicit Writes
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
    * @param writes implicit Writes
    * @tparam T type of the toJsonable
    * @return a Ok with a Json body
    */
  def jsonOk[T](toJsonable: T)(implicit writes: Writes[T]): Result = {
    json[T](
      status = Ok,
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
    logger info s"${status.toString}"
    status(Json toJson toJsonable)
  }
}
