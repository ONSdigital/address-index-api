package uk.gov.ons.addressIndex.demoui.controllers

import org.scalatestplus.play.PlaySpec
import play.api.i18n.MessagesApi
import play.api.mvc.Results
import play.api.test.Helpers._
import play.api.test.WithApplication
import uk.gov.ons.addressIndex.demoui.modules.DemouiConfigModule
import play.api.test.FakeRequest
import uk.gov.ons.addressIndex.demoui.client.{AddressIndexClientInstance, AddressIndexClientMock}
import uk.gov.ons.addressIndex.demoui.utils.ClassHierarchy


import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Tests for single match controller
  */
class ClientToolTest extends PlaySpec with Results {

  "Clerical Tool controller" should {
    "return a page containing additional information" in new WithApplication {
      // Given
      val messagesApi = app.injector.instanceOf[MessagesApi]
      val configuration = app.injector.instanceOf[DemouiConfigModule]
      val apiClient = app.injector.instanceOf[AddressIndexClientInstance]
      val expectedString = "<strong>Street:</strong>  REACH"
      val classHierarchy  = app.injector.instanceOf(classOf[ClassHierarchy])

      // When
      val inputAddress = "7 GATE REACH EXETER EX2 6GA"
      val response = new ClericalToolController(
        configuration,
        messagesApi,
        apiClient.asInstanceOf[AddressIndexClientInstance],
        classHierarchy)
        .doMatchWithInput(inputAddress,"paf", Some(1), Some(1)).apply(FakeRequest())
      val content = contentAsString(response)

      // Then
      status(response) mustBe OK
      content must include(expectedString)
    }

  }
}

