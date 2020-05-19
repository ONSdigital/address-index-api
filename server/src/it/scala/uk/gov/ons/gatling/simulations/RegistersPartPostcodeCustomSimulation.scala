package uk.gov.ons.gatling.simulations

import io.gatling.core.Predef._
import io.gatling.core.feeder.RecordSeqFeederBuilder
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import uk.gov.ons.gatling.conf.ConfigLoader

import scala.concurrent.duration._
import scala.language.postfixOps

class RegistersPartPostcodeCustomSimulation extends Simulation {

  val baseUrl: String = ConfigLoader("baseUrl")
  val apiKey: String = ConfigLoader("apiKey")
  val numOfRequestsPerSecond: Int = ConfigLoader("requestsPerSecond") toInt
  val requestRelPath = ConfigLoader("request_rel_path")
  val requestType = ConfigLoader("request_type")
  val requestName: String = ConfigLoader("request_name_prefix").stripSuffix(" ") + ": " + baseUrl + requestRelPath

  println(s"Running test with numOfRequestsPerSecond: $numOfRequestsPerSecond, " +
    s"baseUrl : $baseUrl, $requestType Request : $requestRelPath")

  val httpProtocol: HttpProtocolBuilder = http
    .baseURL(baseUrl)
    .inferHtmlResources()
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
    .acceptEncodingHeader("gzip, deflate")
    .acceptLanguageHeader("en-GB,en;q=0.5")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.13; rv:60.0) Gecko/20100101 Firefox/60.0")
    .shareConnections
    .authorizationHeader(apiKey)

  val headers: Map[String, String] = Map("Upgrade-Insecure-Requests" -> "1")

  // partial addresses from the Random Address Generator
  val feeder: RecordSeqFeederBuilder[String] = csv("partpostcodes.csv").random

  // user pause of 100ms between postcodes, then get next one from file
  // requestRelpath is addresses/postcode/ (no further)
  val scn: ScenarioBuilder =
  scenario(requestName)
    .pause(100 millis)
    .feed(feeder)
    .exec(http("Postcode")
      .get(requestRelPath + "${postcode}")
    )

  setUp(scn.inject(constantUsersPerSec(numOfRequestsPerSecond) during (10 minutes)))
    .throttle(jumpToRps(numOfRequestsPerSecond), holdFor(10 minutes))
    .protocols(httpProtocol)

}
