package uk.gov.ons.addressIndex.server.modules

import com.sksamuel.elastic4s.RichSearchResponse
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.AddressScheme
import uk.gov.ons.addressIndex.server.controllers.PlayHelperController
import uk.gov.ons.addressIndex.server.modules.Model.Pagination
import scala.concurrent.{ExecutionContext, Future}

trait AddressIndexActions { self: AddressIndexCannedResponse with PlayHelperController =>

  def esRepo: ElasticSearchRepository

  /**
    * required for handing of Futures.
    */
  implicit def ec: ExecutionContext

  case class AddressQueryInput(
    tokens: Seq[CrfTokenResult],
    pagination: Pagination
  )

  def addressSearch(input: AddressQueryInput, format: Option[AddressScheme]): Future[RichSearchResponse] = {
    implicit val implPag = input.pagination
    implicit val implFmt = format
    esRepo queryAddress(
      tokens = input.tokens
    )
  }

  def uprnSearch(uprn: String, format: Option[AddressScheme]): Future[RichSearchResponse] = {
    implicit val implFmt = format
    esRepo queryUprn uprn
  }
}