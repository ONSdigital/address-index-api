package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

/**
  * Contains relevant, to the address request, data
  *
  * @param tokens    address decomposed into relevant parts (building number, city, street, etc.)
  * @param addresses found addresses
  * @param limit     max number of found addresses
  * @param offset    offset of found addresses (for pagination)
  * @param total     total number of found addresses
  */
case class AddressBySearchResponse(tokens: Map[String, String],
                                   addresses: Seq[AddressResponseAddress],
                                   filter: String,
                                   historical: Boolean,
                                   epoch: String,
                                   rangekm: String,
                                   latitude: String,
                                   longitude: String,
                                   startDate: String,
                                   endDate: String,
                                   limit: Int,
                                   offset: Int,
                                   total: Long,
                                   sampleSize: Long,
                                   maxScore: Double,
                                   matchthreshold: Float,
                                   verbose: Boolean)

object AddressBySearchResponse {
  implicit lazy val addressBySearchResponseFormat: Format[AddressBySearchResponse] = Json.format[AddressBySearchResponse]
}
