package uk.gov.ons.addressIndex.server.controllers

import uk.gov.ons.addressIndex.server.model.response.PostcodeAddressFileReplyUnit
import uk.gov.ons.addressIndex.server.modules.ElasticsearchRepository
import com.sksamuel.elastic4s.ElasticClient
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Results
import play.api.test.FakeRequest
import uk.gov.ons.addressIndex.model.db.index.PostcodeAddressFileAddress
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.test._
import org.scalatestplus.play._
import play.api.test.Helpers._
import scala.concurrent.Future

class AddressControllerSpec extends PlaySpec with Results {

  // injected value, change implementations accordingly when needed
  val elasticRepositoryMock = new ElasticsearchRepository {

    override def queryUprn(uprn: String): Future[Seq[PostcodeAddressFileAddress]] =
      Future.successful(Seq(PostcodeAddressFileAddress(
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
        "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29"
      )))

    override def createAll(): Future[Seq[_]] = ???
    override def deleteAll(): Future[Seq[_]] = ???
    override def queryAddress(buildingNumber: Int, postcode: String): Future[Seq[PostcodeAddressFileAddress]] = ???
    override def client(): ElasticClient = ???
  }

  "Address Controller" should {

    "provide a reply on a found address (by uprn)" in {
      // Given
      val controller = new AddressController(elasticRepositoryMock)
      val expected = Json.toJson(Seq(PostcodeAddressFileReplyUnit(
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
        "16", "17", "18", "24", "26", "28"
      )))

      // When
      val result = controller.uprnQuery("4", "paf").apply(FakeRequest())
      val actual: JsValue = contentAsJson(result)

      // Then
      status(result) mustBe OK
      actual mustBe expected
    }

  }
}
