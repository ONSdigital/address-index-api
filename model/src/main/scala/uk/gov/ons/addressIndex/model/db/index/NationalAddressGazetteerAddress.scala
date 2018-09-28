package uk.gov.ons.addressIndex.model.db.index

import uk.gov.ons.addressIndex.model.server.response.address.AddressResponseCustodian
import uk.gov.ons.addressIndex.parsers.Tokens

import scala.util.Try

/**
  * NAG Address DTO
  */
case class NationalAddressGazetteerAddress (
  uprn: String,
  postcodeLocator: String,
  addressBasePostal: String,
  latitude: String,
  longitude: String,
  easting: String,
  northing: String,
  organisation: String,
  legalName: String,
  classificationCode: String,
  usrn: String,
  lpiKey: String,
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
  level: String,
  officialFlag: String,
  streetDescriptor: String,
  townName: String,
  locality: String,
  lpiLogicalStatus: String,
  blpuLogicalStatus: String,
  usrnMatchIndicator: String,
  parentUprn: String,
  streetClassification: String,
  multiOccCount: String,
  language: String,
  classScheme: String,
  localCustodianCode: String,
  localCustodianName: String,
  localCustodianGeogCode: String,
  rpc: String,
  nagAll: String,
  lpiEndDate: String,
  lpiStartDate: String,
  mixedNag: String
)

/**
  * NAF Address DTO companion object that also contains implicits needed for Elastic4s
  */
object NationalAddressGazetteerAddress {

  object Fields {

    /**
      * Document Fields
      */
    val uprn: String = "uprn"
    val postcodeLocator: String = "postcodeLocator"
    val addressBasePostal: String = "addressBasePostal"
    val easting: String = "easting"
    val northing: String = "northing"
    val organisation: String = "organisation"
    val legalName: String = "legalName"
    val classificationCode: String = "classificationCode"
    val usrn: String = "usrn"
    val lpiKey: String = "lpiKey"
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
    val level: String = "level"
    val officialFlag: String = "officialFlag"
    val streetDescriptor: String = "streetDescriptor"
    val townName: String = "townName"
    val locality: String = "locality"
    val lpiLogicalStatus: String = "lpiLogicalStatus"
    val blpuLogicalStatus: String = "blpuLogicalStatus"
    val usrnMatchIndicator: String = "usrnMatchIndicator"
    val parentUprn: String = "parentUprn"
    val streetClassification: String = "streetClassification"
    val multiOccCount: String = "multiOccCount"
    val location: String = "location"
    val language: String = "language"
    val classScheme: String = "classScheme"
    val localCustodianCode: String = "localCustodianCode"
    val localCustodianName: String = "localCustodianName"
    val localCustodianGeogCode: String = "localCustodianGeogCode"
    val rpc: String = "rpc"
    val nagAll: String = "nagAll"
    val lpiEndDate: String = "lpiEndDate"
    val lpiStartDate: String = "lpiStartDate"
    val mixedNag: String = "mixedNag"
  }

  object Languages {
    val english: String = "ENG"
    val welsh: String = "CYM"
  }

