package uk.gov.ons.addressIndex.demoui.controllers

import org.scalatest.{FunSuite, Matchers}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.mockito.Mockito._

import scala.concurrent.{ExecutionContext, Future}
import play.api.i18n._
import uk.gov.ons.addressIndex.demoui.client.AddressIndexClientInstance

class ApplicationHomeTest extends FunSuite with Matchers {
    val mockApiClient   = mock(classOf[AddressIndexClientInstance])
    val mockMessageApi  = mock(classOf[MessagesApi])
    val mockExecutionContext = mock(classOf[ExecutionContext])

    test("home page includes at least one link") {
        val response = new ApplicationHome()(mockExecutionContext, mockMessageApi).indexPage()(FakeRequest())
        val content  = contentAsString(response)
        status(response) should be(200)
        content should include("<h4><a href=\"addresses\">Find an address </a></h4>")
    }
}
