package uk.gov.ons.addressIndex.model.db.index

import com.sksamuel.elastic4s.requests.searches.SearchHit
import com.sksamuel.elastic4s.{Hit, HitReader}

import scala.util.Try

case class HybridAddress(uprn: String,
                         parentUprn: String,
                         relatives: Option[Seq[Relative]],
                         crossRefs: Option[Seq[CrossRef]],
                         postcodeIn: Option[String],
                         postcodeOut: Option[String],
                         lpi: Seq[NationalAddressGazetteerAddress],
                         paf: Seq[PostcodeAddressFileAddress],
                         nisra: Seq[NisraAddress],
                         auxiliary: Seq[AuxiliaryAddress],
                         score: Float,
                         classificationCode: String,
                         censusAddressType: String,
                         censusEstabType: String,
                         fromSource: String,
                         countryCode: String,
                         distance: Double = 0D,
                         highlights: Seq[Map[String,Seq[String]]])

object HybridAddress {
  /**
    * An empty HybridAddress, used in bulk search to show the address that didn't yield any results
    */
  val empty = HybridAddress(
    uprn = "",
    parentUprn = "",
    relatives = Some(Seq.empty),
    crossRefs = Some(Seq.empty),
    postcodeIn = Some(""),
    postcodeOut = Some(""),
    lpi = Seq.empty,
    paf = Seq.empty,
    nisra = Seq.empty,
    auxiliary = Seq.empty,
    score = 0,
    classificationCode = "",
    censusAddressType = "",
    censusEstabType = "",
    fromSource = "",
    countryCode = "",
    distance = 0D,
    highlights = Seq.empty
  )

  // this `implicit` is needed for the library (elastic4s) to work
  implicit object HybridAddressHitReader extends HitReader[HybridAddress] {
    /**
      * Transforms hit from Elastic Search into a Hybrid Address
      * Used for the elastic4s library
      *
      * @param hit Elastic's response
      * @return generated Hybrid Address
      */
      override def read(hit: Hit): Try[HybridAddress] = {
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

      val nisras: Seq[Map[String, AnyRef]] = Try {
        hit.sourceAsMap("nisra").asInstanceOf[List[Map[String, AnyRef]]].map(_.toMap)
      }.getOrElse(Seq.empty)

      val auxiliary: Map[String, AnyRef] = Try {
        hit.sourceAsMap("tokens").asInstanceOf[Map[String, AnyRef]]
      }.getOrElse(Map.empty)

      val sorts = hit.asInstanceOf[SearchHit].sort
      val slist = sorts.getOrElse(Seq())
      val centimetre = if (slist.isEmpty) 0 else 0.01
      val eWDistance = Try(slist.lift(0).get.toString.toDouble).getOrElse(0D)
      val isPartial = (eWDistance == hit.score)
      val niDistance = Try(slist.lift(1).get.toString.toDouble).getOrElse(0D)
      val testUPRN = Try(slist.lift(1).get.toString.toLong).getOrElse(0L)
      val bestDistance = if (isPartial || testUPRN != 0) 0D
                        else if (eWDistance > 0 && niDistance == 0) eWDistance
                          else if (eWDistance == 0 && niDistance > 0 && !niDistance.isInfinite) niDistance
                            else if (eWDistance > niDistance) niDistance
                              else (eWDistance + centimetre)

      val highlights = hit.asInstanceOf[SearchHit].highlight

      Try(HybridAddress(
        uprn = hit.sourceAsMap("uprn").toString,
        parentUprn = Try(hit.sourceAsMap("parentUprn").toString).getOrElse(""),
        relatives = Some(rels.map(Relative.fromEsMap).sortBy(_.level)),
        crossRefs = Some(cRefs.map(CrossRef.fromEsMap)),
        postcodeIn = if (Try(hit.sourceAsMap("postcodeIn").toString).isFailure) None else Some(hit.sourceAsMap("postcodeIn").toString),
        postcodeOut = if (Try(hit.sourceAsMap("postcodeOut").toString).isFailure) None else Some(hit.sourceAsMap("postcodeOut").toString),
        lpi = lpis.map(NationalAddressGazetteerAddress.fromEsMap),
        paf = pafs.map(PostcodeAddressFileAddress.fromEsMap),
        nisra = nisras.map(NisraAddress.fromEsMap),
        auxiliary = if (auxiliary.isEmpty) Seq.empty else Seq(AuxiliaryAddress.fromEsMap(auxiliary)),
        score = hit.score,
        classificationCode = Try(hit.sourceAsMap("classificationCode").toString).getOrElse(""),
        censusAddressType = Try(hit.sourceAsMap("censusAddressType").toString.trim).getOrElse(""),
        censusEstabType = Try(hit.sourceAsMap("censusEstabType").toString).getOrElse(""),
        fromSource = Try(hit.sourceAsMap("fromSource").toString).getOrElse("EW"),
        countryCode = Try(hit.sourceAsMap("countryCode").toString).getOrElse("E"),
        distance = bestDistance,
        highlights = if (highlights == null) Seq() else Seq(highlights)
      ))
    }
  }
}