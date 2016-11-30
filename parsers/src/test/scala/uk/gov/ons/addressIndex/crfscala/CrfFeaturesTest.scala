package uk.gov.ons.addressIndex.crfscala

import org.scalatest.{FlatSpec, Matchers}
import third_party.org.chokkan.crfsuite.Item
import uk.gov.ons.addressIndex.crfscala.CrfScala.{CrfFeature, CrfFeatures}

class CrfFeaturesTest extends FlatSpec with Matchers {

  ignore should "return all features" in {
    val expected = Seq(CrfFeatureTestImpl[Boolean]("name")(str => true))
    val test = CrfFeaturesImpl(expected)
    val actual = test.all
    actual should contain theSameElementsAs expected
  }

  ignore should "return the expected amount of `Item`s for input `arbitrary` (Boolean)" in {
    val input = "arbitrary"
    val feature = CrfFeatureTestImpl[Boolean]("name")(str => true)
    val expected = new Item()
//    expected.add(feature toCrfJniInput input)
    val test = CrfFeaturesImpl(Seq(feature))
//    val actual = test toItem input
//    actual.size shouldBe expected.size
  }

  ignore should "return the expected amount of `Item`s for input `arbitrary` (Int)" in {
    val input = "arbitrary"
    val feature = CrfFeatureTestImpl[Int]("name")(str => 0)
    val expected = new Item()
//    expected.add(feature toCrfJniInput input)
    val test = CrfFeaturesImpl(Seq(feature))
//    val actual = test toItem input
//    actual.size shouldBe expected.size
  }

  ignore should "return the expected amount of `Item`s for input `arbitrary` (Double)" in {
    val input = "arbitrary"
    val feature = CrfFeatureTestImpl[Double]("name")(str => 0d)
    val expected = new Item()
//    expected.add(feature toCrfJniInput input)
    val test = CrfFeaturesImpl(Seq(feature))
//    val actual = test toItem input
//    actual.size shouldBe expected.size
  }

  ignore should "return the expected amount of `Item`s for input `arbitrary` (String)" in {
    val input = "arbitrary"
    val feature = CrfFeatureTestImpl[String]("name")(str => "")
    val expected = new Item()
//    expected.add(feature toCrfJniInput input)
    val test = CrfFeaturesImpl(Seq(feature))
//    val actual = test toItem input
//    actual.size shouldBe expected.size
  }

  ignore should "return the expected amount of `Item`s for input `arbitrary` (String, Boolean, Double, Int)" in {
    val input = "arbitrary"
    val feature1 = CrfFeatureTestImpl[String]("name")(str => "")
    val feature2 = CrfFeatureTestImpl[Boolean]("name")(str => true)
    val feature3 = CrfFeatureTestImpl[Double]("name")(str => 0d)
    val feature4 = CrfFeatureTestImpl[Int]("name")(str => 0)
    val expected = new Item()
//    expected.add(feature1 toCrfJniInput input)
//    expected.add(feature2 toCrfJniInput input)
//    expected.add(feature3 toCrfJniInput input)
//    expected.add(feature4 toCrfJniInput input)
    val test = CrfFeaturesImpl(Seq(feature1, feature2, feature3, feature4))
//    val actual = test toItem input
//    actual.size shouldBe expected.size
  }
}

case class CrfFeaturesImpl(override val all : Seq[CrfFeature[_]]) extends CrfFeatures