import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.scalatest.{FunSuite, Matchers}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.mockito.Mockito._
import org.mockito.Matchers._
import play.api.i18n.MessagesApi
import uk.gov.ons.addressIndex.client.AddressApiClient
import uk.gov.ons.addressIndex.controllers.SingleMatch

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, ExecutionContext, Future}
import play.api.libs.streams.Accumulator._
import uk.gov.ons.addressIndex.conf.OnsFrontendConfiguration
import uk.gov.ons.addressIndex.model.{Address, SingleMatchResponse}

class SingleMatchTest extends FunSuite with Matchers {

  val mockApiClient   = mock(classOf[AddressApiClient])
  val mockMessageApi  = mock(classOf[MessagesApi])
  val mockOnsConfiguration = mock(classOf[OnsFrontendConfiguration])
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

  ignore("showSingleMatchPage returns the single match html page") {
    val response = new SingleMatch(mockApiClient, mockOnsConfiguration, mockMessageApi).showSingleMatchPage()(FakeRequest())
    val content  = contentAsString(response)
    status(response) should be(200)
    content should include("<title>ONS-Address - Single Search</title>")
  }

  ignore("showSingleMatchPage returns a html page with single match form") {
    val response = new SingleMatch(mockApiClient, mockOnsConfiguration, mockMessageApi).showSingleMatchPage()(FakeRequest())
    val content  = contentAsString(response)

    status(response) should be(200)
    content should include("<button class=\"btn btn-success btn-search\" type=\"submit\" id=\"submit\">")
  }

  ignore("doSingleMatch() return alert message when no address is provided") {
    val controller   = new SingleMatch(mockApiClient, mockOnsConfiguration, mockMessageApi)
    val inputAddress = List("address" -> "", "street" -> "", "town" -> "", "postcode" -> "")
    val response     = controller.doSingleMatch()(FakeRequest().withFormUrlEncodedBody(inputAddress: _*))
    val content      = contentAsString(response)

    status(response) should be (200)
    content should include("<strong>Please provide an address for matching!</strong>")
  }

  ignore("doSingleMatch() return 200 response and verify singleMatch call occours once") {
    val list         = List(expectedAddress)
    val controller   = new SingleMatch(mockApiClient, mockOnsConfiguration, mockMessageApi)
    val inputAddress = List("address" -> "123", "street" -> "my street", "town" -> "my town", "postcode" -> "123 1234")
    when(mockApiClient.singleMatch(anyString())).thenReturn(Future.successful(SingleMatchResponse.apply(1, list)))
    val response     = controller.doSingleMatch()(FakeRequest().withFormUrlEncodedBody(inputAddress: _*))

    status(response) should be (200)
    verify(mockApiClient, times(1)).singleMatch(anyString())
    verifyNoMoreInteractions(mockApiClient)
  }
}
