package uk.gov.ons.addressIndex.model.db.index

import com.sksamuel.elastic4s.testkit.ElasticSugar
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.FieldType._
import org.scalatest.{FlatSpec, Matchers}

class PostcodeAddressFileIndexTest extends FlatSpec with Matchers with ElasticSugar {

  it should "have an expected fixed name" in {
    val expected = PostcodeAddressFileIndexTest.name
    val actual = PostcodeAddressFileIndex.name
    expected shouldBe actual
  }

  ignore should "have an expected fixed sequence of mappings" in {
    val expected = Seq(
      mapping(PostcodeAddressFileIndexTest.name) fields (
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
    val actual = PostcodeAddressFileIndex.mappingDefinitions()
    expected should contain theSameElementsAs actual
  }
}

object PostcodeAddressFileIndexTest {
  val name = "PostcodeAddressFile"
}