package uk.gov.ons.addressIndex.server.modules

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

  sealed trait QueryInput[T] {
    def input: T
  }
  implicit case class UprnQueryInput(override val input: String) extends QueryInput[String]
  implicit case class AddressQueryInput(override val input: Seq[CrfTokenResult]) extends QueryInput[Seq[CrfTokenResult]]

  /**
    * @param tokens
    * @param ec
    * @return
    */
  def pafSearch(tokens: QueryInput[Seq[CrfTokenResult]])(implicit ec: ExecutionContext): Future[AddressBySearchResponseContainer] = {
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
    * @param ec
    * @return
    */
  def nagSearch(tokens: QueryInput[Seq[CrfTokenResult]])(implicit ec: ExecutionContext): Future[AddressBySearchResponseContainer] = {
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
    * @param ec
    * @return
    */
  def uprnPafSearch(uprn: QueryInput[String])(implicit ec: ExecutionContext): Future[AddressByUprnResponseContainer] = {
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
    * @param ec
    * @return
    */
  def uprnNagSearch(uprn: QueryInput[String])(implicit ec: ExecutionContext): Future[AddressByUprnResponseContainer] = {
    esRepo queryNagUprn uprn.input map {
      _.map { address =>
        searchUprnContainerTemplate(
          Some(AddressResponseAddress fromNagAddress address)
        )
      } getOrElse NoAddressFoundUprn
    }
  }


  /**
    * This is a PAF or NAG switch helper which can be used for
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
  ): Option[Future[Result]] = {
    (
      formatStr.stringToScheme map {
        case PostcodeAddressFile(_) => pafFn(pafInputForFn)
        case BritishStandard7666(_) => nagFn(nagInputForFn)
      }
    ) map(_.map(jsonOk[T]))
  }
}