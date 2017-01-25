package uk.gov.ons.addressIndex.demoui.controllers

import org.scalatestplus.play.PlaySpec
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import play.api.i18n.MessagesApi
import play.api.mvc.{Result, Results}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithApplication}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Tests for home page
  */
class ApplicationHomeTest extends PlaySpec with Results {
  "Application controller" should {
    new WithApplication {
      val controller = new ApplicationHomeController(
        conf = app.injector.instanceOf[DemouiConfigModule],
        messagesApi = app.injector.instanceOf[MessagesApi]
      )

      "include at least one link" in {

        val expectedString = "<h4><a href=\"/addresses\">Find an address</a></h4>"

        // When
        val response = controller.indexPage().apply(FakeRequest())
        val content = contentAsString(response)

        // Then
        status(response) mustBe OK
        content must include(expectedString)
      }
      
      "return at least one link with language set" in  {
        // can we set the accept language Seq in the fake request so that cy is top?
        // Given
        val expectedString = "<h4><a href=\"/addresses\">Find an address</a></h4>"

        // When
        controller.indexPage().apply(FakeRequest()).map { response: Result =>
          // Then
          response.header.status mustBe OK
          response.body.toString must include(expectedString)
        }
      }
    }
    ()
  }
}
