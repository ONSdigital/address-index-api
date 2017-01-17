package uk.gov.ons.addressIndex.model.db.index

import com.sksamuel.elastic4s.{HitAs, RichSearchHit}
import play.api.libs.json.{JsValue, Json}
import uk.gov.ons.addressIndex.model.db.ElasticIndex

/**
  * PAF Address DTO
  */
case class HybridResult(
  uprn: String,
  lpi: JsValue,
  paf: JsValue
)

object HybridResult extends ElasticIndex[HybridResult] {

  val name: String = "HybridResponse"

  implicit lazy val fmt = Json.format[HybridResult]

  object Fields {

    /**
      * Document Fields
      */
    val uprn: String = "uprn"
    val lpi: String = "lpi"
    val paf: String = "paf"
  }

  /**
    * This is needed to directly transform a collection of objects returned by Elastic
    * request into a collection of PAF addresses
    */
  implicit object HybridResponseHitAs extends HitAs[HybridResult] {
    import Fields._

    override def as(hit: RichSearchHit): HybridResult = {
      val dataMap = hit.sourceAsMap
      def map(key: String) = dataMap(key).toString

      HybridResult(
        uprn = map(uprn),
        lpi = Json.parse(map(lpi)),
        paf = Json.parse(map(paf))
      )
    }
  }
}

/**
  * Data structure containing addresses with the maximum address
  * @param addresses fetched addresses
  * @param maxScore maximum score
  */
case class HybridResults(
  addresses: Seq[HybridResult],
  maxScore: Float
)

object HybridResults {
  implicit lazy val fmt = Json.format[HybridResults]
}
