package uk.gov.ons.gatling.simulations

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._
import io.gatling.http.protocol.HttpProtocolBuilder
import uk.gov.ons.gatling.conf.ConfigLoader

import scala.concurrent.duration._
import scala.language.postfixOps

class RegistersTypeaheadRandomSimulation extends Simulation {

  val baseUrl: String = ConfigLoader("baseUrl").replace("partial/", "")
  val apiKey: String = ConfigLoader("apiKey")
  val duration: Int = ConfigLoader("duration") toInt
  val limit: String = ConfigLoader("limit")
  val requestRelPath = ConfigLoader("request_rel_path")
  val requestType = ConfigLoader("request_type")
  val requestName: String = ConfigLoader("request_name_prefix").stripSuffix(" ") + ": " + baseUrl + requestRelPath
  val randomPath = "random"
  val typeaheadPath = "partial?input="

  println(s"Running test with duration: $duration, " +
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


  // Get random address from other cluster, split up the address and capture UPRN
  // then user pause of 300ms between keystrokes, then get next partial from file
  // test for UPRN being in top 4, stop when it is
  val scn: ScenarioBuilder =
  scenario(requestName)
    .exec(
      pause(300 millis)
        .exec(http("Random")
          .get(randomPath)
          .check(jsonPath("$..formattedAddress").findAll.saveAs("adds"))
          .check(jsonPath("$..uprn").findAll.saveAs("uprns"))
        )

        .foreach("${adds}", "add") {
          exec(foundaddress => {
            val testAddress = foundaddress("add").as[String]
            println(testAddress)
            foundaddress
          })
            .foreach("${uprns}", "uprn") {
              exec(foundaddress => {
                val uprnString = foundaddress("uprn").as[String]
                println(uprnString)
                val testAddress = foundaddress("add").as[String]
                val chars = testAddress.toArray
                val part1 = testAddress.take(5)
                val part2 = testAddress.take(5) + chars(5)
                val part3 = testAddress.take(6) + chars(6)
                val part4 = testAddress.take(7) + chars(7)
                val part5 = testAddress.take(8) + chars(8)
                val part6 = testAddress.take(9) + chars(9)
                val part7 = testAddress.take(10) + chars(10)
                val part8 = testAddress.take(11) + chars(11)
                val part9 = testAddress.take(12) + chars(12)
                val part10 = testAddress.take(13) + chars(13)
                val part11 = testAddress.take(14) + chars(14)
                val part12 = testAddress.take(15) + chars(15)
                val part13 = testAddress.take(16) + chars(16)
                val part14 = testAddress.take(17) + chars(17)
                val part15 = testAddress.take(18) + chars(18)
                val part16 = testAddress.take(19) + chars(19)
                val newsession = foundaddress
                  .set("part1", part1)
                  .set("part2", part2)
                  .set("part3", part3)
                  .set("part4", part4)
                  .set("part5", part5)
                  .set("part6", part6)
                  .set("part7", part7)
                  .set("part8", part8)
                  .set("part9", part9)
                  .set("part10", part10)
                  .set("part11", part11)
                  .set("part12", part12)
                  .set("part13", part13)
                  .set("part14", part14)
                  .set("part15", part15)
                  .set("part16", part16)
                  .set("uprn", uprnString)
                  .set("match", "x")
                newsession
              })

            }

        }
    )
    // .foreach can't be used here so each part-address is a separate chain
    .exec(
    pause(300 millis)
      .exec(http("Typeahead")
        .get(typeaheadPath + "${part1}" + "&limit=" + limit)
        .check(jsonPath("$..uprn").findAll.saveAs("uprns2")))
      .foreach("${uprns2}", "uprn2") {
        exec(newsession => {
          println(newsession("part1").as[String])
          val uprnString1 = newsession("uprn").as[String]
          val uprnString2 = newsession("uprn2").as[String]
          val hit = if (uprnString1 == uprnString2) " => HIT (5 chars)" else "x"
          val newsession2 = if (hit == "x") newsession else newsession.set("match", hit)
          println(uprnString1 + ":" + uprnString2 + hit)
          newsession2
        })
      }
  )

    // only execute the chain if no match has been found
    .doIfEquals("${match}", "x") {
    exec(
      pause(300 millis)
        .exec(http("Typeahead")
          .get(typeaheadPath + "${part2}" + "&limit=" + limit)
          .check(jsonPath("$..uprn").findAll.saveAs("uprns2")))
        .foreach("${uprns2}", "uprn2") {
          exec(newsession => {
            println(newsession("part2").as[String])
            val uprnString1 = newsession("uprn").as[String]
            val uprnString2 = newsession("uprn2").as[String]
            val hit = if (uprnString1 == uprnString2) " => HIT (6 chars)" else "x"
            val newsession3 = if (hit == "x") newsession else newsession.set("match", hit)
            println(uprnString1 + ":" + uprnString2 + hit)
            newsession3
          })
        }
    )
  }
    .doIfEquals("${match}", "x") {
      exec(
        pause(300 millis)
          .exec(http("Typeahead")
            .get(typeaheadPath + "${part3}" + "&limit=" + limit)
            .check(jsonPath("$..uprn").findAll.saveAs("uprns2")))
          .foreach("${uprns2}", "uprn2") {
            exec(newsession => {
              println(newsession("part3").as[String])
              val uprnString1 = newsession("uprn").as[String]
              val uprnString2 = newsession("uprn2").as[String]
              val hit = if (uprnString1 == uprnString2) " => HIT (7 chars)" else "x"
              val newsession4 = if (hit == "x") newsession else newsession.set("match", hit)
              println(uprnString1 + ":" + uprnString2 + hit)
              newsession4
            })
          }
      )
    }
    .doIfEquals("${match}", "x") {
      exec(
        pause(300 millis)
          .exec(http("Typeahead")
            .get(typeaheadPath + "${part4}" + "&limit=" + limit)
            .check(jsonPath("$..uprn").findAll.saveAs("uprns2")))
          .foreach("${uprns2}", "uprn2") {
            exec(newsession => {
              println(newsession("part4").as[String])
              val uprnString1 = newsession("uprn").as[String]
              val uprnString2 = newsession("uprn2").as[String]
              val hit = if (uprnString1 == uprnString2) " => HIT (8 chars)" else "x"
              val newsession5 = if (hit == "x") newsession else newsession.set("match", hit)
              println(uprnString1 + ":" + uprnString2 + hit)
              newsession5
            })
          }
      )
    }
    .doIfEquals("${match}", "x") {
      exec(
        pause(300 millis)
          .exec(http("Typeahead")
            .get(typeaheadPath + "${part5}" + "&limit=" + limit)
            .check(jsonPath("$..uprn").findAll.saveAs("uprns2")))
          .foreach("${uprns2}", "uprn2") {
            exec(newsession => {
              println(newsession("part5").as[String])
              val uprnString1 = newsession("uprn").as[String]
              val uprnString2 = newsession("uprn2").as[String]
              val hit = if (uprnString1 == uprnString2) " => HIT (9 chars)" else "x"
              val newsession6 = if (hit == "x") newsession else newsession.set("match", hit)
              println(uprnString1 + ":" + uprnString2 + hit)
              newsession6
            })
          }
      )
    }
    .doIfEquals("${match}", "x") {
      exec(
        pause(300 millis)
          .exec(http("Typeahead")
            .get(typeaheadPath + "${part6}" + "&limit=" + limit)
            .check(jsonPath("$..uprn").findAll.saveAs("uprns2")))
          .foreach("${uprns2}", "uprn2") {
            exec(newsession => {
              println(newsession("part6").as[String])
              val uprnString1 = newsession("uprn").as[String]
              val uprnString2 = newsession("uprn2").as[String]
              val hit = if (uprnString1 == uprnString2) " => HIT (10 chars)" else "x"
              val newsession7 = if (hit == "x") newsession else newsession.set("match", hit)
              println(uprnString1 + ":" + uprnString2 + hit)
              newsession7
            })
          }
      )
    }
    .doIfEquals("${match}", "x") {
      exec(
        pause(300 millis)
          .exec(http("Typeahead")
            .get(typeaheadPath + "${part7}" + "&limit=" + limit)
            .check(jsonPath("$..uprn").findAll.saveAs("uprns2")))
          .foreach("${uprns2}", "uprn2") {
            exec(newsession => {
              println(newsession("part7").as[String])
              val uprnString1 = newsession("uprn").as[String]
              val uprnString2 = newsession("uprn2").as[String]
              val hit = if (uprnString1 == uprnString2) " => HIT (11 chars)" else "x"
              val newsession8 = if (hit == "x") newsession else newsession.set("match", hit)
              println(uprnString1 + ":" + uprnString2 + hit)
              newsession8
            })
          }
      )
    }
    .doIfEquals("${match}", "x") {
      exec(
        pause(300 millis)
          .exec(http("Typeahead")
            .get(typeaheadPath + "${part8}" + "&limit=" + limit)
            .check(jsonPath("$..uprn").findAll.saveAs("uprns2")))
          .foreach("${uprns2}", "uprn2") {
            exec(newsession => {
              println(newsession("part8").as[String])
              val uprnString1 = newsession("uprn").as[String]
              val uprnString2 = newsession("uprn2").as[String]
              val hit = if (uprnString1 == uprnString2) " => HIT (12 chars)" else "x"
              val newsession9 = if (hit == "x") newsession else newsession.set("match", hit)
              println(uprnString1 + ":" + uprnString2 + hit)
              newsession9
            })
          }
      )
    }
    .doIfEquals("${match}", "x") {
      exec(
        pause(300 millis)
          .exec(http("Typeahead")
            .get(typeaheadPath + "${part9}" + "&limit=" + limit)
            .check(jsonPath("$..uprn").findAll.saveAs("uprns2")))
          .foreach("${uprns2}", "uprn2") {
            exec(newsession => {
              println(newsession("part9").as[String])
              val uprnString1 = newsession("uprn").as[String]
              val uprnString2 = newsession("uprn2").as[String]
              val hit = if (uprnString1 == uprnString2) " => HIT (13 chars)" else "x"
              val newsession10 = if (hit == "x") newsession else newsession.set("match", hit)
              println(uprnString1 + ":" + uprnString2 + hit)
              newsession10
            })
          }
      )
    }
    .doIfEquals("${match}", "x") {
      exec(
        pause(300 millis)
          .exec(http("Typeahead")
            .get(typeaheadPath + "${part10}" + "&limit=" + limit)
            .check(jsonPath("$..uprn").findAll.saveAs("uprns2")))
          .foreach("${uprns2}", "uprn2") {
            exec(newsession => {
              println(newsession("part10").as[String])
              val uprnString1 = newsession("uprn").as[String]
              val uprnString2 = newsession("uprn2").as[String]
              val hit = if (uprnString1 == uprnString2) " => HIT (14 chars)" else "x"
              val newsession11 = if (hit == "x") newsession else newsession.set("match", hit)
              println(uprnString1 + ":" + uprnString2 + hit)
              newsession11
            })
          }
      )
    }
    .doIfEquals("${match}", "x") {
      exec(
        pause(300 millis)
          .exec(http("Typeahead")
            .get(typeaheadPath + "${part11}" + "&limit=" + limit)
            .check(jsonPath("$..uprn").findAll.saveAs("uprns2")))
          .foreach("${uprns2}", "uprn2") {
            exec(newsession => {
              println(newsession("part11").as[String])
              val uprnString1 = newsession("uprn").as[String]
              val uprnString2 = newsession("uprn2").as[String]
              val hit = if (uprnString1 == uprnString2) " => HIT (15 chars)" else "x"
              val newsession12 = if (hit == "x") newsession else newsession.set("match", hit)
              println(uprnString1 + ":" + uprnString2 + hit)
              newsession12
            })
          }
      )
    }
    .doIfEquals("${match}", "x") {
      exec(
        pause(300 millis)
          .exec(http("Typeahead")
            .get(typeaheadPath + "${part12}" + "&limit=" + limit)
            .check(jsonPath("$..uprn").findAll.saveAs("uprns2")))
          .foreach("${uprns2}", "uprn2") {
            exec(newsession => {
              println(newsession("part12").as[String])
              val uprnString1 = newsession("uprn").as[String]
              val uprnString2 = newsession("uprn2").as[String]
              val hit = if (uprnString1 == uprnString2) " => HIT (16 chars)" else "x"
              val newsession13 = if (hit == "x") newsession else newsession.set("match", hit)
              println(uprnString1 + ":" + uprnString2 + hit)
              newsession13
            })
          }
      )
    }
    .doIfEquals("${match}", "x") {
      exec(
        pause(300 millis)
          .exec(http("Typeahead")
            .get(typeaheadPath + "${part13}" + "&limit=" + limit)
            .check(jsonPath("$..uprn").findAll.saveAs("uprns2")))
          .foreach("${uprns2}", "uprn2") {
            exec(newsession => {
              println(newsession("part12").as[String])
              val uprnString1 = newsession("uprn").as[String]
              val uprnString2 = newsession("uprn2").as[String]
              val hit = if (uprnString1 == uprnString2) " => HIT (17 chars)" else "x"
              val newsession14 = if (hit == "x") newsession else newsession.set("match", hit)
              println(uprnString1 + ":" + uprnString2 + hit)
              newsession14
            })
          }
      )
    }
    .doIfEquals("${match}", "x") {
      exec(
        pause(300 millis)
          .exec(http("Typeahead")
            .get(typeaheadPath + "${part14}" + "&limit=" + limit)
            .check(jsonPath("$..uprn").findAll.saveAs("uprns2")))
          .foreach("${uprns2}", "uprn2") {
            exec(newsession => {
              println(newsession("part12").as[String])
              val uprnString1 = newsession("uprn").as[String]
              val uprnString2 = newsession("uprn2").as[String]
              val hit = if (uprnString1 == uprnString2) " => HIT (18 chars)" else "x"
              val newsession15 = if (hit == "x") newsession else newsession.set("match", hit)
              println(uprnString1 + ":" + uprnString2 + hit)
              newsession15
            })
          }
      )
    }
    .doIfEquals("${match}", "x") {
      exec(
        pause(300 millis)
          .exec(http("Typeahead")
            .get(typeaheadPath + "${part15}" + "&limit=" + limit)
            .check(jsonPath("$..uprn").findAll.saveAs("uprns2")))
          .foreach("${uprns2}", "uprn2") {
            exec(newsession => {
              println(newsession("part12").as[String])
              val uprnString1 = newsession("uprn").as[String]
              val uprnString2 = newsession("uprn2").as[String]
              val hit = if (uprnString1 == uprnString2) " => HIT (19 chars)" else "x"
              val newsession16 = if (hit == "x") newsession else newsession.set("match", hit)
              println(uprnString1 + ":" + uprnString2 + hit)
              newsession16
            })
          }
      )
    }
    .doIfEquals("${match}", "x") {
      exec(
        pause(300 millis)
          .exec(http("Typeahead")
            .get(typeaheadPath + "${part16}" + "&limit=" + limit)
            .check(jsonPath("$..uprn").findAll.saveAs("uprns2")))
          .foreach("${uprns2}", "uprn2") {
            exec(newsession => {
              println(newsession("part12").as[String])
              val uprnString1 = newsession("uprn").as[String]
              val uprnString2 = newsession("uprn2").as[String]
              val hit = if (uprnString1 == uprnString2) " => HIT (20 chars)" else "x"
              val newsession17 = if (hit == "x") newsession else newsession.set("match", hit)
              println(uprnString1 + ":" + uprnString2 + hit)
              newsession17
            })
          }
      )
    }

  // give up if no match found at 20 characters

  // run as a single user with a pause between each address.
  // .repeat does not work around scenario with multiple chains, so this does 20 addresses with a time limit
  setUp(scn.inject(
    atOnceUsers(1), nothingFor(10 seconds),
    atOnceUsers(1), nothingFor(10 seconds),
    atOnceUsers(1), nothingFor(10 seconds),
    atOnceUsers(1), nothingFor(10 seconds),
    atOnceUsers(1), nothingFor(10 seconds),
    atOnceUsers(1), nothingFor(10 seconds),
    atOnceUsers(1), nothingFor(10 seconds),
    atOnceUsers(1), nothingFor(10 seconds),
    atOnceUsers(1), nothingFor(10 seconds),
    atOnceUsers(1), nothingFor(10 seconds),
    atOnceUsers(1), nothingFor(10 seconds),
    atOnceUsers(1), nothingFor(10 seconds),
    atOnceUsers(1), nothingFor(10 seconds),
    atOnceUsers(1), nothingFor(10 seconds),
    atOnceUsers(1), nothingFor(10 seconds),
    atOnceUsers(1), nothingFor(10 seconds),
    atOnceUsers(1), nothingFor(10 seconds),
    atOnceUsers(1), nothingFor(10 seconds),
    atOnceUsers(1), nothingFor(10 seconds),
    atOnceUsers(1), nothingFor(10 seconds))).protocols(httpProtocol).maxDuration(duration minutes)

}


