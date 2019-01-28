package uk.gov.ons.addressIndex.demoui.controllers

import org.scalatestplus.play.guice.GuiceOneAppPerTest
import org.scalatestplus.play.PlaySpec
import play.api.http.Port
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import uk.gov.ons.addressIndex.demoui.modules.DemoUIAddressIndexVersionModule
import play.api.i18n.MessagesApi
import play.api.libs.ws.WSClient
import play.api.mvc.{ControllerComponents, Results}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WsTestClient}
import play.api.Environment

/**
  * Tests for home page
  */
class ApplicationHomeTest extends PlaySpec with Results with GuiceOneAppPerTest {
  "Application controller" should {
    
    "include at least one link" in {
      // Given
      val messagesApi = app.injector.instanceOf[MessagesApi]
      val configuration = app.injector.instanceOf[DemouiConfigModule]
      val version = app.injector.instanceOf[DemoUIAddressIndexVersionModule]
      val controllerComponents = app.injector.instanceOf[ControllerComponents]
      val expectedString = "Find an address</a>"
      val environment = app.injector.instanceOf[Environment]
      // When
      val response = new ApplicationHomeController(controllerComponents,configuration, version, messagesApi, environment, WsTestClient.withClient[WSClient](identity)(new Port(9000))).home().apply(FakeRequest().withSession("api-key" -> ""))
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }
  }
}
