package uk.gov.ons.addressIndex.model.db.index

import com.sksamuel.elastic4s.{HitAs, RichSearchHit}
import uk.gov.ons.addressIndex.model.db.ElasticIndex


/**
 * Data structure containing addresses with the maximum address
 * @param addresses fetched addresses
 * @param maxScore maximum score
 */
case class NationalAddressGazetteerAddresses(
  addresses: Seq[NationalAddressGazetteerAddress],
  maxScore: Float
)

/**
  * NAG Address DTO
  */
case class NationalAddressGazetteerAddress(
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
  logicalStatus: String,
  streetDescriptor: String,
  townName: String,
  locality: String,
  score: Float
)

/**
  * NAF Address DTO companion object that also contains implicits needed for Elastic4s
  */
object NationalAddressGazetteerAddress extends ElasticIndex[NationalAddressGazetteerAddress] {

  val name = "NationalAddressGazetteer"

  object Fields {

    /**
      * Document Fields
      */
    val uprn: String = "uprn"
    val postcodeLocator: String = "postcodeLocator"
    val addressBasePostal: String = "addressBasePostal"
    val latitude: String = "latitude"
    val longitude: String = "longitude"
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
    val level: String = "leveal"
    val officialFlag: String = "officialFlag"
    val logicalStatus: String = "logicalStatus"
    val streetDescriptor: String = "streetDescriptor"
    val townName: String = "townName"
    val locality: String = "locality"
  }

  /**
    * This is needed to directly transform a collection of objects returned by Elastic
    * request into a collection of NAF addresses
    */
  implicit object NationalAddressGazetteerAddressHitAs extends HitAs[NationalAddressGazetteerAddress] {
    import Fields._
    override def as(hit: RichSearchHit): NationalAddressGazetteerAddress = {
      NationalAddressGazetteerAddress(
        hit.sourceAsMap(uprn).toString,
        hit.sourceAsMap(postcodeLocator).toString,
        hit.sourceAsMap(addressBasePostal).toString,
        hit.sourceAsMap(latitude).toString,
        hit.sourceAsMap(longitude).toString,
        hit.sourceAsMap(easting).toString,
        hit.sourceAsMap(northing).toString,
        hit.sourceAsMap(organisation).toString,
        hit.sourceAsMap(legalName).toString,
        hit.sourceAsMap(classificationCode).toString,
        hit.sourceAsMap(usrn).toString,
        hit.sourceAsMap(lpiKey).toString,
        hit.sourceAsMap(paoText).toString,
        hit.sourceAsMap(paoStartNumber).toString,
        hit.sourceAsMap(paoStartSuffix).toString,
        hit.sourceAsMap(paoEndNumber).toString,
        hit.sourceAsMap(paoEndSuffix).toString,
        hit.sourceAsMap(saoText).toString,
        hit.sourceAsMap(saoStartNumber).toString,
        hit.sourceAsMap(saoStartSuffix).toString,
        hit.sourceAsMap(saoEndNumber).toString,
        hit.sourceAsMap(saoEndSuffix).toString,
        hit.sourceAsMap(level).toString,
        hit.sourceAsMap(officialFlag).toString,
        hit.sourceAsMap(logicalStatus).toString,
        hit.sourceAsMap(streetDescriptor).toString,
        hit.sourceAsMap(townName).toString,
        hit.sourceAsMap(locality).toString,
        hit.score
      )
    }
  }
}