package uk.gov.ons.addressIndex.model

import java.util.UUID

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.config.QueryParamsConfig

case class AddressIndexUPRNRequest(
  uprn: BigInt,
  id: UUID,
  apiKey: String,
  historical: Boolean
)

case class AddressIndexSearchRequest(
  input: String,
  filter: String,
  historical: Boolean,
  matchthreshold: Int,
  rangekm: String,
  lat: String,
  lon: String,
  limit: String,
  offset: String,
  id: UUID,
  apiKey: String
)

case class AddressIndexPostcodeRequest(
  postcode: String,
  filter: String,
  historical: Boolean,
  limit: String,
  offset: String,
  id: UUID,
  apiKey: String
)

/**
  * The body of the request that is sent to the bulk api endpoint
  * @param addresses collection of addresses that will be queried
  * @param config optional configuration overwrite (prefer the use
  *               of env vars or a new pull request if you want to
  *               do more permanent configuration values)
  */
case class BulkBody(addresses: Seq[BulkQuery], config: Option[QueryParamsConfig] = None)

object BulkBody {
  implicit lazy val fmt: Format[BulkBody] = Json.format[BulkBody]
}

/**
  * An element in the collection of addresses in the bulk request
  * @param id the id of the address that will be returned with the
  *           result (used to join source table with results).
  *           This is not UPRN. Basic example is just line number.
  * @param address the input that will be queried to find corresponding
  *                addresses
  */
case class BulkQuery(id: String, address: String)

object BulkQuery {
  implicit lazy val fmt: Format[BulkQuery] = Json.format[BulkQuery]
}

/**
  * Specific request format where we directly provide tokens instead of the input
  * @param addresses collection of addresses (tokens instead of input) to be queried
  * @param config optional configuration that will overwrite current one
  */
case class BulkBodyDebug(addresses: Seq[BulkQueryDebug], config: Option[QueryParamsConfig] = None)

object BulkBodyDebug {
  implicit lazy val bulkQueryDebugFormat: Format[BulkQueryDebug] = Json.format[BulkQueryDebug]
  implicit lazy val bulkBodyDebugFormat: Format[BulkBodyDebug] = Json.format[BulkBodyDebug]
}

/**
  * An element in the collection of a bulk request, this time we have tokens
  * instead of text input
  * @param id the id of the address
  * @param tokens tokens to be queried (thus this bypasses the parser step)
  */
case class BulkQueryDebug(id: String, tokens: Map[String, String])
