package uk.gov.ons.addressIndex.model.db.index

import com.sksamuel.elastic4s.requests.searches.SearchResponse
import com.sksamuel.elastic4s.{RequestFailure, RequestSuccess, Response}
import uk.gov.ons.addressIndex.model.server.response.postcode.AddressResponsePostcodeGroup

import scala.util.Try


/**
  * Contains the result of an ES query
  *
  * @param addresses returned hybrid addresses
  * @param maxScore  maximum score among all of the found addresses
  *                  (even those that are not in the list because of the limit)
  * @param total     total number of all of the addresses regardless of the limit
  */
case class HybridAddressCollection(addresses: Seq[HybridAddress],
                                   aggregations: Seq[AddressResponsePostcodeGroup],
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

    // capture the aggregration and create postcode group opjects from the buckets
    val aggs = Try(response.aggregationsAsMap.head._2.asInstanceOf[Map[String,Any]]).getOrElse(Map.empty[String,Any])
    val buckets = Try(aggs.last._2.asInstanceOf[List[Map[Any,Any]]]).getOrElse(List.empty[Map[Any,Any]])
    val pcList = Try(buckets.map{bucket =>
      val bucketParts = bucket.getOrElse("key","").toString.split("_")
      val bucketCode: String = bucketParts(0)
      val bucketStreet: String = Try(bucketParts(1)).getOrElse("")
      val bucketTown: String = Try(bucketParts(2)).getOrElse("")
      val bucketCount: Int = bucket.getOrElse("doc_count",0).asInstanceOf[Int]
      val uprnaggs = bucket.getOrElse("uprns",Map.empty[Any,Any]).asInstanceOf[Map[Any,Any]]
      val postTownAggs = bucket.getOrElse("paftowns",Map.empty[Any,Any]).asInstanceOf[Map[Any,Any]]
      val postTown = (Try(postTownAggs.last._2.asInstanceOf[List[Map[Any,Any]]].head.head._2).getOrElse("N")).toString()
      val firstUprn = (Try(uprnaggs.last._2.asInstanceOf[List[Map[Any,Any]]].head.head._2).getOrElse(0)).toString().toLong
      AddressResponsePostcodeGroup(bucketCode,bucketStreet,bucketTown,bucketCount,firstUprn,postTown)
    }).getOrElse(List.empty[AddressResponsePostcodeGroup])

    val pcList2 = if (pcList.size > 0) pcList.zip(pcList.tail).map{
      case(previous,current) =>
      {if (current.postTown == "N")
        current.copy(postTown=previous.postTown)
      else current.copy()}
    } else List.empty[AddressResponsePostcodeGroup]

    val pcList3 = if (pcList.size > 0) pcList.head :: pcList2
    else List.empty[AddressResponsePostcodeGroup]

    // returned total will be number of hits unless we are doing grouped postcode
    val totalOrBuckets = if (pcList.size > 0) pcList.size else total

    HybridAddressCollection(
      addresses = response.to[HybridAddress],
      aggregations = pcList3,
      maxScore = maxScore,
      total = totalOrBuckets
    )
  }
}

