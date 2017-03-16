package uk.gov.ons.addressIndex.demoui.controllers

import org.scalatestplus.play.guice.GuiceOneAppPerTest
import org.scalatestplus.play.PlaySpec
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import play.api.i18n.MessagesApi
import play.api.mvc.Results
import play.api.test.Helpers._
import play.api.test.FakeRequest
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Tests for home page
  */
class ApplicationHomeTest extends PlaySpec with Results with GuiceOneAppPerTest {
  "Application controller" should {
    
    "include at least one link" in {
      // Given
      val messagesApi = app.injector.instanceOf[MessagesApi]
      val configuration = app.injector.instanceOf[DemouiConfigModule]
      val expectedString = "<a href=\"http:///addresses\">Find an address</a>"

      // When
      val response = new ApplicationHomeController(configuration, messagesApi).indexPage().apply(FakeRequest())
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }
  }
}
