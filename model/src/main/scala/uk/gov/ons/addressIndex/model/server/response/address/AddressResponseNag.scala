package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.db.index.NationalAddressGazetteerAddress

/**
  * @param uprn              uprn
  * @param postcodeLocator   postcode
  * @param addressBasePostal Address Base Postal
  * @param usrn              ursn
  * @param lpiKey            lpi key
  * @param pao               PAO
  * @param sao               SAO
  * @param level             ground and first floor
  * @param officialFlag      Official Flag
  * @param logicalStatus     Logical Status
  * @param streetDescriptor  Street Descriptor
  * @param townName          Town Name
  * @param locality          Locality
  * @param organisation      Organisation
  * @param legalName         Legal name
  */
case class AddressResponseNag(uprn: String,
                              postcodeLocator: String,
                              addressBasePostal: String,
                              usrn: String,
                              lpiKey: String,
                              pao: AddressResponsePao,
                              sao: AddressResponseSao,
                              level: String,
                              officialFlag: String,
                              logicalStatus: String,
                              streetDescriptor: String,
                              townName: String,
                              locality: String,
                              organisation: String,
                              legalName: String,
                              localCustodianCode: String,
                              localCustodianName: String,
                              localCustodianGeogCode: String,
                              lpiEndDate: String,
                              lpiStartDate: String)

object AddressResponseNag {
  implicit lazy val addressResponseNagFormat: Format[AddressResponseNag] = Json.format[AddressResponseNag]

  def fromNagAddress(other: NationalAddressGazetteerAddress): AddressResponseNag = {
    AddressResponseNag(
      other.uprn,
      other.postcodeLocator,
      other.addressBasePostal,
      other.usrn,
      other.lpiKey,
      pao = AddressResponsePao(
        other.paoText,
        other.paoStartNumber,
        other.paoStartSuffix,
        other.paoEndNumber,
        other.paoEndSuffix
      ),
      sao = AddressResponseSao(
        other.saoText,
        other.saoStartNumber,
        other.saoStartSuffix,
        other.saoEndNumber,
        other.saoEndSuffix
      ),
      other.level,
      other.officialFlag,
      other.lpiLogicalStatus,
      other.streetDescriptor,
      other.townName,
      other.locality,
      other.organisation,
      other.legalName,
      other.localCustodianCode,
      other.localCustodianName,
      other.localCustodianGeogCode,
      other.lpiEndDate,
      other.lpiStartDate
    )
  }
}
