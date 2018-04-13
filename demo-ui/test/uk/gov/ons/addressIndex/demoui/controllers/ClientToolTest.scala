package uk.gov.ons.addressIndex.demoui.controllers

import org.scalatestplus.play.PlaySpec
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc.{ControllerComponents, Results}
import play.api.test.Helpers._
import play.api.test.WithApplication
import uk.gov.ons.addressIndex.demoui.modules.{DemoUIAddressIndexVersionModule, DemouiConfigModule}
import play.api.test.FakeRequest
import uk.gov.ons.addressIndex.demoui.client.{AddressIndexClientInstance, AddressIndexClientMock}
import uk.gov.ons.addressIndex.demoui.utils.{ClassHierarchy, RelativesExpander}

import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Tests for single match controller
  */
class ClientToolTest extends PlaySpec with Results {

  "Clerical Tool controller" should {
    "return a page containing additional information" in new WithApplication {
      // Given
      val messagesApi = app.injector.instanceOf[MessagesApi]
      val langs = app.injector.instanceOf[Langs]
      val configuration = app.injector.instanceOf[DemouiConfigModule]
      val apiClient = app.injector.instanceOf[AddressIndexClientMock]
      val version = app.injector.instanceOf[DemoUIAddressIndexVersionModule]
      val controllerComponents = app.injector.instanceOf[ControllerComponents]
      val expectedString = "GATE REACH"
      val classHierarchy  = app.injector.instanceOf(classOf[ClassHierarchy])
      val expandedRels = app.injector.instanceOf(classOf[RelativesExpander])

      // When
      val inputAddress = "7 GATE REACH EXETER EX2 6GA"
      val filter = ""
      val response = new ClericalToolController(
        controllerComponents,
        configuration,
        messagesApi,
        langs,
        apiClient.asInstanceOf[AddressIndexClientInstance],
        classHierarchy,
        expandedRels,
        version)
        .doMatchWithInput(inputAddress, filter, Some(1), Some(1)).apply(FakeRequest().withSession("api-key" -> ""))
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }

    "return a page containing additional information with a filter" in new WithApplication {
      // Given
      val messagesApi = app.injector.instanceOf[MessagesApi]
      val langs = app.injector.instanceOf[Langs]
      val configuration = app.injector.instanceOf[DemouiConfigModule]
      val apiClient = app.injector.instanceOf[AddressIndexClientMock]
      val version = app.injector.instanceOf[DemoUIAddressIndexVersionModule]
      val controllerComponents = app.injector.instanceOf[ControllerComponents]
      val expectedString = "Residential"
      val classHierarchy  = app.injector.instanceOf(classOf[ClassHierarchy])
      val expandedRels = app.injector.instanceOf(classOf[RelativesExpander])

      // When
      val inputAddress = "7 GATE REACH EXETER EX2 6GA"
      val filter = "residential"
      val response = new ClericalToolController(
        controllerComponents,
        configuration,
        messagesApi,
        langs,
        apiClient.asInstanceOf[AddressIndexClientInstance],
        classHierarchy,
        expandedRels,
        version)
        .doMatchWithInput(inputAddress, filter, Some(1), Some(1)).apply(FakeRequest().withSession("api-key" -> ""))
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }

  }
}

