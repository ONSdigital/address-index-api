package uk.gov.ons.addressIndex.demoui.controllers

import org.scalatestplus.play.PlaySpec
import play.api.i18n.MessagesApi
import play.api.mvc.Results
import play.api.test.Helpers._
import play.api.test.WithApplication
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import play.api.test.FakeRequest
import uk.gov.ons.addressIndex.demoui.client.{AddressIndexClientInstance, AddressIndexClientMock}
import uk.gov.ons.addressIndex.demoui.utils.ClassHierarchy


import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Tests for single match controller
  */
class SingleMatchTest extends PlaySpec with Results {

  "Single match controller" should {
    "return a page containing a heading" in new WithApplication {
      // Given
      val messagesApi = app.injector.instanceOf[MessagesApi]
      val configuration = app.injector.instanceOf[DemouiConfigModule]
      val apiClient = app.injector.instanceOf[AddressIndexClientMock]
      val expectedString = "Search for Addresses"
      val classHierarchy  = app.injector.instanceOf(classOf[ClassHierarchy])

      // When
      val response = new SingleMatchController(configuration, messagesApi, apiClient, classHierarchy)
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
      val apiClient = app.injector.instanceOf[AddressIndexClientMock]
      val expectedString = "<form action=\"/addresses/search\" method=\"POST\" >"
      val classHierarchy  = app.injector.instanceOf(classOf[ClassHierarchy])

      // When
      val response = new SingleMatchController(configuration, messagesApi, apiClient, classHierarchy)
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
      val apiClient = app.injector.instanceOf[AddressIndexClientMock]
      val expectedString = "<span class=\"error\" onclick=\"setFocus('address');\">Please enter an address</span>"
      val classHierarchy  = app.injector.instanceOf(classOf[ClassHierarchy])

      // When
      val response = new SingleMatchController(configuration, messagesApi, apiClient, classHierarchy)
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
      val classHierarchy  = app.injector.instanceOf(classOf[ClassHierarchy])

      // When
      val response = new SingleMatchController(
        configuration,
        messagesApi,
        apiClient.asInstanceOf[AddressIndexClientInstance],
        classHierarchy)
      .doMatchWithInput(inputAddress, Some(1)).apply(FakeRequest())
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }
  }
}
