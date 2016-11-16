package uk.gov.ons.addressIndex.model.db.index

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.FieldType._
import org.scalatest.{FlatSpec, Matchers}

class PostcodeAddressFileAddressTest extends FlatSpec with Matchers {

  it should "have an expected fixed name" in {
    val expected = PostcodeAddressFileAddressTest.name
    val actual = PostcodeAddressFileAddress.name
    expected shouldBe actual
  }

  ignore should "have an expected fixed sequence of mappings" in {
    val expected = Seq(
      mapping(PostcodeAddressFileAddressTest.name) fields (
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
    val actual = PostcodeAddressFileAddress.mappingDefinitions
    expected should contain theSameElementsAs actual
  }
}

object PostcodeAddressFileAddressTest {
  val name = "PostcodeAddressFile"
}