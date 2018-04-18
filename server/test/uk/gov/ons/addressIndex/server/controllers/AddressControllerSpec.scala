package uk.gov.ons.addressIndex.server.controllers

import com.sksamuel.elastic4s.IndexesAndTypes
import com.sksamuel.elastic4s.searches.SearchDefinition
import org.scalatestplus.play._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Result, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.ons.addressIndex.model.config.QueryParamsConfig
import uk.gov.ons.addressIndex.model.db.index._
import uk.gov.ons.addressIndex.model.db.{BulkAddress, BulkAddressRequestData, BulkAddresses}
import uk.gov.ons.addressIndex.model.server.response._
import uk.gov.ons.addressIndex.server.modules._
import uk.gov.ons.addressIndex.server.utils.HopperScoreHelper

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class AddressControllerSpec extends PlaySpec with Results{

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
    pafAll = "30"
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
    lpiEndDate = "lpiEndDate"
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

  // injected value, change implementations accordingly when needed
  // mock that will return one address as a result
  val elasticRepositoryMock = new ElasticsearchRepository {

    override def queryUprn(uprn: String, historical: Boolean = true): Future[Option[HybridAddress]] =
      Future.successful(Some(validHybridAddress))

    override def queryPostcode(postcode: String, start:Int, limit: Int, filters: String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true): Future[HybridAddresses] =
      Future.successful(HybridAddresses(Seq(validHybridAddress), 1.0f, 1))

    override def queryAddresses(tokens: Map[String, String], start:Int, limit: Int, filters: String, range: String, lat: String, lon:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true): Future[HybridAddresses] =
      Future.successful(HybridAddresses(Seq(validHybridAddress), 1.0f, 1))

    override def queryBulk(requestsData: Stream[BulkAddressRequestData], limit: Int, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true): Future[Stream[Either[BulkAddressRequestData, Seq[BulkAddress]]]] =
      Future.successful{
        requestsData.map(requestData => Right(Seq(BulkAddress.fromHybridAddress(validHybridAddress, requestData))))
      }

    override def queryHealth(): Future[String] = Future.successful("")

    override def generateQueryAddressRequest(tokens: Map[String, String], filters: String, range: String, lat: String, lon:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true): SearchDefinition = SearchDefinition(IndexesAndTypes())
  }

  // mock that won't return any addresses
  val emptyElasticRepositoryMock = new ElasticsearchRepository {

    override def queryUprn(uprn: String, historical: Boolean = true): Future[Option[HybridAddress]] = Future.successful(None)

    override def queryPostcode(postcode: String, start:Int, limit: Int, filters: String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true): Future[HybridAddresses] =
      Future.successful(HybridAddresses(Seq.empty, 1.0f, 0))

    override def queryAddresses(tokens: Map[String, String], start:Int, limit: Int, filters: String, range: String, lat: String, lon:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true): Future[HybridAddresses] =
      Future.successful(HybridAddresses(Seq.empty, 1.0f, 0))

    override def queryBulk(requestsData: Stream[BulkAddressRequestData], limit: Int, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true): Future[Stream[Either[BulkAddressRequestData, Seq[BulkAddress]]]] =
      Future.successful{
        requestsData.map(requestData => Right(Seq(BulkAddress.empty(requestData))))
      }

    override def queryHealth(): Future[String] = Future.successful("")

    override def generateQueryAddressRequest(tokens: Map[String, String], filters: String, range: String, lat: String, lon:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true): SearchDefinition = SearchDefinition(IndexesAndTypes())
  }

  val sometimesFailingRepositoryMock = new ElasticsearchRepository {

    override def queryUprn(uprn: String, historical: Boolean = true): Future[Option[HybridAddress]] = Future.successful(None)

    override def queryPostcode(postcode: String, start:Int, limit: Int, filters: String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true): Future[HybridAddresses] = Future.successful(HybridAddresses(Seq(validHybridAddress), 1.0f, 1))

    override def queryAddresses(tokens: Map[String, String], start:Int, limit: Int, filters: String, range: String, lat: String, lon:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true): Future[HybridAddresses] =
      if (tokens.values.exists(_ == "failed")) Future.failed(new Exception("test failure"))
      else Future.successful(HybridAddresses(Seq(validHybridAddress), 1.0f, 1))

    override def queryBulk(requestsData: Stream[BulkAddressRequestData], limit: Int, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true): Future[Stream[Either[BulkAddressRequestData, Seq[BulkAddress]]]] =
      Future.successful{
        requestsData.map{
          case requestData if requestData.tokens.values.exists(_ == "failed") => Left(requestData)
          case requestData => Right(Seq(BulkAddress.fromHybridAddress(validHybridAddress, requestData)))
        }
      }

    override def queryHealth(): Future[String] = Future.successful("")

    override def generateQueryAddressRequest(tokens: Map[String, String], filters: String, range: String, lat: String, lon:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true): SearchDefinition = SearchDefinition(IndexesAndTypes())
  }

  val failingRepositoryMock = new ElasticsearchRepository {

    override def queryUprn(uprn: String, historical: Boolean = true): Future[Option[HybridAddress]] =
      Future.failed(new Exception("test failure"))

    override def queryPostcode(postcode: String, start:Int, limit: Int, filters: String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true): Future[HybridAddresses] =
      Future.failed(new Exception("test failure"))

    override def queryAddresses(tokens: Map[String, String], start:Int, limit: Int, filters: String, range: String, lat: String, lon:String,  queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true): Future[HybridAddresses] =
      Future.failed(new Exception("Test exception"))

    override def queryBulk(requestsData: Stream[BulkAddressRequestData], limit: Int, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true): Future[Stream[Either[BulkAddressRequestData, Seq[BulkAddress]]]] =
      Future.failed(new Exception("Test exception"))

    override def queryHealth(): Future[String] = Future.successful("")

    override def generateQueryAddressRequest(tokens: Map[String, String], filters: String, range: String, lat: String, lon:String, queryParamsConfig: Option[QueryParamsConfig], historical: Boolean = true): SearchDefinition = SearchDefinition(IndexesAndTypes())
  }

  val parser = new ParserModule {
    override def parse(input: String): Map[String, String] = Map.empty
  }
  val config = new AddressIndexConfigModule

  val apiVersionExpected = "testApi"
  val dataVersionExpected = "testData"

  val versions = new VersionModule {
    val apiVersion = apiVersionExpected
    val dataVersion = dataVersionExpected
  }

  val components = stubControllerComponents()

  val queryController = new AddressController(components, elasticRepositoryMock, parser, config, versions)

  "Address controller" should {

    "reply on a found address (by uprn)" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressByUprnResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByUprnResponse(
          address = Some(AddressResponseAddress.fromHybridAddress(validHybridAddress))
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = controller.uprnQuery(validHybridAddress.uprn).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply on a found address (by postcode)" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressByPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByPostcodeResponse(
          postcode = "some query",
          addresses = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress)),Map.empty,1D),
          filter = "",
          limit = 100,
          offset = 0,
          total = 1,
          maxScore = 1.0f
        ),
        OkAddressResponseStatus
      ))

      // When
      val result: Future[Result] = controller.postcodeQuery("some query").apply(FakeRequest())

      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply with a found address (by address query)" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress)),Map.empty,1D),
          filter = "",
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 1,
          maxScore = 1.0f
        ),
        OkAddressResponseStatus
      ))

      // When
      val result = controller.addressQuery("some query").apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }


    "reply with a found address (by address query with radius)" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = HopperScoreHelper.getScoresForAddresses(Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress)),Map.empty,1D),
          filter = "",
          rangekm = "1",
          latitude = "50.705948",
          longitude = "-3.5091076",
          limit = 10,
          offset = 0,
          total = 1,
          maxScore = 1.0f
        ),
        OkAddressResponseStatus
      ))

      // When
      val result = controller.addressQuery("some query", None, None, None, Some("1"),  Some("50.705948"), Some("-3.5091076")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }


    "reply on a 400 error if an invalid filter value is supplied" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
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


    "reply on a 400 error if a non-numeric offset parameter is supplied" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
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

    "reply on a 400 error if a non-numeric offset parameter is supplied (postcode)" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressByPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByPostcodeResponse(
          postcode = "",
          addresses = Seq.empty,
          filter = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(OffsetNotNumericPostcodeAddressResponseError)
      ))

      // When
      val result = controller.postcodeQuery("some query", Some("thing"), Some("1")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply on a 400 error if a non-numeric limit parameter is supplied" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
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

    "reply on a 400 error if a non-numeric limit parameter is supplied (postcode)" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressByPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByPostcodeResponse(
          postcode = "",
          addresses = Seq.empty,
          filter = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(LimitNotNumericPostcodeAddressResponseError)
      ))

      // When
      val result = controller.postcodeQuery("some query", Some("1"), Some("thing")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply on a 400 error if a negative offset parameter is supplied" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
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

    "reply on a 400 error if a negative offset parameter is supplied (postcode)" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressByPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByPostcodeResponse(
          postcode = "",
          addresses = Seq.empty,
          filter = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(OffsetTooSmallPostcodeAddressResponseError)
      ))

      // When
      val result = controller.postcodeQuery("some query", Some("-1"), Some("1")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply on a 400 error if a negative or zero limit parameter is supplied" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
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

    "reply on a 400 error if a negative or zero limit parameter is supplied (postcode)" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressByPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByPostcodeResponse(
          postcode = "",
          addresses = Seq.empty,
          filter = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(LimitTooSmallPostcodeAddressResponseError)
      ))

      // When
      val result = controller.postcodeQuery("some query", Some("0"), Some("0")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply on a 400 error if an offset parameter greater than the maximum allowed is supplied" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(OffsetTooLargeAddressResponseError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("9999999"), Some("1")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply on a 400 error if an offset parameter greater than the maximum allowed is supplied (postcode)" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressByPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByPostcodeResponse(
          postcode = "",
          addresses = Seq.empty,
          filter = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(OffsetTooLargePostcodeAddressResponseError)
      ))

      // When
      val result = controller.postcodeQuery("some query", Some("9999999"), Some("1")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply on a 400 error if a limit parameter larger than the maximum allowed is supplied" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(LimitTooLargeAddressResponseError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("0"), Some("999999")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply on a 400 error if a limit parameter larger than the maximum allowed is supplied (postcode)" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressByPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByPostcodeResponse(
          postcode = "",
          addresses = Seq.empty,
          filter = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
        ),
        BadRequestAddressResponseStatus,
        errors = Seq(LimitTooLargePostcodeAddressResponseError)
      ))

      // When
      val result = controller.postcodeQuery("some query", Some("0"), Some("999999")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply on a 400 error if a non-numeric rangekm parameter is supplied" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
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

    "reply on a 400 error if a non-numeric latitude parameter is supplied" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
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

    "reply on a 400 error if a non-numeric longitude parameter is supplied" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
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

    "reply on a 400 error if a too far north latitude parameter is supplied" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
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

    "reply on a 400 error if a too far east longitude parameter is supplied" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
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

    "reply on a 400 error if a too far south latitude parameter is supplied" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
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

    "reply on a 400 error if a too far west longitude parameter is supplied" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
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



    "reply on a 400 error if query is empty (by address query)" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
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

    "reply on a 400 error if query is empty (by address query) (postcode)" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressByPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByPostcodeResponse(
          postcode = "",
          addresses = Seq.empty,
          filter = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
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

    "reply on a 500 error if Elastic threw exception (request failed) while querying for address" in {
      // Given
      val controller = new AddressController(components, failingRepositoryMock, parser, config, versions)

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
        ),
        InternalServerErrorAddressResponseStatus,
        errors = Seq(FailedRequestToEsError)
      ))

      // When
      val result = controller.addressQuery("some query", Some("0"), Some("10")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe INTERNAL_SERVER_ERROR
      actual mustBe expected
    }

    "reply on a 500 error if Elastic threw exception (request failed) while querying for postcode" in {
      // Given
      val controller = new AddressController(components, failingRepositoryMock, parser, config, versions)

      val expected = Json.toJson(AddressByPostcodeResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressByPostcodeResponse(
          postcode = "",
          addresses = Seq.empty,
          filter = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
        ),
        InternalServerErrorAddressResponseStatus,
        errors = Seq(FailedRequestToEsPostcodeError)
      ))

      // When - retry param must be true
      val result = controller.postcodeQuery("some query", Some("0"), Some("10")).apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe INTERNAL_SERVER_ERROR
      actual mustBe expected
    }

    "reply on a 500 error if Elastic threw exception (request failed) while querying for uprn" in {
      // Given
      val controller = new AddressController(components, failingRepositoryMock, parser, config, versions)

      val expected = Json.toJson(AddressBySearchResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        AddressBySearchResponse(
          tokens = Map.empty,
          addresses = Seq.empty,
          filter = "",
          rangekm = "",
          latitude = "",
          longitude = "",
          limit = 10,
          offset = 0,
          total = 0,
          maxScore = 0.0f
        ),
        InternalServerErrorAddressResponseStatus,
        errors = Seq(FailedRequestToEsError)
      ))

      // When
      val result = controller.uprnQuery("12345").apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe INTERNAL_SERVER_ERROR
      actual mustBe expected
    }

    "reply a 400 error if address was not numeric (by uprn)" in {
      // Given
      val controller = new AddressController(components, emptyElasticRepositoryMock, parser, config, versions)

      val expected = Json.toJson(AddressByUprnResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByUprnResponse(
          address = None
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
      val controller = new AddressController(components, emptyElasticRepositoryMock, parser, config, versions)

      val expected = Json.toJson(AddressByUprnResponseContainer(
        apiVersion = apiVersionExpected,
        dataVersion = dataVersionExpected,
        response = AddressByUprnResponse(
          address = None
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
      val controller = new AddressController(components, sometimesFailingRepositoryMock, parser, config, versions)

      val requestsData: Stream[BulkAddressRequestData] = Stream(
        BulkAddressRequestData("","1", Map("first" -> "success")),
        BulkAddressRequestData("","2", Map("second" -> "success")),
        BulkAddressRequestData("","3", Map("third" -> "failed"))
      )

      // When
      val result: BulkAddresses = Await.result(controller.queryBulkAddresses(requestsData, 3, None, true), Duration.Inf )

      // Then
      result.successfulBulkAddresses.size mustBe 2
      result.failedRequests.size mustBe 1
    }

    "have process bulk addresses using back-pressure" in {
      // Given
      val controller = new AddressController(components, sometimesFailingRepositoryMock, parser, config, versions)

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
      val result = controller.iterateOverRequestsWithBackPressure(requestsData, 3, None, None, true)

      // Then
      result.size mustBe requestsData.size
    }

    "have back-pressure that should throw an exception if there is an always failing request" in {
      // Given
      val controller = new AddressController(components, sometimesFailingRepositoryMock, parser, config, versions)

      val requestsData: Stream[BulkAddressRequestData] = Stream(
        BulkAddressRequestData("","1", Map("first" -> "success")),
        BulkAddressRequestData("","2", Map("second" -> "success")),
        BulkAddressRequestData("","3", Map("third" -> "failed"))
      )

      // When Then
      an [Exception] should be thrownBy controller.iterateOverRequestsWithBackPressure(requestsData, 10, None, None, true)
    }

  }
}
