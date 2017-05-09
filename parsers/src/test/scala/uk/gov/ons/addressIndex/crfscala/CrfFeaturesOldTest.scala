package uk.gov.ons.addressIndex.crfscala

import org.scalatest.{FlatSpec, Matchers}

class CrfFeaturesOldTest extends FlatSpec with Matchers {

  it should "return all feature analysers" in {
    val expected = Seq(CrfFeatureTestImpl[Boolean]("name")(str => true))
    val test = CrfFeaturesImpl(expected)(Nil)
    val actual = test.features
    actual should contain theSameElementsAs expected
  }

  it should "return the expected `CrfJniInput` for a combination of feature analysers (String, Boolean, Double, Int - (all supported types)) for input `arbitrary`" in {
    val input = "arbitrary"
    val feature1 = CrfFeatureTestImpl[String]("name1")(str => str)
    val feature2 = CrfFeatureTestImpl[Boolean]("name2")(str => true)
    val feature3 = CrfFeatureTestImpl[Double]("name3")(str => 0d)
    val feature4 = CrfFeatureTestImpl[Int]("name4")(str => 0)
    val test = CrfFeaturesImpl(Seq(feature1, feature2, feature3, feature4))(Nil)
    val actual = test toCrfJniInput input
    val expected = s"${CrfScala.arbitraryString}\t${feature1.name}\\:$input:1.0\t${feature2.name}:1.0\t${feature3.name}:0.0\t${feature4.name}:0.0\n"
    actual shouldBe expected
  }

  it should "return the expected `CrfJniInput` for a combination of feature analysers (String, Boolean, Double, Int - (all supported types)) for input `arbitrary:arbitrary`" in {
    val input = "arbitrary:arbitrary"
    val feature1 = CrfFeatureTestImpl[String]("name1")(str => str)
    val feature2 = CrfFeatureTestImpl[Boolean]("name2")(str => true)
    val feature3 = CrfFeatureTestImpl[Double]("name3")(str => 0d)
    val feature4 = CrfFeatureTestImpl[Int]("name4")(str => 0)
    val test = CrfFeaturesImpl(Seq(feature1, feature2, feature3, feature4))(Nil)
    val actual = test toCrfJniInput input
    val expected = s"${CrfScala.arbitraryString}\t${feature1.name}\\:${input.replace(":", "\\:")}:1.0\t${feature2.name}:1.0\t${feature3.name}:0.0\t${feature4.name}:0.0\n"
    actual shouldBe expected
  }
}

case class CrfFeaturesImpl(override val features : Seq[CrfFeature[_]])(override val aggregateFeatures : Seq[CrfAggregateFeature[_]]) extends CrfFeatures