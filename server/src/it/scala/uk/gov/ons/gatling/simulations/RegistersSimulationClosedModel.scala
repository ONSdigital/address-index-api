package uk.gov.ons.gatling.simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import uk.gov.ons.gatling.conf.ConfigLoader

import scala.language.postfixOps

class RegistersSimulationClosedModel extends Simulation {

  private val baseUrl: String = ConfigLoader("baseUrl")
  private val apiKey: String = ConfigLoader("apiKey")
  private val numOfRequestsPerSecond: Int = ConfigLoader("requestsPerSecond") toInt
  private val requestRelPath = ConfigLoader("request_rel_path")
  private val requestType = ConfigLoader("request_type")
  private val concurrentUsers = Integer.parseInt(ConfigLoader("concurrent_users"))
  private val requestName: String = ConfigLoader("request_name_prefix").stripSuffix(" ") + ": " + baseUrl + requestRelPath

  println(s"Running test with numOfRequestsPerSecond: $numOfRequestsPerSecond, " +
    s"concurrentUsers : $concurrentUsers, " +
    s"baseUrl : $baseUrl, $requestType Request : $requestRelPath")

  private val httpProtocol: HttpProtocolBuilder = http
    .baseURL(baseUrl)
    .inferHtmlResources()
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-GB,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:60.0) Gecko/20100101 Firefox/60.0")
    .shareConnections
    .authorizationHeader(apiKey)

  private val postRequestBody = ConfigLoader.getPOSTRequestBodyJSONPath
  println(s"payload name $postRequestBody")

  val headers: Map[String, String] = Map("Upgrade-Insecure-Requests" -> "1")
  private val httpRequestBuilder = if (requestType == "POST") {
    val postRequestBody = ConfigLoader.getPOSTRequestBodyJSONPath
    http(requestName)
      .post(requestRelPath)
      .headers(headers)
      .body(RawFileBody(postRequestBody)).asJSON
  } else {
    http(requestName)
      .get(requestRelPath)
      .headers(headers)
  }

  setUp(scenario("All").exec(httpRequestBuilder).inject(atOnceUsers(concurrentUsers))
    .protocols(httpProtocol))
}
