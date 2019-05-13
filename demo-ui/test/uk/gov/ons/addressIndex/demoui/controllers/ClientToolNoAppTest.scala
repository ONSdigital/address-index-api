package uk.gov.ons.addressIndex.demoui.controllers

import org.scalatestplus.play.PlaySpec
import org.specs2.execute.Results
import play.api.i18n.{DefaultLangs, DefaultMessagesApi, Langs}
import play.api.libs.ws.WSClient
import play.api.test.{FakeRequest, WsTestClient}
import play.api.mvc.Result
import play.api.test.Helpers.{POST, contentAsString, defaultAwaitTimeout, status}
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientMock
import uk.gov.ons.addressIndex.demoui.modules.{DemoUIVersionModuleMock, DemouiConfigModuleMock}
import uk.gov.ons.addressIndex.demoui.utils.{ClassHierarchy, RelativesExpander, StubFactory}

import scala.concurrent.{ExecutionContext, Future}

class ClientToolNoAppTest extends PlaySpec with Results {

  private trait Fixture {

    implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
    val conf: DemouiConfigModuleMock = new DemouiConfigModuleMock
    val wsClient =  WsTestClient.withClient[WSClient](identity)
    val addressIndexClientMock: AddressIndexClientMock = new AddressIndexClientMock(wsClient, conf)
    val version = new DemoUIVersionModuleMock(addressIndexClientMock, executionContext)

    val messagesApi = new DefaultMessagesApi(
      Map("en" -> Map("category.C" -> "Commercial",
        "category.CL" -> "Leisure - Applicable to recreational sites and enterprises",
        "category.CL06" -> "Indoor / Outdoor Leisure / Sporting Activity / Centre",
        "category.CL06RG" -> "Recreation Ground",
        "category.M" -> "Military",
        "category.MF" -> "Air Force",
        "category.MF99UG" -> "Air Force Military Storage",
        "category.R" -> "Residential",
        "category.RD" -> "Dwelling",
        "results.foundexactpre" -> "We have matched",
        "results.foundpost" -> "addresses",
        "single.pleasesupply" -> "Please enter an address",
        "single.sfatext" -> "Search for an address"))
    )
    val langs: Langs = new DefaultLangs()

    val controllerComponents = StubFactory.stubControllerComponents()

    val classHierarchy = new ClassHierarchy(messagesApi, langs)
    val relativesExpander = new RelativesExpander(addressIndexClientMock, conf)

    val singleController = new SingleMatchController(
      controllerComponents,
      conf,
      messagesApi,
      langs,
      addressIndexClientMock,
      classHierarchy,
      relativesExpander,
      version)
  }

  "Single Match Controller" should {
    "return the initial search page" in new Fixture {

      // Given
      val expectedString = "Search for an address"

      // When
      val response: Future[Result] = singleController.showSingleMatchPage()
        .apply(FakeRequest().withSession("api-key" -> ""))

      val content = contentAsString(response)

      // Then
      status(response) mustBe 200
      content must include(expectedString)
    }

    "return a page including an appropriate error message when empty address posted" in new Fixture {

      // Given
      val expectedString = "<div class=\"warning-error-suggestion mars\" role=\"alert\"><span onclick=\"setFocus('address');\">Please enter an address</span></div>"

      // When
      val response: Future[Result] = singleController.
        doMatch().apply(FakeRequest(POST, "/addresses/search").withFormUrlEncodedBody("address" -> "").withSession("api-key" -> ""))


      val content = contentAsString(response)

      // Then
      status(response) mustBe 200
      content must include(expectedString)
    }

    "return a page including some search results" in new Fixture {

      // Given
      val expectedString = "<div class=\"standout\">We have matched 1 addresses</div>"
      val inputAddress = "7 EX2 6GA"
      val filter=""

      // When
      val response: Future[Result] = singleController.
        doMatchWithInput(inputAddress, Some(filter), Some(1)).apply(FakeRequest().withSession("api-key" -> ""))

      val content = contentAsString(response)

      // Then
      status(response) mustBe 200
      content must include(expectedString)
    }

    "return a page including some search results with a filter" in new Fixture {

      // Given
      val expectedString = "[ RD ] [ Residential ] [ Dwelling ]"
      val inputAddress = "7 EX2 6GA"
      val filter = "RD"

      // When
      val response: Future[Result] = singleController.
        doMatchWithInput(inputAddress, Some(filter), Some(1)).apply(FakeRequest().withSession("api-key" -> ""))

      val content = contentAsString(response)

      // Then
      status(response) mustBe 200
      content must include(expectedString)
    }
  }
}


