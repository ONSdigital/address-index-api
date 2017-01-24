package uk.gov.ons.addressIndex.client

import java.util.UUID
import AddressIndexClientHelper._
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.ws.WSClient
import play.api.test.WsTestClient
import uk.gov.ons.addressIndex.model._
import play.api.http.Port
import Resources._

object Resources {
  val apiHost = "host"
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

  object Formats {
    val none: Option[AddressScheme] = None
    val paf: Option[PostcodeAddressFile] = Some(PostcodeAddressFile("paf"))
    val nag: Option[AddressScheme] = Some(BritishStandard7666("bs"))
  }

}

class AddressIndexClientTest extends FlatSpec with Matchers {

  it should "construct a correct uprn WSRequest, no format" in {
    val actual = apiClient.urpnQueryWSRequest(
      request = AddressIndexUPRNRequest(
        format = Formats.none,
        uprn = 101010,
        id = UUID.randomUUID
      )
    ).url
    val expected = s"${apiHost}/addresses/101010"
    actual shouldBe expected
  }

  it should "construct a correct uprn WSRequest, `PostcodeAddressFile` format" in {
    val actual = apiClient.urpnQueryWSRequest(
      request = AddressIndexUPRNRequest(
        format = Formats.paf,
        uprn = 101010,
        id = UUID.randomUUID
      )
    ).queryString
    val expected = Map(
      "format" -> Seq("paf")
    )
    actual shouldBe expected
  }

  it should "construct a correct uprn WSRequest, `BritishStandard7666` format" in {
    val actual = apiClient.urpnQueryWSRequest(
      request = AddressIndexUPRNRequest(
        format = Formats.nag,
        uprn = 101010,
        id = UUID.randomUUID
      )
    ).queryString
    val expected = Map(
      "format" -> Seq("bs")
    )
    actual shouldBe expected
  }

  it should "construct a correct address query WSRequest, no format" in {
    val input = "input"
    val actual = apiClient.addressQueryWSRequest(
      request = AddressIndexSearchRequest(
        format = Formats.none,
        input = input,
        id = UUID.randomUUID,
        limit = "10",
        offset = "0"
      )
    ).queryString
    val expected = Map(
      "input" -> Seq("input"),
      "limit" -> Seq("10"),
      "offset" -> Seq("0")
    )
    actual shouldBe expected
  }

  it should "construct a correct address query WSRequest, `PostcodeAddressFile` format" in {
    val input = "input"
    val actual = apiClient.addressQueryWSRequest(
      request = AddressIndexSearchRequest(
        format = Formats.paf,
        input = input,
        id = UUID.randomUUID,
        limit = "10",
        offset = "0"
      )
    ).queryString
    val expected = Map(
      "format" -> Seq("paf"),
      "input" -> Seq("input"),
      "limit" -> Seq("10"),
      "offset" -> Seq("0")
    )
    actual shouldBe expected
  }

  it should "construct a correct address query WSRequest, `BritishStandard7666` format" in {
    val input = "input"
    val actual = apiClient.addressQueryWSRequest(
      request = AddressIndexSearchRequest(
        format = Formats.nag,
        input = input,
        id = UUID.randomUUID,
        limit = "10",
        offset = "0"
      )
    ).queryString
    val expected = Map(
      "format" -> Seq("bs"),
      "input" -> Seq("input"),
      "limit" -> Seq("10"),
      "offset" -> Seq("0")
    )
    actual shouldBe expected
  }
}

class AugOptFormatTest extends FlatSpec with Matchers {

  it should "append paf format param when `Some(PostcodeAddressFile)" in {
    val actual = apiClient.client.url(apiHost).formatOptionalQueryString(Some(PostcodeAddressFile("paf"))).queryString
    val expected = Map("format" -> Seq("paf"))
    actual shouldBe expected
  }

  it should "append paf format param when `Some(BritishStandard7666)" in {
    val actual = apiClient.client.url(apiHost).formatOptionalQueryString(Some(BritishStandard7666("bs"))).queryString
    val expected = Map("format" -> Seq("bs"))
    actual shouldBe expected
  }
}
