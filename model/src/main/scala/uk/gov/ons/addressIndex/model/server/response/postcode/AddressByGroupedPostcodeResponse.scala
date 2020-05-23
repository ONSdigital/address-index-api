package uk.gov.ons.addressIndex.model.server.response.postcode

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.db.index._

/**
  * Contains relevant, to the address request, data
  *
  * @param partpostcode  postcode from query
  * @param postcodes aggregation output of postcodes matching partial
  * @param limit     max number of found addresses
  * @param offset    offset of found addresses (for pagination)
  * @param total     total number of found addresses
  */
case class AddressByGroupedPostcodeResponse(partpostcode: String,
                                            postcodes: Seq[AddressResponsePostcodeGroup],
                                            filter: String,
                                            historical: Boolean,
                                            epoch: String,
                                            limit: Int,
                                            offset: Int,
                                            total: Long,
                                            maxScore: Double,
                                            verbose: Boolean)

object AddressByGroupedPostcodeResponse {
  implicit lazy val addressByGroupedPostcodeResponseFormat: Format[AddressByGroupedPostcodeResponse] = Json.format[AddressByGroupedPostcodeResponse]
}


