package uk.gov.ons.addressIndex.demoui.controllers

import org.scalatestplus.play.PlaySpec
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import play.api.i18n.MessagesApi
import play.api.mvc.Results
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithApplication}

import scala.concurrent.ExecutionContext.Implicits.global

class ApplicationHomeTest extends PlaySpec with Results {
  "Application controller" should {
    "include at least one link" in new WithApplication {
      // Given
      val messagesApi = app.injector.instanceOf[MessagesApi]
      val configuration = app.injector.instanceOf[DemouiConfigModule]
      val expectedString = "<h4><a href=\"/addresses\">Find an address</a></h4>"

      // When
      val response = new ApplicationHomeController(configuration, messagesApi).indexPage().apply(FakeRequest())
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }
    "return at least one link with language set" in new WithApplication {
      // can we set the acceptlanguage Seq in the fake request so that cy is top?
      // Given
      val messagesApi = app.injector.instanceOf[MessagesApi]
      val configuration = app.injector.instanceOf[DemouiConfigModule]
      val expectedString = "<h4><a href=\"/addresses\">Find an address</a></h4>"
      val langOption = Some("cy")

      // When
      val response = new ApplicationHomeController(configuration, messagesApi).indexPage().apply(FakeRequest())
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }
  }
}
