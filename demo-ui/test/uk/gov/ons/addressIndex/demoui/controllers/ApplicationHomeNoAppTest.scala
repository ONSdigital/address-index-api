package uk.gov.ons.addressIndex.demoui.controllers

import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers, OneInstancePerTest}
import org.scalatestplus.play.PlaySpec
import org.specs2.execute.Results
import play.api.Environment
import play.api.http.Port
import play.api.i18n.{DefaultLangs, DefaultMessagesApi, Langs, MessagesApi}
import play.api.libs.ws.WSClient
import play.api.mvc.{ControllerComponents, DefaultActionBuilder, DefaultControllerComponents}
import play.api.test.{FakeRequest, WsTestClient}
import play.api.test.Helpers.{contentAsString, status}
import uk.gov.ons.addressIndex.client.AddressIndexClient
import uk.gov.ons.addressIndex.demoui.modules.{DemoUIAddressIndexVersionModule, DemouiConfigModule}
import play.api.test.Helpers.defaultAwaitTimeout

import scala.concurrent.ExecutionContext

class ApplicationHomeNoAppTest extends
FlatSpec
 // PlaySpec
  with Results
  with BeforeAndAfterEach with Matchers with MockFactory with OneInstancePerTest {

  private trait Fixture {

    implicit val executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global
    val addressIndexClient: AddressIndexClient = mock[AddressIndexClient]
    val conf: DemouiConfigModule = mock[DemouiConfigModule]
    val version = mock[DemoUIAddressIndexVersionModule]
    val controllerComponents = mock[DefaultControllerComponents]
    val environment = mock[Environment]
    val wsClient = mock[WSClient]
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

    val homeController = new ApplicationHomeController(
      controllerComponents,
      conf,
      version,
      messagesApi,
      environment,
     // addressIndexClient
    //  WsTestClient.withClient[WSClient](identity)
      wsClient
    )
  }

  "Application Controller" should
    "include at least one link" ignore new Fixture {

      // Given
      val expectedString = "Find an address</a>"

      // When
      val response = homeController.home().apply(FakeRequest().withSession("api-key" -> ""))
   // val response = homeController.home().apply(FakeRequest())
   // val response = homeController.doLogin()
      val content = contentAsString(response)

      // Then
      status(response) shouldBe 200
      content should include(expectedString)
    }
}
