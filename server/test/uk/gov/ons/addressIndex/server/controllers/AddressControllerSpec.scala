package uk.gov.ons.addressIndex.server.controllers

import uk.gov.ons.addressIndex.server.model.response._
import uk.gov.ons.addressIndex.server.modules.ElasticsearchRepository
import com.sksamuel.elastic4s.ElasticClient
import org.elasticsearch.common.settings.Settings
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results
import play.api.test.FakeRequest
import uk.gov.ons.addressIndex.model.db.index.PostcodeAddressFileAddress

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.test._
import uk.gov.ons.addressIndex.server.model.response.Implicits._
import org.scalatestplus.play._
import play.api.test.Helpers._

import scala.concurrent.Future

class AddressControllerSpec extends PlaySpec with Results {

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

  // injected value, change implementations accordingly when needed
  // mock that will return one address as a result
  val elasticRepositoryMock = new ElasticsearchRepository {

    override def queryUprn(uprn: String): Future[Option[PostcodeAddressFileAddress]] =
      Future.successful(Some(validPafAddress))

    override def createAll(): Future[Seq[_]] = Future.successful(Seq.empty)

    override def deleteAll(): Future[Seq[_]] = Future.successful(Seq.empty)

    override def queryAddress(tokens: AddressTokens): Future[Seq[PostcodeAddressFileAddress]] =
      Future.successful(Seq(validPafAddress))


    override def client(): ElasticClient = ElasticClient.local(Settings.builder().build())
  }

  // mock that won't return any addresses
  val emptyElasticRepositoryMock = new ElasticsearchRepository {

    override def queryUprn(uprn: String): Future[Option[PostcodeAddressFileAddress]] = Future.successful(None)

    override def createAll(): Future[Seq[_]] = Future.successful(Seq.empty)

    override def deleteAll(): Future[Seq[_]] = Future.successful(Seq.empty)

    override def queryAddress(tokens: AddressTokens): Future[Seq[PostcodeAddressFileAddress]] =
      Future.successful(Seq.empty)


    override def client(): ElasticClient = ElasticClient.local(Settings.builder().build())
  }

  "Address controller" should {

    "reply on a found address (by address query)" in {
      // Given
      val controller = new AddressController(elasticRepositoryMock)

      val expected = Json.toJson(AddressBySearchResponseContainer(
        AddressBySearchResponse(
          tokens = AddressTokens(
            uprn = "",
            buildingNumber = "10",
            postcode = "B16 8TH"
          ),
          addresses = Seq(AddressResponseAddress.fromPafAddress(validPafAddress)),
          limit = 10,
          offset = 0,
          total = 1
        ),
        AddressResponseStatus.ok,
        errors = Seq.empty
      ))

      // When
      val result = controller.addressQuery("10 B16 8TH", "paf").apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply on a 400 error if address format is not supported (by address query)" in {
      // Given
      val controller = new AddressController(elasticRepositoryMock)

      val expected = Json.toJson(AddressBySearchResponseContainer(
        AddressBySearchResponse(
          tokens = AddressTokens.empty,
          addresses = Seq.empty,
          limit = 10,
          offset = 0,
          total = 0
        ),
        AddressResponseStatus.badRequest,
        errors = Seq(AddressResponseError.addressFormatNotSupported)
      ))

      // When
      val result = controller.addressQuery("10 B16 8TH", "format is not supported").apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply on a 400 error if query is empty (by address query)" in {
      // Given
      val controller = new AddressController(elasticRepositoryMock)

      val expected = Json.toJson(AddressBySearchResponseContainer(
        AddressBySearchResponse(
          tokens = AddressTokens.empty,
          addresses = Seq.empty,
          limit = 10,
          offset = 0,
          total = 0
        ),
        AddressResponseStatus.badRequest,
        errors = Seq(AddressResponseError.emptyQuery)
      ))

      // When
      val result = controller.addressQuery("", "paf").apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

    "reply on a found address (by uprn)" in {
      // Given
      val controller = new AddressController(elasticRepositoryMock)

      val expected = Json.toJson(AddressByUprnResponseContainer(
        response = AddressByUprnResponse(
          address = Some(AddressResponseAddress.fromPafAddress(validPafAddress))
        ),
        AddressResponseStatus.ok,
        errors = Seq.empty
      ))

      // When
      val result = controller.uprnQuery("4", "paf").apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "reply a 404 error if address was not found (by uprn)" in {
      // Given
      val controller = new AddressController(emptyElasticRepositoryMock)

      val expected = Json.toJson(AddressByUprnResponseContainer(
        response = AddressByUprnResponse(
          address = None
        ),
        AddressResponseStatus.notFound,
        errors = Seq(AddressResponseError.notFound)
      ))

      // When
      val result = controller.uprnQuery("doesn't exist", "paf").apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe NOT_FOUND
      actual mustBe expected
    }

    "reply a 400 error if address format is not supported (by uprn)" in {
      // Given
      val controller = new AddressController(emptyElasticRepositoryMock)

      val expected = Json.toJson(AddressByUprnResponseContainer(
        response = AddressByUprnResponse(
          address = None
        ),
        AddressResponseStatus.badRequest,
        errors = Seq(AddressResponseError.addressFormatNotSupported)
      ))

      // When
      val result = controller.uprnQuery("4", "format is not supported").apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe BAD_REQUEST
      actual mustBe expected
    }

  }
}
