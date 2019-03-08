package uk.gov.ons.addressIndex.demoui.utils

import play.api.Logger
import play.api.libs.json.{Format, Json}

/**
  * Fake Gateway
  */
object GatewaySimulator {

  val logger = Logger("GatewaySimulator")

  val successResponse = GatewayResponse(
    status = "200",
    errorCode = "",
    errorMessage = "",
    key = "ea15dc73-1991-46db-8c02-e3c483bf9e3e"
  )

  val authFailResponse = GatewayResponse(
    status = "401",
    errorCode = "Authentication Error",
    errorMessage = "Authentication failed. Invalid username or password.",
    key = ""
  )

  /**
    * Return an API key unless Mr. Robot is hacking us
    *
    * @param username
    * @param password
    * @return
    */
  def getApiKey(username: String, password: String): GatewayResponse = {
    logger.info(username + ":" + password)
    if (username == "MrRobot") authFailResponse else successResponse
  }

  case class GatewayResponse(status: String,
                             errorCode: String,
                             errorMessage: String,
                             key: String)

  object GatewayResponse {
    implicit lazy val GatewayResponseFormat: Format[GatewayResponse] = Json.format[GatewayResponse]
  }

}
