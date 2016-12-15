package uk.gov.ons.addressIndex.server.modules

import play.api.libs.json.Writes
import play.api.mvc.Result
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.{BritishStandard7666, PostcodeAddressFile}
import uk.gov.ons.addressIndex.model.db.index.{NationalAddressGazetteerAddresses, PostcodeAddressFileAddresses}
import uk.gov.ons.addressIndex.model.server.response._
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
    def input: T
  }
  case class UprnQueryInput(override val input: String) extends QueryInput[String]
  case class AddressQueryInput(override val input: Seq[CrfTokenResult]) extends QueryInput[Seq[CrfTokenResult]]

  /**
    * @param tokens
    * @return
    */
  def pafSearch(tokens: QueryInput[Seq[CrfTokenResult]]): Future[AddressBySearchResponseContainer] = {
    esRepo queryPafAddresses tokens.input map { case PostcodeAddressFileAddresses(addresses, maxScore) =>
      searchContainerTemplate(
        tokens = tokens.input,
        addresses = addresses map(AddressResponseAddress fromPafAddress maxScore),
        total = addresses.size
      )
    }
  }

  /**
    * @param tokens
    * @return
    */
  def nagSearch(tokens: QueryInput[Seq[CrfTokenResult]]): Future[AddressBySearchResponseContainer] = {
    esRepo queryNagAddresses tokens.input map { case NationalAddressGazetteerAddresses(addresses, maxScore) =>
      searchContainerTemplate(
        tokens = tokens.input,
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
    esRepo queryPafUprn uprn.input map {
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
    esRepo queryNagUprn uprn.input map {
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
    * @param pafInputForFn the input for pafFn
    * @param pafFn the function which will be called if the formatStr resolves to `PostcodeAddressFile`
    * @param nagInputForFn  the input for nagFn
    * @param nagFn the function which will be called if the formatStr resolves to `BritishStandard7666`
    * @tparam T the return type of the object which will be "PlayJson'd"
    * @tparam QueryInputType the input type for the Query
    * @return If None, the formatStr failed to resole.
    *         If Some, the appropriate object for the given function and resolved format.
    */
  def formatQuery[T, QueryInputType](
    formatStr: String,
    pafInputForFn: QueryInput[QueryInputType],
    pafFn: QueryInput[QueryInputType] => Future[T],
    nagInputForFn: QueryInput[QueryInputType],
    nagFn: QueryInput[QueryInputType] => Future[T]
  )(implicit writes: Writes[T]): Option[Future[Result]] = {
    (
      formatStr.stringToScheme map {
        case _: PostcodeAddressFile => pafFn(pafInputForFn)
        case _: BritishStandard7666 => nagFn(nagInputForFn)
      }
    ) map(_.map(jsonOk[T]))
  }
}