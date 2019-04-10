package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.db.index.NisraAddress

object AddressResponseNisra {
  implicit lazy val addressResponseNisraFormat: Format[AddressResponseNisra] = Json.format[AddressResponseNisra]

  def fromNisraAddress(other: NisraAddress): AddressResponseNisra =
    AddressResponseNisra(
      other.organisationName,
      other.subBuildingName,
      other.buildingName,
      other.buildingNumber,
      other.thoroughfare,
      other.altThoroughfare,
      other.dependentThoroughfare,
      other.locality,
      other.townland,
      other.townName,
      other.postcode,
      other.uprn,
      other.classificationCode,
      other.udprn,
      other.creationDate,
      other.commencementDate,
      other.archivedDate,
      other.nisraAll,
      other.mixedNisra
    )
}

/**
  * NISRA data on the address
  *
  * @param organisationName             organisationName
  * @param subBuildingName              subBuildingName
  * @param buildingName                 buildingName
  * @param buildingNumber               buildingNumber
  * @param thoroughfare                 thoroughfare
  * @param altThoroughfare              altThoroughfare
  * @param dependentThoroughfare        dependentThoroughfare
  * @param locality                     locality
  * @param townland                     townland
  * @param townName                     townName
  * @param postcode                     postcode
  * @param uprn                         uprn
  * @param classificationCode           classificationCode
  * @param udprn                        udprn
  * @param creationDate                 creationDate
  * @param commencementDate             commencementDate
  * @param archivedDate                 archivedDate
  * @param nisraAll                     nisraAll
  * @param mixedNisra                   mixedNisra
  */
case class AddressResponseNisra(
                                 organisationName: String,
                                 subBuildingName: String,
                                 buildingName: String,
                                 buildingNumber: String,
                                 thoroughfare: String,
                                 altThoroughfare: String,
                                 dependentThoroughfare: String,
                                 locality: String,
                                 townland: String,
                                 townName: String,
                                 postcode: String,
                                 uprn: String,
                                 classificationCode: String,
                                 udprn: String,
                                 creationDate: String,
                                 commencementDate: String,
                                 archivedDate: String,
                                 nisraAll: String,
                                 mixedNisra: String
                             )