  def fromEsMap (nag: Map[String, Any]): NationalAddressGazetteerAddress = {

    val filteredNag = nag.filter{ case (_, value) => value != null }
    val matchLocationRegex = """-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?""".r
    val location = filteredNag.getOrElse(Fields.location, "").toString
    val Array(longitude, latitude) = Try(matchLocationRegex.findAllIn(location).toArray).getOrElse(Array("0", "0"))

    NationalAddressGazetteerAddress (
      uprn = filteredNag.getOrElse(Fields.uprn, "").toString,
      postcodeLocator = filteredNag.getOrElse(Fields.postcodeLocator, "").toString,
      addressBasePostal = filteredNag.getOrElse(Fields.addressBasePostal, "").toString,
      latitude = latitude,
      longitude = longitude,
      easting = filteredNag.getOrElse(Fields.easting, "").toString,
      northing = filteredNag.getOrElse(Fields.northing, "").toString,
      organisation = filteredNag.getOrElse(Fields.organisation, "").toString,
      legalName = filteredNag.getOrElse(Fields.legalName, "").toString,
      classificationCode = filteredNag.getOrElse(Fields.classificationCode, "").toString,
      usrn = filteredNag.getOrElse(Fields.usrn, "").toString,
      lpiKey = filteredNag.getOrElse(Fields.lpiKey, "").toString,
      paoText = filteredNag.getOrElse(Fields.paoText, "").toString,
      paoStartNumber = filteredNag.getOrElse(Fields.paoStartNumber, "").toString,
      paoStartSuffix = filteredNag.getOrElse(Fields.paoStartSuffix, "").toString,
      paoEndNumber = filteredNag.getOrElse(Fields.paoEndNumber, "").toString,
      paoEndSuffix = filteredNag.getOrElse(Fields.paoEndSuffix, "").toString,
      saoText = filteredNag.getOrElse(Fields.saoText, "").toString,
      saoStartNumber = filteredNag.getOrElse(Fields.saoStartNumber, "").toString,
      saoStartSuffix = filteredNag.getOrElse(Fields.saoStartSuffix, "").toString,
      saoEndNumber = filteredNag.getOrElse(Fields.saoEndNumber, "").toString,
      saoEndSuffix = filteredNag.getOrElse(Fields.saoEndSuffix, "").toString,
      level = filteredNag.getOrElse(Fields.level, "").toString,
      officialFlag = filteredNag.getOrElse(Fields.officialFlag, "").toString,
      streetDescriptor = filteredNag.getOrElse(Fields.streetDescriptor, "").toString,
      townName = filteredNag.getOrElse(Fields.townName, "").toString,
      locality = filteredNag.getOrElse(Fields.locality, "").toString,
      lpiLogicalStatus = filteredNag.getOrElse(Fields.lpiLogicalStatus, "").toString,
      blpuLogicalStatus = filteredNag.getOrElse(Fields.blpuLogicalStatus, "").toString,
      usrnMatchIndicator = filteredNag.getOrElse(Fields.usrnMatchIndicator, "").toString,
      parentUprn = filteredNag.getOrElse(Fields.parentUprn, "").toString,
      streetClassification = filteredNag.getOrElse(Fields.streetClassification, "").toString,
      multiOccCount = filteredNag.getOrElse(Fields.multiOccCount, "").toString,
      language = filteredNag.getOrElse(Fields.language, "").toString,
      classScheme = filteredNag.getOrElse(Fields.classScheme, "").toString,
      localCustodianCode = filteredNag.getOrElse(Fields.localCustodianCode, "").toString,
      localCustodianName = LocalCustodian.getLAName(filteredNag.getOrElse(Fields.localCustodianCode, "").toString),
      localCustodianGeogCode = LocalCustodian.getLACode(filteredNag.getOrElse(Fields.localCustodianCode, "").toString),
      rpc = filteredNag.getOrElse(Fields.rpc, "").toString,
      nagAll = filteredNag.getOrElse(Fields.nagAll, "").toString,
      lpiEndDate = filteredNag.getOrElse(Fields.lpiEndDate, "").toString,
      lpiStartDate = filteredNag.getOrElse(Fields.lpiStartDate, "").toString,
      mixedNag = filteredNag.getOrElse(Fields.mixedNag, "").toString
    )
  }
}

case class LocalCustodian (custodians: Map[String, AddressResponseCustodian])

object LocalCustodian {

  def emptyCustodian = new AddressResponseCustodian("","not found","not found","not found","not found","not found")

  def getCustodian (code:String) = custodians.getOrElse(code,emptyCustodian )

  def getCustName(code:String): String = getCustodian(code).custName
  def getCustCode(code:String): String = getCustodian(code).custCode
  def getLAName(code:String): String = getCustodian(code).laName
  def getLACode(code:String): String = getCustodian(code).laCode
  def getRegionOrCountryName(code:String): String = getCustodian(code).regName
  def getRegionOrCountryCode(code:String): String = getCustodian(code).regCode

  val custodians = Tokens.custodianList.map{custval =>
    (custval.split(",").lift(0).getOrElse(""),
      new AddressResponseCustodian(
      custval.split(",").lift(0).getOrElse(""),
      custval.split(",").lift(1).getOrElse(""),
      custval.split(",").lift(2).getOrElse(""),
      custval.split(",").lift(3).getOrElse(""),
      custval.split(",").lift(4).getOrElse(""),
      custval.split(",").lift(5).getOrElse("")
      )
    )
  }.toMap

}
