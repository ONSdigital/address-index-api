package uk.gov.ons.addressIndex.demoui.controllers

import org.scalatestplus.play.PlaySpec
import org.specs2.execute.Results
import play.api.Environment
import play.api.i18n.{DefaultLangs, DefaultMessagesApi, Langs}
import play.api.libs.ws.WSClient
import play.api.test.{FakeRequest, WsTestClient}
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsString, status, defaultAwaitTimeout}
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientMock
import uk.gov.ons.addressIndex.demoui.modules.{DemoUIAddressIndexVersionModule, DemoUIVersionModuleMock, DemouiConfigModule, DemouiConfigModuleMock}
import uk.gov.ons.addressIndex.demoui.utils.{ClassHierarchy, RelativesExpander,StubFactory}
import scala.concurrent.{ExecutionContext, Future}

class SingleMatchNoAppTest extends PlaySpec with Results {

  private trait Fixture {

    implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
    val conf: DemouiConfigModule = new DemouiConfigModuleMock
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
    "include at least one link" in new Fixture {

      // Given
      val expectedString = "Search for an address"

      // When
      val response: Future[Result] = singleController.showSingleMatchPage()
      .apply(FakeRequest().withSession("api-key" -> ""))

      val content = contentAsString(response)

      // Then
      status(response) mustBe 200
     // println(content)
      content must include(expectedString)
    }
  }
}

