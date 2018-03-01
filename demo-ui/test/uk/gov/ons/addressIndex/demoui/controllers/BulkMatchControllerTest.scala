package uk.gov.ons.addressIndex.demoui.controllers

import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.i18n.MessagesApi
import play.api.mvc.{ControllerComponents, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientMock
import uk.gov.ons.addressIndex.demoui.modules.DemoUIAddressIndexVersionModule

import scala.concurrent.ExecutionContext.Implicits.global

class BulkMatchControllerTest extends PlaySpec with Results with GuiceOneAppPerTest {
  "Bulk Match controller" should {
    "include a form to submit data" in {
      // Given
      implicit val mtzr = app.injector.instanceOf[akka.stream.Materializer]
      val messagesApi = app.injector.instanceOf[MessagesApi]
      val api = app.injector.instanceOf[AddressIndexClientMock]
      val version = app.injector.instanceOf[DemoUIAddressIndexVersionModule]
      val controllerComponents = app.injector.instanceOf[ControllerComponents]
      val expectedString = "<input type=\"file\""

      // When
      val response = new BulkMatchController(controllerComponents, messagesApi, api, version).bulkMatchPage().apply(FakeRequest().withSession("api-key" -> ""))
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }
  }
}
