package uk.gov.ons.gatling.simulations

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import uk.gov.ons.gatling.conf.ConfigLoader

import scala.concurrent.duration._
import scala.language.postfixOps

class RegistersTypeaheadResultsCustomSimulation extends Simulation {

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

  val headers = Map("Upgrade-Insecure-Requests" -> "1")

  // partial addresses from the Random Address Generator
  val feeder = csv("typeahead2.csv").circular

  val uprns: List[String] = List(
    "100021379998",
    "5148433",
    "10002750695",
    "100061135150",
    "130075070",
    "38063271",
    "10091942995",
    "38252540",
    "10000150196",
    "72096502",
    "10009187440",
    "310052801",
    "10024253131",
    "10014515642",
    "200000843531",
    "10008566546",
    "10093567792",
    "100052116048",
    "100070699315"
  )

  // user pause of 300ms between keystrokes, then get next partial from file
  // requestRelpath is addresses/partial/ (no further)
  val scn: ScenarioBuilder =
  scenario(requestName)
    .pause(300 millis)
    .feed(feeder)
    .exec(http("Typeahead")
      .get(requestRelPath + "${addresspart}" + "?limit=4")
      .check(jsonPath("$..uprn").findAll.saveAs("uprns"))
      .check(jsonPath("$..input").findAll.saveAs("inputs"))
    )

    .foreach("${inputs}", "input") {
      exec(foundaddress => {
        println(foundaddress("input").as[String])
        foundaddress
      }).foreach("${uprns}", "uprn") {
        exec(foundaddress => {
          val uprnString = foundaddress("uprn").as[String]
          val hit = {
            if (uprns.contains(uprnString)) " => HIT" else " "
          }
          println(uprnString + hit)
          foundaddress
        })
      }
    }


  setUp(scn.inject(constantUsersPerSec(numOfRequestsPerSecond) during (1 minute)))
    .throttle(jumpToRps(numOfRequestsPerSecond), holdFor(1 minute))
    .protocols(httpProtocol)

}
