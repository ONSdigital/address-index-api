package uk.gov.ons.addressIndex.server.controllers

import com.sksamuel.elastic4s.Indexes
import com.sksamuel.elastic4s.requests.searches.SearchRequest
import org.scalatestplus.play._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{ControllerComponents, RequestHeader, Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.ons.addressIndex.model.config.AddressIndexConfig
import uk.gov.ons.addressIndex.model.db.index._
import uk.gov.ons.addressIndex.model.db.{BulkAddress, BulkAddressRequestData, BulkAddresses}
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.bulk.AddressBulkResponseAddress
import uk.gov.ons.addressIndex.model.server.response.partialaddress.{AddressByPartialAddressResponse, AddressByPartialAddressResponseContainer}
import uk.gov.ons.addressIndex.model.server.response.postcode.{AddressByPostcodeResponse, AddressByPostcodeResponseContainer}
import uk.gov.ons.addressIndex.model.server.response.random.{AddressByRandomResponse, AddressByRandomResponseContainer}
import uk.gov.ons.addressIndex.model.server.response.uprn.{AddressByUprnResponse, AddressByUprnResponseContainer}
import uk.gov.ons.addressIndex.server.modules._
import uk.gov.ons.addressIndex.server.modules.validation._
import uk.gov.ons.addressIndex.server.utils.{APIThrottle, HopperScoreHelper}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class AddressControllerSpec extends PlaySpec with Results {

  val validPafAddress: PostcodeAddressFileAddress = PostcodeAddressFileAddress(
    recordIdentifier = "1",
    changeType = "2",
    proOrder = "3",
    uprn = "4",
    udprn = "5",
    organisationName = "6",
    departmentName = "7",
    subBuildingName = "8",
    buildingName = "9",
    buildingNumber = "10",
    dependentThoroughfare = "11",
    thoroughfare = "12",
    doubleDependentLocality = "13",
    dependentLocality = "14",
    postTown = "15",
    postcode = "B16 8TH",
    postcodeType = "17",
    deliveryPointSuffix = "18",
    welshDependentThoroughfare = "19",
    welshThoroughfare = "20",
    welshDoubleDependentLocality = "21",
    welshDependentLocality = "22",
    welshPostTown = "23",
    poBoxNumber = "24",
    processDate = "25",
    startDate = "26",
    endDate = "27",
    lastUpdateDate = "28",
    entryDate = "29",
    pafAll = "30",
    mixedPaf = "31",
    mixedWelshPaf = "32"
  )

  val validNagAddress: NationalAddressGazetteerAddress = NationalAddressGazetteerAddress(
    uprn = "1",
    postcodeLocator = "B16 8TH",
    addressBasePostal = "3",
    latitude = "24",
    longitude = "25",
    easting = "27",
    northing = "28",
    organisation = "22",
    legalName = "23",
    usrn = "4",
    lpiKey = "5",
    paoText = "6",
    paoStartNumber = "72",
    paoStartSuffix = "8",
    paoEndNumber = "9",
    paoEndSuffix = "10",
    saoText = "11",
    saoStartNumber = "12",
    saoStartSuffix = "13",
    saoEndNumber = "14",
    saoEndSuffix = "15",
    level = "16",
    officialFlag = "17",
    streetDescriptor = "19",
    townName = "20",
    locality = "21",
    lpiLogicalStatus = "lpiLogicalStatus",
    blpuLogicalStatus = "blpuLogicalStatus",
    usrnMatchIndicator = "usrnMatchIndicator",
    parentUprn = "parentUprn",
    streetClassification = "streetClassification",
    multiOccCount = "multiOccCount",
    language = "language",
    localCustodianCode = "localCustodianCode",
    localCustodianName = "localCustodianName",
    localCustodianGeogCode = "localCustodianGeogCode",
    rpc = "rpc",
    nagAll = "nagAll",
    lpiEndDate = "lpiEndDate",
    lpiStartDate = "lpiStartDate",
    mixedNag = "mixedNag",
    mixedWelshNag = "mixedWelshNag"
  )

  val validNisraAddress: NisraAddress = NisraAddress(
    organisationName = "1",
    subBuildingName = "2",
    buildingName = "3",
    buildingNumber = "4",
    paoText = "",
    paoStartNumber = "4",
    paoStartSuffix = "",
    paoEndNumber = "",
    paoEndSuffix = "",
    saoText = "",
    saoStartNumber = "4",
    saoStartSuffix = "",
    saoEndNumber = "",
    saoEndSuffix = "",
    thoroughfare = "5",
    altThoroughfare = "6",
    dependentThoroughfare = "7",
    locality = "8",
    townName = "10",
    postcode = "BT36 5SN",
    uprn = "11",
    classificationCode = "12",
    udprn = "13",
    postTown = "14",
    easting = "15",
    northing = "16",
    creationDate = "17",
    commencementDate = "18",
    archivedDate = "19",
    latitude = "20",
    longitude = "21",
    addressStatus = "APPROVED",
    buildingStatus = "DEMOLISHED",
    localCouncil = "BELFAST",
    LGDCode = "N09000003",
    mixedNisra = "mixedNisra"
 )

  val validRelative: Relative = Relative(
    level = 1,
    siblings = Array(6L, 7L),
    parents = Array(8L, 9L)
  )

  val validCrossRef: CrossRef = CrossRef(
    crossReference = "E05011011",
    source = "7666OW"
  )

  // todo add test highlighting

  val validHybridAddress: HybridAddress = HybridAddress(
    uprn = "1",
    parentUprn = "4",
    relatives = Some(Seq(validRelative)),
    crossRefs = Some(Seq(validCrossRef)),
    postcodeIn = Some("2"),
    postcodeOut = Some("3"),
    paf = Seq(validPafAddress),
    lpi = Seq(validNagAddress),
    nisra = Seq(),
    score = 1f,
    classificationCode = "29",
    censusAddressType = "NA",
    censusEstabType = "NA",
    fromSource = "47",
    countryCode = "E",
    highlights = Seq()
  )

  val validHybridAddressSkinny: HybridAddress = HybridAddress(
    uprn = "1",
    parentUprn = "4",
    relatives = None,
    crossRefs = None,
    postcodeIn = None,
    postcodeOut = None,
    paf = Seq(validPafAddress),
    lpi = Seq(validNagAddress),
    nisra = Seq(validNisraAddress),
    score = 1f,
    classificationCode = "29",
    censusAddressType = "NA",
    censusEstabType = "NA",
    fromSource = "47",
    countryCode = "E",
    highlights = Seq()
  )

  val validCodelistList: String = "{\"codelists\"" +
    ":[{\"name\":\"classification\",\"description\":\"Coded address types e.g. RD02 is a detached house\"}"

  val validSourceList: String = "{\"sources\"" +
    ":[{\"code\":\"7666MT\",\"label\":\"OS MasterMap Topography Layer TOID\"}"

  val validCustodianList: String = "{\"custodians\"" +
    ":[{\"custCode\":\"114\",\"custName\":\"BATH AND NORTH EAST SOMERSET\",\"laName\":\"Bath and North East Somerset UA\",\"regCode\":\"E12000009\",\"regName\":\"South West\",\"laCode\":\"E06000022\"}"

  val validLogicalStatusList: String = "{\"logicalStatuses\"" +
    ":[{\"code\":\"1\",\"label\":\"Approved\"}"

  val validClassificationList: String = "{\"classifications\"" +
    ":[{\"code\":\"C\",\"label\":\"Commercial\"}"

  // injected value, change implementations accordingly when needed
  // mock that will return one address as a result
  val elasticRepositoryMock: ElasticsearchRepository = new ElasticsearchRepository {
    def getHybridAddress(args: QueryArgs): HybridAddress = args match {
      case s: Skinnyable => if (s.skinny) validHybridAddressSkinny else validHybridAddress
      case _ => validHybridAddress
    }

    override def queryHealth(): Future[String] = Future.successful("")

    override def makeQuery(args: QueryArgs): SearchRequest = SearchRequest(Indexes(Seq()))

    override def runUPRNQuery(args: UPRNArgs): Future[Option[HybridAddress]] = Future.successful(Some(getHybridAddress(args)))

    override def runMultiResultQuery(args: MultiResultArgs): Future[HybridAddressCollection] = Future.successful(HybridAddressCollection(Seq(getHybridAddress(args)), 1.0f, 1))

    override def runBulkQuery(args: BulkArgs): Future[Stream[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]] =
      Future.successful {
        args.requestsData.map(requestData => {
          val filledBulk = BulkAddress.fromHybridAddress(getHybridAddress(args), requestData)
          val emptyScored = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(filledBulk.hybridAddress, verbose = true)), requestData.tokens, 1D)
          val filledBulkAddress = AddressBulkResponseAddress.fromBulkAddress(filledBulk, emptyScored.head, includeFullAddress = false)

          Right(Seq(filledBulkAddress))
        })
      }
  }

  val slowElasticRepositoryMock: ElasticsearchRepository = new ElasticsearchRepository {
    def getHybridAddress(args: QueryArgs): HybridAddress = args match {
      case s: Skinnyable => if (s.skinny) validHybridAddressSkinny else validHybridAddress
      case _ => validHybridAddress
    }

    override def queryHealth(): Future[String] = Future {
      Thread.sleep(500)
      ""
    }

    override def makeQuery(args: QueryArgs): SearchRequest = SearchRequest(Indexes(Seq()))

    override def runUPRNQuery(args: UPRNArgs): Future[Option[HybridAddress]] = Future {
      Thread.sleep(500)
      Some(getHybridAddress(args))
    }

    override def runMultiResultQuery(args: MultiResultArgs): Future[HybridAddressCollection] = Future {
      Thread.sleep(500)
      HybridAddressCollection(Seq(getHybridAddress(args)), 1.0f, 1)
    }

    override def runBulkQuery(args: BulkArgs): Future[Stream[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]] =
      Future {
        Thread.sleep(500)
        args.requestsData.map(requestData => {
          val filledBulk = BulkAddress.fromHybridAddress(getHybridAddress(args), requestData)
          val emptyScored = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(filledBulk.hybridAddress, verbose = true)), requestData.tokens, 1D)
          val filledBulkAddress = AddressBulkResponseAddress.fromBulkAddress(filledBulk, emptyScored.head, includeFullAddress = false)

          Right(Seq(filledBulkAddress))
        })
      }
  }

  // mock that won't return any addresses
  val emptyElasticRepositoryMock: ElasticsearchRepository = new ElasticsearchRepository {
    def getHybridAddress(args: QueryArgs): HybridAddress = args match {
      case s: Skinnyable => if (s.skinny) validHybridAddressSkinny else validHybridAddress
      case _ => validHybridAddress
    }

    override def queryHealth(): Future[String] = Future.successful("")

    override def makeQuery(queryArgs: QueryArgs): SearchRequest = SearchRequest(Indexes(Seq()))

    override def runUPRNQuery(args: UPRNArgs): Future[Option[HybridAddress]] = Future.successful(None)

    override def runMultiResultQuery(args: MultiResultArgs): Future[HybridAddressCollection] = Future.successful(HybridAddressCollection(Seq.empty, 1.0f, 0))

    override def runBulkQuery(args: BulkArgs): Future[Stream[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]] =
      Future.successful {
        args.requestsData.map(requestData => {
          val filledBulk = BulkAddress.fromHybridAddress(getHybridAddress(args), requestData)
          val emptyScored = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(filledBulk.hybridAddress, verbose = true)), requestData.tokens, 1D)
          val filledBulkAddress = AddressBulkResponseAddress.fromBulkAddress(filledBulk, emptyScored.head, includeFullAddress = false)

          Right(Seq(filledBulkAddress))
        })
      }
  }

  val sometimesFailingRepositoryMock: ElasticsearchRepository = new ElasticsearchRepository {
    override def queryHealth(): Future[String] = Future.successful("")

    override def makeQuery(queryArgs: QueryArgs): SearchRequest = SearchRequest(Indexes(Seq()))

    override def runUPRNQuery(args: UPRNArgs): Future[Option[HybridAddress]] = Future.successful(None)

    override def runMultiResultQuery(args: MultiResultArgs): Future[HybridAddressCollection] = Future.successful(HybridAddressCollection(Seq.empty, 1.0f, 0))

    override def runBulkQuery(args: BulkArgs): Future[Stream[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]] =
      Future.successful {
        args.requestsData.map {
          case requestData if requestData.tokens.values.exists(_ == "failed") => Left(requestData)
          case requestData =>
            val emptyBulk = BulkAddress.empty(requestData)
            val emptyScored = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(emptyBulk.hybridAddress, verbose = true)), requestData.tokens, 1D)
            val emptyBulkAddress = AddressBulkResponseAddress.fromBulkAddress(emptyBulk, emptyScored.head, includeFullAddress = false)

            Right(Seq(emptyBulkAddress))
        }
      }
  }

  val failingRepositoryMock: ElasticsearchRepository = new ElasticsearchRepository {
    override def queryHealth(): Future[String] = Future.successful("")

    override def makeQuery(queryArgs: QueryArgs): SearchRequest = SearchRequest(Indexes(Seq()))

    override def runUPRNQuery(args: UPRNArgs): Future[Option[HybridAddress]] = Future.failed(new Exception("test failure"))

    override def runMultiResultQuery(args: MultiResultArgs): Future[HybridAddressCollection] = Future.failed(new Exception("test failure"))

    override def runBulkQuery(args: BulkArgs): Future[Stream[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]] = Future.failed(new Exception("test exception"))
  }

  val parser: ParserModule = (_: String) => Map.empty

  val testConfig = new AddressIndexConfigModule

  // Lower CB timeouts so test time isn't silly, Max reset should be reached after 2 iterations
  // as exponential backoff is 2.0
  val tweakedCBConfig: ConfigModule = new ConfigModule {
    override def config: AddressIndexConfig = testConfig.config.copy(
      elasticSearch = testConfig.config.elasticSearch.copy(
        circuitBreakerMaxFailures = 1,
        circuitBreakerCallTimeout = 250,
        circuitBreakerResetTimeout = 250,
        circuitBreakerMaxResetTimeout = 500
      )
    )
  }

  val apiVersionExpected = "testApi"
  val dataVersionExpected = "testData"

  val versions: VersionModule = new VersionModule {
    val apiVersion: String = apiVersionExpected
    val dataVersion: String = dataVersionExpected
  }

  val overloadProtection: APIThrottle = new APIThrottle(tweakedCBConfig)
  val components: ControllerComponents = stubControllerComponents()
  val rh: RequestHeader = FakeRequest(GET, "/")
  val addressValidation: AddressControllerValidation = new AddressControllerValidation()(testConfig, versions)
  val partialAddressValidation: PartialAddressControllerValidation = new PartialAddressControllerValidation()(testConfig, versions)
  val postcodeValidation: PostcodeControllerValidation = new PostcodeControllerValidation()(testConfig, versions)
  val randomValidation: RandomControllerValidation = new RandomControllerValidation()(testConfig, versions)
  val uprnValidation: UPRNControllerValidation = new UPRNControllerValidation()(testConfig, versions)
  val batchValidation: BatchControllerValidation = new BatchControllerValidation()(testConfig, versions)
  val codelistValidation: CodelistControllerValidation = new CodelistControllerValidation()(testConfig, versions)

  val addressController = new AddressController(components, elasticRepositoryMock, parser, testConfig, versions, overloadProtection, addressValidation)
  val partialAddressController = new PartialAddressController(components, elasticRepositoryMock, testConfig, versions, overloadProtection, partialAddressValidation)

  val postcodeController = new PostcodeController(components, elasticRepositoryMock, testConfig, versions, overloadProtection, postcodeValidation)
  val randomController = new RandomController(components, elasticRepositoryMock, testConfig, versions, overloadProtection, randomValidation)
  val uprnController = new UPRNController(components, elasticRepositoryMock, testConfig, versions, overloadProtection, uprnValidation)
  val codelistController = new CodelistController(components, versions)

  "Address controller" should {

    "reply with a found address in concise format (by uprn)" in {
      // Given
      val controller = uprnController

      val expected = Json.toJson(AddressByUprnResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByUprnResponse(
          address = Some(AddressResponseAddress.fromHybridAddress(validHybridAddress, verbose = false)),
          historical = true,
          verbose = false,
          epoch = ""
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = controller.uprnQuery(validHybridAddress.uprn, verbose = Some("false")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply with a found address in verbose format (by uprn)" in {
      // Given
      val controller = uprnController

      val expected = Json.toJson(AddressByUprnResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByUprnResponse(
          address = Some(AddressResponseAddress.fromHybridAddress(validHybridAddress, verbose = true)),
          historical = true,
          verbose = true,
          epoch = ""
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = controller.uprnQuery(validHybridAddress.uprn, verbose = Some("true")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply with a found address in concise format (by postcode)" in {
      // Given
      val controller = postcodeController

      val expected = Json.toJson(AddressByPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByPostcodeResponse(
          postcode = "ab123cd",
          addresses = Seq(AddressResponseAddress.fromHybridAddress(validHybridAddressSkinny, verbose = false)),
          filter = "",
          historical = true,
          limit = 100,
          offset = 0,
          total = 1,
          maxScore = 1.0f,
          verbose = false,
          epoch = ""
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = controller.postcodeQuery("ab123cd", verbose = Some("false")).apply(FakeRequest())

      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply with a found address in verbose format (by postcode)" in {
      // Given
      val controller = postcodeController

      val expected = Json.toJson(AddressByPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByPostcodeResponse(
          postcode = "ab123cd",
          addresses = Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, verbose = true)),
          filter = "",
          historical = true,
          limit = 100,
          offset = 0,
          total = 1,
          maxScore = 1.0f,
          verbose = true,
          epoch = ""
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = controller.postcodeQuery("ab123cd", verbose = Some("true")).apply(FakeRequest())

      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply with a random address in concise format" in {
      // Given
      val controller = randomController

      val expected = Json.toJson(AddressByRandomResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByRandomResponse(
          addresses = Seq(AddressResponseAddress.fromHybridAddress(validHybridAddressSkinny, verbose = false)),
          filter = "",
          historical = true,
          limit = 1,
          verbose = false,
          epoch = "",
          fromsource = "all"
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = controller.randomQuery(verbose = Some("false")).apply(FakeRequest())

      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply with a random address in verbose format" in {
      // Given
      val controller = randomController

      val expected = Json.toJson(AddressByRandomResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByRandomResponse(
          addresses = Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, verbose = true)),
          filter = "",
          historical = true,
          limit = 1,
          verbose = true,
          epoch = "",
          fromsource="all"
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = controller.randomQuery(verbose = Some("true")).apply(FakeRequest())

      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

   "reply on a found address in concise format (by partial)" in {
      // Given
      val expected = Json.toJson(AddressByPartialAddressResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByPartialAddressResponse(
          input = "some query",
          addresses = Seq(AddressResponseAddress.fromHybridAddress(validHybridAddressSkinny, verbose = false).copy(confidenceScore=5)),
          filter = "",
          fallback = true,
          historical = false,
          limit = 20,
          offset = 0,
          total = 1,
          maxScore = 1.0f,
          verbose = false,
          epoch = "",
          fromsource = "all",
          highverbose = true,
          favourpaf = true,
          favourwelsh = true
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = partialAddressController.partialAddressQuery(input = "some query", verbose = Some("false")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply on a found address in verbose format (by partial)" in {
      // Given
      val expected = Json.toJson(AddressByPartialAddressResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByPartialAddressResponse(
          input = "some query",
          addresses = Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, verbose = true).copy(confidenceScore=5)),
          filter = "",
          fallback = true,
          historical = false,
          limit = 20,
          offset = 0,
          total = 1,
          maxScore = 1.0f,
          verbose = true,
          epoch = "",
          fromsource="all",
          highverbose = true,
          favourpaf = true,
          favourwelsh = true
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = partialAddressController.partialAddressQuery(input = "some query", verbose = Some("true")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply with a found address in concise format (by address query)" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, verbose = false)), Map.empty, -1D),
          filter = "",
          historical = true,
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 1,
          sampleSize = 20,
          maxScore = 1.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        OkAddressResponseStatus
      ))

      // When
      val result = controller.addressQuery("some query", verbose = Some("false")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply with a found address in verbose format (by address query)" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, verbose = true)), Map.empty, -1D),
          filter = "",
          historical = true,
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 1,
          sampleSize = 20,
          maxScore = 1.0f,
          matchthreshold = 5f,
          verbose = true,
          epoch = "",
          fromsource="all"
        ),
        OkAddressResponseStatus
      ))

      // When
      val result = controller.addressQuery("some query", verbose = Some("true")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply with a list of addresses when given a range/lat/lon/filter but with no input (by address query)" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, verbose = false)), Map.empty, -1D),
          filter = "commercial",
          historical = true,
          rangekm = "20",
          latitude = "50.7",
          longitude = "-3.5",
          limit = 10,
          offset = 0,
          total = 1,
          sampleSize = 20,
          maxScore = 1.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        OkAddressResponseStatus
      ))

      // When
      val result = controller.addressQuery("*", rangekm = Some("20"), lat = Some("50.7"), lon = Some("-3.5"), classificationfilter = Some("commercial"), verbose = Some("false")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply with a found address in concise format (by address query with radius)" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, verbose = false)), Map.empty, -1D),
          filter = "",
          historical = true,
          rangekm = "1",
          latitude = "50.705948",
          longitude = "-3.5091076",
          limit = 10,
          offset = 0,
          total = 1,
          sampleSize = 20,
          maxScore = 1.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        OkAddressResponseStatus
      ))

      // When
      val result = controller.addressQuery("some query", None, None, None, Some("1"), Some("50.705948"), Some("-3.5091076"), verbose = Some("false")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply with a found address in verbose (by address query with radius)" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, verbose = true)), Map.empty, -1D),
          filter = "",
          historical = true,
          rangekm = "1",
          latitude = "50.705948",
          longitude = "-3.5091076",
          limit = 10,
          offset = 0,
          total = 1,
          sampleSize = 20,
          maxScore = 1.0f,
          matchthreshold = 5f,
          verbose = true,
          epoch = "",
          fromsource="all"
        ),
        OkAddressResponseStatus
      ))

      // When
      val result = controller.addressQuery("some query", None, None, None, Some("1"), Some("50.705948"), Some("-3.5091076"), verbose = Some("true")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply with a 400 error if an invalid fromsource value is supplied" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "RD02",
          historical = true,
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 1,
          offset = 1,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="twitter"
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(FromSourceInvalidError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("1"), Some("1"), Some("RD02"), fromsource=Some("twitter")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if an invalid filter value is supplied" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "BR12",
          historical = true,
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 1,
          offset = 1,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(FilterInvalidError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("1"), Some("1"), Some("BR12")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if an invalid mixed filter value is supplied" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "RD*,RD02",
          historical = true,
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 1,
          offset = 1,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(MixedFilterError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("1"), Some("1"), Some("RD*,RD02")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }


    "reply with a 400 error if a non-numeric offset parameter is supplied" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          historical = true,
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 1,
          offset = 0,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(OffsetNotNumericAddressResponseError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("thing"), Some("1")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if a non-numeric offset parameter is supplied (postcode)" in {
      // Given
      val controller = postcodeController

      val expected = Json.toJson(AddressByPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByPostcodeResponse(
          postcode = "some query",
          addresses = Seq.empty,
          filter = "",
          historical = true,
          limit = 1,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          verbose = false,
          epoch = ""
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(OffsetNotNumericAddressResponseError)
      ))

      // When
      val result = controller.postcodeQuery(postcode = "some query", offset = Some("thing"), limit = Some("1")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if a non-numeric limit parameter is supplied" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          historical = true,
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 1,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(LimitNotNumericAddressResponseError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("1"), Some("thing")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if a non-numeric limit parameter is supplied (postcode)" in {
      // Given
      val controller = postcodeController

      val expected = Json.toJson(AddressByPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByPostcodeResponse(
          postcode = "some query",
          addresses = Seq.empty,
          filter = "",
          historical = true,
          limit = 100,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          verbose = false,
          epoch = ""
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(LimitNotNumericAddressResponseError)
      ))

      // When
      val result = controller.postcodeQuery(postcode = "some query", limit = Some("thing")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if a non-numeric limit parameter is supplied (random)" in {
      // Given
      val controller = randomController

      val expected = Json.toJson(AddressByRandomResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByRandomResponse(
          addresses = Seq.empty,
          filter = "",
          historical = true,
          limit = 1,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(LimitNotNumericAddressResponseError)
      ))

      // When
      val result = controller.randomQuery(limit = Some("thing")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if invalid range/lat/lon/filter is supplied" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "commercial",
          historical = true,
          rangekm = "",
          latitude = "50.7",
          longitude = "-3.5",
          limit = 10,
          offset = 0,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(EmptyRadiusQueryAddressResponseError)
      ))

      // When
      val result = controller.addressQuery("", classificationfilter = Some("commercial"), lat = Some("50.7"), lon = Some("-3.5")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if a negative offset parameter is supplied" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          historical = true,
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 1,
          offset = -1,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(OffsetTooSmallAddressResponseError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("-1"), Some("1")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if a negative offset parameter is supplied (postcode)" in {
      // Given
      val controller = postcodeController

      val expected = Json.toJson(AddressByPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByPostcodeResponse(
          postcode = "some query",
          addresses = Seq.empty,
          filter = "",
          historical = true,
          limit = 100,
          offset = -1,
          total = 0,
          maxScore = 0.0f,
          verbose = false,
          epoch = ""
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(OffsetTooSmallAddressResponseError)
      ))

      // When
      val result = controller.postcodeQuery(postcode = "some query", offset = Some("-1")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if a negative or zero limit parameter is supplied" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          historical = true,
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 0,
          offset = 0,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(LimitTooSmallAddressResponseError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("0"), Some("0")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if a negative or zero limit parameter is supplied (postcode)" in {
      // Given
      val controller = postcodeController

      val expected = Json.toJson(AddressByPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByPostcodeResponse(
          postcode = "some query",
          addresses = Seq.empty,
          filter = "",
          historical = true,
          limit = 0,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          verbose = false,
          epoch = ""
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(LimitTooSmallAddressResponseError)
      ))

      // When
      val result = controller.postcodeQuery(postcode = "some query", limit = Some("0")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if a negative or zero limit parameter is supplied (random)" in {
      // Given
      val controller = randomController

      val expected = Json.toJson(AddressByRandomResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByRandomResponse(
          addresses = Seq.empty,
          filter = "",
          historical = true,
          limit = 0,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(LimitTooSmallAddressResponseError)
      ))

      // When
      val result = controller.randomQuery(limit = Some("0")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if an offset parameter greater than the maximum allowed is supplied" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          historical = true,
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 1,
          offset = 9999999,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(addressValidation.OffsetTooLargeAddressResponseErrorCustom)
      ))

      // When
      val result = controller.addressQuery("some query", Some("9999999"), Some("1")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if an offset parameter greater than the maximum allowed is supplied (postcode)" in {
      // Given
      val controller = postcodeController

      val expected = Json.toJson(AddressByPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByPostcodeResponse(
          postcode = "some query",
          addresses = Seq.empty,
          filter = "",
          historical = true,
          limit = 100,
          offset = 9999999,
          total = 0,
          maxScore = 0.0f,
          verbose = false,
          epoch = ""
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(postcodeValidation.OffsetTooLargeAddressResponseErrorCustom)
      ))

      // When
      val result = controller.postcodeQuery(postcode = "some query", offset = Some("9999999")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if a limit parameter larger than the maximum allowed is supplied" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          historical = true,
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 999999,
          offset = 0,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(addressValidation.LimitTooLargeAddressResponseErrorCustom)
      ))

      // When
      val result = controller.addressQuery("some query", Some("0"), Some("999999")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if a limit parameter larger than the maximum allowed is supplied (postcode)" in {
      // Given
      val controller = postcodeController

      val expected = Json.toJson(AddressByPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByPostcodeResponse(
          postcode = "some query",
          addresses = Seq.empty,
          filter = "",
          historical = true,
          limit = 999999,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          verbose = false,
          epoch = ""
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(postcodeValidation.LimitTooLargeAddressResponseErrorCustom)
      ))

      // When
      val result = controller.postcodeQuery(postcode = "some query", limit = Some("999999")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if a limit parameter larger than the maximum allowed is supplied (random)" in {
      // Given
      val controller = randomController

      val expected = Json.toJson(AddressByRandomResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByRandomResponse(
          addresses = Seq.empty,
          filter = "",
          historical = true,
          limit = 999999,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(randomValidation.LimitTooLargeAddressResponseErrorCustom)
      ))

      // When
      val result = controller.randomQuery(limit = Some("999999")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if epoch is invalid (random)" in {
      // Given
      val controller = randomController

      val expected = Json.toJson(AddressByRandomResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByRandomResponse(
          addresses = Seq.empty,
          filter = "",
          historical = true,
          limit = 1,
          verbose = false,
          epoch = "epoch",
          fromsource="all"
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(randomValidation.EpochNotAvailableErrorCustom)
      ))

      // When
      val result = controller.randomQuery(epoch = Some("epoch")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if a non-numeric rangekm parameter is supplied" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          historical = true,
          rangekm = "alongway",
          latitude = "",
          longitude = "",
          limit = 1,
          offset = 1,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(RangeNotNumericAddressResponseError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("1"), Some("1"), None, Some("alongway")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if a non-numeric latitude parameter is supplied" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          historical = true,
          rangekm = "1",
          latitude = "oopnorth",
          longitude = "0",
          limit = 1,
          offset = 1,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(LatitudeNotNumericAddressResponseError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("1"), Some("1"), None, Some("1"), Some("oopnorth"), Some("0")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if a non-numeric longitude parameter is supplied" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          historical = true,
          rangekm = "1",
          latitude = "50",
          longitude = "eastofthechipshop",
          limit = 1,
          offset = 1,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(LongitudeNotNumericAddressResponseError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("1"), Some("1"), None, Some("1"), Some("50"), Some("eastofthechipshop")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if a too far north latitude parameter is supplied" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          historical = true,
          rangekm = "1",
          latitude = "66.6",
          longitude = "0",
          limit = 1,
          offset = 1,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(LatitudeTooFarNorthAddressResponseError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("1"), Some("1"), None, Some("1"), Some("66.6"), Some("0")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if a too far east longitude parameter is supplied" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          historical = true,
          rangekm = "1",
          latitude = "50",
          longitude = "2.8",
          limit = 1,
          offset = 1,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(LongitudeTooFarEastAddressResponseError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("1"), Some("1"), None, Some("1"), Some("50"), Some("2.8")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if a too far south latitude parameter is supplied" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          historical = true,
          rangekm = "1",
          latitude = "44.4",
          longitude = "0",
          limit = 1,
          offset = 1,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(LatitudeTooFarSouthAddressResponseError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("1"), Some("1"), None, Some("1"), Some("44.4"), Some("0")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if a too far west longitude parameter is supplied" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          historical = true,
          rangekm = "1",
          latitude = "50",
          longitude = "-8.8",
          limit = 1,
          offset = 1,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(LongitudeTooFarWestAddressResponseError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("1"), Some("1"), None, Some("1"), Some("50"), Some("-8.8")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if query is empty (by address query)" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          historical = true,
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(EmptyQueryAddressResponseError)
      ))

      // When
      val result = controller.addressQuery("").apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if query is too short (by partial address query)" in {
      // Given
      val controller = partialAddressController

      val expected = Json.toJson(AddressByPartialAddressResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByPartialAddressResponse(
          input = "foo",
          addresses = Seq.empty,
          filter = "",
          fallback = true,
          historical = false,
          limit = 20,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          verbose = false,
          epoch = "",
          fromsource="all",
          highverbose = true,
          favourpaf = true,
          favourwelsh = true
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(partialAddressValidation.ShortQueryAddressResponseErrorCustom)
      ))

      // When
      val result = controller.partialAddressQuery(input = "foo").apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if epoch is invalid (by partial address query)" in {
      // Given
      val controller = partialAddressController

      val expected = Json.toJson(AddressByPartialAddressResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByPartialAddressResponse(
          input = "something",
          addresses = Seq.empty,
          filter = "",
          fallback = true,
          historical = false,
          limit = 20,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          verbose = false,
          epoch = "epoch",
          fromsource="all",
          highverbose = true,
          favourpaf = true,
          favourwelsh = true
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(partialAddressValidation.EpochNotAvailableErrorCustom)
      ))

      // When
      val result = controller.partialAddressQuery(input = "something", epoch = Some("epoch")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if query is empty (by address query) (postcode)" in {
      // Given
      val controller = postcodeController

      val expected = Json.toJson(AddressByPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByPostcodeResponse(
          postcode = "",
          addresses = Seq.empty,
          filter = "",
          historical = true,
          limit = 100,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          verbose = false,
          epoch = ""
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(EmptyQueryPostcodeAddressResponseError)
      ))

      // When
      val result = controller.postcodeQuery("").apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if epoch is invalid (postcode)" in {
      // Given
      val controller = postcodeController

      val expected = Json.toJson(AddressByPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByPostcodeResponse(
          postcode = "ab123cd",
          addresses = Seq.empty,
          filter = "",
          historical = true,
          limit = 100,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          verbose = false,
          epoch = "epoch"
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(postcodeValidation.EpochNotAvailableErrorCustom)
      ))

      // When
      val result = controller.postcodeQuery("ab123cd", epoch = Some("epoch")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "test the circuit breaker" in {

      /* These tests test the Circuit Breaker implementation. Unfortunately they require Thread.sleep
         to simulate timeouts and resets. To be successful the Circuit Breaker will follow the following states:
         Open
         Half-Open
         Open
         Half-Open
         Open
         Half-Open
         Closed
         Open
         Half-Open
         Closed
       */

      // Given
      val controller = new AddressController(components, failingRepositoryMock, parser, testConfig, versions, overloadProtection, addressValidation)
      val controller1 = new AddressController(components, elasticRepositoryMock, parser, testConfig, versions, overloadProtection, addressValidation)
      val controller2 = new AddressController(components, slowElasticRepositoryMock, parser, testConfig, versions, overloadProtection, addressValidation)

      val enhancedError = new AddressResponseError(FailedRequestToEsError.code, FailedRequestToEsError.message.replace("see logs", "test failure"))
      val cbError = new AddressResponseError(FailedRequestToEsError.code, FailedRequestToEsError.message.replace("see logs", "Circuit Breaker is open; calls are failing fast"))
      val cbTimeoutError = new AddressResponseError(FailedRequestToEsError.code, FailedRequestToEsError.message.replace("see logs", "Circuit Breaker Timed out."))

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, verbose = false)), Map.empty, -1D),
          filter = "",
          historical = true,
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 1,
          sampleSize = 20,
          maxScore = 1.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        OkAddressResponseStatus
      ))

      val expectedFail = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          historical = true,
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        TooManyRequestsResponseStatus,
        errors = Seq(enhancedError)
      ))

      val expectedFailCBOpen = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          historical = true,
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        TooManyRequestsResponseStatus,
        errors = Seq(cbError)
      ))

      val expectedFailCBTimeout = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          historical = true,
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        TooManyRequestsResponseStatus,
        errors = Seq(cbTimeoutError)
      ))


      // When - Normal call should work - CB closed
      def r1 = controller1.addressQuery("some query", Some("0"), Some("10")).apply(FakeRequest())
      val actual1: JsValue = contentAsJson(r1)

      // Then
      status(r1) mustBe OK
      actual1 mustBe expected

      // When - Call failed (Exception returned) should open CB
      def r2 = controller.addressQuery("some query", Some("0"), Some("10")).apply(FakeRequest())
      val actual2: JsValue = contentAsJson(r2)

      // Then
      status(r2) mustBe TOO_MANY_REQUESTS
      actual2 mustBe expectedFail

      // When - Normal call should work but the CB is Open so it will fail
      def r3 = controller1.addressQuery("some query", Some("0"), Some("10")).apply(FakeRequest())
      val actual3: JsValue = contentAsJson(r3)

      // Then
      status(r3) mustBe TOO_MANY_REQUESTS
      actual3 mustBe expectedFailCBOpen

      // Wait for reset timeout to pass (250ms)
      Thread.sleep(400)

      // When - Call failed (Exception returned) CB in Half-Open state. Reset timeout doubled to 500ms by exponential backoff setting
      def r4 = controller.addressQuery("some query", Some("0"), Some("10")).apply(FakeRequest())
      val actual4: JsValue = contentAsJson(r4)

      // The5
      status(r4) mustBe TOO_MANY_REQUESTS
      actual4 mustBe expectedFail

      // When - Normal call should work but the CB is Open so it will fail
      def r5 = controller1.addressQuery("some query", Some("0"), Some("10")).apply(FakeRequest())
      val actual5: JsValue = contentAsJson(r5)

      // Then
      status(r5) mustBe TOO_MANY_REQUESTS
      actual5 mustBe expectedFailCBOpen

      // Wait for reset timeout to pass (500ms)
      Thread.sleep(700)

      // When - Call failed (Exception returned) CB in Half-Open state. Reset timeout max reached so should stay at 500ms
      def r6 = controller.addressQuery("some query", Some("0"), Some("10")).apply(FakeRequest())
      val actual6: JsValue = contentAsJson(r6)

      // Then
      status(r6) mustBe TOO_MANY_REQUESTS
      actual6 mustBe expectedFail

      // When - Normal call should work but the CB is Open so it will fail
      def r7 = controller1.addressQuery("some query", Some("0"), Some("10")).apply(FakeRequest())
      val actual7: JsValue = contentAsJson(r7)

      // Then
      status(r7) mustBe TOO_MANY_REQUESTS
      actual7 mustBe expectedFailCBOpen

      // Wait for reset timeout to pass (500ms)
      Thread.sleep(700)

      // When - Normal call should work - CB Half-Open. Closed after this successful call
      def r8 = controller1.addressQuery("some query", Some("0"), Some("10")).apply(FakeRequest())
      val actual8: JsValue = contentAsJson(r8)

      // Then
      status(r8) mustBe OK
      actual8 mustBe expected

      // When - Normal call should work but won't as this simulates a slow response from ES and timesout the CB.
      def r9 = controller2.addressQuery("some query", Some("0"), Some("10")).apply(FakeRequest())
      val actual9: JsValue = contentAsJson(r9)

      // Then
      status(r9) mustBe TOO_MANY_REQUESTS
      actual9 mustBe expectedFailCBTimeout

      // Wait for reset timeout to pass (250ms) - then CB is Half-Open
      Thread.sleep(400)

      // When - Normal call should work - CB Half-Open. Closed after this successful call. This final test is to reset the CB so it doesn't interfere with any other tests
      def r10 = controller1.addressQuery("some query", Some("0"), Some("10")).apply(FakeRequest())
      val actual10: JsValue = contentAsJson(r10)

      // Then
      status(r10) mustBe OK
      actual10 mustBe expected

    }

    "reply with a 429 error if Elastic threw exception (request failed) while querying for address" in {
      // Given
      val controller = new AddressController(components, failingRepositoryMock, parser, testConfig, versions, overloadProtection, addressValidation)

      val enhancedError = new AddressResponseError(FailedRequestToEsError.code, FailedRequestToEsError.message.replace("see logs", "test failure"))

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          historical = true,
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        TooManyRequestsResponseStatus,
        errors = Seq(enhancedError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("0"), Some("10")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe TOO_MANY_REQUESTS
      actual mustBe expected

      // This test tripped the CB so wait for reset timeout to pass (250ms) - then CB is Half-Open. Need to make sure CB has transitioned to Half-Open
      // Otherwise next test will fail
      Thread.sleep(400)
    }

    "reply with a 429 error if Elastic threw exception (request failed) while querying for postcode" in {
      // Given
      val controller = new PostcodeController(components, failingRepositoryMock, testConfig, versions, overloadProtection, postcodeValidation)

      val enhancedError = new AddressResponseError(FailedRequestToEsPostcodeError.code, FailedRequestToEsPostcodeError.message.replace("see logs", "test failure"))

      val expected = Json.toJson(AddressByPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByPostcodeResponse(
          postcode = "ab123cd",
          addresses = Seq.empty,
          filter = "",
          historical = true,
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          verbose = false,
          epoch = ""
        ),
        TooManyRequestsResponseStatus,
        errors = Seq(enhancedError)
      ))

      // When - retry param must be true
      val result = controller.postcodeQuery(postcode = "ab123cd", offset = Some("0"), limit = Some("10")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe TOO_MANY_REQUESTS
      actual mustBe expected

      // This test tripped the CB so wait for reset timeout to pass (250ms) - then CB is Half-Open. Need to make sure CB has transitioned to Half-Open
      // Otherwise next test will fail
      Thread.sleep(400)
    }

    "reply with a 429 error if Elastic threw exception (request failed) while querying for a random address" in {
      // Given
      val controller = new RandomController(components, failingRepositoryMock, testConfig, versions, overloadProtection, randomValidation)

      val enhancedError = new AddressResponseError(FailedRequestToEsRandomError.code, FailedRequestToEsRandomError.message.replace("see logs", "test failure"))

      val expected = Json.toJson(AddressByRandomResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByRandomResponse(
          addresses = Seq.empty,
          filter = "",
          historical = true,
          limit = 1,
          verbose = false,
          epoch = "",
          fromsource="all"
        ),
        TooManyRequestsResponseStatus,
        errors = Seq(enhancedError)
      ))

      // When - retry param must be true
      val result = controller.randomQuery(limit = Some("1")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe TOO_MANY_REQUESTS
      actual mustBe expected

      // This test tripped the CB so wait for reset timeout to pass (250ms) - then CB is Half-Open. Need to make sure CB has transitioned to Half-Open
      // Otherwise next test will fail
      Thread.sleep(400)
    }

    "reply with a 429 error if Elastic threw exception (request failed) while querying for a partial address" in {
      // Given

      val controller = new PartialAddressController(components, failingRepositoryMock, testConfig, versions, overloadProtection, partialAddressValidation)

      val enhancedError = new AddressResponseError(FailedRequestToEsPartialAddressError.code, FailedRequestToEsPartialAddressError.message.replace("see logs", "test failure"))

      val expected = Json.toJson(AddressByPartialAddressResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByPartialAddressResponse(
          input = "some query",
          addresses = Seq.empty,
          filter = "",
          fallback = true,
          historical = false,
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          verbose = false,
          epoch = "",
          fromsource="all",
          highverbose = true,
          favourpaf = true,
          favourwelsh = true
        ),
        TooManyRequestsResponseStatus,
        errors = Seq(enhancedError)
      ))

      // When - retry param must be true
      val result = controller.partialAddressQuery(input = "some query", offset = Some("0"), limit = Some("10")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe TOO_MANY_REQUESTS
      actual mustBe expected

      // This test tripped the CB so wait for reset timeout to pass (250ms) - then CB is Half-Open. Need to make sure CB has transitioned to Half-Open
      // Otherwise next test will fail
      Thread.sleep(400)
    }

    "reply with a 429 error if Elastic threw exception (request failed) while querying for uprn" in {
      // Given
      val controller = new UPRNController(components, failingRepositoryMock, testConfig, versions, overloadProtection, uprnValidation)

      val enhancedError = new AddressResponseError(FailedRequestToEsUprnError.code, FailedRequestToEsUprnError.message.replace("see logs", "test failure"))

      val expected = Json.toJson(AddressByUprnResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByUprnResponse(
          address = None,
          historical = true,
          verbose = false,
          epoch = ""
        ),
        TooManyRequestsResponseStatus,
        errors = Seq(enhancedError)
      ))

      // When
      val result = controller.uprnQuery("12345").apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe TOO_MANY_REQUESTS
      actual mustBe expected

      // This test tripped the CB so wait for reset timeout to pass (250ms) - then CB is Half-Open. Need to make sure CB has transitioned to Half-Open
      // Otherwise next test will fail
      Thread.sleep(400)
    }

    "reply a 400 error if address was not numeric (by uprn)" in {
      // Given
      val controller = new UPRNController(components, emptyElasticRepositoryMock, testConfig, versions, overloadProtection, uprnValidation)

      val expected = Json.toJson(AddressByUprnResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByUprnResponse(
          address = None,
          historical = true,
          verbose = false,
          epoch = ""
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(UprnNotNumericAddressResponseError)
      ))

      // When
      val result = controller.uprnQuery("221B").apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply a 404 error if address was not found (by uprn)" in {
      // Given
      val controller = new UPRNController(components, emptyElasticRepositoryMock, testConfig, versions, overloadProtection, uprnValidation)

      val expected = Json.toJson(AddressByUprnResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByUprnResponse(
          address = None,
          historical = true,
          verbose = false,
          epoch = ""
        ),
        NotFoundAddressResponseStatus,
        errors = Seq(NotFoundAddressResponseError)
      ))

      // When
      val result = controller.uprnQuery("123456789").apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe NOT_FOUND
      actual mustBe expected
    }

    "do multiple search from an iterator with tokens (AddressIndexActions method)" in {
      // Given
      val controller = new BatchController(components, sometimesFailingRepositoryMock, parser, testConfig, versions, batchValidation)

      val requestsData: Stream[BulkAddressRequestData] = Stream(
        BulkAddressRequestData("", "1", Map("first" -> "success")),
        BulkAddressRequestData("", "2", Map("second" -> "success")),
        BulkAddressRequestData("", "3", Map("third" -> "failed"))
      )

      // When
      val result: BulkAddresses = Await.result(controller.queryBulkAddresses(requestsData, 3, None, "", "", historical = true, epoch = "", 5F), Duration.Inf)

      // Then
      result.successfulBulkAddresses.size mustBe 2
      result.failedRequests.size mustBe 1
    }

    "have process bulk addresses using back-pressure" in {
      // Given
      val controller = new BatchController(components, sometimesFailingRepositoryMock, parser, testConfig, versions, batchValidation)

      val requestsData: Stream[BulkAddressRequestData] = Stream(
        BulkAddressRequestData("", "1", Map("first" -> "success")),
        BulkAddressRequestData("", "2", Map("second" -> "success")),
        BulkAddressRequestData("", "3", Map("third" -> "success")),
        BulkAddressRequestData("", "4", Map("forth" -> "success")),
        BulkAddressRequestData("", "5", Map("fifth" -> "success")),
        BulkAddressRequestData("", "6", Map("sixth" -> "success")),
        BulkAddressRequestData("", "7", Map("seventh" -> "success")),
        BulkAddressRequestData("", "8", Map("eighth" -> "success")),
        BulkAddressRequestData("", "9", Map("ninth" -> "success"))
      )

      // When
      val result = controller.iterateOverRequestsWithBackPressure(requestsData, 3, None, None, "", "", historical = true, epoch = "", 5F)

      // Then
      result.size mustBe requestsData.size
    }

    "have back-pressure that should throw an exception if there is an always failing request" in {
      // Given
      val controller = new BatchController(components, sometimesFailingRepositoryMock, parser, testConfig, versions, batchValidation)

      val requestsData: Stream[BulkAddressRequestData] = Stream(
        BulkAddressRequestData("", "1", Map("first" -> "success")),
        BulkAddressRequestData("", "2", Map("second" -> "success")),
        BulkAddressRequestData("", "3", Map("third" -> "failed"))
      )

      // When Then
      an[Exception] should be thrownBy controller.iterateOverRequestsWithBackPressure(requestsData, 10, None, None, "", "", historical = true, epoch = "", 5F)
    }

    "return list of codelists" in {
      // Given
      val expectedCodelist = validCodelistList
      val controller = codelistController

      // When
      val result = controller.codeList().apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      actual.toString().substring(0, expectedCodelist.length) mustBe expectedCodelist
    }

    "return list of sources" in {
      // Given
      val expectedCodelist = validSourceList
      val controller = codelistController

      // When
      val result = controller.codeListSource().apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      actual.toString().substring(0, expectedCodelist.length) mustBe expectedCodelist
    }

    "return list of classifications" in {
      // Given
      val expectedCodelist = validClassificationList
      val controller = codelistController

      // When
      val result = controller.codeListClassification().apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      actual.toString().substring(0, expectedCodelist.length) mustBe expectedCodelist
    }

    "return list of custodians" in {
      // Given
      val expectedCodelist = validCustodianList
      val controller = codelistController

      // When
      val result = controller.codeListCustodian().apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      actual.toString().substring(0, expectedCodelist.length) mustBe expectedCodelist
    }

    "return list of logical statuses" in {
      // Given
      val expectedCodelist = validLogicalStatusList
      val controller = codelistController

      // When
      val result = controller.codeListLogicalStatus().apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      actual.toString().substring(0, expectedCodelist.length) mustBe expectedCodelist
    }

    "sort highlights correctly in partial controller" in {
     //Given
     val controller = new PartialAddressController(components, failingRepositoryMock, testConfig, versions, overloadProtection, partialAddressValidation)
     //When
      val high1 = new AddressResponseHighlightHit( source = "L", lang = "E",distinctHitCount = 3, highLightedText ="6 Long Lane Liverpool")
      val high2 = new AddressResponseHighlightHit( source = "P", lang = "E",distinctHitCount = 3, highLightedText ="6 Long Lane Liverpool")
      val high3 = new AddressResponseHighlightHit( source = "P", lang = "W",distinctHitCount = 3, highLightedText ="6 Long Lane Liverpool")
      val high4 = new AddressResponseHighlightHit( source = "N", lang = "E",distinctHitCount = 4, highLightedText ="6 Long Lane Belfast")

      val result = controller.sortHighs(Seq(high1,high2,high3,high4),true,true)
      val expected = Seq(high4,high3,high2,high1)

      // Then
      result mustBe expected
    }
  }
}
