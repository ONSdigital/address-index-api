package uk.gov.ons.addressIndex.server.modules

import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.db.index.{NationalAddressGazetteerAddresses, PostcodeAddressFileAddresses}
import uk.gov.ons.addressIndex.model.server.response._

import scala.concurrent.{ExecutionContext, Future}

trait AddressActions {
  self: AddressIndexCannedResponse =>

  /**
    * @return
    */
  def esRepo: ElasticsearchRepository

  /**
    * @param tokens
    * @param ec
    * @return
    */
  def pafSearch(tokens:  Seq[CrfTokenResult])(implicit ec: ExecutionContext): Future[AddressBySearchResponseContainer] = {
    esRepo queryPafAddresses tokens map { case PostcodeAddressFileAddresses(addresses, maxScore) =>
      searchContainerTemplate(
        tokens = tokens,
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
  def nagSearch(tokens: Seq[CrfTokenResult])(implicit ec: ExecutionContext): Future[AddressBySearchResponseContainer] = {
    esRepo queryNagAddresses tokens map { case NationalAddressGazetteerAddresses(addresses, maxScore) =>
      searchContainerTemplate(
        tokens = tokens,
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
  def uprnPafSearch(uprn: String)(implicit ec: ExecutionContext): Future[AddressByUprnResponseContainer] = {
    esRepo queryPafUprn uprn map {
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
  def uprnNagSearch(uprn: String)(implicit ec: ExecutionContext): Future[AddressByUprnResponseContainer] = {
    esRepo queryNagUprn uprn map {
      _.map { address =>
        searchUprnContainerTemplate(
          Some(AddressResponseAddress fromNagAddress address)
        )
      } getOrElse NoAddressFoundUprn
    }
  }
}