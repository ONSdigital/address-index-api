package uk.gov.ons.addressIndex.crfscala

import org.scalatest.{FlatSpec, Matchers}
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokens

class CrfParserTest extends FlatSpec with Matchers {

  it should "return the expected `CrfJniInput` for a combination of feature analysers (String, Boolean, Double, Int - (all supported types)) for `CrfTokens` size 1" in {
    val token1 = "token1"
    val input: CrfTokens = Array(token1)
    val feature1 = CrfFeatureTestImpl[String]("name1")(identity)
    val feature2 = CrfFeatureTestImpl[Boolean]("name2")(str => true)
    val feature3 = CrfFeatureTestImpl[Double]("name3")(str => 0d)
    val feature4 = CrfFeatureTestImpl[Int]("name4")(str => 0)
    val features = CrfFeaturesImpl(Seq(feature1, feature2, feature3, feature4))(Nil)
    val actual = features toCrfJniInput token1
    val expected = s"\t${feature1.name}\\:${token1.replace(":", "\\:")}:1.0\t${feature2.name}:1.0\t${feature3.name}:0.0\t${feature4.name}:0.0\n"
    actual shouldBe expected
  }

  ignore should "return the expected `CrfJniInput` for a combination of feature analysers (String, Boolean, Double, Int - (all supported types)) for `CrfTokens` size 2" in {
    val token1 = "token1"
    val token2 = "token2"
    val input: CrfTokens = Array(token1, token2)
    val feature1 = CrfFeatureTestImpl[String]("name1")(identity)
    val feature2 = CrfFeatureTestImpl[Boolean]("name2")(str => true)
    val feature3 = CrfFeatureTestImpl[Double]("name3")(str => 0d)
    val feature4 = CrfFeatureTestImpl[Int]("name4")(str => 0)
    val features = CrfFeaturesImpl(Seq(feature1, feature2, feature3, feature4))(Nil)
    val actual = features toCrfJniInput token1

    val expected1 = s"\t${feature1.name}\\:${token1.replace(":", "\\:")}:1.0\t${feature2.name}:1.0\t${feature3.name}:0.0\t${feature4.name}:0.0"
    val expected1next = s"\tnext:\\${feature1.name}\\:${token2.replace(":", "\\:")}:1.0\tnext:\\${feature2.name}:1.0\tnext:\\${feature3.name}:0.0\tnext:\\${feature4.name}:0.0\n"

    val expected2 = s"\t${feature1.name}\\:${token2.replace(":", "\\:")}:1.0\t${feature2.name}:1.0\t${feature3.name}:0.0\t${feature4.name}:0.0"
    val expected2prev = s"\tprevious\\:${feature1.name}\\:${token1.replace(":", "\\:")}:1.0\tprevious\\:${feature2.name}:1.0\tprevious\\:${feature3.name}:0.0\tprevious\\:${feature4.name}:0.0\n"




    val expected = s"\t${feature1.name}\\:${token1.replace(":", "\\:")}:1.0\t${feature2.name}:1.0\t${feature3.name}:0.0\t${feature4.name}:0.0\tnext\\:${feature1.name}\\:${token2.replace(":", "\\:")}:1.0\tnext\\:${feature2.name}:1.0\tnext\\:${feature3.name}:0.0\tnext\\:${feature4.name}:0.0\n"

    actual shouldBe expected
  }
}

//object CrfParserImpl extends CrfParser
