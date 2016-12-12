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

  /**
    * This is needed to directly transform a collection of objects returned by Elastic
    * request into a collection of NAF addresses
    */
  implicit object NationalAddressGazetteerAddessHitAs extends HitAs[NationalAddressGazetteerAddress] {
    override def as(hit: RichSearchHit): NationalAddressGazetteerAddress = {
      NationalAddressGazetteerAddress(
        hit.sourceAsMap("uprn").toString,
        hit.sourceAsMap("postcodeLocator").toString,
        hit.sourceAsMap("addressBasePostal").toString,
        hit.sourceAsMap("latitude").toString,
        hit.sourceAsMap("longitude").toString,
        hit.sourceAsMap("easting").toString,
        hit.sourceAsMap("northing").toString,
        hit.sourceAsMap("organisation").toString,
        hit.sourceAsMap("legalName").toString,
        hit.sourceAsMap("classificationCode").toString,
        hit.sourceAsMap("ursn").toString,
        hit.sourceAsMap("lpiKey").toString,
        hit.sourceAsMap("paoText").toString,
        hit.sourceAsMap("paoStartNumber").toString,
        hit.sourceAsMap("paoStartSuffix").toString,
        hit.sourceAsMap("paoEndNumber").toString,
        hit.sourceAsMap("paoEndSuffix").toString,
        hit.sourceAsMap("saoText").toString,
        hit.sourceAsMap("saoStartNumber").toString,
        hit.sourceAsMap("saoStartSuffix").toString,
        hit.sourceAsMap("saoEndNumber").toString,
        hit.sourceAsMap("saoEndSuffix").toString,
        hit.sourceAsMap("level").toString,
        hit.sourceAsMap("officialFlag").toString,
        hit.sourceAsMap("logicalStatus").toString,
        hit.sourceAsMap("streetDescriptor").toString,
        hit.sourceAsMap("townName").toString,
        hit.sourceAsMap("locality").toString,
        hit.score
      )
    }
  }
}