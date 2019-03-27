package uk.gov.ons.addressIndex.model.db.index

import com.sksamuel.elastic4s.http.search.SearchResponse
import com.sksamuel.elastic4s.http.{RequestFailure, RequestSuccess}
import com.sksamuel.elastic4s.{Hit, HitReader}

import scala.util.Try

/**
  * DTO object containing hybrid address returned from ES
  *
  * @param uprn  address's upn
  * @param lpi   list of corresponding nag addresses
  * @param paf   list of corresponding paf addresses
  * @param score score of the address in the returned ES result
  */
@deprecated
case class HybridAddressSkinny(uprn: String,
                               parentUprn: String,
                               lpi: Seq[NationalAddressGazetteerAddress],
                               paf: Seq[PostcodeAddressFileAddress],
                               score: Float,
                               classificationCode: String) extends HybridAddress

object HybridAddressSkinny {
  // this `implicit` is needed for the library (elastic4s) to work
  implicit object HybridAddressHitReader extends HitReader[HybridAddressSkinny] {

    /**
      * Transforms hit from Elastic Search into a Hybrid Address
      * Used for the elastic4s library
      *
      * @param hit Elastic's response
      * @return generated Hybrid Address
      */
    override def read(hit: Hit): Either[Throwable, HybridAddressSkinny] = {
      val lpis: Seq[Map[String, AnyRef]] = Try {
        hit.sourceAsMap("lpi").asInstanceOf[List[Map[String, AnyRef]]].map(_.toMap)
      }.getOrElse(Seq.empty)

      val pafs: Seq[Map[String, AnyRef]] = Try {
        hit.sourceAsMap("paf").asInstanceOf[List[Map[String, AnyRef]]].map(_.toMap)
      }.getOrElse(Seq.empty)

      Right(HybridAddressSkinny(
        uprn = hit.sourceAsMap("uprn").toString,
        parentUprn = hit.sourceAsMap("parentUprn").toString,
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
@deprecated
case class HybridAddressesSkinny(addresses: Seq[HybridAddressSkinny],
                                 maxScore: Double,
                                 total: Long)

@deprecated
object HybridAddressesSkinny {
  def fromEither(resp: Either[RequestFailure, RequestSuccess[SearchResponse]]): HybridAddressesSkinny = {
    resp match {
      case Left(l) => throw new Exception("search failed - " + l.error.reason)
      case Right(r) => fromSearchResponse(r.result)
    }
  }

  /**
    * Transforms `SearchResponse` into a hybrid address
    * It needs implicit `HitAs[HybridAddressPartial]` that's why the definition should be after
    * the compamion object of `HybridAddressPartial`
    *
    * @throws Exception if there is at least one shard failing
    * @param response Response
    * @return
    */
  def fromSearchResponse(response: SearchResponse): HybridAddressesSkinny = {
    if (response.shards.failed > 0)
      throw new Exception(s"${response.shards.failed} failed shards out of ${response.shards.total}, the returned result would be partial and not reliable")

    val total = response.totalHits
    // if the query doesn't find anything, the score is `Nan` that messes up with Json converter
    val maxScore = if (total == 0) 0 else response.maxScore

    HybridAddressesSkinny(
      addresses = response.to[HybridAddressSkinny],
      maxScore = maxScore,
      total = total
    )
  }

}

