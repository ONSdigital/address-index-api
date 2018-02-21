package uk.gov.ons.addressIndex.client

import java.util.UUID

import org.scalatest.{FlatSpec, Matchers}
import play.api.http.Port
import play.api.libs.ws.WSClient
import play.api.test.WsTestClient
import uk.gov.ons.addressIndex.client.Resources._
import uk.gov.ons.addressIndex.model._

object Resources {
  val apiHost = "http://localhost"
  val apiClient = new AddressIndexClient {

    /**
      * @return a standard web service client
      */
    override def client: WSClient = WsTestClient.withClient[WSClient](identity)(new Port(9000))

    /**
      * @return the host address of the address index server
      */
    override def host: String = apiHost
  }
}

class AddressIndexClientTest extends FlatSpec with Matchers {

  it should "construct a correct uprn WSRequest" in {
    val actual = apiClient.uprnQueryWSRequest(
      request = AddressIndexUPRNRequest(
        uprn = 101010,
        id = UUID.randomUUID,
        apiKey = ""
      )
    ).url
    val expected = s"${apiHost}/addresses/101010"
    actual shouldBe expected
  }

  it should "construct a correct address query WSRequest" in {
    val input = "input"
    val filter = "filter"
    val actual = apiClient.addressQueryWSRequest(
      request = AddressIndexSearchRequest(
        input = input,
        filter = filter,
        id = UUID.randomUUID,
        limit = "10",
        offset = "0",
        apiKey = ""
      )
    ).queryString
    val expected = Map(
      "input" -> Seq("input"),
      "filter" -> Seq("filter"),
      "limit" -> Seq("10"),
      "offset" -> Seq("0")
    )
    actual shouldBe expected
  }
}