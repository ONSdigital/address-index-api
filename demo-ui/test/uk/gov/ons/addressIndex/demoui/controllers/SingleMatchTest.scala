package uk.gov.ons.addressIndex.demoui.controllers


import org.scalatestplus.play.PlaySpec
import play.api.i18n.MessagesApi
import play.api.mvc.Results
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithApplication}
import scala.concurrent.ExecutionContext.Implicits.global

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.{FunSuite, Matchers}
import play.api.test.FakeRequest
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.demoui.model.{Address, SingleMatchResponse}

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
      val expectedString = "<title>ONS-Address - Single Search</title>"

      // When
      val response = new SingleMatch(messagesApi).showSingleMatchPage().apply(FakeRequest())
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }
  }


  "Single match controller" should {
    "return a page including a single match form" in new WithApplication {
      // Given
      val messagesApi = app.injector.instanceOf[MessagesApi]
      val expectedString = "<button class=\"btn btn-success btn-search\" type=\"submit\" id=\"submit\">"

      // When
      val response = new SingleMatch(messagesApi).showSingleMatchPage().apply(FakeRequest())
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }
  }

}
