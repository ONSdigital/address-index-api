package uk.gov.ons.addressIndex.demoui.controllers

import org.scalatestplus.play.PlaySpec
import play.api.i18n.MessagesApi
import play.api.mvc.Results
import play.api.test.Helpers._
import play.api.test.WithApplication
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import play.api.test.FakeRequest
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.demoui.utils.ClassHierarchy
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Tests for single match controller
  */
class SingleMatchTest extends PlaySpec with Results {

  "Single match controller" should {

    new WithApplication {
      val controller = new SingleMatchController(
        conf = app.injector.instanceOf[DemouiConfigModule],
        messagesApi = app.injector.instanceOf[MessagesApi],
        apiClient = app.injector.instanceOf[AddressIndexClientInstance],
        classHierarchy = app.injector.instanceOf[ClassHierarchy]
      )

      "return a page containing a heading and return a page including a single match form" in {
        // Given
        val expectedString = "<form action=\"/addresses/search\" method=\"POST\" >"
        val expectedSearchString = "Search for Addresses"

        // When
        val response = controller.showSingleMatchPage()(FakeRequest())
        val content = contentAsString(response)

        // Then
        status(response) mustBe OK
        content must include(expectedString)
        content must include(expectedSearchString)
      }

      "return a page including an appropriate error message when empty address posted" in {
        // Given
        val expectedString = "<span class=\"error\" onclick=\"setFocus('address');\">Please enter an address</span>"

        // When
        controller.doMatch()(
          FakeRequest(
            method = POST,
            path = "/addresses/search"
          ).withFormUrlEncodedBody(
            "address" -> ""
          )
        ) map { response =>
          response.header.status mustBe Ok
          response.body.toString must include(expectedString)
        }
      }

      "return a page including some search results" in {
        // Given
        val expectedString = "1 addresses found"
        val inputAddress = "7 EX2 6GA"

        // When
        controller.doMatchWithInput(inputAddress)(FakeRequest()) map { response =>
          //Then
          response.header.status mustBe OK
          response.body.toString must include(expectedString)

        }
      }
    }
    ()
  }
}
