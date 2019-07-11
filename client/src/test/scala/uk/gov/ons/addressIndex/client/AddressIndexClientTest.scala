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
        historical = true,
        id = UUID.randomUUID,
        apiKey = "",
        startdate = "",
        enddate = "",
        verbose = true,
        epoch = ""
      )
    ).url
    val expected = s"$apiHost/addresses/uprn/101010"
    actual shouldBe expected
  }

  it should "construct a correct address query WSRequest" in {
    val input = "input"
    val filter = "filter"
    val rangeKM = "rangekm"
    val lat = "lat"
    val lon = "lon"
    val historical = true
    val verbose = true
    val matchThreshold = 5
    val startDate = "startdate"
    val endDate = "enddate"
    val actual = apiClient.addressQueryWSRequest(
      request = AddressIndexSearchRequest(
        input = input,
        filter = filter,
        historical = historical,
        matchthreshold = matchThreshold,
        startdate = startDate,
        enddate = endDate,
        rangekm = rangeKM,
        lat = lat,
        lon = lon,
        id = UUID.randomUUID,
        limit = "10",
        offset = "0",
        apiKey = "",
        verbose = verbose,
        epoch = "",
        fromsource = "all"
      )
    ).queryString
    val expected = Map(
      "input" -> Seq("input"),
      "classificationfilter" -> Seq("filter"),
      "historical" -> Seq("true"),
      "matchthreshold" -> Seq("5"),
      "rangekm" -> Seq("rangekm"),
      "verbose" -> Seq("true"),
      "lat" -> Seq("lat"),
      "lon" -> Seq("lon"),
      "startdate" -> Seq("startdate"),
      "enddate" -> Seq("enddate"),
      "limit" -> Seq("10"),
      "offset" -> Seq("0"),
      "fromsource" -> List("all")
    )
    actual shouldBe expected
  }
}