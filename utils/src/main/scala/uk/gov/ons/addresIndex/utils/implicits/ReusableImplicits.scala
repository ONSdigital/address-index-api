package main.scala.uk.gov.ons.addresIndex.utils.implicits

import scala.concurrent.Future
import scala.util._

trait ReusableImplicits {

  /**
    * Implicitly converts a `Try[T]` to a `Future[T]` using the
    * implicit tryToFuture method.
    * @param tried - The try to convert.
    * @tparam T - The type.
    */
  implicit class TryFuture[T](tried: Try[T]) {
    def asFuture: Future[T] = tried
  }

  /**
    * Implicitly converts a `Try` to a `Future`.
    * The outcome of a `Try` is mapped to the outcome of the `Future`.
    *
    * @param t - The try.
    * @tparam A - The type.
    * @return A future which contains the result of the `Try`.
    */
  implicit def tryToFuture[A](t: => Try[A]): Future[A] = {
    Future(t) flatMap {
      case Success(s) => Future successful s
      case Failure(f) => Future failed f
    }
  }
}