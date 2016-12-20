package uk.gov.ons.addressIndex.demoui.controllers

import org.scalatestplus.play.PlaySpec
import play.api.i18n.MessagesApi
import play.api.mvc.Results
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithApplication}
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import play.api.test.FakeRequest
import uk.gov.ons.addressIndex.demoui.client.{AddressIndexClientInstance, AddressIndexClientMock}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Tests for single match controleer
  */
class SingleMatchTest extends PlaySpec with Results {

  "Single match controller" should {
    "return a page containing a heading" in new WithApplication {
      // Given
      val messagesApi = app.injector.instanceOf[MessagesApi]
      val configuration = app.injector.instanceOf[DemouiConfigModule]
      val apiClient = app.injector.instanceOf[AddressIndexClientInstance]
      val expectedString = "Search for Addresses"

      // When
      val response = new SingleMatchController(configuration, messagesApi, apiClient)
        .showSingleMatchPage().apply(FakeRequest())
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }

    "return a page including a single match form" in new WithApplication {
      // Given
      val messagesApi = app.injector.instanceOf[MessagesApi]
      val configuration = app.injector.instanceOf[DemouiConfigModule]
      val apiClient = app.injector.instanceOf[AddressIndexClientInstance]
      val expectedString = "<form action=\"/addresses/search\" method=\"POST\" >"

      // When
      val response = new SingleMatchController(configuration, messagesApi, apiClient)
        .showSingleMatchPage().apply(FakeRequest())
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }

    "return a page including an appropriate error message when empty address posted" in new WithApplication {
      // Given
      val messagesApi = app.injector.instanceOf[MessagesApi]
      val configuration = app.injector.instanceOf[DemouiConfigModule]
      val apiClient = app.injector.instanceOf[AddressIndexClientInstance]
      val expectedString = "<span class=\"error\" onclick=\"setFocus('address');\">Please enter an address</span>"

      // When
      val response = new SingleMatchController(configuration, messagesApi, apiClient)
        .doMatch().apply(FakeRequest(POST,"/addresses/search").withFormUrlEncodedBody("address" -> ""))
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }

    "return a page including some search results" in new WithApplication {
      // Given
      val messagesApi = app.injector.instanceOf[MessagesApi]
      val configuration = app.injector.instanceOf[DemouiConfigModule]
      val apiClient = app.injector.instanceOf[AddressIndexClientMock]
      val expectedString = "<h3 class=\"green\">1 addresses found</h3>"
      val inputAddress = "7 EX2 6GA"

      // When
      val response = new SingleMatchController(
        configuration,
        messagesApi,
        apiClient.asInstanceOf[AddressIndexClientInstance])
      .doMatchWithInput(inputAddress,"paf").apply(FakeRequest())
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }

  }

}
