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

case class BulkItem(
  id: String,
  inputAddress: String,
  matchedFormattedAddress: String,
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
  score: Float
)
object BulkItem {
  implicit lazy val fmt: Format[BulkItem] = Json.format[BulkItem]

  def fromBulkAddress(bulkAddress: BulkAddress): BulkItem =
    BulkItem(
      inputAddress = bulkAddress.inputAddress,
      // This will need to be fixed in the PR where we adapt the response model to the one
      // of the single match request
      matchedFormattedAddress = AddressResponseAddress.fromHybridAddress(bulkAddress.hybridAddress).formattedAddressNag,
      id = bulkAddress.id,
      organisationName = bulkAddress.tokens.getOrElse(Tokens.organisationName, ""),
      departmentName = bulkAddress.tokens.getOrElse(Tokens.departmentName, ""),
      subBuildingName = bulkAddress.tokens.getOrElse(Tokens.subBuildingName, ""),
      buildingName = bulkAddress.tokens.getOrElse(Tokens.buildingName, ""),
      buildingNumber = bulkAddress.tokens.getOrElse(Tokens.buildingNumber, ""),
      streetName = bulkAddress.tokens.getOrElse(Tokens.streetName, ""),
      locality = bulkAddress.tokens.getOrElse(Tokens.locality, ""),
      townName = bulkAddress.tokens.getOrElse(Tokens.townName, ""),
      postcode = bulkAddress.tokens.getOrElse(Tokens.postcode, ""),
      uprn = bulkAddress.hybridAddress.uprn,
      score = bulkAddress.hybridAddress.score
    )
}

//mini model for output
case class BulkResp(resp: Seq[BulkItem])
object BulkResp {
  implicit lazy val fmt: Format[BulkResp] = Json.format[BulkResp]
}

