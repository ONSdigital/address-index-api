package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.db.index.{AuxiliaryAddress, AuxiliaryAddressLocation}

/**
 * Auxiliary data on the address
 *
 * @param lat             lat
 * @param lon             lon
 *
 */
case class AddressResponseAuxiliaryAddressLocation(lat: String, lon: String)

object AddressResponseAuxiliaryAddressLocation {

  implicit lazy val addressResponseAuxiliaryAddressLocationFormat: Format[AddressResponseAuxiliaryAddressLocation] = Json.format[AddressResponseAuxiliaryAddressLocation]

  def fromAuxiliaryAddressLocation(other: AuxiliaryAddressLocation): AddressResponseAuxiliaryAddressLocation =
    AddressResponseAuxiliaryAddressLocation(
      other.lat,
      other.lon
    )
}

/**
  * Auxiliary data on the address
  *
  * @param uprn                         uprn
  * @param organisationName             organisationName
  * @param subBuildingName              subBuildingName
  * @param buildingName                 buildingName
  * @param buildingNumber               buildingNumber
  * @param paoStartNumber               paoStartNumber
  * @param paoStartSuffix               paoStartSuffix
  * @param paoEndNumber                 paoEndNumber
  * @param saoStartSuffix               saoStartSuffix
  * @param saoEndSuffix                 saoEndSuffix
  * @param streetName                   streetName
  * @param locality                     locality
  * @param townName                     townName
  * @param location                     location
  * @param addressLevel                 addressLevel
  * @param addressAll                   addressAll
  * @param addressLine1                 addressLine1
  * @param addressLine2                 addressLine2
  * @param addressLine3                 addressLine3
  */
case class AddressResponseAuxiliary(
                                     uprn: String,
                                     organisationName: String,
                                     subBuildingName: String,
                                     buildingName: String,
                                     buildingNumber: String,
                                     paoStartNumber: String,
                                     paoStartSuffix: String,
                                     paoEndNumber: String,
                                     saoStartSuffix: String,
                                     saoEndSuffix: String,
                                     streetName: String,
                                     locality: String,
                                     townName: String,
                                     location: AddressResponseAuxiliaryAddressLocation,
                                     addressLevel: String,
                                     addressAll: String,
                                     addressLine1: String,
                                     addressLine2: String,
                                     addressLine3: String
                                  )

object AddressResponseAuxiliary {

  implicit lazy val addressResponseAuxiliaryFormat: Format[AddressResponseAuxiliary] = Json.format[AddressResponseAuxiliary]

  def fromAuxiliaryAddress(other: AuxiliaryAddress): AddressResponseAuxiliary =
    AddressResponseAuxiliary(
      other.uprn,
      other.organisationName,
      other.subBuildingName,
      other.buildingName,
      other.buildingNumber,
      other.paoStartNumber,
      other.paoStartSuffix,
      other.paoEndNumber,
      other.saoStartSuffix,
      other.saoEndSuffix,
      other.streetName,
      other.locality,
      other.townName,
      AddressResponseAuxiliaryAddressLocation.fromAuxiliaryAddressLocation(other.location),
      other.addressLevel,
      other.addressAll,
      other.addressLine1,
      other.addressLine2,
      other.addressLine3
    )
}