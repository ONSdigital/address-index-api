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

  val Name = "NationalAddressGazetteer"

  object Fields {

    /**
      * Document Fields
      */
    val Uprn: String = "uprn"
    val PostcodeLocator: String = "postcodeLocator"
    val AddressBasePostal: String = "addressBasePostal"
    val Latitude: String = "latitude"
    val Longitude: String = "longitude"
    val Easting: String = "easting"
    val Northing: String = "northing"
    val Organisation: String = "organisation"
    val LegalName: String = "legalName"
    val ClassificationCode: String = "classificationCode"
    val Usrn: String = "usrn"
    val LpiKey: String = "lpiKey"
    val PaoText: String = "paoText"
    val PaoStartNumber: String = "paoStartNumber"
    val PaoStartSuffix: String = "paoStartSuffix"
    val PaoEndNumber: String = "paoEndNumber"
    val PaoEndSuffix: String = "paoEndSuffix"
    val SaoText: String = "saoText"
    val SaoStartNumber: String = "saoStartNumber"
    val SaoStartSuffix: String = "saoStartSuffix"
    val SaoEndNumber: String = "saoEndNumber"
    val SaoEndSuffix: String = "saoEndSuffix"
    val Level: String = "level"
    val OfficialFlag: String = "officialFlag"
    val LogicalStatus: String = "logicalStatus"
    val StreetDescriptor: String = "streetDescriptor"
    val TownName: String = "townName"
    val Locality: String = "locality"
  }

  /**
    * This is needed to directly transform a collection of objects returned by Elastic
    * request into a collection of NAF addresses
    */
  implicit object NationalAddressGazetteerAddressHitAs extends HitAs[NationalAddressGazetteerAddress] {
    import Fields._
    override def as(hit: RichSearchHit): NationalAddressGazetteerAddress = {
      NationalAddressGazetteerAddress(
        hit.sourceAsMap(Uprn).toString,
        hit.sourceAsMap(PostcodeLocator).toString,
        hit.sourceAsMap(AddressBasePostal).toString,
        hit.sourceAsMap(Latitude).toString,
        hit.sourceAsMap(Longitude).toString,
        hit.sourceAsMap(Easting).toString,
        hit.sourceAsMap(Northing).toString,
        hit.sourceAsMap(Organisation).toString,
        hit.sourceAsMap(LegalName).toString,
        hit.sourceAsMap(ClassificationCode).toString,
        hit.sourceAsMap(Usrn).toString,
        hit.sourceAsMap(LpiKey).toString,
        hit.sourceAsMap(PaoText).toString,
        hit.sourceAsMap(PaoStartNumber).toString,
        hit.sourceAsMap(PaoStartSuffix).toString,
        hit.sourceAsMap(PaoEndNumber).toString,
        hit.sourceAsMap(PaoEndSuffix).toString,
        hit.sourceAsMap(SaoText).toString,
        hit.sourceAsMap(SaoStartNumber).toString,
        hit.sourceAsMap(SaoStartSuffix).toString,
        hit.sourceAsMap(SaoEndNumber).toString,
        hit.sourceAsMap(SaoEndSuffix).toString,
        hit.sourceAsMap(Level).toString,
        hit.sourceAsMap(OfficialFlag).toString,
        hit.sourceAsMap(LogicalStatus).toString,
        hit.sourceAsMap(StreetDescriptor).toString,
        hit.sourceAsMap(TownName).toString,
        hit.sourceAsMap(Locality).toString,
        hit.score
      )
    }
  }
}