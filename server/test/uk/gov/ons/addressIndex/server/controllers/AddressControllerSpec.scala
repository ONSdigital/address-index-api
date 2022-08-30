package uk.gov.ons.addressIndex.server.controllers

import com.sksamuel.elastic4s.Indexes
import com.sksamuel.elastic4s.requests.searches.SearchRequest
import org.scalatestplus.play._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.api.test.{FakeHeaders, FakeRequest}
import play.api.test.Helpers._
import uk.gov.ons.addressIndex.model.MultiUprnBody
import uk.gov.ons.addressIndex.model.config.AddressIndexConfig
import uk.gov.ons.addressIndex.model.db.index._
import uk.gov.ons.addressIndex.model.db.{BulkAddress, BulkAddressRequestData, BulkAddresses}
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.bulk.AddressBulkResponseAddress
import uk.gov.ons.addressIndex.model.server.response.eq._
import uk.gov.ons.addressIndex.model.server.response.partialaddress.{AddressByPartialAddressResponse, AddressByPartialAddressResponseContainer}
import uk.gov.ons.addressIndex.model.server.response.postcode._
import uk.gov.ons.addressIndex.model.server.response.random.{AddressByRandomResponse, AddressByRandomResponseContainer}
import uk.gov.ons.addressIndex.model.server.response.rh.{AddressByRHPartialAddressResponse, AddressByRHPartialAddressResponseContainer, AddressByRHPostcodeResponse, AddressByRHPostcodeResponseContainer}
import uk.gov.ons.addressIndex.model.server.response.uprn.{AddressByMultiUprnResponse, AddressByMultiUprnResponseContainer, AddressByUprnResponse, AddressByUprnResponseContainer}
import uk.gov.ons.addressIndex.server.controllers.general.ApplicationController
import uk.gov.ons.addressIndex.server.modules._
import uk.gov.ons.addressIndex.server.modules.validation._
import uk.gov.ons.addressIndex.server.utils.{APIThrottle, HighlightFuncs, HopperScoreHelper}

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
    mixedPaf = "mixedPaf",
    mixedWelshPaf = "mixedWelshPaf"
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
    addressLines = Nil,
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

  val validHighlight= Map(
    "mixedPartial" -> Seq("<em>mixedPaf</em>")
  )

  val validHighlightWelsh = Map(
    "mixedPartial" -> Seq("<em>mixedWelshPaf</em>")
  )

  val validHighlightBoth = Map(
    "mixedPartial" -> Seq("<em>mixedPaf</em> <em>mixedWelshPaf</em>")
  )

  val validHighlights = Seq(validHighlightBoth)

  val validBuckets = Seq(AddressResponsePostcodeGroup("EX4 1AA","Aardvark Avenue","Exeter",47,1,"Exeter"))

  val validHybridAddress: HybridAddress = HybridAddress(
    onsAddressId = "",
    uprn = "1",
    parentUprn = "4",
    relatives = Some(Seq(validRelative)),
    crossRefs = Some(Seq(validCrossRef)),
    postcodeIn = Some("2"),
    postcodeOut = Some("3"),
    paf = Seq(validPafAddress),
    lpi = Seq(validNagAddress),
    nisra = Seq(),
    auxiliary = Seq(),
    score = 1f,
    classificationCode = "29",
    censusAddressType = "NA",
    censusEstabType = "NA",
    fromSource = "47",
    countryCode = "E",
    highlights = Seq()
  )

  val validHybridAddressSkinny: HybridAddress = HybridAddress(
    onsAddressId = "",
    uprn = "1",
    parentUprn = "4",
    relatives = None,
    crossRefs = None,
    postcodeIn = None,
    postcodeOut = None,
    paf = Seq(validPafAddress),
    lpi = Seq(validNagAddress),
    nisra = Seq(validNisraAddress),
    auxiliary = Seq(),
    score = 1f,
    classificationCode = "29",
    censusAddressType = "NA",
    censusEstabType = "NA",
    fromSource = "47",
    countryCode = "E",
    highlights = validHighlights
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

    override def runMultiUPRNQuery(args: UPRNArgs): Future[HybridAddressCollection] =  Future.successful(HybridAddressCollection(Seq(getHybridAddress(args)),Seq(),1.0f,1))

    override def runMultiResultQuery(args: MultiResultArgs): Future[HybridAddressCollection] = {
      args.groupFullPostcodesOrDefault match {
        case "no" =>    Future.successful(HybridAddressCollection(Seq(getHybridAddress(args)), Seq(), 1.0f, 1))
        case "yes" =>   Future.successful(HybridAddressCollection(Seq(), validBuckets, 1.0f, 1))
        case "combo" => Future.successful(HybridAddressCollection(Seq(getHybridAddress(args)), validBuckets, 1.0f, 1))
        case _ =>       Future.successful(HybridAddressCollection(Seq(getHybridAddress(args)), validBuckets, 1.0f, 1))
      }
    }

    override def runBulkQuery(args: BulkArgs): Future[LazyList[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]] = {
      val scaleFactor = testConfig.config.bulk.scaleFactor
      Future.successful {
        args.requestsData.map(requestData => {
          val filledBulk = BulkAddress.fromHybridAddress(getHybridAddress(args), requestData)
          val emptyScored = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(filledBulk.hybridAddress, verbose = true)), requestData.tokens, 1D, scaleFactor)
          val filledBulkAddress = AddressBulkResponseAddress.fromBulkAddress(filledBulk, emptyScored.head, includeFullAddress = false)

          Right(Seq(filledBulkAddress))
        })
      }
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

    override def runMultiUPRNQuery(args: UPRNArgs): Future[HybridAddressCollection] =  Future {
      Thread.sleep(500)
      HybridAddressCollection(Seq(getHybridAddress(args)), Seq(), 1.0f, 1)
    }

    override def runMultiResultQuery(args: MultiResultArgs): Future[HybridAddressCollection] = Future {
      Thread.sleep(500)
      HybridAddressCollection(Seq(getHybridAddress(args)), validBuckets, 1.0f, 1)
    }

    override def runBulkQuery(args: BulkArgs): Future[LazyList[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]] = {

    val scaleFactor = testConfig.config.bulk.scaleFactor
    Future {
      Thread.sleep(500)
      args.requestsData.map(requestData => {
        val filledBulk = BulkAddress.fromHybridAddress(getHybridAddress(args), requestData)
        val emptyScored = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(filledBulk.hybridAddress, verbose = true)), requestData.tokens, 1D,scaleFactor)
        val filledBulkAddress = AddressBulkResponseAddress.fromBulkAddress(filledBulk, emptyScored.head, includeFullAddress = false)

        Right(Seq(filledBulkAddress))
      })
    }
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

    override def runMultiUPRNQuery(args: UPRNArgs): Future[HybridAddressCollection] = Future.successful(HybridAddressCollection(Seq.empty, Seq.empty, 1.0f, 0))

    override def runMultiResultQuery(args: MultiResultArgs): Future[HybridAddressCollection] = Future.successful(HybridAddressCollection(Seq.empty, Seq.empty, 1.0f, 0))

    override def runBulkQuery(args: BulkArgs): Future[LazyList[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]] = {
      val scaleFactor = testConfig.config.bulk.scaleFactor
      Future.successful {
        args.requestsData.map(requestData => {
          val filledBulk = BulkAddress.fromHybridAddress(getHybridAddress(args), requestData)
          val emptyScored = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(filledBulk.hybridAddress, verbose = true)), requestData.tokens, 1D,scaleFactor)
          val filledBulkAddress = AddressBulkResponseAddress.fromBulkAddress(filledBulk, emptyScored.head, includeFullAddress = false)

          Right(Seq(filledBulkAddress))
        })
      }
    }
  }

  val sometimesFailingRepositoryMock: ElasticsearchRepository = new ElasticsearchRepository {
    override def queryHealth(): Future[String] = Future.successful("")

    override def makeQuery(queryArgs: QueryArgs): SearchRequest = SearchRequest(Indexes(Seq()))

    override def runUPRNQuery(args: UPRNArgs): Future[Option[HybridAddress]] = Future.successful(None)

    override def runMultiUPRNQuery(args: UPRNArgs): Future[HybridAddressCollection] = Future.successful(HybridAddressCollection(Seq.empty, Seq.empty, 1.0f, 0))

    override def runMultiResultQuery(args: MultiResultArgs): Future[HybridAddressCollection] = Future.successful(HybridAddressCollection(Seq.empty, Seq.empty, 1.0f, 0))

    override def runBulkQuery(args: BulkArgs): Future[LazyList[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]] = {
      val scaleFactor = testConfig.config.bulk.scaleFactor
      Future.successful {
        args.requestsData.map {
          case requestData if requestData.tokens.values.exists(_ == "failed") => Left(requestData)
          case requestData =>
            val emptyBulk = BulkAddress.empty(requestData)
            val emptyScored = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(emptyBulk.hybridAddress, verbose = true)), requestData.tokens, 1D, scaleFactor)
            val emptyBulkAddress = AddressBulkResponseAddress.fromBulkAddress(emptyBulk, emptyScored.head, includeFullAddress = false)

            Right(Seq(emptyBulkAddress))
        }
      }
    }
  }

  val failingRepositoryMock: ElasticsearchRepository = new ElasticsearchRepository {
    override def queryHealth(): Future[String] = Future.successful("")

    override def makeQuery(queryArgs: QueryArgs): SearchRequest = SearchRequest(Indexes(Seq()))

    override def runUPRNQuery(args: UPRNArgs): Future[Option[HybridAddress]] = Future.failed(new Exception("test failure"))

    override def runMultiUPRNQuery(args: UPRNArgs): Future[HybridAddressCollection] = Future.failed(new Exception("test failure"))

    override def runMultiResultQuery(args: MultiResultArgs): Future[HybridAddressCollection] = Future.failed(new Exception("test failure"))

    override def runBulkQuery(args: BulkArgs): Future[LazyList[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]] = Future.failed(new Exception("test exception"))
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

  val sboost: Int = testConfig.config.elasticSearch.defaultStartBoost

  val apiVersionExpected = "testApi"
  val dataVersionExpected = "testData"
  val termsAndConditionsExpected = "https://census.gov.uk/terms-and-conditions"
  val epochListExpected = List ("39","89","NA")
  val epochDatesEpected = {
    Map("39" -> "Exeter Sample",
      "87" -> "September 2021",
      "88" -> "October 2021",
      "89" -> "December 2021",
      "90" -> "January 2022",
      "91" -> "March 2022",
      "92" -> "April 2022",
      "93" -> "June 2022",
      "94" -> "July 2022",
      "95" -> "August 2022",
      "80" -> "Census no extras",
      "80C" -> "Census with extras",
      "80N" -> "Census with extras and NISRA",
      "NA" -> "test index")
  }

  val versions: VersionModule = new VersionModule {
    val apiVersion: String = apiVersionExpected
    val dataVersion: String = dataVersionExpected
    val termsAndConditions: String = termsAndConditionsExpected
    val epochList: List[String] = epochListExpected
    val epochDates: Map[String,String] = epochDatesEpected
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

  val eqPartialAddressController = new EQPartialAddressController(components, elasticRepositoryMock, testConfig, versions, overloadProtection, partialAddressValidation)
  val rhPartialAddressController = new RHPartialAddressController(components, elasticRepositoryMock, testConfig, versions, overloadProtection, partialAddressValidation)

  val postcodeController = new PostcodeController(components, elasticRepositoryMock, testConfig, versions, overloadProtection, postcodeValidation)
  val groupedPostcodeController = new GroupedPostcodeController(components, elasticRepositoryMock, testConfig, versions, overloadProtection, postcodeValidation)
  val eqPostcodeController = new EQPostcodeController(components, elasticRepositoryMock, testConfig, versions, overloadProtection, postcodeValidation)
  val eqBucketController = new EQBucketController(components, elasticRepositoryMock, testConfig, versions, overloadProtection, postcodeValidation)
  val rhPostcodeController = new RHPostcodeController(components, elasticRepositoryMock, testConfig, versions, overloadProtection, postcodeValidation)
  val randomController = new RandomController(components, elasticRepositoryMock, testConfig, versions, overloadProtection, randomValidation)
  val uprnController = new UPRNController(components, elasticRepositoryMock, testConfig, versions, overloadProtection, uprnValidation)
  val multiUprnController = new MultiUprnController(components, elasticRepositoryMock, testConfig, versions, overloadProtection, uprnValidation)
  val codelistController = new CodelistController(components, versions)

  val eqController = new EQController(components, eqPartialAddressController, versions, eqPostcodeController, groupedPostcodeController)

  val applicationController = new ApplicationController(components, postcodeController, uprnController)

  val UPRNControllerKaput : UPRNController = new UPRNController(components, elasticRepositoryMock, testConfig, versions, overloadProtection, uprnValidation) {
    override def uprnQuery(uprn: String, historical: Option[String], verbose: Option[String], epoch: Option[String], includeauxiliarysearch: Option[String]): Action[AnyContent] = Action {
      ImATeapot
    }
  }

  val postcodeControllerKaput : PostcodeController = new PostcodeController(components, elasticRepositoryMock, testConfig, versions, overloadProtection, postcodeValidation) {
    override def postcodeQuery(postcode: String, offset: Option[String], limit: Option[String], classificationfilter: Option[String], historical: Option[String], verbose: Option[String], epoch: Option[String], includeauxiliarysearch: Option[String], eboost: Option[String], nboost: Option[String],sboost: Option[String],wboost: Option[String]): Action[AnyContent] = Action {
      ImATeapot
    }
  }

  "Address controller" should {

    "reply with hello world" in {
      // Given
      val controller = applicationController

      val expected = "hello world"

      // When
      val result: Future[Result] = controller.index().apply(FakeRequest())
      val actual: String = contentAsString(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply with healthy status" in {
      // Given
      val controller = applicationController

      // When
      val result: Future[Result] = controller.healthz().apply(FakeRequest())

      // Then
      status(result) mustBe OK
    }

    "reply with unhealthy status - fat cluster" in {
      // Given
      val controller = new ApplicationController(components, postcodeController, UPRNControllerKaput)

      // When
      val result: Future[Result] = controller.healthz().apply(FakeRequest())

      // Then
      status(result) mustBe IM_A_TEAPOT
    }

    "reply with unhealthy status - skinny cluster" in {
      // Given
      val controller = new ApplicationController(components, postcodeControllerKaput, uprnController)

      // When
      val result: Future[Result] = controller.healthz().apply(FakeRequest())

      // Then
      status(result) mustBe IM_A_TEAPOT
    }

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

    "reply with multiple addresses (by Multiuprn)" in {
      // Given
      val controller = multiUprnController

      val expected = Json.toJson(AddressByMultiUprnResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByMultiUprnResponse(
          addresses = Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, verbose = false)),
          historical = true,
          verbose = false,
          epoch = ""
        ),
        OkAddressResponseStatus
      ))

      val uprns = Seq(validHybridAddress.uprn,validHybridAddress.uprn)
      val uBody = new MultiUprnBody(uprns)

      val request = FakeRequest[MultiUprnBody](
        method = "POST",
        uri = "/",
        headers = FakeHeaders(
          Seq("Content-type"->"application/json")
        ),
        body =  uBody
      )
      val result = controller.multiUprn().apply(request)

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
          historical = false,
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
          historical = false,
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

    "reply with a a group of postcodes" in {
      // Given
      val controller = groupedPostcodeController

      val expected = Json.toJson(AddressByGroupedPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        termsAndConditions = termsAndConditionsExpected,
        response = AddressByGroupedPostcodeResponse(
          partpostcode = "EX4",
          postcodes = validBuckets,
          filter = "",
          historical = false,
          limit = 1,
          offset = 0,
          total = 1,
          maxScore = 1.0f,
          verbose = false,
          epoch = ""
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = controller.groupedPostcodeQuery(postcode="EX4",limit=Some("1")).apply(FakeRequest())

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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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

     val addresses = Seq(AddressResponseAddress.fromHybridAddress(validHybridAddressSkinny, verbose = false).copy(confidenceScore=5))

     val sortAddresses = partialAddressController.boostAtStart(addresses, input="some query", favourPaf = true, favourWelsh = false, highVerbose = false)

     // Given
      val expected = Json.toJson(AddressByPartialAddressResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByPartialAddressResponse(
          input = "some query",
          addresses = sortAddresses,
          filter = "",
          fallback = false,
          historical = false,
          limit = 20,
          offset = 0,
          total = 1,
          maxScore = 1.0f,
          verbose = false,
          epoch = "",
          highlight = "on",
          favourpaf = true,
          favourwelsh = false,
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1,
          timeout = 250
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

    "reply with a found address in eq format when a partial is supplied to EQController" in {

      val addresses = Seq(AddressResponseAddressEQ.fromHybridAddress(validHybridAddressSkinny, favourPaf = true, favourWelsh = true))

      val sortAddresses = if (sboost > 0) eqPartialAddressController.boostAtStart(addresses, input="some query", favourPaf = true, favourWelsh = false, highVerbose = false) else addresses

      // Given
      val expected = Json.toJson(AddressByEQPartialAddressResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        termsAndConditions = termsAndConditionsExpected,
        response = AddressByEQPartialAddressResponse(
          input = "some query",
          addresses = AddressByEQPartialAddressResponse.toEQAddressByPartialResponse(sortAddresses),
          filter = "",
          fallback = false,
          limit = 20,
          offset = 0,
          total = 1,
          maxScore = 1.0f,
          epoch = "",
          favourpaf = true,
          favourwelsh = false,
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = eqController.eqQuery(input = "some query").apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply on a found address in concise format where input is less than 6 digits (by partial)" in {

      val addresses = Seq(AddressResponseAddress.fromHybridAddress(validHybridAddressSkinny, verbose = false).copy(confidenceScore=5))

      val sortAddresses = partialAddressController.boostAtStart(addresses, input="12345", favourPaf = true, favourWelsh = false, highVerbose = false)

      // Given
      val expected = Json.toJson(AddressByPartialAddressResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByPartialAddressResponse(
          input = "12345",
          addresses = sortAddresses,
          filter = "",
          fallback = false,
          historical = false,
          limit = 20,
          offset = 0,
          total = 1,
          maxScore = 1.0f,
          verbose = false,
          epoch = "",
          highlight = "on",
          favourpaf = true,
          favourwelsh = false,
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1,
          timeout = 250
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = partialAddressController.partialAddressQuery(input = "12345", verbose = Some("false")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply on a found address in concise format where input is greater than 5 digits (by partial)" in {

      val addresses = Seq(AddressResponseAddress.fromHybridAddress(validHybridAddressSkinny, verbose = false).copy(confidenceScore=5))

      val sortAddresses = partialAddressController.boostAtStart(addresses, input="123456", favourPaf = true, favourWelsh = false, highVerbose = false)

      // Given
      val expected = Json.toJson(AddressByPartialAddressResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByPartialAddressResponse(
          input = "123456",
          addresses = sortAddresses,
          filter = "",
          fallback = false,
          historical = false,
          limit = 20,
          offset = 0,
          total = 1,
          maxScore = 1.0f,
          verbose = false,
          epoch = "",
          highlight = "on",
          favourpaf = true,
          favourwelsh = false,
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1,
          timeout = 250
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = partialAddressController.partialAddressQuery(input = "123456", verbose = Some("false")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply with a postcode list when a postcode is supplied to EQController" in {
      // Given
      val controller = eqController

      val expected = Json.toJson(AddressByEQPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        termsAndConditions = termsAndConditionsExpected,
        response = AddressByEQPostcodeResponse(
          postcode = "PO155RR",
          postcodes = None,
          addresses = Seq(AddressResponseAddressPostcodeEQ.fromHybridAddress(validHybridAddressSkinny, favourPaf = true, favourWelsh = false)),
          filter = "",
          limit = 100,
          offset = 0,
          total = 1,
          maxScore = 1.0f,
          epoch = "",
          groupfullpostcodes = "no"
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = controller.eqQuery("Po155Rr", favourpaf = Some("true"), favourwelsh = Some("false")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply with a postcode group list when a postcode is supplied to EQController and groupfullpostcodes flag is true" in {
      // Given
      val controller = eqController

      val expected = Json.toJson(AddressByGroupedPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        termsAndConditions = termsAndConditionsExpected,
        response = AddressByGroupedPostcodeResponse(
          partpostcode = "EX4 1AA",
          postcodes = Seq(AddressResponsePostcodeGroup("EX4 1AA","Aardvark Avenue","Exeter",47,1,"Exeter")),
          filter = "",
          historical = false,
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
      val result: Future[Result] = controller.eqQuery("EX4 1AA", favourpaf = Some("true"), favourwelsh = Some("false"), groupfullpostcodes = Some("yes")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }



    "reply with a part postcode list when an outcode is supplied to EQController" in {
      // Given
      val controller = eqController

      val expected = Json.toJson(AddressByGroupedPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        termsAndConditions = termsAndConditionsExpected,
        response = AddressByGroupedPostcodeResponse(
          partpostcode = "EX4",
          postcodes = Seq(AddressResponsePostcodeGroup("EX4 1AA","Aardvark Avenue","Exeter",47,1,"Exeter")),
          filter = "",
          historical = false,
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
      val result: Future[Result] = controller.eqQuery("EX4", favourpaf = Some("true"), favourwelsh = Some("false")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply with a part postcode list when an outcode and sector is supplied to EQController" in {
      // Given
      val controller = eqController

      val expected = Json.toJson(AddressByGroupedPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        termsAndConditions = termsAndConditionsExpected,
        response = AddressByGroupedPostcodeResponse(
          partpostcode = "EX4 1",
          postcodes = Seq(AddressResponsePostcodeGroup("EX4 1AA","Aardvark Avenue","Exeter",47,1,"Exeter")),
          filter = "",
          historical = false,
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
      val result: Future[Result] = controller.eqQuery("EX4 1", favourpaf = Some("true"), favourwelsh = Some("false")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply with a filtered list of postcode results via eq bucket endpoint" in {
      // Given

      val expected = Json.toJson(AddressByEQBucketResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        termsAndConditions = termsAndConditionsExpected,
        response = AddressByEQBucketResponse(
          postcode = "EX4 1A*",
          streetname = "Aardvark Avenue",
          townname = "Exeter",
          addresses = Seq(AddressResponseAddressBucketEQ("1", "31", "PAF")),
          filter = "",
          limit = 100,
          offset = 0,
          total = 1,
          maxScore = 1.0f,
          epoch = ""
        ),
        OkAddressResponseStatus
      ))
    }
      "reply with a 400 error for invalid call to eq bucket endpoint" in {
        // Given
        val controller = eqBucketController

        val expected = Json.toJson(AddressByEQBucketResponseContainer(
          apiVersion = apiVersionExpected,
          dataVersion = dataVersionExpected,
          termsAndConditions = termsAndConditionsExpected,
          response = AddressByEQBucketResponse(
            postcode = "*",
            streetname = "*",
            townname = "*",
            addresses = Seq(),
            filter = "",
            limit = 100,
            offset = 0,
            total = 0,
            maxScore = 0,
            epoch = ""
          ),
          BadRequestAddressResponseStatus,
          errors = Seq(InvalidEQBucketError)
        ))

      // When
      val result: Future[Result] = controller.bucketQueryEQ(postcode= Some("*"),streetname = Some("*"), townname = Some("*"), favourpaf = Some("true"), favourwelsh = Some("false")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a part postcode list when an outcode and sector and first half of unit is supplied to EQController" in {
      // Given
      val controller = eqController

      val expected = Json.toJson(AddressByGroupedPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        termsAndConditions = termsAndConditionsExpected,
        response = AddressByGroupedPostcodeResponse(
          partpostcode = "EX4 1A",
          postcodes = Seq(AddressResponsePostcodeGroup("EX4 1AA","Aardvark Avenue","Exeter",47,1,"Exeter")),
          filter = "",
          historical = false,
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
      val result: Future[Result] = controller.eqQuery("EX4 1A", favourpaf = Some("true"), favourwelsh = Some("false")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply with a combined list from a full postcode with groupedfullpostcodes = combo in EQController" in {
      // Given
      val controller = eqController

      val expected = Json.toJson(AddressByEQPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        termsAndConditions = termsAndConditionsExpected,
        response = AddressByEQPostcodeResponse(
          postcode = "EX41AA",
          addresses = Seq(AddressResponseAddressPostcodeEQ.fromHybridAddress(validHybridAddressSkinny, favourPaf = true, favourWelsh = false)),
          postcodes = Some(Seq(AddressResponsePostcodeGroup("EX4 1AA","Aardvark Avenue","Exeter",47,1,"Exeter"))),
          filter = "",
          limit = 100,
          offset = 0,
          total = 1,
          maxScore = 1.0f,
          epoch = "",
          groupfullpostcodes = "combo"
          ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = controller.eqQuery("EX4 1AA", favourpaf = Some("true"), favourwelsh = Some("false"),groupfullpostcodes = Some("combo")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }


    "reply with a found address in rh format when a partial is supplied to RH Partial Controller" in {

      val addresses = Seq(AddressResponseAddressRH.fromHybridAddress(validHybridAddressSkinny, favourPaf = true, favourWelsh = true))

      val sortAddresses = if (sboost > 0) rhPartialAddressController.boostAtStart(addresses, input="some query", favourPaf=true, favourWelsh = false, highVerbose = false) else addresses

      // Given
      val expected = Json.toJson(AddressByRHPartialAddressResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByRHPartialAddressResponse(
          input = "some query",
          addresses = AddressByRHPartialAddressResponse.toRHAddressByPartialResponse(sortAddresses),
          filter = "",
          fallback = false,
          limit = 20,
          offset = 0,
          total = 1,
          maxScore = 1.0f,
          epoch = "",
          favourpaf = true,
          favourwelsh = false,
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = rhPartialAddressController.partialAddressQueryRH(input = "some query").apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply with an rh postcode response when a postcode is supplied to RH Postcode Controller" in {
      // Given

      val expected = Json.toJson(AddressByRHPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByRHPostcodeResponse(
          postcode = "Po155Rr",
          addresses = Seq(AddressResponseAddressPostcodeRH.fromHybridAddress(validHybridAddressSkinny, favourPaf = true, favourWelsh = false).copy(confidenceScore=100001)),
          filter = "",
          limit = 100,
          offset = 0,
          total = 1,
          maxScore = 1.0f,
          epoch = ""
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = rhPostcodeController.postcodeQueryRH("Po155Rr", favourpaf = Some("true"), favourwelsh = Some("false")).apply(FakeRequest())
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
          fallback = false,
          historical = false,
          limit = 20,
          offset = 0,
          total = 1,
          maxScore = 1.0f,
          verbose = true,
          epoch = "",
          highlight= "on",
          favourpaf = true,
          favourwelsh = false,
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1,
          timeout = 250
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
      val scaleFactor = testConfig.config.elasticSearch.scaleFactor
      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, verbose = false)), Map.empty, -1D, scaleFactor),
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
      val scaleFactor = testConfig.config.elasticSearch.scaleFactor
      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, verbose = true)), Map.empty, -1D, scaleFactor),
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
      val scaleFactor = testConfig.config.elasticSearch.scaleFactor
      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, verbose = false)), Map.empty, -1D, scaleFactor),
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
      val scaleFactor = testConfig.config.elasticSearch.scaleFactor
      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, verbose = false)), Map.empty, -1D, scaleFactor),
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
      val scaleFactor = testConfig.config.elasticSearch.scaleFactor
      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, verbose = true)), Map.empty, -1D, scaleFactor),
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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

    "reply with a 400 error if a non-numeric country boost value is supplied" in {
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(CountryBoostsInvalidError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("1"), Some("1"), Some("RD02"), sboost=Some("single malt")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if an out of range country boost value is supplied" in {
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 42
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(CountryBoostsInvalidError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("1"), Some("1"), Some("RD02"), wboost=Some("42")).apply(FakeRequest())
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          historical = false,
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          historical = false,
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          historical = false,
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          historical = false,
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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

    "reply with a 400 error if a non-numeric timeout parameter is supplied (partial)" in {
      // Given
      val controller = partialAddressController

      val expected = Json.toJson(AddressByPartialAddressResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByPartialAddressResponse(
          input = "mon repo",
          addresses = Seq.empty,
          filter = "",
          fallback = false,
          historical = false,
          limit = 20,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          verbose = false,
          epoch = "",
          highlight = "on",
          favourpaf = true,
          favourwelsh = false,
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1,
          timeout = 250
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(TimeoutNotNumericAddressResponseError)
      ))

      // When
      val result = controller.partialAddressQuery(input = "mon repo",timeout=Some("wibble")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if a too small timeout parameter is supplied (partial)" in {
      // Given
      val controller = partialAddressController

      val expected = Json.toJson(AddressByPartialAddressResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByPartialAddressResponse(
          input = "mon repo",
          addresses = Seq.empty,
          filter = "",
          fallback = false,
          historical = false,
          limit = 20,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          verbose = false,
          epoch = "",
          highlight = "on",
          favourpaf = true,
          favourwelsh = false,
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1,
          timeout = 3
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(TimeoutTooSmallAddressResponseError)
      ))

      // When
      val result = controller.partialAddressQuery(input = "mon repo",timeout=Some("3")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply with a 400 error if a too large timeout parameter is supplied (partial)" in {
      // Given
      val controller = partialAddressController

      val expected = Json.toJson(AddressByPartialAddressResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByPartialAddressResponse(
          input = "mon repo",
          addresses = Seq.empty,
          filter = "",
          fallback = false,
          historical = false,
          limit = 20,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          verbose = false,
          epoch = "",
          highlight = "on",
          favourpaf = true,
          favourwelsh = false,
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1,
          timeout = 9999999
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(partialAddressValidation.TimeoutTooLargeAddressResponseErrorCustom)
      ))

      // When
      val result = controller.partialAddressQuery(input = "mon repo",timeout=Some("9999999")).apply(FakeRequest())
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          historical = false,
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          historical = false,
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          fallback = false,
          historical = false,
          limit = 20,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          verbose = false,
          epoch = "",
          highlight = "on",
          favourpaf = true,
          favourwelsh = false,
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1,
          timeout = 250
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
          fallback = false,
          historical = false,
          limit = 20,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          verbose = false,
          epoch = "epoch",
          highlight = "on",
          favourpaf = true,
          favourwelsh = false,
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1,
          timeout = 250
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
          historical = false,
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
          historical = false,
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

    // ignore this test whilst testing the retry mechanism
    "test the circuit breaker" ignore {

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
      val scaleFactor = testConfig.config.elasticSearch.scaleFactor
      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, verbose = false)), Map.empty, -1D, scaleFactor),
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          historical = false,
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
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1
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
          fallback = false,
          historical = false,
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          verbose = false,
          epoch = "",
          highlight = "on",
          favourpaf = true,
          favourwelsh = false,
          eboost = 1,
          nboost = 1,
          sboost = 1,
          wboost = 1,
          timeout = 250
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

     "reply a 400 error if one of the uprns was not numeric (by Multiuprn)" in {
      // Given
      val controller = multiUprnController

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

      val uprns = Seq("100040202477","antidisestablishmentarianism")
      val uBody = new MultiUprnBody(uprns)

       val request = FakeRequest[MultiUprnBody](
               method = "POST",
               uri = "/",
               headers = FakeHeaders(
                 Seq("Content-type"->"application/json")
               ),
               body =  uBody
             )
      val result = controller.multiUprn().apply(request)
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

      val requestsData: LazyList[BulkAddressRequestData] = LazyList(
        BulkAddressRequestData("", "1", Map("first" -> "success")),
        BulkAddressRequestData("", "2", Map("second" -> "success")),
        BulkAddressRequestData("", "3", Map("third" -> "failed"))
      )

      // When
      val result: BulkAddresses = Await.result(controller.queryBulkAddresses(requestsData, 3, None, "", "", historical = true, epoch = "", 5F, auth=""), Duration.Inf)

      // Then
      result.successfulBulkAddresses.size mustBe 2
      result.failedRequests.size mustBe 1
    }

    "have process bulk addresses using back-pressure" in {
      // Given
      val controller = new BatchController(components, sometimesFailingRepositoryMock, parser, testConfig, versions, batchValidation)

      val requestsData: LazyList[BulkAddressRequestData] = LazyList(
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

      val requestsData: LazyList[BulkAddressRequestData] = LazyList(
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
      val high1 = new AddressResponseHighlightHit( source = "L", lang = "E",distinctHitCount = 3, highLightedText ="6 Long Lane Liverpool")
      val high2 = new AddressResponseHighlightHit( source = "P", lang = "E",distinctHitCount = 3, highLightedText ="6 Long Lane Liverpool")
      val high3 = new AddressResponseHighlightHit( source = "P", lang = "W",distinctHitCount = 3, highLightedText ="6 Long Lane Liverpool")
      val high4 = new AddressResponseHighlightHit( source = "N", lang = "E",distinctHitCount = 4, highLightedText ="6 Long Lane Belfast")

      //When
      val result = HighlightFuncs.sortHighs(Seq(high1,high2,high3,high4),favourPaf = true,favourWelsh = true)
      val expected = Seq(high4,high3,high2,high1)

      // Then
      result mustBe expected
    }
  }
}
