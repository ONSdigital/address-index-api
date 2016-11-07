import java.io.File
import java.nio.file.Files

import org.scalatest.FunSuite
import org.scalatest.{FunSuite, Matchers}
import play.api.test.{FakeHeaders, FakeRequest}
import play.api.test.Helpers._
import org.mockito.Mockito._
import org.mockito.Matchers._
import play.api.i18n.MessagesApi
import play.api.libs.Files.TemporaryFile
import play.api.mvc.{MultipartFormData, Request}
import play.api.mvc.MultipartFormData.FilePart
import uk.gov.ons.address.client.AddressApiClient
import uk.gov.ons.address.conf.OnsFrontendConfiguration
import uk.gov.ons.address.controllers.{MultipleMatch, SingleMatch}
import uk.gov.ons.address.model.BulkMatchResponse
import play.api.libs.Files.TemporaryFile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, ExecutionContext, Future}

class MultipleMatchTest extends FunSuite with Matchers {

  ignore("showMultipleMatchPage() returns the bulk match html page") {
    val mockApiClient     = mock(classOf[AddressApiClient])
    val mockMessageApi    = mock(classOf[MessagesApi])
    val mockConfiguration = mock(classOf[OnsFrontendConfiguration])
    val response = new MultipleMatch(mockApiClient, mockMessageApi, mockConfiguration).showMultipleMatchPage()(FakeRequest())
    val content  = contentAsString(response)
    status(response) should be(200)
    content should include("<title>ONS-Address - Bulk match</title>")
  }

  ignore("doBulkMatch return error message for non csv file format") {
    val mockMessageApi    = mock(classOf[MessagesApi])
    val mockApiClient     = mock(classOf[AddressApiClient])
    val mockConfiguration = mock(classOf[OnsFrontendConfiguration])

    when(mockApiClient.multipleMatch(any())).thenReturn(Future.successful(BulkMatchResponse.apply(Some(1),Some(1),Some(1),Some(1))))

    val controller  = new MultipleMatch(mockApiClient, mockMessageApi, mockConfiguration)
    val fakeRequest = FakeRequest(POST, "", FakeHeaders(), {
      val tempFile = new TemporaryFile(mock(classOf[File]))
      val filePart = FilePart("file", "test.file", Some("invalid/type"), tempFile)
      MultipartFormData(Map(), List(filePart), List())
    }
    )
    val response   = controller.doBulkMatch()(fakeRequest)
    await(response)

    val content = contentAsString(response)

    status(response) should be(200)
    content should include("<div class=\"alert alert-danger\" role=\"alert\"><strong>Currently the API is only supporting .csv format</strong></div>")
  }

  ignore("doBulkMatch return error message when file not present") {
    val mockMessageApi    = mock(classOf[MessagesApi])
    val mockApiClient     = mock(classOf[AddressApiClient])
    val mockConfiguration = mock(classOf[OnsFrontendConfiguration])

    when(mockApiClient.multipleMatch(any())).thenReturn(Future.successful(BulkMatchResponse.apply(Some(1),Some(1),Some(1),Some(1))))
    val buildFakeRequest =  FakeRequest(POST, "", FakeHeaders(), {
      val tempFile = new TemporaryFile(mock(classOf[File]))
      val filePart = FilePart("", "", Some(""), tempFile)
      MultipartFormData(Map(), List(filePart), List())
    }
    )

    val controller = new MultipleMatch(mockApiClient, mockMessageApi, mockConfiguration)
    val response   = controller.doBulkMatch()(buildFakeRequest)
    val content    = contentAsString(response)

    status(response) should be(200)
    content should include("Something went wrong while uploading the file. Please contact System Administrator")
  }
}