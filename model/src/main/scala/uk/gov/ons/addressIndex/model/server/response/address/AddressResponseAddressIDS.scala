package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.db.index.{HybridAddress, NationalAddressGazetteerAddress}

/**
  * Contains address information retrieved in ES (PAF or NAG)
  *
  * @param uprn             uprn
  * @param formattedAddress cannonical address form
  * @param paf              optional, information from Paf index
  * @param nag              optional, information from Nag index
  * @param nisra            optional, information from Nisra index
  * @param underlyingScore  score from elastic search
  *
  */
case class AddressResponseAddressIDS(onsAddressId: String,
                                     confidenceScore: Double
                                 )

object AddressResponseAddressIDS {
  implicit lazy val addressResponseAddressIDSFormat: Format[AddressResponseAddressIDS] = Json.format[AddressResponseAddressIDS]

  /**
    * Transforms hybrid object returned by ES into an Address that will be in the json response
    *
    * @param other HybridAddress from ES
    * @return
    */
  def fromHybridAddress(other: HybridAddress, verbose: Boolean): AddressResponseAddressIDS = {

    AddressResponseAddressIDS(
      onsAddressId = other.onsAddressId,
      confidenceScore = 100D,
    )
  }
}




