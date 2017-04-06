package uk.gov.ons.addressIndex.model

import java.util.UUID

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.db.BulkAddress
import uk.gov.ons.addressIndex.model.server.response.AddressResponseAddress
import uk.gov.ons.addressIndex.parsers.Tokens

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


//mini model for input post body
case class BulkBody(addresses: Seq[BulkQuery])
object BulkBody {
  implicit lazy val fmt: Format[BulkBody] = Json.format[BulkBody]
}

case class BulkQuery(id: String, address: String)
object BulkQuery {
  implicit lazy val fmt: Format[BulkQuery] = Json.format[BulkQuery]
}

