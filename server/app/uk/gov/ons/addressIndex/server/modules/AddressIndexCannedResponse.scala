package uk.gov.ons.addressIndex.server.modules

import uk.gov.ons.addressIndex.model.server.response._

trait AddressIndexCannedResponse {

  def searchContainerTemplate(tokens: AddressTokens, addresses: Seq[AddressResponseAddress], total: Int): AddressBySearchResponseContainer = {
    AddressBySearchResponseContainer(
      response = AddressBySearchResponse(
        tokens = tokens,
        addresses = addresses,
        limit = 10,
        offset = 0,
        total = addresses.size
      ),
      status = OkAddressResponseStatus
    )
  }
  def searchUprnContainerTemplate(optAddresses: Option[AddressResponseAddress]): AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      response = AddressByUprnResponse(
        address = optAddresses
      ),
      status = OkAddressResponseStatus
    )
  }

  val NoAddressFoundUprn = {
    AddressByUprnResponseContainer(
      response = AddressByUprnResponse(
        address = None
      ),
      status = NotFoundAddressResponseStatus,
      errors = Seq(NotFoundAddressResponseError)
    )
  }

  val UnsupportedFormatUprn = {
    AddressByUprnResponseContainer(
      response = AddressByUprnResponse(
        address = None
      ),
      status = BadRequestAddressResponseStatus,
      errors = Seq(FormatNotSupportedAddressResponseError)
    )
  }

  val UnsupportedFormat = {
    AddressBySearchResponseContainer(
      response = Error,
      status = BadRequestAddressResponseStatus,
      errors = Seq(FormatNotSupportedAddressResponseError)
    )
  }

  val EmptySearch = {
    AddressBySearchResponseContainer(
      response = Error,
      status = BadRequestAddressResponseStatus,
      errors = Seq(EmptyQueryAddressResponseError)
    )
  }

  val Error = {
    AddressBySearchResponse(
      AddressTokens.empty,
      addresses = Seq.empty,
      limit = 10,
      offset = 0,
      total = 0
    )
  }
}