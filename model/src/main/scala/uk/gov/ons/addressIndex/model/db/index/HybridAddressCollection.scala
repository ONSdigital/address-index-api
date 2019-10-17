package uk.gov.ons.addressIndex.model.db.index

import com.sksamuel.elastic4s.requests.searches.SearchResponse
import com.sksamuel.elastic4s.{RequestFailure, RequestSuccess, Response}

/**
  * Contains the result of an ES query
  *
  * @param addresses returned hybrid addresses
  * @param maxScore  maximum score among all of the found addresses
  *                  (even those that are not in the list because of the limit)
  * @param total     total number of all of the addresses regardless of the limit
  */
case class HybridAddressCollection(addresses: Seq[HybridAddress],
                                   maxScore: Double,
                                   total: Long)

object HybridAddressCollection {
  def fromEither(resp: Either[RequestFailure, RequestSuccess[SearchResponse]]): HybridAddressCollection = {
    resp match {
      case Left(l) => throw new Exception("search failed - " + l.error.reason)
      case Right(r) => fromSearchResponse(r.result)
    }
  }

  def fromResponse(resp: Response[SearchResponse]): HybridAddressCollection = {
    if (resp.isError) throw new Exception("search failed - " + resp.error.reason)
    else fromSearchResponse(resp.result)
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
      addresses = response.to[HybridAddress],
      maxScore = maxScore,
      total = total
    )
  }
}

