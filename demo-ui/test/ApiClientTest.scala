import org.scalatest.{FunSuite, Matchers}
import play.api.libs.ws._
import uk.gov.ons.addressIndex.client.AddressApiClient
import org.mockito.Mockito._
import org.mockito.Matchers._
import play.api.libs.json.{JsString, _}
import uk.gov.ons.addressIndex.conf.OnsFrontendConfiguration
import uk.gov.ons.addressIndex.model.{Address, BulkMatchResponse, SingleMatchResponse}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import play.api.test.FutureAwaits

import scala.concurrent.duration.Duration

class ApiClientTest extends FunSuite with Matchers {

  private val url      = "any"
  private val bulkFile = "/public/testing.resources/test-file.csv"
  private val defaultMockWSRequest = getMockWSRequest()

  ignore("singleMatch() is invoked with the right parameter") {
    val mockWSClient  = getMockWSClient(defaultMockWSRequest)
    val apiClient     = new AddressApiClient(mockWSClient, getMockConfiguration(url))
    apiClient.singleMatch("address")
    verify(mockWSClient, times(1)).url(s"$url/search")
    verify(defaultMockWSRequest, times(1)).withQueryString("address" -> "address")
    verify(defaultMockWSRequest, times(1)).get()
    verifyNoMoreInteractions(mockWSClient, defaultMockWSRequest)
  }

  ignore("singleMatch() returns the valid information from response") {
    val expectedAddress = Address(
      "123",
      "postcode",
      "primaryAddress",
      "secondaryAddress",
      "street",
      "town",
      1.0f,
      "fullAddress"
    )
    val expectedResponse = SingleMatchResponse(1, List(expectedAddress))
    val responseJson     = JsObject(Seq("numberOfHits" -> JsNumber(expectedResponse.totalHits), "addresses" -> JsArray(
        Seq(
          JsObject(
            Seq(
              "uprn"              -> JsString(expectedAddress.uprn),
              "postcodeLocator"   -> JsString(expectedAddress.postcodeLocator),
              "primaryAddress"    -> JsString(expectedAddress.primaryAddress),
              "secondaryAddress"  -> JsString(expectedAddress.secondaryAddress),
              "streetDescription" -> JsString(expectedAddress.streetDescription),
              "town"              -> JsString(expectedAddress.town),
              "matchScore"        -> JsNumber(expectedAddress.matchScore.toDouble),
              "fullAddress"       -> JsString(expectedAddress.fullAddress)
            )
          )
      ))
    ))
    val mockWSRequest  = getMockWSRequest(response = getMockWSResponse(200, responseJson))
    val mockWSClient   = getMockWSClient(mockWSRequest)
    val apiClient      = new AddressApiClient(mockWSClient, getMockConfiguration())
    val actualResponse = Await.result(apiClient.singleMatch("address"), Duration.Inf)
    actualResponse should be (expectedResponse)
  }

  ignore("singleMatch() throws exception when response status is not 200") {
    val apiResponse    = getMockWSResponse(status = 404)
    when(defaultMockWSRequest.get()).thenReturn(Future.successful(apiResponse)).toString
    val mockWSClient   = getMockWSClient(defaultMockWSRequest)
    val apiClient      = new AddressApiClient(mockWSClient, getMockConfiguration())
    val exception      = intercept[Exception] {
      Await.result(apiClient.singleMatch(("address")), Duration.Inf)
    }
    exception.getMessage should be ("Unexpected response from the API" + apiResponse.toString)
  }

  ignore("multipleMatch() is invoked with valid parameter") {
    val mockWSClient  = getMockWSClient(defaultMockWSRequest)
    val apiClient     = new AddressApiClient(mockWSClient, getMockConfiguration(url))
    apiClient.multipleMatch(bulkFile)
    verify(mockWSClient,  times(1)).url(s"$url/bulk")
    verify(defaultMockWSRequest, times(1)).withQueryString(("fileName" -> bulkFile), ("fileLocation" -> "any"))
  }

  ignore("multipleMatch() returns the valid information from response") {
    val expectedResponse = BulkMatchResponse(Some(1), Some(1), Some(1), Some(1))
    val responseJson     = JsObject(
      Seq(
        "matchFound"             -> JsNumber(expectedResponse.matchFound.get),
        "possibleMatches"        -> JsNumber(expectedResponse.possibleMatches.get),
        "noMatch"                -> JsNumber(expectedResponse.noMatch.get),
        "totalNumberOfAddresses" -> JsNumber(expectedResponse.totalNumberOfAddresses.get)
      )
    )
    val mockWSRequest  = getMockWSRequest(response = getMockWSResponse(200, responseJson))
    val mockWSClient   = getMockWSClient(mockWSRequest)
    val apiClient      = new AddressApiClient(mockWSClient, getMockConfiguration())
    val actualResponse = Await.result(apiClient.multipleMatch(bulkFile), Duration.Inf)
    actualResponse should be(expectedResponse)
  }

  ignore("multipleMatch() throws exception when response status is not 200") {
    val apiResponse    = getMockWSResponse(status = 404)
    when(defaultMockWSRequest.get()).thenReturn(Future.successful(apiResponse)).toString
    val mockWSClient   = getMockWSClient(defaultMockWSRequest)
    val apiClient      = new AddressApiClient(mockWSClient, getMockConfiguration())
    val exception = intercept[Exception] {
      Await.result(apiClient.multipleMatch((bulkFile)), Duration.Inf)
    }
    exception.getMessage should be ("Unexpected response from the API" + apiResponse.toString)
  }

  private def getMockWSRequest(response: WSResponse = getMockWSResponse()) = {
    val mockWSRequest = mock(classOf[WSRequest])
    when(mockWSRequest.withQueryString(anyObject())).thenReturn(mockWSRequest)
    when(mockWSRequest.withRequestTimeout(anyObject())).thenReturn(mockWSRequest)
    when(mockWSRequest.get()).thenReturn(Future.successful(response))
    mockWSRequest
  }

  private def getMockWSResponse(status: Int = 200, json: JsValue = null) = {
    val mockResponse = mock(classOf[WSResponse])
    when(mockResponse.status).thenReturn(status)
    when(mockResponse.json).thenReturn(json)
    mockResponse
  }

  private def getMockWSClient(mockWSRequest: WSRequest = getMockWSRequest()) = {
    val mockWSClient = mock(classOf[WSClient])
    when(mockWSClient.url(anyString())).thenReturn(mockWSRequest)
    mockWSClient
  }

  private def getMockConfiguration(apiUrl: String = "url", timeout: Int = 123, uploadFileLocation: String = "any") = {
    val mockConfiguration = mock(classOf[OnsFrontendConfiguration])
    when(mockConfiguration.onsAddressApiUri).thenReturn(apiUrl)
    when(mockConfiguration.onsApiCallTimeout).thenReturn(timeout)
    when(mockConfiguration.onsUploadFileLocation).thenReturn(uploadFileLocation)
    mockConfiguration
  }
}
