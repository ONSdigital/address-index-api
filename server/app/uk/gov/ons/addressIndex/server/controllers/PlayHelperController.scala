package uk.gov.ons.addressIndex.server.controllers

import play.api.libs.json.{Json, Writes}
import play.api.mvc.{Controller, Result}
import scala.concurrent.Future

abstract class PlayHelperController extends Controller {

  /**
    *
    * @param toJsonable
    * @param writes
    * @tparam T
    * @return
    */
  def futureJsonBadRequest[T](toJsonable: T)(implicit writes: Writes[T]): Future[Result] = {
    futureJson[T](
      status = BadRequest,
      toJsonable = toJsonable
    )
  }

  /**
    *
    * @param toJsonable
    * @param writes
    * @tparam T
    * @return
    */
  def jsonBadRequest[T](toJsonable: T)(implicit writes: Writes[T]): Result = {
    json[T](
      status = BadRequest,
      toJsonable = toJsonable
    )
  }

  /**
    *
    * @param toJsonable
    * @param writes
    * @tparam T
    * @return
    */
  def futureJsonNotFound[T](toJsonable: T)(implicit writes: Writes[T]): Future[Result] = {
    futureJson[T](
      status = NotFound,
      toJsonable = toJsonable
    )
  }

  /**
    *
    * @param toJsonable
    * @param writes
    * @tparam T
    * @return
    */
  def jsonNotFound[T](toJsonable: T)(implicit writes: Writes[T]): Result = {
    json[T](
      status = NotFound,
      toJsonable = toJsonable
    )
  }

  /**
    *
    * @param toJsonable
    * @param writes
    * @tparam T
    * @return
    */
  def futureJsonOk[T](toJsonable: T)(implicit writes: Writes[T]): Future[Result] = {
    futureJson[T](
      status = Ok,
      toJsonable = toJsonable
    )
  }

  /**
    *
    * @param toJsonable
    * @param writes
    * @tparam T
    * @return
    */
  def jsonOk[T](toJsonable: T)(implicit writes: Writes[T]): Result = {
    json[T](
      status = Ok,
      toJsonable = toJsonable
    )
  }

  /**
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
}
