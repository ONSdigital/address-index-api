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

  def esRepo: ElasticSearchRepository

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
    * @param input
    * @return
    */
  def addressSearch(input: AddressQueryInput, format: Option[AddressScheme]): Future[RichSearchResponse] = {
    implicit val implPag = input.pagination
    esRepo queryAddress(
      tokens = input.tokens
    )
  }
}