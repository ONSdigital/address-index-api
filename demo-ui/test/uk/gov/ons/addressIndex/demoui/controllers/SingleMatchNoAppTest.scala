package uk.gov.ons.addressIndex.demoui.controllers

import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers, OneInstancePerTest}
import org.scalatestplus.play.PlaySpec
import org.specs2.execute.Results
import play.api.Environment
import play.api.i18n.{DefaultLangs, DefaultMessagesApi, Langs}
import play.api.libs.ws.WSClient
import play.api.mvc.{DefaultControllerComponents, Result}
import play.api.test.{FakeRequest, WsTestClient}
import play.api.test.Helpers.{contentAsString, status}
import play.api.test.Helpers.defaultAwaitTimeout
import uk.gov.ons.addressIndex.client.AddressIndexClient
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientMock
import uk.gov.ons.addressIndex.demoui.modules.{DemoUIAddressIndexVersionModule, DemoUIVersionModuleMock, DemouiConfigModule}
import uk.gov.ons.addressIndex.demoui.utils.{ClassHierarchy, RelativesExpander}

import scala.concurrent.{ExecutionContext, Future}

class SingleMatchNoAppTest extends
  //FlatSpec
  PlaySpec
  with Results
 // with BeforeAndAfterEach
 // with Matchers
  with MockFactory
  //with OneInstancePerTest
 {

  private trait Fixture {

    implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
    val addressIndexClient: AddressIndexClient = mock[AddressIndexClient]
    val conf: DemouiConfigModule = mock[DemouiConfigModule]
    val wsClient =  WsTestClient.withClient[WSClient](identity)
    val addressIndexClientMock: AddressIndexClientMock = new AddressIndexClientMock(wsClient, conf)
    val version = new DemoUIVersionModuleMock(addressIndexClientMock, executionContext)
    val controllerComponents = mock[DefaultControllerComponents]
    val environment = mock[Environment]
   // val wsClient = mock[WSClient]

    val messagesApi = new DefaultMessagesApi(
      Map("en" -> Map("category.C" -> "Commercial",
        "category.CL" -> "Leisure - Applicable to recreational sites and enterprises",
        "category.CL06" -> "Indoor / Outdoor Leisure / Sporting Activity / Centre",
        "category.CL06RG" -> "Recreation Ground",
        "category.M" -> "Military",
        "category.MF" -> "Air Force",
        "category.MF99UG" -> "Air Force Military Storage"))
    )
    val langs: Langs = new DefaultLangs()
    val classHierarchy = new ClassHierarchy(messagesApi, langs)
    val relativesExpander = new RelativesExpander(addressIndexClient, conf)

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
      val expectedString = "Find an address</a>"

      // When
      val response: Future[Result] = singleController.showSingleMatchPage().apply(FakeRequest())
      //.apply(FakeRequest().withSession("api-key" -> ""))

      //   home().apply(FakeRequest().withSession("api-key" -> ""))
      // val response = homeController.home().apply(FakeRequest())
      // val response = homeController.doLogin()
      val content = contentAsString(response)

      // Then
      status(response) mustBe 200
      content must include(expectedString)
    }
  }
}

