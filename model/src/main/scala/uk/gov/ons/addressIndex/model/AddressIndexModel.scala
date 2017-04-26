package uk.gov.ons.addressIndex.model

import java.util.UUID

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.config.QueryParamsConfig

case class AddressIndexUPRNRequest(
  uprn: BigInt,
  id: UUID
)

case class AddressIndexSearchRequest(
  input: String,
  limit: String,
  offset: String,
  id: UUID
)


/**
  * The body of the request that is sent to the bulk api endpoint
  * @param addresses collection of addresses that will be queried
  * @param config optional configuration overwrite (prefer the use
  *               of env vars or a new pull request if you want to
  *               do more permanent configuration values)
  */
case class BulkBody(addresses: Seq[BulkQuery], config: Option[QueryParamsConfig])

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

