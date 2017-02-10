package uk.gov.ons.addressIndex.model

import java.util.UUID
import play.api.libs.json.{Format, Json}

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

case class BulkItem(
  id: String,
  organisationName: String = "",
  departmentName: String = "",
  subBuildingName: String = "",
  buildingName: String = "",
  buildingNumber: String = "",
  streetName: String = "",
  locality: String = "",
  townName: String = "",
  postcode: String = "",
  uprn: String,
  score: Float,
  exceptionMessage: String
)
object BulkItem {
  implicit lazy val fmt: Format[BulkItem] = Json.format[BulkItem]
}

//mini model for output
case class BulkResp(resp: List[BulkItem])
object BulkResp {
  implicit lazy val fmt: Format[BulkResp] = Json.format[BulkResp]
}

