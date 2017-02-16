package uk.gov.ons.addressIndex.server.controllers

import akka.stream.Materializer
import uk.gov.ons.addressIndex.model.server.response._
import uk.gov.ons.addressIndex.server.modules._
import com.sksamuel.elastic4s.{ElasticClient, SearchDefinition}
import org.elasticsearch.common.settings.Settings
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Result, Results}
import play.api.test.{FakeRequest, WithApplication}
import uk.gov.ons.addressIndex.model.db.index._
import org.scalatestplus.play._
import play.api.Logger
import play.api.http.HeaderNames
import play.api.test.Helpers._
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.{BulkBody, BulkQuery}
import uk.gov.ons.addressIndex.model.db.{BulkAddressRequestData, BulkAddresses}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class AddressControllerSpec extends PlaySpec with Results with AddressIndexCannedResponse {

  val validPafAddress = PostcodeAddressFileAddress(
    recordIdentifier = "1",
    changeType = "2",
    proOrder = "3",
    uprn = "4",
    udprn = "5",
    organizationName = "6",
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
    entryDate = "29"
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
    logicalStatus = "18",
    streetDescriptor = "19",
    townName = "20",
    locality = "21"
  )

  val validHybridAddress = HybridAddress(
    uprn = "1",
    paf = Seq(validPafAddress),
    lpi = Seq(validNagAddress),
    score = 1f
  )

  // injected value, change implementations accordingly when needed
  // mock that will return one address as a result
  val elasticRepositoryMock = new ElasticsearchRepository {

    override def queryUprn(uprn: String): Future[Option[HybridAddress]] =
      Future.successful(Some(validHybridAddress))

    override def queryAddresses(start:Int, limit: Int, tokens: Seq[CrfTokenResult]): Future[HybridAddresses] =
      Future.successful(HybridAddresses(Seq(validHybridAddress), 1.0f, 1))

    override def client(): ElasticClient = ElasticClient.local(Settings.builder().build())

    override def logger: Logger = ???

    override def generateQueryUprnRequest(uprn: String): SearchDefinition = ???

    override def generateQueryAddressRequest(tokens: Map[String, String]): SearchDefinition = ???
  }

  // mock that won't return any addresses
  val emptyElasticRepositoryMock = new ElasticsearchRepository {

    override def queryUprn(uprn: String): Future[Option[HybridAddress]] = Future.successful(None)

    override def queryAddresses(start:Int, limit: Int, tokens: Seq[CrfTokenResult]): Future[HybridAddresses] =
      Future.successful(HybridAddresses(Seq.empty, 1.0f, 0))

    override def client(): ElasticClient = ElasticClient.local(Settings.builder().build())

    override def logger: Logger = ???

    override def generateQueryUprnRequest(uprn: String): SearchDefinition = ???

    override def generateQueryAddressRequest(tokens: Map[String, String]): SearchDefinition = ???
  }

  val sometimesFailingRepositoryMock = new ElasticsearchRepository {

    override def queryUprn(uprn: String): Future[Option[HybridAddress]] = Future.successful(None)

    override def queryAddresses(start:Int, limit: Int, tokens: Seq[CrfTokenResult]): Future[HybridAddresses] =
      if (tokens.head.value == "failed") Future.failed(new Exception("test failure"))
      else Future.successful(HybridAddresses(Seq(validHybridAddress), 1.0f, 1))

    override def client(): ElasticClient = ElasticClient.local(Settings.builder().build())

    override def logger: Logger = ???

    override def generateQueryUprnRequest(uprn: String): SearchDefinition = ???

    override def generateQueryAddressRequest(tokens: Map[String, String]): SearchDefinition = ???
  }
  
  val parser = new ParserModule {
    override def tag(input: String): Seq[CrfTokenResult] = Seq.empty
  }
  val config = new AddressIndexConfigModule

  val queryController = new AddressController(elasticRepositoryMock, parser, config)

  "Address controller" should {

    "reply on a found address (by uprn)" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressByUprnResponseContainer(
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

    "reply with a found address (by address query)" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        AddressBySearchResponse(
          tokens = Seq.empty,
          addresses = Seq(AddressResponseAddress.fromHybridAddress(validHybridAddress)),
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
    
    "reply on a 400 error if a non-numeric offset parameter is supplied" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        AddressBySearchResponse(
          tokens = Seq.empty,
          addresses = Seq.empty,
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
    
    "reply on a 400 error if a non-numeric limit parameter is supplied" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        AddressBySearchResponse(
          tokens = Seq.empty,
          addresses = Seq.empty,
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

    "reply on a 400 error if a negative offset parameter is supplied" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        AddressBySearchResponse(
          tokens = Seq.empty,
          addresses = Seq.empty,
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
    
    "reply on a 400 error if a negative or zero limit parameter is supplied" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        AddressBySearchResponse(
          tokens = Seq.empty,
          addresses = Seq.empty,
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
    
    "reply on a 400 error if an offset parameter greater than the maximum allowed is supplied" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        AddressBySearchResponse(
          tokens = Seq.empty,
          addresses = Seq.empty,
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

    "reply on a 400 error if a limit parameter larger than the maximum allowed is supplied" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        AddressBySearchResponse(
          tokens = Seq.empty,
          addresses = Seq.empty,
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

    "reply on bulk post req" ignore new WithApplication {
      val mtrlzr = app.injector.instanceOf[Materializer]
      // Given
      val controller = queryController
      val request = {
        FakeRequest(
          method = "POST",
          path = uk.gov.ons.addressIndex.server.controllers.routes.AddressController.bulkQuery.url
        ).withJsonBody(
          Json.toJson(
            BulkBody(
              Seq(
                BulkQuery(
                  id = "",
                  address = ""
                )
              )
            )
          )
        ).withHeaders(
          HeaderNames.CONTENT_TYPE -> "application/json"
        )
      }

      // When
      val result: Future[Result] = controller.bulkQuery().apply(request).run()(mtrlzr)
      // Then
      status(result) mustBe Ok
    }
    "reply on a 400 error if query is empty (by address query)" in {
      // Given
      val controller = queryController

      val expected = Json.toJson(AddressBySearchResponseContainer(
        AddressBySearchResponse(
          tokens = Seq.empty,
          addresses = Seq.empty,
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
    
    "reply a 404 error if address was not found (by uprn)" in {
      // Given
      val controller = new AddressController(emptyElasticRepositoryMock, parser, config)

      val expected = Json.toJson(AddressByUprnResponseContainer(
        response = AddressByUprnResponse(
          address = None
        ),
        NotFoundAddressResponseStatus,
        errors = Seq(NotFoundAddressResponseError)
      ))

      // When
      val result = controller.uprnQuery("doesn't exist").apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe NOT_FOUND
      actual mustBe expected
    }

    "do multiple search from an iterator with tokens (AddressIndexActions method)" in {
      // Given
      val controller = new AddressController(sometimesFailingRepositoryMock, parser, config)

      val requestsData: Stream[BulkAddressRequestData] = Stream(
        BulkAddressRequestData("", "1", Seq(CrfTokenResult("success", "first"))),
        BulkAddressRequestData("", "2", Seq(CrfTokenResult("success", "second"))),
        BulkAddressRequestData("", "3", Seq(CrfTokenResult("failed", "third")))
      )

      // When
      val result: BulkAddresses = Await.result(controller.queryBulkAddresses(requestsData, 3), Duration.Inf )

      // Then
      result.successfulBulkAddresses.size mustBe 2
      result.failedRequests.size mustBe 1
    }

    "have process bulk addresses using back-pressure" in {
      // Given
      val controller = new AddressController(sometimesFailingRepositoryMock, parser, config)

      val requestsData: Stream[BulkAddressRequestData] = Stream(
        BulkAddressRequestData("", "1", Seq(CrfTokenResult("success", "first"))),
        BulkAddressRequestData("", "2", Seq(CrfTokenResult("success", "second"))),
        BulkAddressRequestData("", "3", Seq(CrfTokenResult("success", "third"))),
        BulkAddressRequestData("", "4", Seq(CrfTokenResult("success", "forth"))),
        BulkAddressRequestData("", "5", Seq(CrfTokenResult("success", "fifth"))),
        BulkAddressRequestData("", "6", Seq(CrfTokenResult("success", "sixth"))),
        BulkAddressRequestData("", "7", Seq(CrfTokenResult("success", "seventh"))),
        BulkAddressRequestData("", "8", Seq(CrfTokenResult("success", "eighth"))),
        BulkAddressRequestData("", "9", Seq(CrfTokenResult("success", "ninth")))
      )

      // When
      val result = controller.iterateOverRequestsWithBackPressure(requestsData, 3, Seq.empty)

      // Then
      result.size mustBe requestsData.size
    }

    "have back-pressure that should throw an exception if there is an always failing request" in {
      // Given
      val controller = new AddressController(sometimesFailingRepositoryMock, parser, config)

      val requestsData: Stream[BulkAddressRequestData] = Stream(
        BulkAddressRequestData("", "1", Seq(CrfTokenResult("success", "first"))),
        BulkAddressRequestData("", "2", Seq(CrfTokenResult("success", "second"))),
        BulkAddressRequestData("", "3", Seq(CrfTokenResult("failed", "third")))
      )

      // When Then
      an [Exception] should be thrownBy controller.iterateOverRequestsWithBackPressure(requestsData, 10, Seq.empty)
    }

  }
}
