package uk.gov.ons.addressIndex.demoui.controllers

import org.scalatestplus.play.PlaySpec
import play.api.i18n.{Langs, MessagesApi}
import play.api.mvc.{ControllerComponents, Result, Results}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, WithApplication}
import uk.gov.ons.addressIndex.demoui.client.{AddressIndexClientInstance, AddressIndexClientMock}
import uk.gov.ons.addressIndex.demoui.modules.{DemoUIAddressIndexVersionModule, DemouiConfigModule}
import uk.gov.ons.addressIndex.demoui.utils.{ClassHierarchy, RelativesExpander}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Tests for single match controller
  */
class ClientToolTest extends PlaySpec with Results {

  "Clerical Tool controller" should {
    "return a page containing additional information" in new WithApplication {
      // Given
      val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
      val langs: Langs = app.injector.instanceOf[Langs]
      val configuration: DemouiConfigModule = app.injector.instanceOf[DemouiConfigModule]
      val apiClient: AddressIndexClientMock = app.injector.instanceOf[AddressIndexClientMock]
      val version: DemoUIAddressIndexVersionModule = app.injector.instanceOf[DemoUIAddressIndexVersionModule]
      val controllerComponents: ControllerComponents = app.injector.instanceOf[ControllerComponents]
      val expectedString = "GATE REACH"
      val classHierarchy: ClassHierarchy = app.injector.instanceOf(classOf[ClassHierarchy])
      val expandedRels: RelativesExpander = app.injector.instanceOf(classOf[RelativesExpander])

      // When
      val inputAddress = "7 GATE REACH EXETER EX2 6GA"
      val filter = ""
      val historical = true
      val matchThreshold = 5
      val response: Future[Result] = new ClericalToolController(
        controllerComponents,
        configuration,
        messagesApi,
        langs,
        apiClient.asInstanceOf[AddressIndexClientInstance],
        classHierarchy,
        expandedRels,
        version)
        .doMatchWithInput(inputAddress, Some(filter), Some(1), Some(1), Some(historical), Some(matchThreshold), None, None, None).apply(FakeRequest().withSession("api-key" -> ""))
      val content: String = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }

    "return a page containing additional information with dates" in new WithApplication {
      // Given
      val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
      val langs: Langs = app.injector.instanceOf[Langs]
      val configuration: DemouiConfigModule = app.injector.instanceOf[DemouiConfigModule]
      val apiClient: AddressIndexClientMock = app.injector.instanceOf[AddressIndexClientMock]
      val version: DemoUIAddressIndexVersionModule = app.injector.instanceOf[DemoUIAddressIndexVersionModule]
      val controllerComponents: ControllerComponents = app.injector.instanceOf[ControllerComponents]
      val expectedString = "GATE REACH"
      val classHierarchy: ClassHierarchy = app.injector.instanceOf(classOf[ClassHierarchy])
      val expandedRels: RelativesExpander = app.injector.instanceOf(classOf[RelativesExpander])

      // When
      val inputAddress = "7 GATE REACH EXETER EX2 6GA"
      val filter = ""
      val historical = true
      val matchThreshold = 5
      val startDate = "2012-01-01"
      val endDate = "2013-01-01"
      val response: Future[Result] = new ClericalToolController(
        controllerComponents,
        configuration,
        messagesApi,
        langs,
        apiClient.asInstanceOf[AddressIndexClientInstance],
        classHierarchy,
        expandedRels,
        version)
        .doMatchWithInput(inputAddress, Some(filter), Some(1), Some(1), Some(historical), Some(matchThreshold), Some(startDate), Some(endDate), Some("")).apply(FakeRequest().withSession("api-key" -> ""))
      val content: String = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }

    "return a page containing additional information with a filter" in new WithApplication {
      // Given
      val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
      val langs: Langs = app.injector.instanceOf[Langs]
      val configuration: DemouiConfigModule = app.injector.instanceOf[DemouiConfigModule]
      val apiClient: AddressIndexClientMock = app.injector.instanceOf[AddressIndexClientMock]
      val version: DemoUIAddressIndexVersionModule = app.injector.instanceOf[DemoUIAddressIndexVersionModule]
      val controllerComponents: ControllerComponents = app.injector.instanceOf[ControllerComponents]
      val expectedString = "Residential"
      val classHierarchy: ClassHierarchy = app.injector.instanceOf(classOf[ClassHierarchy])
      val expandedRels: RelativesExpander = app.injector.instanceOf(classOf[RelativesExpander])

      // When
      val inputAddress = "7 GATE REACH EXETER EX2 6GA"
      val filter = "residential"
      val historical = true
      val matchThreshold = 5
      val response: Future[Result] = new ClericalToolController(
        controllerComponents,
        configuration,
        messagesApi,
        langs,
        apiClient.asInstanceOf[AddressIndexClientInstance],
        classHierarchy,
        expandedRels,
        version)
        .doMatchWithInput(inputAddress, Some(filter), Some(1), Some(1), Some(historical), Some(matchThreshold), None, None, None).apply(FakeRequest().withSession("api-key" -> ""))
      val content: String = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }

    "return a page containing additional information with a filter and dates" in new WithApplication {
      // Given
      val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
      val langs: Langs = app.injector.instanceOf[Langs]
      val configuration: DemouiConfigModule = app.injector.instanceOf[DemouiConfigModule]
      val apiClient: AddressIndexClientMock = app.injector.instanceOf[AddressIndexClientMock]
      val version: DemoUIAddressIndexVersionModule = app.injector.instanceOf[DemoUIAddressIndexVersionModule]
      val controllerComponents: ControllerComponents = app.injector.instanceOf[ControllerComponents]
      val expectedString = "Residential"
      val classHierarchy: ClassHierarchy = app.injector.instanceOf(classOf[ClassHierarchy])
      val expandedRels: RelativesExpander = app.injector.instanceOf(classOf[RelativesExpander])

      // When
      val inputAddress = "7 GATE REACH EXETER EX2 6GA"
      val filter = "residential"
      val historical = true
      val matchThreshold = 5
      val startDate = "2012-01-01"
      val endDate = "2013-01-01"
      val response: Future[Result] = new ClericalToolController(
        controllerComponents,
        configuration,
        messagesApi,
        langs,
        apiClient.asInstanceOf[AddressIndexClientInstance],
        classHierarchy,
        expandedRels,
        version)
        .doMatchWithInput(inputAddress, Some(filter), Some(1), Some(1), Some(historical), Some(matchThreshold), Some(startDate), Some(endDate), Some("")).apply(FakeRequest().withSession("api-key" -> ""))
      val content: String = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }
  }
}

