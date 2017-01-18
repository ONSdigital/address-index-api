package uk.gov.ons.addressIndex.server.modules

import com.sksamuel.elastic4s.RichSearchResponse
import play.api.libs.json.Writes
import play.api.mvc.Result
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.{AddressScheme, BritishStandard7666, PostcodeAddressFile}
import uk.gov.ons.addressIndex.server.controllers.PlayHelperController
import uk.gov.ons.addressIndex.model.AddressScheme._

import scala.concurrent.{ExecutionContext, Future}

trait AddressIndexActions { self: AddressIndexCannedResponse with PlayHelperController =>

  def esRepo: ElasticsearchRepository

  /**
    * required for handing of Futures.
    */
  implicit def ec: ExecutionContext

  /**
    * A simple type class which is used for distinction between query input types
    */
  sealed trait QueryInput[T] {
    def tokens: T
  }

  trait Pagination {
    def pagination: Model.Pagination
  }

  case class UprnQueryInput(
   override val tokens: String
  ) extends QueryInput[String]

  case class AddressQueryInput(
    override val tokens: Seq[CrfTokenResult],
    override val pagination: Model.Pagination
  ) extends QueryInput[Seq[CrfTokenResult]] with Pagination


  /**
    * This is a PAF or NAG switch helper which can be used for creating a Future[Ok[Json]]
    *
    * @param formatStr the input format String
    * @param inputForPafFn the input for pafFn
    * @param pafFn the function which will be called if the formatStr resolves to `PostcodeAddressFile`
    * @param inputForNagFn  the input for nagFn
    * @param nagFn the function which will be called if the formatStr resolves to `BritishStandard7666`
    * @tparam T the return type of the object which will be "PlayJson'd"
    * @tparam QueryInputType the input type for the Query
    * @return If None, the formatStr failed to resole.
    *         If Some, the appropriate object for the given function and resolved format.
    */
  def formatQuery[T, X, QueryInputType <: QueryInput[X]](
    formatStr: String,
    inputForPafFn: QueryInputType,
    pafFn: QueryInputType => Future[T],
    inputForNagFn: QueryInputType,
    nagFn: QueryInputType => Future[T]
  )(implicit writes: Writes[T]): Option[Future[Result]] = {
    (
      formatStr.stringToScheme map {
        case _: PostcodeAddressFile => pafFn(inputForPafFn)
        case _: BritishStandard7666 => nagFn(inputForNagFn)
      }
    ) map(_.map(jsonOk[T]))
  }

  /**
    * @param input
    * @return
    */
  def addressSearch(input: AddressQueryInput, format: Option[AddressScheme]): Option[Future[RichSearchResponse]] = {
    implicit val implPag = input.pagination
    Some(
      esRepo queryAddress(
        tokens = input.tokens
      )
    )
  }
}