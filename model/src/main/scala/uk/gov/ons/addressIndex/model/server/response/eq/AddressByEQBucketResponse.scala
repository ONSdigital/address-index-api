package uk.gov.ons.addressIndex.model.server.response.eq

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.server.response.address.AddressResponseAddressBucketEQ

/**
  * Contains relevant, to the address request, data
  *
  * @param postcode postcode from query
  * @param addresses found addresses
  * @param filter classification filter
  * @param epoch which AB Epoch to use
  * @param limit max number of found addresses
  * @param offset offset of found addresses (for pagination)
  * @param total total number of found addresses
  * @param maxScore the max score
  */
case class AddressByEQBucketResponse(postcode: String,
                                     streetname: String,
                                     townname: String,
                                     addresses: Seq[AddressResponseAddressBucketEQ],
                                     filter: String,
                                     epoch: String,
                                     limit: Int,
                                     offset: Int,
                                     total: Long,
                                     maxScore: Double)
object AddressByEQBucketResponse {
  implicit lazy val addressByEQBucketResponseFormat: Format[AddressByEQBucketResponse] = Json.format[AddressByEQBucketResponse]
}

