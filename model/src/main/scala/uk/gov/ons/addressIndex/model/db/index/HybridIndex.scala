package uk.gov.ons.addressIndex.model.db.index

import com.sksamuel.elastic4s.{HitAs, RichSearchHit}
import play.api.libs.json.{JsValue, Json}
import uk.gov.ons.addressIndex.model.db.ElasticIndex

/**
  * PAF Address DTO
  */
case class HybridIndex(
  uprn: String,
  lpi: JsValue,
  paf: JsValue
)

object HybridIndex extends ElasticIndex[HybridIndex] {

  val name: String = "hybrid"

  implicit lazy val fmt = Json.format[HybridIndex]

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
  implicit object HybridResponseHitAs extends HitAs[HybridIndex] {
    import Fields._

    override def as(hit: RichSearchHit): HybridIndex = {
      def map(key: String): String = hit.stringValue(key)
      hit.sourceAsMap(lpi)

      HybridIndex(
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
  addresses: Seq[HybridIndex],
  maxScore: Float
)

object HybridResults {
  implicit lazy val fmt = Json.format[HybridResults]
}
