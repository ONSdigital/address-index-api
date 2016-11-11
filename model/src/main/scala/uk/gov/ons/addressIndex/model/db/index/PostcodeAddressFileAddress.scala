package uk.gov.ons.addressIndex.model.db.index

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.{HitAs, RichSearchHit}
import com.sksamuel.elastic4s.mappings.FieldType._
import com.sksamuel.elastic4s.mappings.MappingDefinition
import uk.gov.ons.addressIndex.model.db.ElasticIndex

/**
  * PAF Address
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

object PostcodeAddressFileAddress extends ElasticIndex[PostcodeAddressFileAddress] {

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
        "recordIdentifier" typed StringType,
        "changeType" typed StringType,
        "proOrder" typed StringType,
        "uprn" typed StringType,
        "udprn" typed StringType,
        "organizationName" typed StringType,
        "departmentName" typed StringType,
        "subBuildingName" typed StringType,
        "buildingName" typed StringType,
        "buildingNumber" typed StringType,
        "dependentThoroughfare" typed StringType,
        "thoroughfare" typed StringType,
        "doubleDependentLocality" typed StringType,
        "dependentLocality" typed StringType,
        "postTown" typed StringType,
        "postcode" typed StringType,
        "postcodeType" typed StringType,
        "deliveryPointSuffix" typed StringType,
        "welshDependentThoroughfare" typed StringType,
        "welshThoroughfare" typed StringType,
        "welshDoubleDependentLocality" typed StringType,
        "welshDependentLocality" typed StringType,
        "welshPostTown" typed StringType,
        "poBoxNumber" typed StringType,
        "processDate" typed StringType,
        "startDate" typed StringType,
        "endDate" typed StringType,
        "lastUpdateDate" typed StringType,
        "entryDate" typed StringType
      )
    )
  }
}