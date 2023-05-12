package uk.gov.ons.addressIndex.model.server.response.partialaddress

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.server.response.address.{AddressBySearchResponse, AddressResponseAddress}

/**
  * Contains relevant, to the address request, data
  *
  * @param input     input from query
  * @param addresses found addresses
  * @param limit     max number of found addresses
  * @param offset    offset of found addresses (for pagination)
  * @param total     total number of found addresses
  */
case class AddressByPartialAddressResponse(input: String,
                                           addresses: Seq[AddressResponseAddress],
                                           filter: String,
                                           fallback: Boolean,
                                           historical: Boolean,
                                           epoch: String,
                                           limit: Int,
                                           offset: Int,
                                           total: Long,
                                           maxScore: Double,
                                           verbose: Boolean,
                                           highlight: String,
                                           favourpaf: Boolean,
                                           favourwelsh: Boolean,
  //                                         includeauxiliarysearch: Boolean = false,
                                           eboost: Double,
                                           nboost: Double,
                                           sboost: Double,
                                           wboost: Double,
                                           timeout: Int)

object AddressByPartialAddressResponse {
  implicit lazy val addressByPartialAddressResponseFormat: Format[AddressByPartialAddressResponse] = Json.format[AddressByPartialAddressResponse]

def toAddressBySearchResponse(partResponse: AddressByPartialAddressResponse): AddressBySearchResponse = {

  new AddressBySearchResponse(
    tokens = Map("input" -> partResponse.input),
      addresses = partResponse.addresses,
      filter = partResponse.filter,
      historical = partResponse.historical,
      epoch  = partResponse.epoch,
      rangekm = "",
      latitude = "",
      longitude = "",
      limit  = partResponse.limit,
      offset  = partResponse.offset,
      total = partResponse.total,
      sampleSize = partResponse.limit * 2,
      maxScore = partResponse.maxScore,
      matchthreshold = 0F,
      verbose = partResponse.verbose,
      eboost = partResponse.eboost,
      nboost = partResponse.nboost,
      sboost = partResponse.sboost,
      wboost = partResponse.wboost,
      pafdefault = false
  )

}

}
