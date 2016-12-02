package uk.gov.ons.addressIndex.demoui.controllers


import org.scalatestplus.play.PlaySpec
import play.api.i18n.MessagesApi
import play.api.mvc.Results
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithApplication}

import scala.concurrent.ExecutionContext.Implicits.global
import org.mockito.Mockito._
import play.api.libs.json.Json
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import play.api.test.FakeRequest
import uk.gov.ons.addressIndex.demoui.model.{Address, SingleSearchForm}
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance

import scala.concurrent.{ExecutionContext, Future}

class SingleMatchTest extends PlaySpec with Results {

  val mockApiClient   = mock(classOf[AddressIndexClientInstance])
  val expectedAddress = Address(
    "123",
    "postcode",
    "primaryAddress",
    "secondyAddress",
    "street",
    "town",
    1.0f,
    "fullAddress"
    )

  "Single match controller" should {
    "return an html page" in new WithApplication {
      // Given
      val messagesApi = app.injector.instanceOf[MessagesApi]
      val configuration = app.injector.instanceOf[DemouiConfigModule]
      val apiClient = app.injector.instanceOf[AddressIndexClientInstance]
      val expectedString = "Search for Addresses"

      // When
      val response = new SingleMatch(configuration, messagesApi, apiClient).showSingleMatchPage().apply(FakeRequest())
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
      val expectedString = "btn btn-success btn-search"

      // When
      val response = new SingleMatch(configuration, messagesApi, apiClient).showSingleMatchPage().apply(FakeRequest())
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }

    "return a page including a no content error message" in new WithApplication {
      // Given
      val messagesApi = app.injector.instanceOf[MessagesApi]
      val configuration = app.injector.instanceOf[DemouiConfigModule]
      val apiClient = app.injector.instanceOf[AddressIndexClientInstance]
      val expectedString = "Please enter an address"

      // When
      val response = new SingleMatch(configuration, messagesApi, apiClient).doMatch().apply(FakeRequest(POST,"/addresses/search").withFormUrlEncodedBody("address" -> ""))
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }

    "return a page including some search results" in new WithApplication {
      // Given
      val messagesApi = app.injector.instanceOf[MessagesApi]
      val configuration = app.injector.instanceOf[DemouiConfigModule]
      val apiClient = app.injector.instanceOf[AddressIndexClientInstance]
      val expectedString = "GATE REACH"

      // When
      val response = new SingleMatch(configuration, messagesApi, apiClient).doMatchQS("7 EX2 6GA").apply(FakeRequest())
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }

  }

}
