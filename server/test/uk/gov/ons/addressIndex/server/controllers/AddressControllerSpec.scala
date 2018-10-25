package uk.gov.ons.addressIndex.server.controllers

import com.sksamuel.elastic4s.IndexesAndTypes
import com.sksamuel.elastic4s.searches.SearchDefinition
import org.scalatestplus.play._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{ControllerComponents, RequestHeader, Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.ons.addressIndex.model.config.QueryParamsConfig
import uk.gov.ons.addressIndex.model.db.index._
import uk.gov.ons.addressIndex.model.db.{BulkAddress, BulkAddressRequestData, BulkAddresses}
import uk.gov.ons.addressIndex.model.server.response._
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.bulk.AddressBulkResponseAddress
import uk.gov.ons.addressIndex.model.server.response.partialaddress.{AddressByPartialAddressResponse, AddressByPartialAddressResponseContainer, AddressResponsePartialAddress}
import uk.gov.ons.addressIndex.model.server.response.postcode.{AddressByPostcodeResponse, AddressByPostcodeResponseContainer}
import uk.gov.ons.addressIndex.model.server.response.uprn.{AddressByUprnResponse, AddressByUprnResponseContainer}
import uk.gov.ons.addressIndex.server.modules._
import uk.gov.ons.addressIndex.server.modules.validation._
import uk.gov.ons.addressIndex.server.utils.{APIThrottle, HopperScoreHelper}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class AddressControllerSpec extends PlaySpec with Results {

  val validPafAddress = PostcodeAddressFileAddress(
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

  val validNagAddress = NationalAddressGazetteerAddress(
    uprn = "1",
    postcodeLocator = "B16 8TH",
    addressBasePostal = "3",
    latitude = "24",
    longitude = "25",
    easting = "27",
    northing = "28",
    organisation = "22",
    legalName = "23",
    classificationCode = "29",
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
    classScheme = "classScheme",
    localCustodianCode = "localCustodianCode",
    localCustodianName = "localCustodianName",
    localCustodianGeogCode = "localCustodianGeogCode",
    rpc = "rpc",
    nagAll = "nagAll",
    lpiEndDate = "lpiEndDate",
    lpiStartDate = "lpiStartDate",
    mixedNag = "mixedNag"
  )

   val validRelative = Relative (
    level = 1,
    siblings = Array(6L,7L),
    parents = Array(8L,9L)
  )

  val validCrossRef = CrossRef (
    crossReference = "E05011011",
    source = "7666OW"
  )

  val validHybridAddress = HybridAddress(
    uprn = "1",
    parentUprn = "4",
    relatives = Seq(validRelative),
    crossRefs = Seq(validCrossRef),
    postcodeIn = "2",
    postcodeOut = "3",
    paf = Seq(validPafAddress),
    lpi = Seq(validNagAddress),
    score = 1f
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

    override def queryUprn(uprn: String, startDate:String, endDate:String, historical: Boolean = true): Future[Option[HybridAddress]] =
      Future.successful(Some(validHybridAddress))

    override def queryPostcode(postcode: String, start:Int, limit: Int, filters: String, startDate:String, endDate:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true): Future[HybridAddresses] =
      Future.successful(HybridAddresses(Seq(validHybridAddress), 1.0f, 1))

    override def queryPartialAddress(input: String, start:Int, limit: Int, filters: String, startDate:String, endDate:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true): Future[HybridAddresses] =
      Future.successful(HybridAddresses(Seq(validHybridAddress), 1.0f, 1))

    override def queryAddresses(tokens: Map[String, String], start:Int, limit: Int, filters: String, range: String, lat: String, lon:String, startDate:String, endDate:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true, isBulk: Boolean = false): Future[HybridAddresses] =
      Future.successful(HybridAddresses(Seq(validHybridAddress), 1.0f, 1))

    override def queryBulk(requestsData: Stream[BulkAddressRequestData], limit: Int, startDate:String, endDate:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true, matchThreshold: Float, includeFullAddres: Boolean = false): Future[Stream[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]] =

        Future.successful {
          requestsData.map(requestData => {
            val filledBulk = BulkAddress.fromHybridAddress(validHybridAddress, requestData)
            val emptyScored = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(filledBulk.hybridAddress, true)), requestData.tokens, 1D)
            val filledBulkAddress = AddressBulkResponseAddress.fromBulkAddress(filledBulk, emptyScored.head, false)

            Right(Seq(filledBulkAddress))
          }
          )
        }

    override def queryHealth(): Future[String] = Future.successful("")

    override def generateQueryAddressRequest(tokens: Map[String, String], filters: String, range: String, lat: String, lon:String, startDate:String, endDate:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true, isBulk: Boolean = false): SearchDefinition = SearchDefinition(IndexesAndTypes())
  }

  // mock that won't return any addresses
  val emptyElasticRepositoryMock: ElasticsearchRepository = new ElasticsearchRepository {

    override def queryUprn(uprn: String, startDate:String, endDate:String, historical: Boolean = true): Future[Option[HybridAddress]] =
      Future.successful(None)

    override def queryPostcode(postcode: String, start:Int, limit: Int, filters: String, startDate:String, endDate:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true): Future[HybridAddresses] =
      Future.successful(HybridAddresses(Seq.empty, 1.0f, 0))

    override def queryPartialAddress(input: String, start:Int, limit: Int, filters: String, startDate:String, endDate:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true): Future[HybridAddresses] =
      Future.successful(HybridAddresses(Seq.empty, 1.0f, 0))

    override def queryAddresses(tokens: Map[String, String], start:Int, limit: Int, filters: String, range: String, lat: String, lon:String, startDate:String, endDate:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true, isBulk: Boolean = false): Future[HybridAddresses] =
      Future.successful(HybridAddresses(Seq.empty, 1.0f, 0))

    override def queryBulk(requestsData: Stream[BulkAddressRequestData], limit: Int, startDate:String, endDate:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true, matchThreshold: Float, includeFullAddres: Boolean = false): Future[Stream[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]] =
      Future.successful{
        requestsData.map(requestData => {
          val filledBulk = BulkAddress.fromHybridAddress(validHybridAddress, requestData)
          val emptyScored = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(filledBulk.hybridAddress, true)), requestData.tokens, 1D)
          val filledBulkAddress = AddressBulkResponseAddress.fromBulkAddress(filledBulk, emptyScored.head, false)

          Right(Seq(filledBulkAddress))
        }
        )
      }

    override def queryHealth(): Future[String] = Future.successful("")

    override def generateQueryAddressRequest(tokens: Map[String, String], filters: String, range: String, lat: String, lon:String, startDate:String, endDate:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true, isBulk: Boolean = false): SearchDefinition = SearchDefinition(IndexesAndTypes())
  }

  val sometimesFailingRepositoryMock: ElasticsearchRepository = new ElasticsearchRepository {

    override def queryUprn(uprn: String, startDate:String, endDate:String, historical: Boolean = true): Future[Option[HybridAddress]] = Future.successful(None)

    override def queryPartialAddress(postcode: String, start:Int, limit: Int, filters: String, startDate:String, endDate:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true): Future[HybridAddresses] = Future.successful(HybridAddresses(Seq(validHybridAddress), 1.0f, 1))

    override def queryPostcode(postcode: String, start:Int, limit: Int, filters: String, startDate:String, endDate:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true): Future[HybridAddresses] = Future.successful(HybridAddresses(Seq(validHybridAddress), 1.0f, 1))

    override def queryAddresses(tokens: Map[String, String], start:Int, limit: Int, filters: String, range: String, lat: String, lon:String, startDate:String, endDate:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true, isBulk: Boolean = false): Future[HybridAddresses] =
      if (tokens.values.exists(_ == "failed")) Future.failed(new Exception("test failure"))
      else Future.successful(HybridAddresses(Seq(validHybridAddress), 1.0f, 1))

    override def queryBulk(requestsData: Stream[BulkAddressRequestData], limit: Int, startDate:String, endDate:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true, matchThreshold: Float, includeFullAddres: Boolean = false): Future[Stream[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]] =
      Future.successful {
        requestsData.map {
          case requestData if requestData.tokens.values.exists(_ == "failed") => Left(requestData)
          case requestData => {
              val emptyBulk = BulkAddress.empty(requestData)
              val emptyScored = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(emptyBulk.hybridAddress, true)),requestData.tokens, 1D)
              val emptyBulkAddress =  AddressBulkResponseAddress.fromBulkAddress(emptyBulk, emptyScored.head, false)

              Right(Seq(emptyBulkAddress))
            }

        }
      }

    override def queryHealth(): Future[String] = Future.successful("")

    override def generateQueryAddressRequest(tokens: Map[String, String], filters: String, range: String, lat: String, lon:String, startDate:String, endDate:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true, isBulk: Boolean = false): SearchDefinition = SearchDefinition(IndexesAndTypes())
  }

  val failingRepositoryMock: ElasticsearchRepository = new ElasticsearchRepository {

    override def queryUprn(uprn: String, startDate:String, endDate:String, historical: Boolean = true): Future[Option[HybridAddress]] =
      Future.failed(new Exception("test failure"))

    override def queryPostcode(postcode: String, start:Int, limit: Int, filters: String, startDate:String, endDate:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true): Future[HybridAddresses] =
      Future.failed(new Exception("test failure"))

    override def queryPartialAddress(postcode: String, start:Int, limit: Int, filters: String, startDate:String, endDate:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true): Future[HybridAddresses] =
      Future.failed(new Exception("test failure"))

    override def queryAddresses(tokens: Map[String, String], start:Int, limit: Int, filters: String, range: String, lat: String, lon:String, startDate:String, endDate:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true, isBulk: Boolean = false): Future[HybridAddresses] =
      Future.failed(new Exception("Test exception"))

    override def queryBulk(requestsData: Stream[BulkAddressRequestData], limit: Int, startDate:String, endDate:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true, matchThreshold: Float, includeFullAddres: Boolean = false): Future[Stream[Either[BulkAddressRequestData, Seq[AddressBulkResponseAddress]]]] =
      Future.failed(new Exception("Test exception"))

    override def queryHealth(): Future[String] = Future.successful("")

    override def generateQueryAddressRequest(tokens: Map[String, String], filters: String, range: String, lat: String, lon:String, startDate:String, endDate:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true, isBulk: Boolean = false): SearchDefinition = SearchDefinition(IndexesAndTypes())
  }

  val parser: ParserModule = (_: String) => Map.empty

  val config = new AddressIndexConfigModule

  val apiVersionExpected = "testApi"
  val dataVersionExpected = "testData"

  val versions: VersionModule = new VersionModule {
    val apiVersion: String = apiVersionExpected
    val dataVersion: String = dataVersionExpected
  }

  val overloadProtection: APIThrottle = new APIThrottle(config)
  val components: ControllerComponents = stubControllerComponents()
  val rh: RequestHeader = FakeRequest(GET, "/")
  val addressValidation : AddressControllerValidation = new AddressControllerValidation()(config, versions)
  val partialAddressValidation : PartialAddressControllerValidation = new PartialAddressControllerValidation()(config, versions)
  val postcodeValidation : PostcodeControllerValidation = new PostcodeControllerValidation()(config, versions)
  val uprnValidation : UPRNControllerValidation = new UPRNControllerValidation()(config, versions)
  val batchValidation : BatchControllerValidation = new BatchControllerValidation()(config, versions)
  val codelistValidation : CodelistControllerValidation = new CodelistControllerValidation()(config, versions)

  val addressController = new AddressController(components, elasticRepositoryMock, parser, config, versions, overloadProtection, addressValidation)
  val partialAddressController = new PartialAddressController(components, elasticRepositoryMock, parser, config, versions, overloadProtection, partialAddressValidation)

  val postcodeController = new PostcodeController(components, elasticRepositoryMock, parser, config, versions, overloadProtection, postcodeValidation)
  val uprnController = new UPRNController(components, elasticRepositoryMock, parser, config, versions, overloadProtection, uprnValidation)
  val codelistController = new CodelistController(components, elasticRepositoryMock, parser, config, versions, overloadProtection, codelistValidation)

  "Address controller" should {

    "reply with a found address in concise format (by uprn)" in {
      // Given
      val controller = uprnController

      val expected = Json.toJson(AddressByUprnResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByUprnResponse(
          address = Some(AddressResponseAddress.fromHybridAddress(validHybridAddress, false)),
          historical = true,
          startDate = "",
          endDate = "",
          verbose = false
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = controller.uprnQuery(validHybridAddress.uprn, verbose=Some("false")).apply(FakeRequest())
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
          address = Some(AddressResponseAddress.fromHybridAddress(validHybridAddress, true)),
          historical = true,
          startDate = "",
          endDate = "",
          verbose = true
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = controller.uprnQuery(validHybridAddress.uprn,  verbose=Some("true")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply on a found address in concise format (by uprn query with start and end date)" in {
      // Given
      val controller = uprnController

      val expected = Json.toJson(AddressByUprnResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByUprnResponse(
          address = Some(AddressResponseAddress.fromHybridAddress(validHybridAddress, false)),
          historical = true,
          startDate = "2013-01-01",
          endDate = "2014-01-01",
          verbose = false
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = controller.uprnQuery(validHybridAddress.uprn, Some("2013-01-01"), Some("2014-01-01"), verbose=Some("false")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply on a found address in verbose format (by uprn query with start and end date)" in {
      // Given
      val controller = uprnController

      val expected = Json.toJson(AddressByUprnResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByUprnResponse(
          address = Some(AddressResponseAddress.fromHybridAddress(validHybridAddress, true)),
          historical = true,
          startDate = "2013-01-01",
          endDate = "2014-01-01",
          verbose = true
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = controller.uprnQuery(validHybridAddress.uprn, Some("2013-01-01"), Some("2014-01-01"), verbose=Some("true")).apply(FakeRequest())
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
          postcode = "some query",
          addresses = Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, false)),
          filter = "",
          historical = true,
          limit = 100,
          offset = 0,
          total = 1,
          maxScore = 1.0f,
          startDate = "",
          endDate = "",
          verbose = false
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = controller.postcodeQuery("some query",verbose=Some("false")).apply(FakeRequest())

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
          postcode = "some query",
          addresses = Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, true)),
          filter = "",
          historical = true,
          limit = 100,
          offset = 0,
          total = 1,
          maxScore = 1.0f,
          startDate = "",
          endDate = "",
          verbose = true
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = controller.postcodeQuery("some query",verbose=Some("true")).apply(FakeRequest())

      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply on a found address in concise format (by postcode query with start and end date)" in {
      // Given
      val controller = postcodeController

      val expected = Json.toJson(AddressByPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByPostcodeResponse(
          postcode = "some query",
          addresses = Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, false)),
          filter = "",
          historical = true,
          limit = 100,
          offset = 0,
          total = 1,
          maxScore = 1.0f,
          startDate = "2013-01-01",
          endDate = "2014-01-01",
          verbose = false
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = controller.postcodeQuery("some query", None, None, None, Some("2013-01-01"), Some("2014-01-01"), verbose=Some("false")).apply(FakeRequest())

      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply on a found address in verbose format (by postcode query with start and end date)" in {
      // Given
      val controller = postcodeController

      val expected = Json.toJson(AddressByPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByPostcodeResponse(
          postcode = "some query",
          addresses = Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, true)),
          filter = "",
          historical = true,
          limit = 100,
          offset = 0,
          total = 1,
          maxScore = 1.0f,
          startDate = "2013-01-01",
          endDate = "2014-01-01",
          verbose = true
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = controller.postcodeQuery("some query", None, None, None, Some("2013-01-01"), Some("2014-01-01"), verbose=Some("true")).apply(FakeRequest())

      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply on a found address in concise format (by partial address query with start and end date)" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressByPartialAddressResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByPartialAddressResponse(
          input = "some query",
          addresses = Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress,false)),
          filter = "",
          historical = true,
          limit = 20,
          offset = 0,
          total = 1,
          maxScore = 1.0f,
          startDate = "2013-01-01",
          endDate = "2014-01-01",
          verbose = false
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = partialAddressController.partialAddressQuery("some query", None, None, None, Some("2013-01-01"), Some("2014-01-01"), verbose=Some("false")).apply(FakeRequest())

      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply on a found address in verbose format (by partial address query with start and end date)" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressByPartialAddressResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByPartialAddressResponse(
          input = "some query",
          addresses = Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress,true)),
          filter = "",
          historical = true,
          limit = 20,
          offset = 0,
          total = 1,
          maxScore = 1.0f,
          startDate = "2013-01-01",
          endDate = "2014-01-01",
          verbose = true
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = partialAddressController.partialAddressQuery("some query", None, None, None, Some("2013-01-01"), Some("2014-01-01"), verbose=Some("true")).apply(FakeRequest())

      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }


    "reply on a found address in concise format (by partial)" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressByPartialAddressResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByPartialAddressResponse(
          input = "some query",
          addresses = Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, false)),
          filter = "",
          historical = true,
          limit = 20,
          offset = 0,
          total = 1,
          maxScore = 1.0f,
          startDate = "",
          endDate = "",
          verbose = false
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = partialAddressController.partialAddressQuery("some query", None, None, None, None, None, verbose=Some("false")).apply(FakeRequest())

      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply on a found address in verbose format (by partial)" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressByPartialAddressResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByPartialAddressResponse(
          input = "some query",
          addresses = Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, true)),
          filter = "",
          historical = true,
          limit = 20,
          offset = 0,
          total = 1,
          maxScore = 1.0f,
          startDate = "",
          endDate = "",
          verbose = true
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = partialAddressController.partialAddressQuery("some query", None, None, None, None, None, verbose=Some("true")).apply(FakeRequest())

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
          addresses = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, false)),Map.empty,-1D),
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
          startDate = "",
          endDate = "",
          verbose = false
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
          addresses = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, true)),Map.empty,-1D),
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
          startDate = "",
          endDate = "",
          verbose = true
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


    "reply with a found address in concise format (by address query with radius)" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress,false)),Map.empty,-1D),
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
          startDate = "",
          endDate = "",
          verbose = false
        ),
        OkAddressResponseStatus
      ))

      // When
      val result = controller.addressQuery("some query", None, None, None, Some("1"), Some("50.705948"), Some("-3.5091076"), verbose=Some("false")).apply(FakeRequest())
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
          addresses = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress,true)),Map.empty,-1D),
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
          startDate = "",
          endDate = "",
          verbose = true
        ),
        OkAddressResponseStatus
      ))

      // When
      val result = controller.addressQuery("some query", None, None, None, Some("1"), Some("50.705948"), Some("-3.5091076"), verbose=Some("true")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }


    "reply with a found address in concise format (by address query with start and end date)" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, false)),Map.empty,-1D),
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
          startDate = "2013-01-01",
          endDate = "2014-01-01",
          verbose = false
        ),
        OkAddressResponseStatus
      ))

      // When
      val result = controller.addressQuery("some query", None, None, None, None, None, None, Some("2013-01-01"), Some("2014-01-01"),verbose=Some("false")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply with a found address in verbose format (by address query with start and end date)" in {
      // Given
      val controller = addressController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress, true)),Map.empty,-1D),
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
          startDate = "2013-01-01",
          endDate = "2014-01-01",
          verbose = true
        ),
        OkAddressResponseStatus
      ))

      // When
      val result = controller.addressQuery("some query", None, None, None, None, None, None, Some("2013-01-01"), Some("2014-01-01"),verbose=Some("true")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
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
          startDate = "",
          endDate = "",
          verbose = true
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
          limit = 10,
          offset = 0,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          startDate = "",
          endDate = "",
          verbose = true
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
          postcode = "",
          addresses = Seq.empty,
          filter = "",
          historical = true,
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          startDate = "",
          endDate = "",
          verbose = true
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(OffsetNotNumericAddressResponseError)
      ))

      // When
      val result = controller.postcodeQuery("some query", Some("thing"), Some("1")).apply(FakeRequest())
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
          offset = 0,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          startDate = "",
          endDate = "",
          verbose = true
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
          postcode = "",
          addresses = Seq.empty,
          filter = "",
          historical = true,
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          startDate = "",
          endDate = "",
          verbose = true
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(LimitNotNumericAddressResponseError)
      ))

      // When
      val result = controller.postcodeQuery("some query", Some("1"), Some("thing")).apply(FakeRequest())
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
          limit = 10,
          offset = 0,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          startDate = "",
          endDate = "",
          verbose = true
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
          postcode = "",
          addresses = Seq.empty,
          filter = "",
          historical = true,
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          startDate = "",
          endDate = "",
          verbose = true
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(OffsetTooSmallAddressResponseError)
      ))

      // When
      val result = controller.postcodeQuery("some query", Some("-1"), Some("1")).apply(FakeRequest())
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
          limit = 10,
          offset = 0,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          startDate = "",
          endDate = "",
          verbose = true
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
          postcode = "",
          addresses = Seq.empty,
          filter = "",
          historical = true,
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          startDate = "",
          endDate = "",
          verbose = true
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(LimitTooSmallAddressResponseError)
      ))

      // When
      val result = controller.postcodeQuery("some query", Some("0"), Some("0")).apply(FakeRequest())
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
          limit = 10,
          offset = 0,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          startDate = "",
          endDate = "",
          verbose = true
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
          postcode = "",
          addresses = Seq.empty,
          filter = "",
          historical = true,
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          startDate = "",
          endDate = "",
          verbose = true
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(postcodeValidation.OffsetTooLargeAddressResponseErrorCustom)
      ))

      // When
      val result = controller.postcodeQuery("some query", Some("9999999"), Some("1")).apply(FakeRequest())
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
          limit = 10,
          offset = 0,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          startDate = "",
          endDate = "",
          verbose = true
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
          postcode = "",
          addresses = Seq.empty,
          filter = "",
          historical = true,
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          startDate = "",
          endDate = "",
          verbose = true
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(postcodeValidation.LimitTooLargeAddressResponseErrorCustom)
      ))

      // When
      val result = controller.postcodeQuery("some query", Some("0"), Some("999999")).apply(FakeRequest())
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
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          startDate = "",
          endDate = "",
          verbose = true
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(RangeNotNumericAddressResponseError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("1"), Some("1"),None,Some("alongway")).apply(FakeRequest())
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
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          startDate = "",
          endDate = "",
          verbose = true
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(LatitudeNotNumericAddressResponseError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("1"), Some("1"),None,Some("1"),Some("oopnorth"),Some("0")).apply(FakeRequest())
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
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          startDate = "",
          endDate = "",
          verbose = true
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(LongitudeNotNumericAddressResponseError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("1"), Some("1"),None,Some("1"),Some("50"),Some("eastofthechipshop")).apply(FakeRequest())
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
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          startDate = "",
          endDate = "",
          verbose = true
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(LatitudeTooFarNorthAddressResponseError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("1"), Some("1"),None,Some("1"),Some("66.6"),Some("0")).apply(FakeRequest())
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
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          startDate = "",
          endDate = "",
          verbose = true
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(LongitudeTooFarEastAddressResponseError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("1"), Some("1"),None,Some("1"),Some("50"),Some("2.8")).apply(FakeRequest())
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
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          startDate = "",
          endDate = "",
          verbose = true
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(LatitudeTooFarSouthAddressResponseError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("1"), Some("1"),None,Some("1"),Some("44.4"),Some("0")).apply(FakeRequest())
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
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          sampleSize = 20,
          maxScore = 0.0f,
          matchthreshold = 5f,
          startDate = "",
          endDate = "",
          verbose = true
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(LongitudeTooFarWestAddressResponseError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("1"), Some("1"),None,Some("1"),Some("50"),Some("-8.8")).apply(FakeRequest())
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
          startDate = "",
          endDate = "",
          verbose = true
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

    "reply on a 400 error if startDate is not valid (by address query)" in {
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
          startDate = "",
          endDate = "",
          verbose = true
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(StartDateInvalidResponseError)
      ))

      // When
      val result = controller.addressQuery("query", Some("1"), Some("1"), None, None, None, None, Some("xyz"), Some("2013-01-01")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply on a 400 error if endDate is not valid (by address query)" in {
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
          startDate = "",
          endDate = "",
          verbose = true
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(EndDateInvalidResponseError)
      ))

      // When
      val result = controller.addressQuery("query", Some("1"), Some("1"), None, None, None, None, Some("2013-01-01"), Some("xyz")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply on a 400 error if startDate is not valid (by uprn query)" in {
      // Given
      val controller =  uprnController

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
          startDate = "",
          endDate = "",
          verbose = true
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(StartDateInvalidResponseError)
      ))

      // When
      val result = controller.uprnQuery("1234", Some("xyz"), Some("2013-01-01")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply on a 400 error if endDate is not valid (by uprn query)" in {
      // Given
      val controller = uprnController

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
          startDate = "",
          endDate = "",
          verbose = true
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(EndDateInvalidResponseError)
      ))

      // When
      val result = controller.uprnQuery("1234", Some("2013-01-01"), Some("xyz")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply on a 400 error if startDate is not valid (by postcode query)" in {
      // Given
      val controller = postcodeController

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
          startDate = "",
          endDate = "",
          verbose = true
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(StartDateInvalidResponseError)
      ))

      // When
      val result = controller.postcodeQuery("1234", None, None, None, Some("xyz"), Some("2013-01-01")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply on a 400 error if endDate is not valid (by postcode query)" in {
      // Given
      val controller = postcodeController

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
          startDate = "",
          endDate = "",
          verbose = true
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(EndDateInvalidResponseError)
      ))

      // When
      val result = controller.postcodeQuery("1234", None, None, None, Some("2013-01-01"), Some("xyz")).apply(FakeRequest())
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
          input = "",
          addresses = Seq.empty,
          filter = "",
          historical = true,
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          startDate = "",
          endDate = "",
          verbose = true
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(partialAddressValidation.ShortQueryAddressResponseErrorCustom)
      ))

      // When
      val result = controller.partialAddressQuery("foo", startDate = None, endDate = None).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply on a 400 error if startDate is not valid (by partial address query)" in {
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
          startDate = "",
          endDate = "",
          verbose = true
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(StartDateInvalidResponseError)
      ))

      // When
      val result = partialAddressController.partialAddressQuery("query", None, None, None, Some("xyz"), Some("2013-01-01")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply on a 400 error if endDate is not valid (by partial address query)" in {
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
          startDate = "",
          endDate = "",
          verbose = true
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(EndDateInvalidResponseError)
      ))

      // When
      val result = partialAddressController.partialAddressQuery("query", None, None, None, Some("2013-01-01"), Some("xyz")).apply(FakeRequest())
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
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          startDate = "",
          endDate = "",
          verbose = true
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

    "reply with a 429 error if Elastic threw exception (request failed) while querying for address" in {
      // Given
      val controller = new AddressController(components, failingRepositoryMock, parser, config, versions, overloadProtection, addressValidation)

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
          startDate = "",
          endDate = "",
          verbose = true
        ),
        TooManyRequestsResponseStatus,
        errors = Seq(FailedRequestToEsError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("0"), Some("10")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe TOO_MANY_REQUESTS
      actual mustBe expected
    }

    "reply with a 429 error if Elastic threw exception (request failed) while querying for postcode" in {
      // Given
      val controller = new PostcodeController(components, failingRepositoryMock, parser, config, versions, overloadProtection, postcodeValidation)

      val expected = Json.toJson(AddressByPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByPostcodeResponse(
          postcode = "",
          addresses = Seq.empty,
          filter = "",
          historical = true,
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f,
          startDate = "",
          endDate = "",
          verbose = true
        ),
        TooManyRequestsResponseStatus,
        errors = Seq(FailedRequestToEsError)
      ))

      // When - retry param must be true
      val result = controller.postcodeQuery("some query", Some("0"), Some("10")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe TOO_MANY_REQUESTS
      actual mustBe expected
    }

    "reply with a 429 error if Elastic threw exception (request failed) while querying for uprn" in {
      // Given
      val controller = new UPRNController(components, failingRepositoryMock, parser, config, versions, overloadProtection, uprnValidation)

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
          startDate = "",
          endDate = "",
          verbose = true
        ),
        TooManyRequestsResponseStatus,
        errors = Seq(FailedRequestToEsError)
      ))

      // When
      val result = controller.uprnQuery("12345").apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe TOO_MANY_REQUESTS
      actual mustBe expected
    }

    "reply a 400 error if address was not numeric (by uprn)" in {
      // Given
      val controller = new UPRNController(components, emptyElasticRepositoryMock, parser, config, versions, overloadProtection, uprnValidation)

      val expected = Json.toJson(AddressByUprnResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByUprnResponse(
          address = None,
          historical = true,
          startDate = "",
          endDate = "",
          verbose = true
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
      val controller = new UPRNController(components, emptyElasticRepositoryMock, parser, config, versions, overloadProtection, uprnValidation)

      val expected = Json.toJson(AddressByUprnResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByUprnResponse(
          address = None,
          historical = true,
          startDate = "",
          endDate = "",
          verbose = true
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
      val controller = new BatchController(components, sometimesFailingRepositoryMock, parser, config, versions, batchValidation)

      val requestsData: Stream[BulkAddressRequestData] = Stream(
        BulkAddressRequestData("","1", Map("first" -> "success")),
        BulkAddressRequestData("","2", Map("second" -> "success")),
        BulkAddressRequestData("","3", Map("third" -> "failed"))
      )

      // When
      val result: BulkAddresses = Await.result(controller.queryBulkAddresses(requestsData, 3, None, "", "", true, 5F), Duration.Inf )

      // Then
      result.successfulBulkAddresses.size mustBe 2
      result.failedRequests.size mustBe 1
    }

    "have process bulk addresses using back-pressure" in {
      // Given
      val controller = new BatchController(components, sometimesFailingRepositoryMock, parser, config, versions, batchValidation)

      val requestsData: Stream[BulkAddressRequestData] = Stream(
        BulkAddressRequestData("","1", Map("first" -> "success")),
        BulkAddressRequestData("","2", Map("second" -> "success")),
        BulkAddressRequestData("","3", Map("third" -> "success")),
        BulkAddressRequestData("","4", Map("forth" -> "success")),
        BulkAddressRequestData("","5", Map("fifth" -> "success")),
        BulkAddressRequestData("","6", Map("sixth" -> "success")),
        BulkAddressRequestData("","7", Map("seventh" -> "success")),
        BulkAddressRequestData("","8", Map("eighth" -> "success")),
        BulkAddressRequestData("","9", Map("ninth" -> "success"))
      )

      // When
      val result = controller.iterateOverRequestsWithBackPressure(requestsData, 3, None, None, "", "", true, 5F)

      // Then
      result.size mustBe requestsData.size
    }

    "have back-pressure that should throw an exception if there is an always failing request" in {
      // Given
      val controller = new BatchController(components, sometimesFailingRepositoryMock, parser, config, versions, batchValidation)

      val requestsData: Stream[BulkAddressRequestData] = Stream(
        BulkAddressRequestData("","1", Map("first" -> "success")),
        BulkAddressRequestData("","2", Map("second" -> "success")),
        BulkAddressRequestData("","3", Map("third" -> "failed"))
      )

      // When Then
      an [Exception] should be thrownBy controller.iterateOverRequestsWithBackPressure(requestsData, 10, None, None, "", "", true, 5F)
    }

    "return list of codelists" in {
      // Given
      val expectedCodelist = validCodelistList
      val controller = codelistController

      // When
      val result = controller.codeList().apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      actual.toString().substring(0,expectedCodelist.length) mustBe expectedCodelist
    }

    "return list of sources" in {
      // Given
      val expectedCodelist = validSourceList
      val controller = codelistController

      // When
      val result = controller.codeListSource().apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      actual.toString().substring(0,expectedCodelist.length) mustBe expectedCodelist
    }

    "return list of classifications" in {
      // Given
      val expectedCodelist = validClassificationList
      val controller = codelistController

      // When
      val result = controller.codeListClassification().apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      actual.toString().substring(0,expectedCodelist.length) mustBe expectedCodelist
    }

    "return list of custodians" in {
      // Given
      val expectedCodelist = validCustodianList
      val controller = codelistController

      // When
      val result = controller.codeListCustodian().apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      actual.toString().substring(0,expectedCodelist.length) mustBe expectedCodelist
    }

    "return list of logical statuses" in {
      // Given
      val expectedCodelist = validLogicalStatusList
      val controller = codelistController

      // When
      val result = controller.codeListLogicalStatus().apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      actual.toString().substring(0,expectedCodelist.length) mustBe expectedCodelist
    }
  }
}
