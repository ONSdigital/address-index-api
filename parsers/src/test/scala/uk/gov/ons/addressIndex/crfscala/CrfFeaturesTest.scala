package uk.gov.ons.addressIndex.crfscala

import org.scalatest.{FlatSpec, Matchers}
import uk.gov.ons.addressIndex.crfscala.CrfScala.{CrfFeature, CrfFeatures}

class CrfFeaturesTest extends FlatSpec with Matchers {

  it should "return all features" in {
    val expected = Seq(CrfFeatureTestImpl[Boolean]("name")(str => true))
    val test = CrfFeaturesImpl(expected)
    val actual = test.all
    actual should contain theSameElementsAs expected
  }

  ignore should "return the expected amount of `Item`s for input `arbitrary` (String, Boolean, Double, Int)" in {
    val input = "arbitrary"
    val feature1 = CrfFeatureTestImpl[String]("name1")(str => "")
    val feature2 = CrfFeatureTestImpl[Boolean]("name2")(str => true)
    val feature3 = CrfFeatureTestImpl[Double]("name3")(str => 0d)
    val feature4 = CrfFeatureTestImpl[Int]("name4")(str => 0)
    val test = CrfFeaturesImpl(Seq(feature1, feature2, feature3, feature4))
    val actual = test.analyse(input)
    val expected = s"\t${feature1.name}\\:$input:1.0\t${feature2.name}:1.0\t${feature3.name}:0.0\t${feature4.name}:0.0\n"
    actual shouldBe expected
  }
}

case class CrfFeaturesImpl(override val all : Seq[CrfFeature[_]]) extends CrfFeatures