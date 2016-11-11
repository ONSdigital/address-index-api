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
        "guid" typed StringType,
        "startDate" typed DateType,
        "udprn" typed IntegerType,
        "buildingNumber" typed IntegerType,
        "poBoxNumber" typed IntegerType,
        "buildingName" typed StringType,
        "subBuildingName" typed StringType,
        "organisationName" typed StringType,
        "postTown" typed StringType,
        "welshPostTown" typed StringType,
        "postcode" typed StringType,
        "departmentName" typed StringType,
        "thoroughfare" typed StringType,
        "welshThoroughfare" typed StringType,
        "dependantThoroughfare" typed StringType,
        "dependentLocality" typed StringType,
        "welshDependentLocality" typed StringType,
        "doubleDependentLocality" typed StringType,
        "welshDoubleDependentLocality" typed StringType
      )
    )
    val actual = PostcodeAddressFileAddress.mappingDefinitions
    expected should contain theSameElementsAs actual
  }
}

object PostcodeAddressFileAddressTest {
  val name = "PostcodeAddressFile"
}