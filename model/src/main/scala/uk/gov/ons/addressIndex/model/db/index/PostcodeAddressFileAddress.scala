package uk.gov.ons.addressIndex.model.db.index

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.{HitAs, RichSearchHit}
import com.sksamuel.elastic4s.mappings.FieldType._
import com.sksamuel.elastic4s.mappings.MappingDefinition
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

  /**
    * This is needed to directly transform a collection of objects returned by Elastic
    * request into a collection of PAF addresses
    */
  implicit object PostcodeAddressFileAddressHitAs extends HitAs[PostcodeAddressFileAddress] {
    override def as(hit: RichSearchHit): PostcodeAddressFileAddress = {
      PostcodeAddressFileAddress(
        hit.sourceAsMap("recordIdentifier").toString,
        hit.sourceAsMap("changeType").toString,
        hit.sourceAsMap("proOrder").toString,
        hit.sourceAsMap("uprn").toString,
        hit.sourceAsMap("udprn").toString,
        hit.sourceAsMap("organizationName").toString,
        hit.sourceAsMap("departmentName").toString,
        hit.sourceAsMap("subBuildingName").toString,
        hit.sourceAsMap("buildingName").toString,
        hit.sourceAsMap("buildingNumber").toString,
        hit.sourceAsMap("dependentThoroughfare").toString,
        hit.sourceAsMap("thoroughfare").toString,
        hit.sourceAsMap("doubleDependentLocality").toString,
        hit.sourceAsMap("dependentLocality").toString,
        hit.sourceAsMap("postTown").toString,
        hit.sourceAsMap("postcode").toString,
        hit.sourceAsMap("postcodeType").toString,
        hit.sourceAsMap("deliveryPointSuffix").toString,
        hit.sourceAsMap("welshDependentThoroughfare").toString,
        hit.sourceAsMap("welshThoroughfare").toString,
        hit.sourceAsMap("welshDoubleDependentLocality").toString,
        hit.sourceAsMap("welshDependentLocality").toString,
        hit.sourceAsMap("welshPostTown").toString,
        hit.sourceAsMap("poBoxNumber").toString,
        hit.sourceAsMap("processDate").toString,
        hit.sourceAsMap("startDate").toString,
        hit.sourceAsMap("endDate").toString,
        hit.sourceAsMap("lastUpdateDate").toString,
        hit.sourceAsMap("entryDate").toString
      )
    }
  }

  val name = "PostcodeAddressFile"

  def mappingDefinitions(): Seq[MappingDefinition] = {
    Seq(
      mapping(name) fields(
        field("recordIdentifier", StringType),
        field("changeType", StringType),
        field("proOrder", StringType),
        field("uprn", StringType),
        field("udprn", StringType),
        field("organizationName", StringType),
        field("departmentName", StringType),
        field("subBuildingName", StringType),
        field("buildingName", StringType),
        field("buildingNumber", StringType),
        field("dependentThoroughfare", StringType),
        field("thoroughfare", StringType),
        field("doubleDependentLocality", StringType),
        field("dependentLocality", StringType),
        field("postTown", StringType),
        field("postcode", StringType),
        field("postcodeType", StringType),
        field("deliveryPointSuffix", StringType),
        field("welshDependentThoroughfare", StringType),
        field("welshThoroughfare", StringType),
        field("welshDoubleDependentLocality", StringType),
        field("welshDependentLocality", StringType),
        field("welshPostTown", StringType),
        field("poBoxNumber", StringType),
        field("processDate", StringType),
        field("startDate", StringType),
        field("endDate", StringType),
        field("lastUpdateDate", StringType),
        field("entryDate", StringType)
        )
    )
  }
}