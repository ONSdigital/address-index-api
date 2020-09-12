package uk.gov.ons.addressIndex.demoui.client

import javax.inject.{Inject, Singleton}
import play.api.libs.ws.WSClient
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientMock._
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import uk.gov.ons.addressIndex.model.db.index.{CrossRef, Relative}
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.postcode.{AddressByPostcodeResponse, AddressByPostcodeResponseContainer}
import uk.gov.ons.addressIndex.model.server.response.uprn.{AddressByUprnResponse, AddressByUprnResponseContainer}
import uk.gov.ons.addressIndex.model.{AddressIndexPostcodeRequest, AddressIndexSearchRequest, AddressIndexUPRNRequest}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Mock client class for tests - returns precanned results
  *
  * @param client client
  * @param conf   conf
  */
@Singleton
class AddressIndexClientMock @Inject()(override val client: WSClient, conf: DemouiConfigModule)
  extends AddressIndexClientInstance(client, conf) {
  //  set config entry to "http://localhost:9001" to run locally
  //  set config entry to "https://addressindexapitest.cfapps.io" to run from cloud
  override def host: String = s"${conf.config.apiURL.host}:${conf.config.apiURL.port}"

  val mockAddressTokens = Map.empty[String, String]

  val mockPostcode = ""

  val mockAddressBySearchResponse: AddressBySearchResponse = AddressBySearchResponse(
    tokens = mockAddressTokens,
    addresses = Seq(mockAddressResponseAddress: AddressResponseAddress),
    limit = 1,
    offset = 1,
    filter = "",
    historical = true,
    rangekm = "2",
    latitude = "50.705948",
    longitude = "-3.5091076",
    total = 1,
    sampleSize = 20,
    maxScore = 1f,
    matchthreshold = 5f,
    verbose = true,
    epoch = "",
    fromsource = "EW",
    eboost = 1,
    nboost = 1,
    sboost = 1,
    wboost = 1
  )

  val mockAddressByPostcodeResponse: AddressByPostcodeResponse = AddressByPostcodeResponse(
    postcode = mockPostcode,
    addresses = Seq(mockAddressResponseAddress: AddressResponseAddress),
    limit = 1,
    offset = 1,
    filter = "",
    historical = true,
    total = 1,
    maxScore = 1f,
    verbose = false,
    epoch = ""
  )

  val mockSearchResponseContainer: AddressBySearchResponseContainer = AddressBySearchResponseContainer(
    apiVersion = "mockApi",
    dataVersion = "mockData",
    response = mockAddressBySearchResponse,
    status = mockAddressResponseStatus,
    errors = Seq.empty[AddressResponseError]
  )

  val mockPostcodeResponseContainer: AddressByPostcodeResponseContainer = AddressByPostcodeResponseContainer(
    apiVersion = "mockApi",
    dataVersion = "mockData",
    response = mockAddressByPostcodeResponse,
    status = mockAddressResponseStatus,
    errors = Seq.empty[AddressResponseError]
  )

  val mockUprnResponseContainer: AddressByUprnResponseContainer = AddressByUprnResponseContainer(
    apiVersion = "mockApi",
    dataVersion = "mockData",
    response = mockAddressByUprnResponse,
    status = mockAddressResponseStatus,
    errors = Seq.empty[AddressResponseError]
  )

  override def addressQuery(request: AddressIndexSearchRequest)(implicit ec: ExecutionContext): Future[AddressBySearchResponseContainer] =
    Future.successful(mockSearchResponseContainer)

  override def postcodeQuery(request: AddressIndexPostcodeRequest)(implicit ec: ExecutionContext): Future[AddressByPostcodeResponseContainer] =
    Future.successful(mockPostcodeResponseContainer)

  override def uprnQuery(request: AddressIndexUPRNRequest)(implicit ec: ExecutionContext): Future[AddressByUprnResponseContainer] =
    Future.successful(mockUprnResponseContainer)
}

object AddressIndexClientMock {
  val mockAddressResponseStatus: AddressResponseStatus = AddressResponseStatus(
    code = 200,
    message = "OK"
  )

  val mockRelative: Relative = Relative(
    level = 1,
    siblings = Array(6L, 7L),
    parents = Array(8L, 9L)
  )

  val mockCrossRef: CrossRef = CrossRef(
    crossReference = "osgb1000000347959147",
    source = "7666MT"
  )

  val mockRelativeResponse: AddressResponseRelative = AddressResponseRelative.fromRelative(mockRelative)
  val mockCrossRefResponse: AddressResponseCrossRef = AddressResponseCrossRef.fromCrossRef(mockCrossRef)

