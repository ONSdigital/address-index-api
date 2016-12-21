package uk.gov.ons.addressIndex.model.db.index

import com.sksamuel.elastic4s.{HitAs, RichSearchHit}
import uk.gov.ons.addressIndex.model.db.ElasticIndex

/**
 * Data structure containing addresses with the maximum address
 * @param addresses fetched addresses
 * @param maxScore maximum score
 */
case class PostcodeAddressFileAddresses(
  addresses: Seq[PostcodeAddressFileAddress],
  maxScore: Float
)

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
  entryDate: String,
  score: Float
)

/**
  * PAF Address DTO companion object that also contains implicits needed for Elastic4s
  */
object PostcodeAddressFileAddress extends ElasticIndex[PostcodeAddressFileAddress] {

  val Name: String = "PostcodeAddressFile"

  object Fields {

    /**
      * Document Fields
      */
    val Postcode: String = "postcode"
    val RecordIdentifier: String = "recordIdentifier"
    val ChangeType: String = "changeType"
    val PostTown: String = "postTown"
    val ProOrder: String = "proOrder"
    val Uprn: String = "uprn"
    val Udprn: String = "udprn"
    val OrganizationName: String = "organizationName"
    val DepartmentName: String = "departmentName"
    val SubBuildingName: String = "subBuildingName"
    val BuildingName: String = "buildingName"
    val BuildingNumber: String = "buildingNumber"
    val DependentThoroughfare: String = "dependentThoroughfare"
    val Thoroughfare: String = "thoroughfare"
    val DoubleDependentLocality: String = "doubleDependentLocality"
    val DependentLocality: String = "dependentLocality"
    val PostcodeType: String = "postcodeType"
    val DeliveryPointSuffix: String = "deliveryPointSuffix"
    val WelshDependentThoroughfare: String = "welshDependentThoroughfare"
    val WelshThoroughfare: String = "welshThoroughfare"
    val WelshDoubleDependentLocality: String = "welshDoubleDependentLocality"
    val WelshDependentLocality: String = "welshDependentLocality"
    val WelshPostTown: String = "welshPostTown"
    val PoBoxNumber: String = "poBoxNumber"
    val ProcessDate: String = "processDate"
    val StartDate: String = "startDate"
    val EndDate: String = "endDate"
    val LastUpdateDate: String = "lastUpdateDate"
    val EntryDate: String = "entryDate"
  }


  /**
    * This is needed to directly transform a collection of objects returned by Elastic
    * request into a collection of PAF addresses
    */
  implicit object PostcodeAddressFileAddressHitAs extends HitAs[PostcodeAddressFileAddress] {
    import Fields._

    override def as(hit: RichSearchHit): PostcodeAddressFileAddress = {
      PostcodeAddressFileAddress(
        hit.sourceAsMap(RecordIdentifier).toString,
        hit.sourceAsMap(ChangeType).toString,
        hit.sourceAsMap(ProOrder).toString,
        hit.sourceAsMap(Uprn).toString,
        hit.sourceAsMap(Udprn).toString,
        hit.sourceAsMap(OrganizationName).toString,
        hit.sourceAsMap(DepartmentName).toString,
        hit.sourceAsMap(SubBuildingName).toString,
        hit.sourceAsMap(BuildingName).toString,
        hit.sourceAsMap(BuildingNumber).toString,
        hit.sourceAsMap(DependentThoroughfare).toString,
        hit.sourceAsMap(Thoroughfare).toString,
        hit.sourceAsMap(DoubleDependentLocality).toString,
        hit.sourceAsMap(DependentLocality).toString,
        hit.sourceAsMap(PostTown).toString,
        hit.sourceAsMap(Postcode).toString,
        hit.sourceAsMap(PostcodeType).toString,
        hit.sourceAsMap(DeliveryPointSuffix).toString,
        hit.sourceAsMap(WelshDependentThoroughfare).toString,
        hit.sourceAsMap(WelshThoroughfare).toString,
        hit.sourceAsMap(WelshDoubleDependentLocality).toString,
        hit.sourceAsMap(WelshDependentLocality).toString,
        hit.sourceAsMap(WelshPostTown).toString,
        hit.sourceAsMap(PoBoxNumber).toString,
        hit.sourceAsMap(ProcessDate).toString,
        hit.sourceAsMap(StartDate).toString,
        hit.sourceAsMap(EndDate).toString,
        hit.sourceAsMap(LastUpdateDate).toString,
        hit.sourceAsMap(EntryDate).toString,
        hit.score
      )
    }
  }
}