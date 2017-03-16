package uk.gov.ons.addressIndex.model.db.index

import java.util

import com.sksamuel.elastic4s.{HitAs, RichSearchHit, RichSearchResponse}
import scala.collection.JavaConverters._
import scala.util.Try

/**
  * DTO object containing hybrid address returned from ES
  * @param uprn address's upn
  * @param lpi list of corresponding nag addresses
  * @param paf list of corresponding paf addresses
  * @param score score of the address in the returned ES result
  */
case class HybridAddress(
  uprn: String,
//  parentUprn: String,
//  relatives: Seq[Relative],
  postcodeIn: String,
  postcodeOut: String,
  lpi: Seq[NationalAddressGazetteerAddress],
  paf: Seq[PostcodeAddressFileAddress],
  score: Float
)

object HybridAddress {

  val name = "HybridAddress"

  /**
    * An empty HybridAddress, used in bulk search to show the address that didn't yield any results
    */
  val empty = HybridAddress(
    uprn = "",
//    parentUprn = "",
//    relatives = Seq.empty,
    postcodeIn = "",
    postcodeOut = "",
    lpi = Seq.empty,
    paf = Seq.empty,
    score = 0
  )

  // this `implicit` is needed for the library (elastic4s) to work
  implicit object HybridAddressHitAs extends HitAs[HybridAddress] {

    /**
      * Transforms hit from Elastic Search into a Hybrid Address
      * Used for the elastic4s library
      * @param hit Elastic's response
      * @return generated Hybrid Address
      */
    override def as(hit: RichSearchHit): HybridAddress = {

      val lpis: Seq[Map[String, AnyRef]] = Try {
        // Complex logic to cast field that contains a list of NAGs into a Scala's Map[String, AnyRef] so that we could
        // extract the information into a NAG DTO
        hit.sourceAsMap("lpi").asInstanceOf[util.ArrayList[java.util.HashMap[String, AnyRef]]].asScala.toList.map(_.asScala.toMap)
      }.getOrElse(Seq.empty)

      val pafs: Seq[Map[String, AnyRef]] = Try {
        // Complex logic to cast field that contains a list of PAFs into a Scala's Map[String, AnyRef] so that we could
        // extract the information into a PAF DTO
        hit.sourceAsMap("paf").asInstanceOf[util.ArrayList[java.util.HashMap[String, AnyRef]]].asScala.toList.map(_.asScala.toMap)
      }.getOrElse(Seq.empty)

      val rels: Seq[Map[String, AnyRef]] = Try {
        // Complex logic to cast field that contains a list of PAFs into a Scala's Map[String, AnyRef] so that we could
        // extract the information into a PAF DTO
        hit.sourceAsMap("relatives").asInstanceOf[util.ArrayList[java.util.HashMap[String, AnyRef]]].asScala.toList.map(_.asScala.toMap)
      }.getOrElse(Seq.empty)


      HybridAddress(
        uprn = hit.sourceAsMap("uprn").toString,
//        parentUprn = hit.sourceAsMap("parentUprn").toString,
//        relatives = rels.map(Relative.fromEsMap),
        postcodeIn = hit.sourceAsMap("postcodeIn").toString,
        postcodeOut = hit.sourceAsMap("postcodeOut").toString,
        lpi = lpis.map(NationalAddressGazetteerAddress.fromEsMap),
        paf = pafs.map(PostcodeAddressFileAddress.fromEsMap),
        score = hit.score
      )
    }
  }

}

/**
  * Contains the result of an ES query
  * @param addresses returned hybrid addresses
  * @param maxScore maximum score among all of the found addresses
  *                 (even those that are not in the list because of the limit)
  * @param total total number of all of the addresses regardless of the limit
  */
case class HybridAddresses(
  addresses: Seq[HybridAddress],
  maxScore: Float,
  total: Long
)

object HybridAddresses {

  /**
    * Transforms `RichSearchResponse` into a hybrid address
    * It needs implicit `HitAs[HybridAddress]` that's why the definition should be after
    * the compamion object of `HybridAddress`
    *
    * @throws Exception if there is at least one shard failing
    * @param response
    * @return
    */
  def fromRichSearchResponse(response: RichSearchResponse): HybridAddresses = {

    if (response.shardFailures.nonEmpty)
      throw new Exception(s"${response.shardFailures.length} failed shards out of ${response.totalShards}, the returned result would be partial and not reliable")

    val total = response.totalHits
    // if the query doesn't find anything, the score is `Nan` that messes up with Json converter
    val maxScore = if (total == 0) 0 else response.maxScore

    HybridAddresses(
      addresses = response.as[HybridAddress],
      maxScore = maxScore,
      total = total
    )
  }

}

