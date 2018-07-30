package uk.gov.ons.gatling.simulations

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import uk.gov.ons.gatling.conf.ConfigLoader

import scala.concurrent.duration._
import scala.language.postfixOps

class RegistersSimulation extends Simulation {

  val baseUrl: String = ConfigLoader("baseUrl")
  val numOfConcurrentUsers: Int = ConfigLoader("concurrentUsers") toInt
  val requestRelPath = ConfigLoader("request_rel_path")
  val request_type = ConfigLoader("request_type")
  val requestName: String = ConfigLoader("request_name_prefix").stripSuffix(" ") + ": " + baseUrl + requestRelPath

  println(s"Running test with numOfConcurrentUsers: $numOfConcurrentUsers, baseUrl : $baseUrl, $request_type Request : $requestRelPath")

  val httpProtocol: HttpProtocolBuilder = http
    .baseURL(baseUrl)
    .inferHtmlResources()
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-GB,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:60.0) Gecko/20100101 Firefox/60.0")
    .shareConnections

  val headers_0 = Map("Upgrade-Insecure-Requests" -> "1")

  var scn: ScenarioBuilder = scenario(requestName);
  if (request_type == "POST") {
    val postRequestBody = ConfigLoader.getPOSTRequestBodyJSONPath()

    scn = scn
      .exec(
        http(requestName)
          .post(requestRelPath)
          .headers(headers_0)
          .body(RawFileBody(postRequestBody)).asJSON
      )
  } else {
    scn = scn
      .exec(
        http(requestName)
          .get(requestRelPath)
          .headers(headers_0)
      )
  }

  setUp(scn.inject(constantUsersPerSec(numOfConcurrentUsers) during (1 minute)))
    .throttle(jumpToRps(numOfConcurrentUsers), holdFor(1 minute))
    .protocols(httpProtocol)

}
