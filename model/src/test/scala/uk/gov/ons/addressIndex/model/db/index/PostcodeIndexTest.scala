package uk.gov.ons.addressIndex.model.db.index

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.FieldType._
import org.scalatest.{FlatSpec, Matchers}

class PostcodeIndexTest extends FlatSpec with Matchers {

  it should "have an expected fixed name" in {
    val expected = PostcodeIndexTest.name
    val actual = PostcodeIndex.name
    expected shouldBe actual
  }

//  it should "have an expected fixed sequence of mappings" in {
//    val expected = Seq(
//      mapping(PostcodeIndexTest.name) fields (
//        "inCode" typed StringType,
//        "outCode" typed StringType
//      )
//    )
//    val actual = PostcodeIndex.mappingDefinitions
//    expected should contain theSameElementsAs actual
//  }
}

object PostcodeIndexTest {
  val name = "Postcode"
}