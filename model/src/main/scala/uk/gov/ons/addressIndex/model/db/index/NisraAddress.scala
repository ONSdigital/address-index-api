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
  paoText: String,
  paoStartNumber: String,
  paoStartSuffix: String,
  paoEndNumber: String,
  paoEndSuffix: String,
  saoText: String,
  saoStartNumber: String,
  saoStartSuffix: String,
  saoEndNumber: String,
  saoEndSuffix: String,
  thoroughfare: String,
  altThoroughfare: String,
  dependentThoroughfare: String,
  locality: String,
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
  addressStatus: String,
  buildingStatus: String,
  localCouncil: String,
  LGDCode: String,
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
    val paoText: String = "paoText"
    val paoStartNumber: String = "paoStartNumber"
    val paoStartSuffix: String = "paoStartSuffix"
    val paoEndNumber: String = "paoEndNumber"
    val paoEndSuffix: String = "paoEndSuffix"
    val saoText: String = "saoText"
    val saoStartNumber: String = "saoStartNumber"
    val saoStartSuffix: String = "saoStartSuffix"
    val saoEndNumber: String = "saoEndNumber"
    val saoEndSuffix: String = "saoEndSuffix"
    val thoroughfare: String = "thoroughfare"
    val altThoroughfare: String = "altThoroughfare"
    val dependentThoroughfare: String = "dependentThoroughfare"
    val locality: String = "locality"
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
    val mixedNisra: String = "mixedNisra"
    val addressStatus: String = "addressStatus"
    val buildingStatus: String = "buildingStatus"
    val localCouncil: String = "localCouncil"
    val LGDCode: String = "LGDCode"
  }

  def fromEsMap (nisra: Map[String, Any]): NisraAddress = {
    val filteredNisra = nisra.filter { case (_, value) => value != null && value !="" }

    val matchLocationRegex = """-?\d+(?:\.\d*)?(?:[E][+\-]?\d+)?""".r
    val location = filteredNisra.getOrElse(Fields.location, "0,0").toString
    val Array(longitude, latitude) = Try(matchLocationRegex.findAllIn(location).toArray).getOrElse(Array("0", "0"))

    NisraAddress (
      organisationName = filteredNisra.getOrElse(Fields.organisationName, "").toString,
      subBuildingName = filteredNisra.getOrElse(Fields.subBuildingName, "").toString,
      buildingName = filteredNisra.getOrElse(Fields.buildingName, "").toString,
      buildingNumber = filteredNisra.getOrElse(Fields.buildingNumber, "").toString,
      paoText = filteredNisra.getOrElse(Fields.paoText, "").toString,
      paoStartNumber = filteredNisra.getOrElse(Fields.paoStartNumber, "").toString,
      paoStartSuffix = filteredNisra.getOrElse(Fields.paoStartSuffix, "").toString,
      paoEndNumber = filteredNisra.getOrElse(Fields.paoEndNumber, "").toString,
      paoEndSuffix = filteredNisra.getOrElse(Fields.paoEndSuffix, "").toString,
      saoText = filteredNisra.getOrElse(Fields.saoText, "").toString,
      saoStartNumber = filteredNisra.getOrElse(Fields.saoStartNumber, "").toString,
      saoStartSuffix =filteredNisra.getOrElse(Fields.saoStartSuffix, "").toString,
      saoEndNumber = filteredNisra.getOrElse(Fields.saoEndNumber, "").toString,
      saoEndSuffix = filteredNisra.getOrElse(Fields.saoEndSuffix, "").toString,
      thoroughfare = filteredNisra.getOrElse(Fields.thoroughfare, "").toString,
      altThoroughfare = filteredNisra.getOrElse(Fields.altThoroughfare, "").toString,
      dependentThoroughfare = filteredNisra.getOrElse(Fields.dependentThoroughfare, "").toString,
      locality = filteredNisra.getOrElse(Fields.locality, "").toString,
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
      addressStatus = filteredNisra.getOrElse(Fields.addressStatus, "").toString,
      buildingStatus = filteredNisra.getOrElse(Fields.buildingStatus, "").toString,
      localCouncil = filteredNisra.getOrElse(Fields.localCouncil, "").toString,
      LGDCode = filteredNisra.getOrElse(Fields.LGDCode, "").toString,
      mixedNisra = filteredNisra.getOrElse(Fields.mixedNisra, "").toString
    )
  }
}