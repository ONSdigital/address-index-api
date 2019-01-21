package uk.gov.ons.addressIndex.server.modules.response

import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.uprn.{AddressByUprnResponse, AddressByUprnResponseContainer}

trait UPRNControllerResponse extends Response {

  def UprnNotNumeric: AddressByUprnResponseContainer = {
    BadRequestNonNumericUprn
  }

  def BadRequestNonNumericUprn: AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByUprnResponse(
        address = None,
        historical = true,
        epoch = "",
        startDate = "",
        endDate = "",
        verbose = true
      ),
      status = BadRequestAddressResponseStatus,
      errors = Seq(UprnNotNumericAddressResponseError)
    )
  }

  def ErrorUprn: AddressByUprnResponse = {
    AddressByUprnResponse(
      address = None,
      historical = true,
      epoch = "",
      startDate = "",
      endDate = "",
      verbose = true
    )
  }

  def BadRequestUprnTemplate(errors: AddressResponseError*): AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorUprn,
      status = BadRequestAddressResponseStatus,
      errors = errors
    )
  }

  def searchUprnContainerTemplate(optAddresses: Option[AddressResponseAddress]): AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByUprnResponse(
        address = optAddresses,
        historical = true,
        epoch = "",
        startDate = "",
        endDate = "",
        verbose = true
      ),
      status = OkAddressResponseStatus
    )
  }

  def NoAddressFoundUprn: AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByUprnResponse(
        address = None,
        historical = true,
        epoch = "",
        startDate = "",
        endDate = "",
        verbose = true
      ),
      status = NotFoundAddressResponseStatus,
      errors = Seq(NotFoundAddressResponseError)
    )
  }

  def UnsupportedFormatUprn: AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByUprnResponse(
        address = None,
        historical = true,
        epoch = "",
        startDate = "",
        endDate = "",
        verbose = true
      ),
      status = BadRequestAddressResponseStatus,
      errors = Seq(FormatNotSupportedAddressResponseError)
    )
  }

  def UprnEpochInvalid: AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByUprnResponse(
        address = None,
        historical = true,
        epoch = "",
        startDate = "",
        endDate = "",
        verbose = true
      ),
      status = BadRequestAddressResponseStatus,
      errors = Seq(EpochNotAvailableError)
    )
  }

}
