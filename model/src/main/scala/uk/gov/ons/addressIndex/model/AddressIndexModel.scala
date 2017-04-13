package uk.gov.ons.addressIndex.model

import java.util.UUID

import play.api.libs.json.{Format, Json}

case class AddressIndexUPRNRequest(
  uprn: BigInt,
  id: UUID,
  apiKey: String
)

case class AddressIndexSearchRequest(
  input: String,
  limit: String,
  offset: String,
  id: UUID,
  apiKey: String
)


//mini model for input post body
case class BulkBody(addresses: Seq[BulkQuery], apiKey: String)
object BulkBody {
  implicit lazy val fmt: Format[BulkBody] = Json.format[BulkBody]
}

case class BulkQuery(id: String, address: String)
object BulkQuery {
  implicit lazy val fmt: Format[BulkQuery] = Json.format[BulkQuery]
}

