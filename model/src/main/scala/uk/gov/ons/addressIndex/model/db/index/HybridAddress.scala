package uk.gov.ons.addressIndex.model.db.index

import java.util

import com.sksamuel.elastic4s.{HitAs, RichSearchHit}
import uk.gov.ons.addressIndex.model.db.ElasticIndex
import uk.gov.ons.addressIndex.model.db.index.NationalAddressGazetteerAddress.{Fields => PafFields}
import uk.gov.ons.addressIndex.model.db.index.PostcodeAddressFileAddress.{Fields => NagFields}

import scala.collection.JavaConverters._
import scala.util.Try

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

/**
  * DTO object containing hybrid address returned from ES
  * @param uprn address's upn
  * @param lpi list of corresponding nag addresses
  * @param paf list of corresponding paf addresses
  * @param score score of the address in the returned ES result
  */
case class HybridAddress(
  uprn: String,
  lpi: Seq[NationalAddressGazetteerAddress],
  paf: Seq[PostcodeAddressFileAddress],
  score: Float
)

object HybridAddress extends ElasticIndex[HybridAddress] {

  val name = "HybridAddress"

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

      HybridAddress(
        uprn = hit.sourceAsMap("uprn").toString,
        lpi = lpis.map { (nag: Map[String, AnyRef]) =>
          NationalAddressGazetteerAddress(
            uprn = nag.getOrElse(PafFields.uprn, "").toString,
            postcodeLocator = nag.getOrElse(PafFields.postcodeLocator, "").toString,
            addressBasePostal = nag.getOrElse(PafFields.addressBasePostal, "").toString,
            latitude = nag.getOrElse(PafFields.latitude, "").toString,
            longitude = nag.getOrElse(PafFields.longitude, "").toString,
            easting = nag.getOrElse(PafFields.easting, "").toString,
            northing = nag.getOrElse(PafFields.northing, "").toString,
            organisation = nag.getOrElse(PafFields.organisation, "").toString,
            legalName = nag.getOrElse(PafFields.legalName, "").toString,
            classificationCode = nag.getOrElse(PafFields.classificationCode, "").toString,
            usrn = nag.getOrElse(PafFields.usrn, "").toString,
            lpiKey = nag.getOrElse(PafFields.lpiKey, "").toString,
            paoText = nag.getOrElse(PafFields.paoText, "").toString,
            paoStartNumber = nag.getOrElse(PafFields.paoStartNumber, "").toString,
            paoStartSuffix = nag.getOrElse(PafFields.paoStartSuffix, "").toString,
            paoEndNumber = nag.getOrElse(PafFields.paoEndNumber, "").toString,
            paoEndSuffix = nag.getOrElse(PafFields.paoEndSuffix, "").toString,
            saoText = nag.getOrElse(PafFields.saoText, "").toString,
            saoStartNumber = nag.getOrElse(PafFields.saoStartNumber, "").toString,
            saoStartSuffix = nag.getOrElse(PafFields.saoStartSuffix, "").toString,
            saoEndNumber = nag.getOrElse(PafFields.saoEndNumber, "").toString,
            saoEndSuffix = nag.getOrElse(PafFields.saoEndSuffix, "").toString,
            level = nag.getOrElse(PafFields.level, "").toString,
            officialFlag = nag.getOrElse(PafFields.officialFlag, "").toString,
            logicalStatus = nag.getOrElse(PafFields.logicalStatus, "").toString,
            streetDescriptor = nag.getOrElse(PafFields.streetDescriptor, "").toString,
            townName = nag.getOrElse(PafFields.townName, "").toString,
            locality = nag.getOrElse(PafFields.locality, "").toString
          )
        },
        paf = pafs.map { (paf: Map[String, AnyRef]) =>
          PostcodeAddressFileAddress(
            recordIdentifier = paf.getOrElse(NagFields.recordIdentifier, "").toString,
            changeType = paf.getOrElse(NagFields.changeType, "").toString,
            proOrder = paf.getOrElse(NagFields.proOrder, "").toString,
            uprn = paf.getOrElse(NagFields.uprn, "").toString,
            udprn = paf.getOrElse(NagFields.udprn, "").toString,
            organizationName = paf.getOrElse(NagFields.organizationName, "").toString,
            departmentName = paf.getOrElse(NagFields.departmentName, "").toString,
            subBuildingName = paf.getOrElse(NagFields.subBuildingName, "").toString,
            buildingName = paf.getOrElse(NagFields.buildingName, "").toString,
            buildingNumber = paf.getOrElse(NagFields.buildingNumber, "").toString,
            dependentThoroughfare = paf.getOrElse(NagFields.dependentThoroughfare, "").toString,
            thoroughfare = paf.getOrElse(NagFields.thoroughfare, "").toString,
            doubleDependentLocality = paf.getOrElse(NagFields.doubleDependentLocality, "").toString,
            dependentLocality = paf.getOrElse(NagFields.dependentLocality, "").toString,
            postTown = paf.getOrElse(NagFields.postTown, "").toString,
            postcode = paf.getOrElse(NagFields.postcode, "").toString,
            postcodeType = paf.getOrElse(NagFields.postcodeType, "").toString,
            deliveryPointSuffix = paf.getOrElse(NagFields.deliveryPointSuffix, "").toString,
            welshDependentThoroughfare = paf.getOrElse(NagFields.welshDependentThoroughfare, "").toString,
            welshThoroughfare = paf.getOrElse(NagFields.welshThoroughfare, "").toString,
            welshDoubleDependentLocality = paf.getOrElse(NagFields.welshDoubleDependentLocality, "").toString,
            welshDependentLocality = paf.getOrElse(NagFields.welshDependentLocality, "").toString,
            welshPostTown = paf.getOrElse(NagFields.welshPostTown, "").toString,
            poBoxNumber = paf.getOrElse(NagFields.poBoxNumber, "").toString,
            processDate = paf.getOrElse(NagFields.processDate, "").toString,
            startDate = paf.getOrElse(NagFields.startDate, "").toString,
            endDate = paf.getOrElse(NagFields.endDate, "").toString,
            lastUpdateDate = paf.getOrElse(NagFields.lastUpdateDate, "").toString,
            entryDate = paf.getOrElse(NagFields.entryDate, "").toString
          )
        },
        score = hit.score
      )
    }
  }

}

