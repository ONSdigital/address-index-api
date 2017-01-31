package uk.gov.ons.addressIndex.model.db.index

import com.sksamuel.elastic4s.{HitAs, RichSearchHit}
import uk.gov.ons.addressIndex.model.db.ElasticIndex

/**
  * PAF Address DTO
  */
case class PostcodeAddressFileAddress(
  recordIdentifier: String,
  changeType: String,
  proOrder: String,
  uprn: String,
  udprn: String,
  organizationName: String,
  departmentName: String,
  subBuildingName: String,
  buildingName: String,
  buildingNumber: String,
  dependentThoroughfare: String,
  thoroughfare: String,
  doubleDependentLocality: String,
  dependentLocality: String,
  postTown: String,
  postcode: String,
  postcodeType: String,
  deliveryPointSuffix: String,
  welshDependentThoroughfare: String,
  welshThoroughfare: String,
  welshDoubleDependentLocality: String,
  welshDependentLocality: String,
  welshPostTown: String,
  poBoxNumber: String,
  processDate: String,
  startDate: String,
  endDate: String,
  lastUpdateDate: String,
  entryDate: String
)

/**
  * PAF Address DTO companion object that also contains implicits needed for Elastic4s
  */
object PostcodeAddressFileAddress extends ElasticIndex[PostcodeAddressFileAddress] {

  val name: String = "PostcodeAddressFile"

  object Fields {

    /**
      * Document Fields
      */
    val postcode: String = "postcode"
    val recordIdentifier: String = "recordIdentifier"
    val changeType: String = "changeType"
    val postTown: String = "postTown"
    val proOrder: String = "proOrder"
    val uprn: String = "uprn"
    val udprn: String = "udprn"
    val organizationName: String = "organizationName"
    val departmentName: String = "departmentName"
    val subBuildingName: String = "subBuildingName"
    val buildingName: String = "buildingName"
    val buildingNumber: String = "buildingNumber"
    val dependentThoroughfare: String = "dependentThoroughfare"
    val thoroughfare: String = "thoroughfare"
    val doubleDependentLocality: String = "doubleDependentLocality"
    val dependentLocality: String = "dependentLocality"
    val postcodeType: String = "postcodeType"
    val deliveryPointSuffix: String = "deliveryPointSuffix"
    val welshDependentThoroughfare: String = "welshDependentThoroughfare"
    val welshThoroughfare: String = "welshThoroughfare"
    val welshDoubleDependentLocality: String = "welshDoubleDependentLocality"
    val welshDependentLocality: String = "welshDependentLocality"
    val welshPostTown: String = "welshPostTown"
    val poBoxNumber: String = "poBoxNumber"
    val processDate: String = "processDate"
    val startDate: String = "startDate"
    val endDate: String = "endDate"
    val lastUpdateDate: String = "lastUpdateDate"
    val entryDate: String = "entryDate"
  }


  /**
    * This is needed to directly transform a collection of objects returned by Elastic
    * request into a collection of PAF addresses
    */
  implicit object PostcodeAddressFileAddressHitAs extends HitAs[PostcodeAddressFileAddress] {
    import Fields._

    override def as(hit: RichSearchHit): PostcodeAddressFileAddress = {
      PostcodeAddressFileAddress(
        hit.sourceAsMap(recordIdentifier).toString,
        hit.sourceAsMap(changeType).toString,
        hit.sourceAsMap(proOrder).toString,
        hit.sourceAsMap(uprn).toString,
        hit.sourceAsMap(udprn).toString,
        hit.sourceAsMap(organizationName).toString,
        hit.sourceAsMap(departmentName).toString,
        hit.sourceAsMap(subBuildingName).toString,
        hit.sourceAsMap(buildingName).toString,
        hit.sourceAsMap(buildingNumber).toString,
        hit.sourceAsMap(dependentThoroughfare).toString,
        hit.sourceAsMap(thoroughfare).toString,
        hit.sourceAsMap(doubleDependentLocality).toString,
        hit.sourceAsMap(dependentLocality).toString,
        hit.sourceAsMap(postTown).toString,
        hit.sourceAsMap(postcode).toString,
        hit.sourceAsMap(postcodeType).toString,
        hit.sourceAsMap(deliveryPointSuffix).toString,
        hit.sourceAsMap(welshDependentThoroughfare).toString,
        hit.sourceAsMap(welshThoroughfare).toString,
        hit.sourceAsMap(welshDoubleDependentLocality).toString,
        hit.sourceAsMap(welshDependentLocality).toString,
        hit.sourceAsMap(welshPostTown).toString,
        hit.sourceAsMap(poBoxNumber).toString,
        hit.sourceAsMap(processDate).toString,
        hit.sourceAsMap(startDate).toString,
        hit.sourceAsMap(endDate).toString,
        hit.sourceAsMap(lastUpdateDate).toString,
        hit.sourceAsMap(entryDate).toString
      )
    }
  }
}