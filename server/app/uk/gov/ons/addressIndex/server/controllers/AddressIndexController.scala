package uk.gov.ons.addressIndex.server.controllers

import play.api.libs.json.Json
import play.api.mvc.{Controller, Result}
import scala.concurrent.Future

abstract class AddressIndexController extends Controller {

  /**
    * @param toJsonable
    * @tparam T
    * @return
    */
  def futureJsonBadRequest[T](toJsonable: T): Future[Result] = {
    futureJson[T](
      status = BadRequest,
      toJsonable = toJsonable
    )
  }

  /**
    * @param toJsonable
    * @tparam T
    * @return
    */
  def jsonBadRequest[T](toJsonable: T): Result = {
    json[T](
      status = BadRequest,
      toJsonable = toJsonable
    )
  }
  /**
    * @param toJsonable
    * @tparam T
    * @return
    */
  def futureJsonNotFound[T](toJsonable: T): Future[Result] = {
    futureJson[T](
      status = NotFound,
      toJsonable = toJsonable
    )
  }

  /**
    * @param toJsonable
    * @tparam T
    * @return
    */
  def jsonNotFound[T](toJsonable: T): Result = {
    json[T](
      status = NotFound,
      toJsonable = toJsonable
    )
  }

  /**
    * @param toJsonable
    * @tparam T
    * @return
    */
  def futureJsonOk[T](toJsonable: T): Future[Result] = {
    futureJson[T](
      status = Ok,
      toJsonable = toJsonable
    )
  }

  /**
    * @param toJsonable
    * @tparam T
    * @return
    */
  def jsonOk[T](toJsonable: T): Result = {
    json[T](
      status = Ok,
      toJsonable = toJsonable
    )
  }

  /**
    * @param status
    * @param toJsonable
    * @tparam T
    * @return
    */
  def futureJson[T](status: Status, toJsonable: T): Future[Result] = {
    Future successful json(status, toJsonable)
  }

  /**
    * @param status
    * @param toJsonable
    * @tparam T
    * @return
    */
  def json[T](status: Status, toJsonable: T): Result = {
    status(Json toJson toJsonable)
  }
}