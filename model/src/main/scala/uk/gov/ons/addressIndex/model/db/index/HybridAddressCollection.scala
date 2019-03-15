package uk.gov.ons.addressIndex.model.db.index

import com.sksamuel.elastic4s.{Hit, HitReader}
import com.sksamuel.elastic4s.http.search.SearchResponse
import com.sksamuel.elastic4s.http.{RequestFailure, RequestSuccess}

import scala.util.Try

trait HybridAddress {

}

case class HybridAddressOpt(uprn: String,
                             parentUprn: String,
                             relatives: Option[Seq[Relative]],
                             crossRefs: Option[Seq[CrossRef]],
                             postcodeIn: Option[String],
                             postcodeOut: Option[String],
                             lpi: Seq[NationalAddressGazetteerAddress],
                             paf: Seq[PostcodeAddressFileAddress],
                             score: Float,
                             classificationCode: String)

object HybridAddressOpt {

  /**
    * An empty HybridAddress, used in bulk search to show the address that didn't yield any results
    */
  val empty = HybridAddressOpt(
    uprn = "",
    parentUprn = "",
    relatives = Some(Seq.empty),
    crossRefs = Some(Seq.empty),
    postcodeIn = Some(""),
    postcodeOut = Some(""),
    lpi = Seq.empty,
    paf = Seq.empty,
    score = 0,
    classificationCode = ""
  )

  // this `implicit` is needed for the library (elastic4s) to work
  implicit object HybridAddressHitReader extends HitReader[HybridAddressOpt] {

    /**
      * Transforms hit from Elastic Search into a Hybrid Address
      * Used for the elastic4s library
      *
      * @param hit Elastic's response
      * @return generated Hybrid Address
      */
    override def read(hit: Hit): Either[Throwable, HybridAddressOpt] = {
      val cRefs: Seq[Map[String, AnyRef]] = Try {
        hit.sourceAsMap("crossRefs").asInstanceOf[List[Map[String, AnyRef]]].map(_.toMap)
      }.getOrElse(Seq.empty)

      val rels: Seq[Map[String, AnyRef]] = Try {
        hit.sourceAsMap("relatives").asInstanceOf[List[Map[String, AnyRef]]].map(_.toMap)
      }.getOrElse(Seq.empty)

      val lpis: Seq[Map[String, AnyRef]] = Try {
        hit.sourceAsMap("lpi").asInstanceOf[List[Map[String, AnyRef]]].map(_.toMap)
      }.getOrElse(Seq.empty)

      val pafs: Seq[Map[String, AnyRef]] = Try {
        hit.sourceAsMap("paf").asInstanceOf[List[Map[String, AnyRef]]].map(_.toMap)
      }.getOrElse(Seq.empty)

      Right(HybridAddressOpt(
        uprn = hit.sourceAsMap("uprn").toString,
        parentUprn = hit.sourceAsMap("parentUprn").toString,
        relatives = Some(rels.map(Relative.fromEsMap).sortBy(_.level)),
        crossRefs = Some(cRefs.map(CrossRef.fromEsMap)),
        postcodeIn = Some(hit.sourceAsMap("postcodeIn").toString),
        postcodeOut = Some(hit.sourceAsMap("postcodeOut").toString),
        lpi = lpis.map(NationalAddressGazetteerAddress.fromEsMap),
        paf = pafs.map(PostcodeAddressFileAddress.fromEsMap),
        score = hit.score,
        classificationCode = hit.sourceAsMap("classificationCode").toString
      ))
    }
  }
}

/**
  * Contains the result of an ES query
  *
  * @param addresses returned hybrid addresses
  * @param maxScore  maximum score among all of the found addresses
  *                  (even those that are not in the list because of the limit)
  * @param total     total number of all of the addresses regardless of the limit
  */
case class HybridAddressCollection(addresses: Seq[HybridAddressFull],
                                   maxScore: Double,
                                   total: Long)

object HybridAddressCollection {
  def fromEither(resp: Either[RequestFailure, RequestSuccess[SearchResponse]]): HybridAddressCollection = {
    resp match {
      case Left(l) => throw new Exception("search failed - " + l.error.reason)
      case Right(r) => fromSearchResponse(r.result)
    }
  }

  /**
    * Transforms `SearchResponse` into a hybrid address
    * It needs implicit `HitAs[HybridAddress]` that's why the definition should be after
    * the compamion object of `HybridAddress`
    *
    * @throws Exception if there is at least one shard failing
    * @param response Response
    * @return
    */
  def fromSearchResponse(response: SearchResponse): HybridAddressCollection = {
    if (response.shards.failed > 0)
      throw new Exception(s"${response.shards.failed} failed shards out of ${response.shards.total}, the returned result would be partial and not reliable")

    val total = response.totalHits
    // if the query doesn't find anything, the score is `Nan` that messes up with Json converter
    val maxScore = if (total == 0) 0 else response.maxScore

    HybridAddressCollection(
      addresses = response.to[HybridAddressFull],
      maxScore = maxScore,
      total = total
    )
  }

}

