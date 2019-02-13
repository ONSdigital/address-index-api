package uk.gov.ons.addressIndex.server.modules.response

import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.uprn.{AddressByUprnResponse, AddressByUprnResponseContainer}

trait UPRNControllerResponse extends Response {

  def UprnNotNumeric(queryValues: Map[String,Any]): AddressByUprnResponseContainer = {
    BadRequestNonNumericUprn(queryValues)
  }

  def BadRequestNonNumericUprn(queryValues: Map[String,Any]): AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByUprnResponse(
        address = None,
        historical = queryValues("historical").asInstanceOf[Boolean],
        epoch = queryValues("epoch").toString,
        startDate = queryValues("startDate").toString,
        endDate = queryValues("endDate").toString,
        verbose = queryValues("verbose").asInstanceOf[Boolean]
      ),
      status = BadRequestAddressResponseStatus,
      errors = Seq(UprnNotNumericAddressResponseError)
    )
  }

  def ErrorUprn(queryValues: Map[String,Any]): AddressByUprnResponse = {
    AddressByUprnResponse(
      address = None,
      historical = queryValues("historical").asInstanceOf[Boolean],
      epoch = queryValues("epoch").toString,
      startDate = queryValues("startDate").toString,
      endDate = queryValues("endDate").toString,
      verbose = queryValues("verbose").asInstanceOf[Boolean]
    )
  }

  def BadRequestUprnTemplate(queryValues: Map[String,Any], errors: AddressResponseError*): AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = ErrorUprn(queryValues),
      status = BadRequestAddressResponseStatus,
      errors = errors
    )
  }

  def searchUprnContainerTemplate(queryValues: Map[String,Any], optAddresses: Option[AddressResponseAddress]): AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByUprnResponse(
        address = optAddresses,
        historical = queryValues("historical").asInstanceOf[Boolean],
        epoch = queryValues("epoch").toString,
        startDate = queryValues("startDate").toString,
        endDate = queryValues("endDate").toString,
        verbose = queryValues("verbose").asInstanceOf[Boolean]
      ),
      status = OkAddressResponseStatus
    )
  }

  def NoAddressFoundUprn(queryValues: Map[String,Any]): AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByUprnResponse(
        address = None,
        historical = queryValues("historical").asInstanceOf[Boolean],
        epoch = queryValues("epoch").toString,
        startDate = queryValues("startDate").toString,
        endDate = queryValues("endDate").toString,
        verbose = queryValues("verbose").asInstanceOf[Boolean]
      ),
      status = NotFoundAddressResponseStatus,
      errors = Seq(NotFoundAddressResponseError)
    )
  }

  def UnsupportedFormatUprn(queryValues: Map[String,Any]): AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByUprnResponse(
        address = None,
        historical = queryValues("historical").asInstanceOf[Boolean],
        epoch = queryValues("epoch").toString,
        startDate = queryValues("startDate").toString,
        endDate = queryValues("endDate").toString,
        verbose = queryValues("verbose").asInstanceOf[Boolean]
      ),
      status = BadRequestAddressResponseStatus,
      errors = Seq(FormatNotSupportedAddressResponseError)
    )
  }

  def UprnEpochInvalid(queryValues: Map[String,Any]): AddressByUprnResponseContainer = {
    AddressByUprnResponseContainer(
      apiVersion = apiVersion,
      dataVersion = dataVersion,
      response = AddressByUprnResponse(
        address = None,
        historical = queryValues("historical").asInstanceOf[Boolean],
        epoch = queryValues("epoch").toString,
        startDate = queryValues("startDate").toString,
        endDate = queryValues("endDate").toString,
        verbose = queryValues("verbose").asInstanceOf[Boolean]
      ),
      status = BadRequestAddressResponseStatus,
      errors = Seq(EpochNotAvailableError)
    )
  }

}
