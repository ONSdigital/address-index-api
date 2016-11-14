package uk.gov.ons.addressIndex.client

import java.util.concurrent.TimeUnit
import javax.inject.Inject

import play.api.Logger
import play.api.libs.ws.{WSClient, WSResponse}
import uk.gov.ons.addressIndex.conf.OnsFrontendConfiguration
import uk.gov.ons.addressIndex.model.{Address, BulkMatchResponse, SingleMatchResponse}

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by amits on 04/07/2016.
  */
class AddressApiClient @Inject()(
    wsClient: WSClient,
    configuration: OnsFrontendConfiguration)(implicit exec: ExecutionContext) {
import uk.gov.ons.addressIndex.model.JSONImplicit.addressRead
  val logger = Logger("app-log")

  def singleMatch(inputAddress: String): Future[SingleMatchResponse] = {
    logger.info("Entering singleMatch Action")
    wsClient
      .url(s"${configuration.onsAddressApiUri}/search")
      .withQueryString(parameters = ("address", inputAddress))
      .get() map { response =>
      logger.debug("Trying to match response")
      response.status match {
        case 200 => parseSingleMatchResponse(response)
        case _ =>
          throw new Exception("Unexpected response from the API" + response)
      }
    }
  }

  private def parseSingleMatchResponse(
      response: WSResponse): SingleMatchResponse = {
    logger.debug("Trying to parse single match response")
    val responseJson = response.json
    logger.debug("Trying to return a SingleMatchResponse response")
    SingleMatchResponse(
        totalHits = (responseJson \ "numberOfHits").as[Int],
        candidate = (responseJson \ "addresses").as[List[Address]]
    )
  }

  def multipleMatch(fileName: String): Future[BulkMatchResponse] = {
    logger.info("Entering multipleMatch API Client")
    wsClient
      .url(s"${configuration.onsAddressApiUri}/bulk")
      .withRequestTimeout(
          Duration(configuration.onsApiCallTimeout, TimeUnit.MILLISECONDS))
      .withQueryString(parameters = ("fileName", fileName),
                       ("fileLocation", configuration.onsUploadFileLocation))
      .get() map { response =>
      logger.debug("Trying to match Multiple match response")
      response.status match {
        case 200 => parseMultiMatchResponse(response)
        case _ =>
          throw new Exception("Unexpected response from the API" + response)
      }
    }
  }

  private def parseMultiMatchResponse(
      bulkResponse: WSResponse): BulkMatchResponse = {
    logger.debug("Trying to parse multiple match response")
    val bulkResponseJson = bulkResponse.json
    logger.debug("Trying to return a BulkMatchResponse response")
    BulkMatchResponse(
        matchFound = (bulkResponseJson \ "matchFound").asOpt[Int],
        possibleMatches = (bulkResponseJson \ "possibleMatches").asOpt[Int],
        noMatch = (bulkResponseJson \ "noMatch").asOpt[Int],
        totalNumberOfAddresses =
          (bulkResponseJson \ "totalNumberOfAddresses").asOpt[Int]
    )
  }
}
