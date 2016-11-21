package uk.gov.ons.addressIndex.demoui.controllers

import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.{FunSuite, Matchers}
import play.api.i18n._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance
import uk.gov.ons.addressIndex.demoui.model.{Address, SingleMatchResponse}

import scala.concurrent.{ExecutionContext, Future}

class SingleMatchTest extends FunSuite with Matchers {

  val mockApiClient   = mock(classOf[AddressIndexClientInstance])
  val mockMessageApi  = mock(classOf[MessagesApi])
  val mockExecutionContext = mock(classOf[ExecutionContext])
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

  test("showSingleMatchPage returns the single match html page") {
    val response = new SingleMatch()(mockExecutionContext, mockMessageApi).showSingleMatchPage()(FakeRequest())
    val content  = contentAsString(response)
    status(response) should be(200)
    content should include("<title>ONS-Address - Single Search</title>")
  }

  test("showSingleMatchPage returns a html page with single match form") {
    val response = new SingleMatch()(mockExecutionContext, mockMessageApi).showSingleMatchPage()(FakeRequest())
    val content  = contentAsString(response)

    status(response) should be(200)
    content should include("<button class=\"btn btn-success btn-search\" type=\"submit\" id=\"submit\">")
  }

  ignore("doSingleMatch() return alert message when no address is provided") {
    val controller   = new SingleMatch() (mockExecutionContext, mockMessageApi)
    val inputAddress = List("address" -> "", "street" -> "", "town" -> "", "postcode" -> "")
    val response     = controller.doSingleMatch()(FakeRequest().withFormUrlEncodedBody(inputAddress: _*))
    val content      = contentAsString(response)

    status(response) should be (200)
    content should include("<strong>Please provide an address for matching!</strong>")
  }

  ignore("doSingleMatch() return 200 response and verify singleMatch call occours once") {
    val list         = List(expectedAddress)
    val controller   = new SingleMatch() (mockExecutionContext, mockMessageApi)
    val inputAddress = List("address" -> "123", "street" -> "my street", "town" -> "my town", "postcode" -> "123 1234")
    //when(mockApiClient.singleMatch(anyString())).thenReturn(Future.successful(SingleMatchResponse.apply(1, list)))
    val response     = controller.doSingleMatch()(FakeRequest().withFormUrlEncodedBody(inputAddress: _*))

    status(response) should be (200)
    //verify(mockApiClient, times(1)).singleMatch(anyString())
   // verifyNoMoreInteractions(mockApiClient)
  }
}
