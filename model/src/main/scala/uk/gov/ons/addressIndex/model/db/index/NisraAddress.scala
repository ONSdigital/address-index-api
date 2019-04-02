package uk.gov.ons.addressIndex.model.db.index

import scala.util.Try

/**
  * NISRA Address DTO
  */
case class NisraAddress(
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
  postTown: String,
  easting: String,
  northing: String,
  creationDate: String,
  commencementDate: String,
  archivedDate: String,
  latitude: String,
  longitude: String,
  nisraAll: String,
  mixedNisra: String
  )

/**
  * NISRA Address DTO companion object that also contains implicits needed for Elastic4s
  */
object NisraAddress {

  object Fields {

    /**
      * Document Fields
      */
    val organisationName: String = "organisationName"
    val subBuildingName: String = "subBuildingName"
    val buildingName: String = "buildingName"
    val buildingNumber: String = "buildingNumber"
    val thoroughfare: String = "thoroughfare"
    val altThoroughfare: String = "altThoroughfare"
    val dependentThoroughfare: String = "dependentThoroughfare"
    val locality: String = "locality"
    val townland: String = "townland"
    val townName: String = "townName"
    val postcode: String = "postcode"
    val uprn: String = "uprn"
    val classificationCode: String = "classificationCode"
    val udprn: String = "udprn"
    val postTown: String = "postTown"
    val easting: String = "easting"
    val northing: String = "northing"
    val creationDate: String = "creationDate"
    val commencementDate: String = "commencementDate"
    val archivedDate: String = "archivedDate"
    val location: String = "location"
    val nisraAll: String = "nisraAll"
    val mixedNisra: String = "mixedNisra"
  }

  def fromEsMap (nisra: Map[String, Any]): NisraAddress = {
    val filteredNisra = nisra.filter { case (_, value) => value != null && value !="" }

    val matchLocationRegex = """-?\d+(?:\.\d*)?(?:[E][+\-]?\d+)?""".r
    val location = filteredNisra.getOrElse(Fields.location, "").toString
    val Array(longitude, latitude) = Try(matchLocationRegex.findAllIn(location).toArray).getOrElse(Array("0", "0"))

    NisraAddress (
      organisationName = filteredNisra.getOrElse(Fields.organisationName, "").toString,
      subBuildingName = filteredNisra.getOrElse(Fields.subBuildingName, "").toString,
      buildingName = filteredNisra.getOrElse(Fields.buildingName, "").toString,
      buildingNumber = filteredNisra.getOrElse(Fields.buildingNumber, "").toString,
      thoroughfare = filteredNisra.getOrElse(Fields.thoroughfare, "").toString,
      altThoroughfare = filteredNisra.getOrElse(Fields.altThoroughfare, "").toString,
      dependentThoroughfare = filteredNisra.getOrElse(Fields.dependentThoroughfare, "").toString,
      locality = filteredNisra.getOrElse(Fields.locality, "").toString,
      townland = filteredNisra.getOrElse(Fields.townland, "").toString,
      townName = filteredNisra.getOrElse(Fields.townName, "").toString,
      postcode = filteredNisra.getOrElse(Fields.postcode, "").toString,
      uprn = filteredNisra.getOrElse(Fields.uprn, "").toString,
      classificationCode = filteredNisra.getOrElse(Fields.classificationCode, "").toString,
      udprn = filteredNisra.getOrElse(Fields.udprn, "").toString,
      postTown = filteredNisra.getOrElse(Fields.postTown, "").toString,
      easting = filteredNisra.getOrElse(Fields.easting, "").toString,
      northing = filteredNisra.getOrElse(Fields.northing, "").toString,
      creationDate = filteredNisra.getOrElse(Fields.creationDate, "").toString,
      commencementDate = filteredNisra.getOrElse(Fields.commencementDate, "").toString,
      archivedDate = filteredNisra.getOrElse(Fields.archivedDate, "").toString,
      latitude = latitude,
      longitude = longitude,
      nisraAll = filteredNisra.getOrElse(Fields.nisraAll, "").toString,
      mixedNisra = filteredNisra.getOrElse(Fields.mixedNisra, "").toString
    )
  }
}