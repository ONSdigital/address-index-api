package uk.gov.ons.addressIndex.server.modules

import play.api.libs.json.Writes
import play.api.mvc.Result
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.{AddressScheme, BritishStandard7666, PostcodeAddressFile}
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
    def offset: Int
    def limit: Int
  }
  case class UprnQueryInput(override val input: String, val offset: Int = 0, val limit: Int = 1) extends QueryInput[String]
  case class AddressQueryInput(override val input: Seq[CrfTokenResult], val offset: Int, val limit: Int) extends QueryInput[Seq[CrfTokenResult]]

  /**
    * @param tokens
    * @return
    */
  def pafSearch(tokens: QueryInput[Seq[CrfTokenResult]]): Future[AddressBySearchResponseContainer] = {
    esRepo queryPafAddresses(tokens.offset, tokens.limit, tokens.input) map { case PostcodeAddressFileAddresses(addresses, maxScore) =>
      searchContainerTemplate(
        tokens = tokens.input,
        addresses = addresses map(AddressResponseAddress fromPafAddress maxScore),
        total = addresses.size,
        limit = tokens.limit,
        offset = tokens.offset - 1
      )
    }
  }

  /**
    * @param tokens
    * @return
    */
  def nagSearch(tokens: QueryInput[Seq[CrfTokenResult]]): Future[AddressBySearchResponseContainer] = {
    esRepo queryNagAddresses (tokens.offset, tokens.limit, tokens.input) map { case NationalAddressGazetteerAddresses(addresses, maxScore) =>
      searchContainerTemplate(
        tokens = tokens.input,
        addresses = addresses map(AddressResponseAddress fromNagAddress maxScore),
        total = addresses.size,
        limit = tokens.limit,
        offset = tokens.offset - 1
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

  case class RejectedAddress(tokens: Seq[CrfTokenResult], exception: Throwable)
  case class MultipleSearchResult(
    successfulAddresses: Seq[AddressBySearchResponseContainer],
    failedAddresses: Seq[RejectedAddress]
  )

  /**
    * A fairly complex method
    * I left type annotations for `val` to make it easier to follow
    * @param inputs an iterator containing a collectioin of tokens per each lines,
    *               typically a result of a parser applied to `Source.fromFile("/path").getLines`
    * @param format format scheme
    * @return MultipleSearchResult containing successful addresses and the information on the rejected ones
    */
  def multipleSearch(inputs: Iterator[Seq[CrfTokenResult]], format: AddressScheme ): Future[MultipleSearchResult] = {

    val addressesRequests: Iterator[Future[Either[RejectedAddress, AddressBySearchResponseContainer]]] =
      inputs.map { tokens =>

        val addressRequest: Future[AddressBySearchResponseContainer] = format match {
          case _: PostcodeAddressFile => pafSearch(AddressQueryInput(tokens, 0, 1))
          case _: BritishStandard7666 => nagSearch(AddressQueryInput(tokens, 0, 1))
        }

        // Successful requests are stored in the `Right`
        // Failed requests will be stored in the `Left`
        addressRequest.map(Right(_)).recover {
          case exception: Throwable => Left(RejectedAddress(tokens, exception))
        }

      }

    // This also transforms lazy `Iterator` into an in-memory sequence
    val addresses: Future[Seq[Either[RejectedAddress, AddressBySearchResponseContainer]]] = Future.sequence(addressesRequests.toList)

    val successfulAddresses: Future[Seq[AddressBySearchResponseContainer]] = addresses.map(collectSuccessfulAddresses)

    val failedAddresses: Future[Seq[RejectedAddress]] = addresses.map(collectFailedAddresses)

    // transform (Future(X), Future[Y]) into Future(X, Y)
    for {
      successful <- successfulAddresses
      failed <- failedAddresses
    } yield MultipleSearchResult(successful, failed)
  }


  private def collectSuccessfulAddresses(addresses: Seq[Either[RejectedAddress, AddressBySearchResponseContainer]]): Seq[AddressBySearchResponseContainer] =
    addresses.collect {
      case Right(address) => address
    }

  private def collectFailedAddresses(addresses: Seq[Either[RejectedAddress, AddressBySearchResponseContainer]]): Seq[RejectedAddress] =
    addresses.collect {
      case Left(address) => address
    }

}