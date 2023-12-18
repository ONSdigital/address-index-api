package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.db.index._

/**
  * Contains address information retrieved in ES (PAF or NAG)
  *
  * @param uprn             uprn
  * @param formattedAddress cannonical address form
  * @param paf              optional, information from Paf index
  * @param nag              optional, information from Nag index
  * @param underlyingScore  score from elastic search
  *
  */
case class AddressResponseAddressNonIDS(uprn: String,
                                        parentUprn: String,
                                        relatives: Option[Seq[AddressResponseRelative]],
                                        crossRefs: Option[Seq[AddressResponseCrossRef]],
                                        formattedAddress: String,
                                        formattedAddressNag: String,
                                        formattedAddressPaf: String,
                                        welshFormattedAddressNag: String,
                                        welshFormattedAddressPaf: String,
                                        highlights: Option[AddressResponseHighlight],
                                        paf: Option[AddressResponsePaf],
                                        nag: Option[Seq[AddressResponseNag]],
                                        geo: Option[AddressResponseGeo],
                                        classificationCode: String,
                                        countryCode:String,
                                        lpiLogicalStatus: String,
                                        confidenceScore: Double,
                                        underlyingScore: Float
                                      )

object AddressResponseAddressNonIDS {
  implicit lazy val addressResponseAddressNonIDSFormat: Format[AddressResponseAddressNonIDS] = Json.format[AddressResponseAddressNonIDS]

  def fromAddress(addressIn: AddressResponseAddress): AddressResponseAddressNonIDS = {
    new AddressResponseAddressNonIDS(
      uprn = addressIn.uprn,
      parentUprn = addressIn.parentUprn,
      relatives = addressIn.relatives,
      crossRefs = addressIn.crossRefs,
      formattedAddress = addressIn.formattedAddress,
      formattedAddressNag = addressIn.formattedAddressNag,
      formattedAddressPaf = addressIn.formattedAddressPaf,
      welshFormattedAddressNag = addressIn.welshFormattedAddressNag,
      welshFormattedAddressPaf = addressIn.welshFormattedAddressPaf,
      highlights=  addressIn.highlights,
      paf = addressIn.paf,
      nag = addressIn.nag,
      geo = addressIn.geo,
      classificationCode = addressIn.classificationCode,
      countryCode = addressIn.countryCode,
      lpiLogicalStatus = addressIn.lpiLogicalStatus,
      confidenceScore = addressIn.confidenceScore,
      underlyingScore = addressIn.underlyingScore
    )
  }


}



