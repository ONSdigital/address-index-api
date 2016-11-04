import org.scalatest.{FunSuite, Matchers}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.mockito.Mockito._
import uk.gov.ons.address.conf.OnsFrontendConfiguration
import uk.gov.ons.address.controllers.ApplicationHome

class ApplicationHomeTest extends FunSuite with Matchers {

    test("indexPage returns a html page with links to 3 other pages") {
        val mockOnsConfiguration = mock(classOf[OnsFrontendConfiguration])
        val response = new ApplicationHome(mockOnsConfiguration).indexPage(FakeRequest())
        status(response) should be(200)
        val content  = contentAsString(response)
        content should include("<h4><a href=\"addresses\">Find an address </a></h4>")
        content should include("<h4><a href=\"addresses/bulk\">Match multiple addresses</a></h4>")
        content should include("<h4><a href=\"/addresses/propose/new/address/form\">Propose new address</a></h4>")
    }
}
