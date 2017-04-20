package uk.gov.ons.addressIndex.demoui.controllers

import org.scalatestplus.play.PlaySpec
import play.api.i18n.MessagesApi
import play.api.mvc.Results
import play.api.test.Helpers._
import play.api.test.WithApplication
import uk.gov.ons.addressIndex.demoui.modules.{DemoUIAddressIndexVersionModule, DemouiConfigModule}
import play.api.test.FakeRequest
import uk.gov.ons.addressIndex.demoui.client.{AddressIndexClientInstance, AddressIndexClientMock}
import uk.gov.ons.addressIndex.demoui.utils.ClassHierarchy
import uk.gov.ons.addressIndex.demoui.modules.DemoUIVersionModuleMock

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
      val version = app.injector.instanceOf[DemoUIAddressIndexVersionModule]
      val expectedString = "Search for an address"
      val classHierarchy  = app.injector.instanceOf(classOf[ClassHierarchy])

      // When
      val response = new SingleMatchController(configuration, messagesApi, apiClient, classHierarchy, version)
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
      val version = app.injector.instanceOf[DemoUIAddressIndexVersionModule]
      val expectedString = "<form action=\"/addresses/search\" method=\"POST\" >"
      val classHierarchy  = app.injector.instanceOf(classOf[ClassHierarchy])

      // When
      val response = new SingleMatchController(configuration, messagesApi, apiClient, classHierarchy, version)
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
      val version = app.injector.instanceOf[DemoUIAddressIndexVersionModule]
      val expectedString = "<span class=\"error\" onclick=\"setFocus('address');\">Please enter an address</span>"
      val classHierarchy  = app.injector.instanceOf(classOf[ClassHierarchy])

      // When
      val response = new SingleMatchController(configuration, messagesApi, apiClient, classHierarchy, version)
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
      val version = app.injector.instanceOf[DemoUIAddressIndexVersionModule]
      val expectedString = "<h3 class=\"green\">1 addresses found</h3>"
      val inputAddress = "7 EX2 6GA"
      val classHierarchy  = app.injector.instanceOf(classOf[ClassHierarchy])

      // When
      val response = new SingleMatchController(
        configuration,
        messagesApi,
        apiClient.asInstanceOf[AddressIndexClientInstance],
        classHierarchy,
        version)
      .doMatchWithInput(inputAddress, Some(1)).apply(FakeRequest())
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }
  }
}
