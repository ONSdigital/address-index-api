package uk.gov.ons.addressIndex.demoui.controllers

import org.scalatestplus.play.PlaySpec
import org.specs2.execute.Results
import play.api.i18n.{DefaultLangs, DefaultMessagesApi, Langs}
import play.api.libs.ws.WSClient
import play.api.test.{FakeRequest, NoMaterializer, WsTestClient}
import play.api.mvc.{ControllerComponents, Result}
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, status}
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientMock
import uk.gov.ons.addressIndex.demoui.modules.{DemoUIVersionModuleMock, DemouiConfigModuleMock}
import uk.gov.ons.addressIndex.demoui.utils.{ClassHierarchy, RelativesExpander, StubFactory}

import scala.concurrent.{ExecutionContext, Future}

class BulkMatchControllerNoAppTest extends PlaySpec with Results  {

  private trait Fixture {

    implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
    val conf: DemouiConfigModuleMock = new DemouiConfigModuleMock
    val wsClient: WSClient =  WsTestClient.withClient[WSClient](identity)
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
    val controllerComponents: ControllerComponents = StubFactory.stubControllerComponents()
    val classHierarchy: ClassHierarchy = new ClassHierarchy(messagesApi, langs)
    val relativesExpander: RelativesExpander = new RelativesExpander(addressIndexClientMock, conf)

    val bulkController = new BulkMatchController(
      controllerComponents,
      messagesApi,
      addressIndexClientMock,
      version)(executionContext, NoMaterializer)
  }

  "Bulk Match Controller" should {
    "include a form to submit data" in new Fixture {

      // Given
      val expectedString = "<input type=\"file\""

      // When
      val response: Future[Result] = bulkController
        .bulkMatchPage().apply(FakeRequest().withSession("api-key" -> ""))

      val content: String = contentAsString(response)

      // Then
      status(response) mustBe 200
      content must include(expectedString)
    }

  }
}


