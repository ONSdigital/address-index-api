package uk.gov.ons.addressIndex.server.modules

import play.api.libs.json.Writes
import play.api.mvc.Result
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.{BritishStandard7666, PostcodeAddressFile}
import uk.gov.ons.addressIndex.model.db.index.{NationalAddressGazetteerAddresses, PostcodeAddressFileAddresses}
import uk.gov.ons.addressIndex.model.server.response._
import uk.gov.ons.addressIndex.server.controllers.PlayHelperController
import uk.gov.ons.addressIndex.model.AddressScheme._
import uk.gov.ons.addressIndex.server.modules.Model.Pagination

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
    def pagination: Pagination
  }

  case class UprnQueryInput(
   override val tokens: String,
   pagination: Pagination
  ) extends QueryInput[String]

  case class AddressQueryInput(
    override val tokens: Seq[CrfTokenResult],
    pagination: Pagination
  ) extends QueryInput[Seq[CrfTokenResult]]

  /**
    * @param input
    * @return
    */
  def pafSearch(input: QueryInput[Seq[CrfTokenResult]]): Future[AddressBySearchResponseContainer] = {
    implicit val pagination = input.pagination
    esRepo queryPafAddresses(
      tokens = input.tokens
      ) map { case PostcodeAddressFileAddresses(addresses, maxScore) =>
      searchContainerTemplate(
        tokens = input.tokens,
        addresses = addresses map(AddressResponseAddress fromPafAddress maxScore),
        total = addresses.size
      )
    }
  }

  /**
    * @param input
    * @return
    */
  def nagSearch(input: QueryInput[Seq[CrfTokenResult]]): Future[AddressBySearchResponseContainer] = {
    implicit val pagination = input.pagination
    esRepo queryNagAddresses (
      tokens = input.tokens
    ) map { case NationalAddressGazetteerAddresses(addresses, maxScore) =>
      searchContainerTemplate(
        tokens = input.tokens,
        addresses = addresses map(AddressResponseAddress fromNagAddress maxScore),
        total = addresses.size
      )
    }
  }

  /**
    * @param uprn
    * @return
    */
  def uprnPafSearch(uprn: QueryInput[String]): Future[AddressByUprnResponseContainer] = {
    esRepo queryPafUprn uprn.tokens map {
      _.map { address =>
        searchUprnContainerTemplate(
          Some(AddressResponseAddress fromPafAddress address)
        )
      } getOrElse NoAddressFoundUprn
    }
  }

  /**
    * @param uprn
    * @return
    */
  def uprnNagSearch(uprn: QueryInput[String]): Future[AddressByUprnResponseContainer] = {
    esRepo queryNagUprn uprn.tokens map {
      _.map { address =>
        searchUprnContainerTemplate(
          Some(AddressResponseAddress fromNagAddress address)
        )
      } getOrElse NoAddressFoundUprn
    }
  }

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
  def formatQuery[T, QueryInputType](
    formatStr: String,
    inputForPafFn: QueryInput[QueryInputType],
    pafFn: QueryInput[QueryInputType] => Future[T],
    inputForNagFn: QueryInput[QueryInputType],
    nagFn: QueryInput[QueryInputType] => Future[T]
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
  def hybridSearch(input: AddressQueryInput): Option[Future[_]] = {
    implicit val implPag = input.pagination
    Some(
      esRepo queryHybrid(
        tokens = input.tokens
      )
    )
  }
}