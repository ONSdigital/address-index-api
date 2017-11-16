package uk.gov.ons.addressIndex.model.db.index

import java.util

import com.sksamuel.elastic4s.http.search.SearchResponse
import com.sksamuel.elastic4s.http.update.RequestFailure
import com.sksamuel.elastic4s.{Hit, HitReader}
//import com.sksamuel.elastic4s.searches.{RichSearchHit, RichSearchResponse}

import scala.collection.JavaConverters._
import scala.collection.JavaConversions._
import scala.collection.{mutable,immutable,breakOut}

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
  parentUprn: String,
  relatives: Seq[Relative],
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
    parentUprn = "",
    relatives = Seq.empty,
    postcodeIn = "",
    postcodeOut = "",
    lpi = Seq.empty,
    paf = Seq.empty,
    score = 0
  )

  // this `implicit` is needed for the library (elastic4s) to work
  implicit object HybridAddressHitReader extends HitReader[HybridAddress] {

    /**
      * Transforms hit from Elastic Search into a Hybrid Address
      * Used for the elastic4s library
      * @param hit Elastic's response
      * @return generated Hybrid Address
      */
    override def read(hit: Hit): Either[Throwable, HybridAddress] = {

      Right(HybridAddress(
        uprn = hit.sourceAsMap("uprn").toString,
        parentUprn = hit.sourceAsMap("parentUprn").toString,
        relatives = Relative.fromEsMap(hit.sourceAsMap("relatives")).sortBy(_.level),
        postcodeIn = hit.sourceAsMap("postcodeIn").toString,
        postcodeOut = hit.sourceAsMap("postcodeOut").toString,
        lpi = NationalAddressGazetteerAddress.fromEsMap(hit.sourceAsMap("lpi")),
        paf = PostcodeAddressFileAddress.fromEsMap(hit.sourceAsMap("paf")),
        score = hit.score
      ))
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
  maxScore: Double,
  total: Long
)

object HybridAddresses {

  def fromEither(resp: Either[RequestFailure, SearchResponse]): HybridAddresses = {
    resp match {
      case Left(l) => throw new Exception("search failed" + l.error.reason)
      case Right(r) => fromSearchResponse(r)
    }

  }

  /**
    * Transforms `RichSearchResponse` into a hybrid address
    * It needs implicit `HitAs[HybridAddress]` that's why the definition should be after
    * the compamion object of `HybridAddress`
    *
    * @throws Exception if there is at least one shard failing
    * @param response
    * @return
    */
  def fromSearchResponse(response: SearchResponse): HybridAddresses = {

//  response.shards.failed > 0
//    response.shards.total
     if (response.shards.failed > 0)
      throw new Exception(s"${response.shards.failed} failed shards out of ${response.shards.total}, the returned result would be partial and not reliable")

//    System.out.println("response = " + response)

    val total = response.totalHits
    // if the query doesn't find anything, the score is `Nan` that messes up with Json converter
    val maxScore = if (total == 0) 0 else response.maxScore

    HybridAddresses(
      addresses = response.to[HybridAddress],
      maxScore = maxScore,
      total = total
    )
  }

}

