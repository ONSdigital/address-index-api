package uk.gov.ons.addressIndex.model.server.response.bulk

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.db.BulkAddress
import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseAddress, AddressResponseAddressNonIDS}

/**
  *
  * Container for relevant information on each of the address result in bulk search
  *
  * @param id                      address's id provided in the input
  * @param inputAddress            input address
  * @param uprn                    found address' uprn
  * @param matchedFormattedAddress formatted found address
  * @param matchedAddress          found address
  * @param tokens                  tokens into which the input address was split
  * @param confidenceScore         resulting address score
  * @param underlyingScore
  */
case class AddressBulkResponseAddress(id: String,
                                      inputAddress: String,
                                      uprn: String,
                                      parentUprn: String,
                                      udprn: String,
                                      matchedFormattedAddress: String,
                                      matchedAddress: Option[AddressResponseAddressNonIDS],
                                      tokens: Map[String, String],
                                      confidenceScore: Double,
                                      underlyingScore: Float,
                                      recommendationCode: String,
                                      matchtype:String)

object AddressBulkResponseAddress {
  implicit lazy val addressBulkResponseAddressFormat: Format[AddressBulkResponseAddress] = Json.format[AddressBulkResponseAddress]

  def fromBulkAddress(bulkAddress: BulkAddress,
                      addressResponseAddress: AddressResponseAddressNonIDS,
                      includeFullAddress: Boolean
                     ): AddressBulkResponseAddress = AddressBulkResponseAddress(
    id = bulkAddress.id,
    inputAddress = bulkAddress.inputAddress,
    uprn = bulkAddress.hybridAddress.uprn,
    parentUprn = bulkAddress.hybridAddress.parentUprn,
    udprn = bulkAddress.hybridAddress.paf.headOption.map(_.udprn).getOrElse(""),
    matchedFormattedAddress = addressResponseAddress.formattedAddress,
    matchedAddress = if (includeFullAddress) Some(addressResponseAddress) else None,
    tokens = bulkAddress.tokens,
    confidenceScore = addressResponseAddress.confidenceScore,
    underlyingScore = bulkAddress.hybridAddress.score,
    recommendationCode = "",
    matchtype = ""
  )
}
