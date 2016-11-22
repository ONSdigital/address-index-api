package uk.gov.ons.addressIndex.demoui.controllers

import org.scalatestplus.play.PlaySpec
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
      val expectedString = "Find an address"
      // When
      val response = new ApplicationHome(messagesApi).indexPage().apply(FakeRequest())
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }
  }
}
