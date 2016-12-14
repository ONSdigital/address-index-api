package uk.gov.ons.addressIndex.server.modules

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
  def pafSearch(tokens: AddressTokens)(implicit ec: ExecutionContext): Future[AddressBySearchResponseContainer] = {
    esRepo queryPafAddresses tokens map { case PostcodeAddressFileAddresses(addresses, maxScore) =>
      AddressBySearchResponseContainer(
        response = AddressBySearchResponse(
          tokens = tokens,
          addresses = addresses map(AddressResponseAddress fromPafAddress maxScore),
          limit = 10,
          offset = 0,
          total = addresses.size
        ),
        status = OkAddressResponseStatus
      )
    }
  }

  /**
    * @param tokens
    * @param ec
    * @return
    */
  def nagSearch(tokens: AddressTokens)(implicit ec: ExecutionContext): Future[AddressBySearchResponseContainer] = {
    esRepo queryNagAddresses tokens map { case NationalAddressGazetteerAddresses(addresses, maxScore) =>
      AddressBySearchResponseContainer(
        response = AddressBySearchResponse(
          tokens = tokens,
          addresses = addresses map(AddressResponseAddress fromNagAddress maxScore),
          limit = 10,
          offset = 0,
          total = addresses.size
        ),
        status = OkAddressResponseStatus
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
        AddressByUprnResponseContainer(
          response = AddressByUprnResponse(
            address = Some(AddressResponseAddress.fromPafAddress(address))
          ),
          status = OkAddressResponseStatus
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
        AddressByUprnResponseContainer(
          response = AddressByUprnResponse(
            address = Some(AddressResponseAddress fromNagAddress address)
          ),
          status = OkAddressResponseStatus
        )
      } getOrElse NoAddressFoundUprn
    }
  }
}