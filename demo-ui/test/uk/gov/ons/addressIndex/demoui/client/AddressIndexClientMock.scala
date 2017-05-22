package uk.gov.ons.addressIndex.demoui.client

import javax.inject.{Inject, Singleton}

import play.api.libs.ws.WSClient
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import uk.gov.ons.addressIndex.model.AddressIndexSearchRequest
import uk.gov.ons.addressIndex.model.db.index.Relative
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

  val mockAddressResponseStatus = AddressResponseStatus(
    code = 200,
    message = "OK"
  )

  val mockAddressTokens = Map.empty[String, String]

  val mockPafAddress1 = AddressResponsePaf(
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

  val mockNagAddress1 = AddressResponseNag(
    uprn = "",
    postcodeLocator = "PO7 6GA",
    addressBasePostal=  "",
    usrn = "",
    lpiKey = "",
    pao = AddressResponsePao(
      paoText = "",
      paoStartNumber = "7",
      paoStartSuffix = "",
      paoEndNumber = "",
      paoEndSuffix = ""
    ),
    sao = AddressResponseSao(
      saoText = "",
      saoStartNumber= "",
      saoStartSuffix= "",
      saoEndNumber= "",
      saoEndSuffix = ""
    ),
    level= "",
    officialFlag= "",
    logicalStatus= "1",
    streetDescriptor= "",
    townName= "EXETER",
    locality= "",
    organisation= "",
    legalName= "",
    classificationCode = "R",
    localCustodianCode = "435",
    localCustodianName = "MILTON KEYNES",
    localCustodianGeogCode = "E06000042",
    lpiEndDate = ""
  )

  val mockRelative = Relative (
    level = 1,
    siblings = Array(6L,7L),
    parents = Array(8L,9L)
  )

  val mockRelativeResponse = AddressResponseRelative.fromRelative(mockRelative)

  val mockBespokeScore = AddressResponseScore(
    objectScore = 0d,
    structuralScore = 0d,
    buildingScore = 0d,
    localityScore = 0d,
    unitScore = 0d,
    buildingScoreDebug = "0",
    localityScoreDebug = "0",
    unitScoreDebug = "0")

  val mockAddressResponseAddress = AddressResponseAddress(
    uprn = "",
    parentUprn = "",
    relatives = Seq(mockRelativeResponse),
    formattedAddress = "7, GATE REACH, EXETER, EX2 9GA",
    formattedAddressNag = "7, GATE REACH, EXETER, EX2 9GA",
    formattedAddressPaf = "7, GATE REACH, EXETER, EX2 9GA",
    welshFormattedAddressNag = "7, GATE REACH, EXETER, EX2 9GA",
    welshFormattedAddressPaf = "7, GATE REACH, EXETER, EX2 9GA",
    paf = Some(mockPafAddress1),
    nag = Some(mockNagAddress1),
    geo = None,
    underlyingScore = 1.0f,
    bespokeScore = Some(mockBespokeScore)
  )

  val mockAddressBySearchResponse = AddressBySearchResponse (
    tokens = mockAddressTokens,
    addresses = Seq(mockAddressResponseAddress: AddressResponseAddress),
    limit = 1,
    offset = 1,
    total = 1,
    maxScore = 1f
  )

  val mockSearchResponseContainer = AddressBySearchResponseContainer (
    apiVersion = "mockApi",
    dataVersion = "mockData",
    response = mockAddressBySearchResponse,
    status = mockAddressResponseStatus,
    errors = Seq.empty[AddressResponseError]
  )


  override def addressQuery(request: AddressIndexSearchRequest)(implicit ec: ExecutionContext): Future[AddressBySearchResponseContainer] =
    Future.successful(mockSearchResponseContainer)
}