  val mockPafAddress1: AddressResponsePaf = AddressResponsePaf(
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

  val mockNagAddress1: AddressResponseNag = AddressResponseNag(
    uprn = "",
    postcodeLocator = "PO7 6GA",
    addressBasePostal = "",
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
      saoStartNumber = "",
      saoStartSuffix = "",
      saoEndNumber = "",
      saoEndSuffix = ""
    ),
    level = "",
    officialFlag = "",
    logicalStatus = "1",
    streetDescriptor = "",
    townName = "EXETER",
    locality = "",
    organisation = "",
    legalName = "",
    localCustodianCode = "435",
    localCustodianName = "MILTON KEYNES",
    localCustodianGeogCode = "E06000042",
    lpiEndDate = "",
    lpiStartDate = ""
  )

  val mockNisraAddress1: AddressResponseNisra = AddressResponseNisra(
    organisationName = "",
    subBuildingName = "",
    buildingName = "",
    buildingNumber = "7",
    pao = AddressResponsePao(
      paoText = "",
      paoStartNumber = "7",
      paoStartSuffix = "",
      paoEndNumber = "",
      paoEndSuffix = ""
    ),
    sao = AddressResponseSao(
      saoText = "",
      saoStartNumber = "",
      saoStartSuffix = "",
      saoEndNumber = "",
      saoEndSuffix = ""
    ),
    thoroughfare = "",
    altThoroughfare = "",
    dependentThoroughfare = "GATE REACH",
    locality = "",
    townName = "EXETER",
    postcode = "PO7 6GA",
    uprn = "",
    classificationCode = "",
    udprn = "",
    creationDate = "",
    commencementDate = "",
    archivedDate = "",
    mixedNisra = "",
    addressStatus = "APPROVED",
    buildingStatus = "",
    localCouncil = "BELFAST"
  )

  val mockAuxiliaryAddress = AddressResponseAuxiliary(
    uprn = "1",
    organisationName = "2",
    subBuildingName = "3",
    buildingName = "4",
    buildingNumber = "5",
    paoStartNumber = "6",
    paoStartSuffix = "7",
    paoEndNumber = "8",
    saoStartSuffix = "9",
    saoEndSuffix = "10",
    streetName = "11",
    locality = "12",
    townName = "13",
    location = AddressResponseAuxiliaryAddressLocation("14", "15"),
    addressLevel = "16",
    addressAll = "mixedAuxiliary",
    addressLine1 = "17",
    addressLine2 = "18",
    addressLine3 = "19"
  )

  val mockBespokeScore: AddressResponseScore = AddressResponseScore(
    objectScore = 0d,
    structuralScore = 0d,
    buildingScore = 0d,
    localityScore = 0d,
    unitScore = 0d,
    buildingScoreDebug = "0",
    localityScoreDebug = "0",
    unitScoreDebug = "0",
    ambiguityPenalty = 1d)

  val mockAddressResponseAddress: AddressResponseAddress = AddressResponseAddress(
    uprn = "",
    parentUprn = "",
    relatives = Some(Seq(mockRelativeResponse)),
    crossRefs = Some(Seq(mockCrossRefResponse)),
    formattedAddress = "7, GATE REACH, EXETER, EX2 9GA",
    formattedAddressNag = "7, GATE REACH, EXETER, EX2 9GA",
    formattedAddressPaf = "7, GATE REACH, EXETER, EX2 9GA",
    formattedAddressNisra = "",
    welshFormattedAddressNag = "7, GATE REACH, EXETER, EX2 9GA",
    welshFormattedAddressPaf = "7, GATE REACH, EXETER, EX2 9GA",
    formattedAddressAuxiliary = "7, GATE REACH, EXETER, EX2 9GA",
    paf = Some(mockPafAddress1),
    nag = Some(Seq(mockNagAddress1)),
    nisra = Some(mockNisraAddress1),
    auxiliary = Some(mockAuxiliaryAddress),
    geo = None,
    classificationCode = "RD",
    lpiLogicalStatus = "1",
    confidenceScore = 100f,
    underlyingScore = 1.0f,
    census = AddressResponseCensus("TBA", "TBA", "E"),
    highlights = None
  )

  val mockAddressByUprnResponse: AddressByUprnResponse = AddressByUprnResponse(
    address = Some(mockAddressResponseAddress: AddressResponseAddress),
    historical = true,
    verbose = true,
    epoch = ""
  )
}
