package uk.gov.ons.addressIndex.server.controllers

import uk.gov.ons.addressIndex.server.model.response._
import uk.gov.ons.addressIndex.server.modules.ElasticsearchRepository
import com.sksamuel.elastic4s.ElasticClient
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results
import play.api.test.FakeRequest
import uk.gov.ons.addressIndex.model.db.index.PostcodeAddressFileAddress

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.test._
import uk.gov.ons.addressIndex.server.model.response.implicits._
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
    postcode = "B16 18TH",
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
  val elasticRepositoryMock = new ElasticsearchRepository {

    override def queryUprn(uprn: String): Future[Option[PostcodeAddressFileAddress]] =
      Future.successful(Some(validPafAddress))

    override def createAll(): Future[Seq[_]] = ???

    override def deleteAll(): Future[Seq[_]] = ???

    override def queryAddress(tokens: AddressTokens): Future[Seq[PostcodeAddressFileAddress]] =
      Future.successful(Seq(validPafAddress))


    override def client(): ElasticClient = ???
  }

  "Address controller" should {

    "provide a reply on a found address (by address query)" in {
      // Given
      val controller = new AddressController(elasticRepositoryMock)

      val expected = Json.toJson(AddressBySearchResponseContainer(
        AddressResponse(
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
        AddressResponseStatus(
          code = 200,
          message = "Ok"
        ),
        errors = Seq()
      ))

      // When
      val result = controller.addressQuery("10 B16 8TH", "paf").apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

    "provide a reply on a found address (by uprn)" in {
      // Given
      val controller = new AddressController(elasticRepositoryMock)

      val expected = Json.toJson(AddressByUprnResponseContainer(
        address = AddressResponseAddress.fromPafAddress(validPafAddress),
        AddressResponseStatus(
          code = 200,
          message = "Ok"
        ),
        errors = Seq()
      ))

      // When
      val result = controller.uprnQuery("4", "paf").apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

  }
}
