package uk.gov.ons.addressIndex.demoui.client

import javax.inject.{Inject, Singleton}

import play.api.libs.ws.WSClient
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import uk.gov.ons.addressIndex.model.AddressIndexSearchRequest
import uk.gov.ons.addressIndex.model.server.response._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Mock client class for tests - returns precanned results
  * @param client
  * @param conf
  */
@Singleton
class AddressIndexClientMock @Inject()(override val client : WSClient,
                                           conf : DemouiConfigModule) extends AddressIndexClientInstance(client,conf) {
  //  set config entry to "http://localhost:9001" to run locally
  //  set config entry to "https://addressindexapitest.cfapps.io" to run from cloud
  override def host: String = s"${conf.config.apiURL.host}:${conf.config.apiURL.port}"

  val mockAddressResponseStatus = Status(
    code = 200,
    message = "OK"
  )

  val mockAddressTokens = Seq.empty
//  AddressTokens(
//    uprn = "",
//    buildingNumber = "7",
//    postcode = "EX2 9GA"
//  )

  val mockPafAddress1 = PAF(
    udprn = "",
    organisationName = "",
    departmentName = "",
    subBuildingName = "",
    buildingName = "",
    buildingNumber = "7",
    dependentThoroughfare = "GATE REACH",
    thoroughfare = "",
    doubleDependentLocality = "",
    dependentLocality = "",
    postTown = "EXETER",
    postcode = "PO7 6GA",
    postcodeType = "",
    deliveryPointSuffix = "",
    welshDependentThoroughfare = "",
    welshThoroughfare = "",
    welshDoubleDependentLocality = "",
    welshDependentLocality = "",
    welshPostTown = "",
    poBoxNumber = "",
    startDate = "",
    endDate = ""
  )

  val mockAddressResponseAddress = AddressResponseAddress(
    uprn = "",
    formattedAddress = "7, GATE REACH, EXETER, EX2 9GA",
    paf = Some(mockPafAddress1),
    nag = None,
    geo = None,
    underlyingScore = 1.0f,
    underlyingMaxScore =  1.0f
  )

  val mockAddressBySearchResponse = Results (
    tokens = mockAddressTokens,
    addresses = Seq(mockAddressResponseAddress: AddressResponseAddress),
    limit = 1,
    offset = 1,
    total = 1
  )

  val mockSearchResponseContainer = Container (
    response = mockAddressBySearchResponse,
    status = mockAddressResponseStatus,
    errors = Seq.empty[Error]
  )


  override def addressQuery(request: AddressIndexSearchRequest)(implicit ec: ExecutionContext): Future[Container] =
    Future.successful(mockSearchResponseContainer)
}