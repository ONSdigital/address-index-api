package uk.gov.ons.addressIndex.model.db.index

import com.sksamuel.elastic4s.requests.searches.SearchResponse
import com.sksamuel.elastic4s.{Hit, RequestFailure, RequestSuccess, Response}
import org.elasticsearch.client.{Request, Response, RestClient}
import play.api.libs.json._
import play.api.Logger
//import play.libs.Json
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

  val logger: Logger = Logger("address-index-server:HybridAddressCollection")

  def fromEither(resp: Either[RequestFailure, RequestSuccess[SearchResponse]]): HybridAddressCollection = {
    resp match {
      case Left(l) => throw new Exception("search failed - " + l.error.reason)
      case Right(r) => fromSearchResponse(r.result)
    }
  }

//  def fromLowResponse(resp: org.elasticsearch.client.Response): HybridAddressCollection = {
//  // val sresp: com.sksamuel.elastic4s.Response[SearchResponse] = resp.asInstanceOf[com.sksamuel.elastic4s.Response[SearchResponse]]
//  val sresp: com.sksamuel.elastic4s.Response[SearchResponse] = new SearchResponse()
//  val test = resp.getEntity.t
//    fromResponse(sresp)
//  }

  def fromLowResponse(lowRes: String) : HybridAddressCollection = {
    val resJson: JsValue = Json.parse(lowRes)
    val maxScore = (resJson \ "hits" \ "max_score").get
    println(maxScore.toString())
    val hit1 = (resJson \ "hits" \ "hits" \ 0).get
    println(hit1.toString())
    val uprn = (hit1 \ "_id" ).get
    val score = (hit1 \ "_score" ).get
    val address1: HybridAddress = new HybridAddress(addressEntryId = "",
      uprn = uprn.toString.substring(1).dropRight(1),
      parentUprn = "",
      relatives = Some(Seq.empty),
      crossRefs = Some(Seq.empty),
      postcodeIn = Some(""),
      postcodeOut = Some(""),
      lpi = Seq.empty,
      paf = Seq.empty,
      nisra = Seq.empty,
      auxiliary = Seq.empty,
      score = score.toString.toFloat,
      classificationCode = "",
      censusAddressType = "",
      censusEstabType = "",
      fromSource = "",
      countryCode = "",
      distance = 0D,
      highlights = Seq.empty)

    new HybridAddressCollection(Seq(address1),Seq(),maxScore.toString.toDouble,1)
  }

  def fromResponse(resp: com.sksamuel.elastic4s.Response[SearchResponse]): HybridAddressCollection = {
    if (resp.isError) {
      val err = s"${resp.status} - ${resp.error.`type`}, ${resp.error.reason}"
      val rootCauseErr = resp.error.rootCause.map(rc => s"${rc.`type`}, ${rc.reason}" ).mkString
      logger.error(s"Elasticsearch Error: ${err} with root cause: ${rootCauseErr}")
      throw new Exception("search failed - " + resp.error.reason)
    }
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

