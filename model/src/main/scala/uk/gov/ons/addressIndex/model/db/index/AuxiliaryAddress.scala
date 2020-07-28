package uk.gov.ons.addressIndex.model.db.index

import play.api.libs.json.{Format, Json}

/**
 * Auxiliary Address Location DTO
 */
case class AuxiliaryAddressLocation(
  lat: String,
  lon: String
)

object AuxiliaryAddressLocation {

  implicit lazy val auxiliaryAddressLocationFormat: Format[AuxiliaryAddressLocation] = Json.format[AuxiliaryAddressLocation]

  object Fields {
    /**
     * Document Fields
     */
    val lat: String = "lat"
    val lon: String = "lon"
  }
}

/**
  * Auxiliary Address DTO
  */
case class AuxiliaryAddress(
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
  location: AuxiliaryAddressLocation,
  addressLevel: String,
  addressAll: String,
  addressLine1: String,
  addressLine2: String,
  addressLine3: String
 )

object AuxiliaryAddress {

  implicit lazy val auxiliaryAddressFormat: Format[AuxiliaryAddress] = Json.format[AuxiliaryAddress]

  object Fields {
    /**
     * Document Fields
     */
    val uprn: String = "uprn"
    val organisationName: String = "organisationName"
    val subBuildingName: String = "subBuildingName"
    val buildingName: String = "buildingName"
    val buildingNumber: String = "buildingNumber"
    val paoStartNumber: String = "paoStartNumber"
    val paoStartSuffix: String = "paoStartSuffix"
    val paoEndNumber: String = "paoEndNumber"
    val saoStartSuffix: String = "saoStartSuffix"
    val saoEndSuffix: String = "saoEndSuffix"
    val streetName: String = "streetName"
    val locality: String = "locality"
    val townName: String = "townName"
    val location: String = "location"
    val addressLevel: String = "addressLevel"
    val addressAll: String = "addressAll"
    val addressLine1: String = "addressLine1"
    val addressLine2: String = "addressLine2"
    val addressLine3: String = "addressLine3"
  }

  def fromEsMap(aux: Map[String, Any]): AuxiliaryAddress = {

    val filteredAux = aux.filter { case (_, value) => value != null && value != "" }

    val location = filteredAux.getOrElse(Fields.location, Map.empty[String, Any]).asInstanceOf[Map[String, Any]]

    AuxiliaryAddress(
      uprn = filteredAux.getOrElse(Fields.uprn, "").toString,
      organisationName = filteredAux.getOrElse(Fields.organisationName, "").toString,
      subBuildingName = filteredAux.getOrElse(Fields.subBuildingName, "").toString,
      buildingName = filteredAux.getOrElse(Fields.buildingName, "").toString,
      buildingNumber = filteredAux.getOrElse(Fields.buildingNumber, "").toString,
      paoStartNumber = filteredAux.getOrElse(Fields.paoStartNumber, "").toString,
      paoStartSuffix = filteredAux.getOrElse(Fields.paoStartSuffix, "").toString,
      paoEndNumber = filteredAux.getOrElse(Fields.paoEndNumber, "").toString,
      saoStartSuffix = filteredAux.getOrElse(Fields.saoStartSuffix, "").toString,
      saoEndSuffix = filteredAux.getOrElse(Fields.saoEndSuffix, "").toString,
      streetName = filteredAux.getOrElse(Fields.streetName, "").toString,
      locality = filteredAux.getOrElse(Fields.locality, "").toString,
      townName = filteredAux.getOrElse(Fields.townName, "").toString,
      location =  AuxiliaryAddressLocation(
        location.getOrElse(AuxiliaryAddressLocation.Fields.lat, "").toString,
        location.getOrElse(AuxiliaryAddressLocation.Fields.lon, "").toString
      ),
      addressLevel = filteredAux.getOrElse(Fields.addressLevel, "").toString,
      addressAll = filteredAux.getOrElse(Fields.addressAll, "").toString,
      addressLine1 = filteredAux.getOrElse(Fields.addressLine1, "").toString,
      addressLine2 = filteredAux.getOrElse(Fields.addressLine2, "").toString,
      addressLine3 = filteredAux.getOrElse(Fields.addressLine3, "").toString
    )
  }
}




