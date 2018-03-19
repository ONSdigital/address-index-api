package uk.gov.ons.addressIndex.demoui.controllers

import org.scalatestplus.play.PlaySpec
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc.{ControllerComponents, Results}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithApplication}
import uk.gov.ons.addressIndex.demoui.client.{AddressIndexClientInstance, AddressIndexClientMock}
import uk.gov.ons.addressIndex.demoui.modules.{DemoUIAddressIndexVersionModule, DemouiConfigModule}
import uk.gov.ons.addressIndex.demoui.utils.ClassHierarchy

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Tests for postcode match controller
  */
class PostcodeMatchTest extends PlaySpec with Results {

  "Postcode match controller" should {
    "return a page containing a heading" in new WithApplication {
      // Given
      val messagesApi = app.injector.instanceOf[MessagesApi]
      val langs = app.injector.instanceOf[Langs]
      val configuration = app.injector.instanceOf[DemouiConfigModule]
      val apiClient = app.injector.instanceOf[AddressIndexClientMock]
      val version = app.injector.instanceOf[DemoUIAddressIndexVersionModule]
      val controllerComponents = app.injector.instanceOf[ControllerComponents]
      val expectedString = "Search for an address by postcode"
      val classHierarchy  = app.injector.instanceOf(classOf[ClassHierarchy])

      // When
      val response = new PostcodeController(
        controllerComponents,
        configuration,
        messagesApi,
        langs,
        apiClient,
        classHierarchy,
        version)
        .showPostcodeMatchPage().apply(FakeRequest().withSession("api-key" -> ""))
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }

    "return a page including a postcode match form" in new WithApplication {
      // Given
      val messagesApi = app.injector.instanceOf[MessagesApi]
      val langs = app.injector.instanceOf[Langs]
      val configuration = app.injector.instanceOf[DemouiConfigModule]
      val apiClient = app.injector.instanceOf[AddressIndexClientMock]
      val version = app.injector.instanceOf[DemoUIAddressIndexVersionModule]
      val controllerComponents = app.injector.instanceOf[ControllerComponents]
      val expectedString = "<form action=\"/postcode/search\" method=\"POST\" >"
      val classHierarchy  = app.injector.instanceOf(classOf[ClassHierarchy])

      // When
      val response = new PostcodeController(
        controllerComponents,
        configuration,
        messagesApi,
        langs,
        apiClient,
        classHierarchy,
        version)
        .showPostcodeMatchPage().apply(FakeRequest().withSession("api-key" -> ""))
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }

    "return a page including an appropriate error message when empty address posted" in new WithApplication {
      // Given
      val messagesApi = app.injector.instanceOf[MessagesApi]
      val langs = app.injector.instanceOf[Langs]
      val configuration = app.injector.instanceOf[DemouiConfigModule]
      val apiClient = app.injector.instanceOf[AddressIndexClientMock]
      val version = app.injector.instanceOf[DemoUIAddressIndexVersionModule]
      val controllerComponents = app.injector.instanceOf[ControllerComponents]
      val expectedString = "<span class=\"error\" onclick=\"setFocus('address');\">Please enter a postcode</span>"
      val classHierarchy  = app.injector.instanceOf(classOf[ClassHierarchy])

      // When
      val response = new PostcodeController(
        controllerComponents,
        configuration,
        messagesApi,
        langs,
        apiClient,
        classHierarchy,
        version)
        .doMatch().apply(FakeRequest(POST,"/postcode/search").withFormUrlEncodedBody("address" -> "").withSession("api-key" -> ""))
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }

    "return a page including some search results" in new WithApplication {
      // Given
      val messagesApi = app.injector.instanceOf[MessagesApi]
      val langs = app.injector.instanceOf[Langs]
      val configuration = app.injector.instanceOf[DemouiConfigModule]
      val apiClient = app.injector.instanceOf[AddressIndexClientMock]
      val version = app.injector.instanceOf[DemoUIAddressIndexVersionModule]
      val controllerComponents = app.injector.instanceOf[ControllerComponents]
      val expectedString = "<div class=\"standout\">We have matched 26 addresses</div>"
      val inputAddress = "EX2 6GA"
      val filter = ""
      val classHierarchy  = app.injector.instanceOf(classOf[ClassHierarchy])

      // When
      val response = new PostcodeController(
        controllerComponents,
        configuration,
        messagesApi,
        langs,
        apiClient.asInstanceOf[AddressIndexClientInstance],
        classHierarchy,
        version)
      .doMatchWithInput(inputAddress, filter, Some(1)).apply(FakeRequest().withSession("api-key" -> ""))
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }

    "return a page including some search results with a filter" in new WithApplication {
      // Given
      val messagesApi = app.injector.instanceOf[MessagesApi]
      val langs = app.injector.instanceOf[Langs]
      val configuration = app.injector.instanceOf[DemouiConfigModule]
      val apiClient = app.injector.instanceOf[AddressIndexClientMock]
      val version = app.injector.instanceOf[DemoUIAddressIndexVersionModule]
      val controllerComponents = app.injector.instanceOf[ControllerComponents]
      val expectedString = "Detached"
      val inputAddress = "EX2 6GA"
      val filter = "RD02"
      val classHierarchy  = app.injector.instanceOf(classOf[ClassHierarchy])

      // When
      val response = new PostcodeController(
        controllerComponents,
        configuration,
        messagesApi,
        langs,
        apiClient.asInstanceOf[AddressIndexClientInstance],
        classHierarchy,
        version)
        .doMatchWithInput(inputAddress, filter, Some(1)).apply(FakeRequest().withSession("api-key" -> ""))
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }

  }
}
