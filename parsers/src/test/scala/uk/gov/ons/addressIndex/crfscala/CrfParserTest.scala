package uk.gov.ons.addressIndex.crfscala

import org.scalatest.{FlatSpec, Matchers}
import uk.gov.ons.addressIndex.parsers.Tokens

class CrfParserTest extends FlatSpec with Matchers {

  it should "return the expected `CrfJniInput` for a combination of feature analysers (String, Boolean, Double, Int - (all supported types)) for `CrfTokens` size 1" in {
    val token1 = "token1"
    val input = token1
    val feature1 = CrfFeatureTestImpl[String]("name1")(identity)
    val feature2 = CrfFeatureTestImpl[Boolean]("name2")(str => true)
    val feature3 = CrfFeatureTestImpl[Double]("name3")(str => 0d)
    val feature4 = CrfFeatureTestImpl[Int]("name4")(str => 0)
    val features = CrfFeaturesImpl(Seq(feature1, feature2, feature3, feature4))(Nil)
    val actual = CrfParserImpl.parse(input, features, Tokens)
    val expected = s"\t${feature1.name}\\:${token1.replace(":", "\\:").toUpperCase}:1.0\t${feature2.name}:1.0\t${feature3.name}:0.0\t${feature4.name}:0.0\tsingleton:1.0\n"
    actual shouldBe expected
  }

  it should "return the expected `CrfJniInput` for a combination of feature analysers (String, Boolean, Double, Int - (all supported types)) for `CrfTokens` size 2" in {
    val token1 = "token1"
    val token2 = "token2"
    val input = s"$token1, $token2"
    val feature1 = CrfFeatureTestImpl[String]("f1")(identity)
    val feature2 = CrfFeatureTestImpl[Boolean]("f2")(str => true)
    val feature3 = CrfFeatureTestImpl[Double]("f3")(str => 3d)
    val feature4 = CrfFeatureTestImpl[Int]("f4")(str => 7)
    val features = CrfFeaturesImpl(Seq(feature1, feature2, feature3, feature4))(Nil)
    val actual = CrfParserImpl.parse(input, features, Tokens)
    val token1Expectedf1 = s"\t${feature1.name}\\:${token1.replace(":", "\\:").toUpperCase}:1.0\tnext\\:${feature1.name}\\:${token2.replace(":", "\\:").toUpperCase}:1.0"
    val token1Expectedf2 = s"\t${feature2.name}:1.0\tnext\\:${feature2.name}:1.0"
    val token1Expectedf3 = s"\t${feature3.name}:3.0\tnext\\:${feature3.name}:3.0"
    val token1Expectedf4 = s"\t${feature4.name}:7.0\tnext\\:${feature4.name}:7.0\trawstring.start:1.0\tnext\\:rawstring.end:1.0\n"
    val token2Expectedf1 = s"\t${feature1.name}\\:${token2.replace(":", "\\:").toUpperCase}:1.0\tprevious\\:${feature1.name}\\:${token1.replace(":", "\\:").toUpperCase}:1.0"
    val token2Expectedf2 = s"\t${feature2.name}:1.0\tprevious\\:${feature2.name}:1.0"
    val token2Expectedf3 = s"\t${feature3.name}:3.0\tprevious\\:${feature3.name}:3.0"
    val token2Expectedf4 = s"\t${feature4.name}:7.0\tprevious\\:${feature4.name}:7.0\trawstring.end:1.0\tnext\\:rawstring.start:1.0\n"
    val expected =
      token1Expectedf1 + token1Expectedf2 + token1Expectedf3 + token1Expectedf4 +
      token2Expectedf1 + token2Expectedf2 + token2Expectedf3 + token2Expectedf4
    actual shouldBe expected
  }

  it should "return the expected `CrfJniInput` for a combination of feature analysers (String, Boolean, Double, Int - (all supported types)) for `CrfTokens` size multiple (3)" in {
    val token1 = "token1"
    val token2 = "token2"
    val token3 = "token3"
    val input = s"$token1, $token2, $token3"
    val feature1 = CrfFeatureTestImpl[String]("f1")(identity)
    val feature2 = CrfFeatureTestImpl[Boolean]("f2")(str => true)
    val feature3 = CrfFeatureTestImpl[Double]("f3")(str => 3d)
    val feature4 = CrfFeatureTestImpl[Int]("f4")(str => 7)
    val features = CrfFeaturesImpl(Seq(feature1, feature2, feature3, feature4))(Nil)
    val actual = CrfParserImpl.parse(input, features, Tokens)
    val token1Expectedf1 = s"\t${feature1.name}\\:${token1.replace(":", "\\:").toUpperCase}:1.0\tnext\\:${feature1.name}\\:${token2.replace(":", "\\:").toUpperCase}:1.0"
    val token1Expectedf2 = s"\t${feature2.name}:1.0\tnext\\:${feature2.name}:1.0"
    val token1Expectedf3 = s"\t${feature3.name}:3.0\tnext\\:${feature3.name}:3.0"
    val token1Expectedf4 = s"\t${feature4.name}:7.0\tnext\\:${feature4.name}:7.0\trawstring.start:1.0\n"
    val token2Expectedf1 = s"\t${feature1.name}\\:${token2.replace(":", "\\:").toUpperCase}:1.0\tnext\\:${feature1.name}\\:${token3.replace(":", "\\:").toUpperCase}:1.0\tprevious\\:${feature1.name}\\:${token1.replace(":", "\\:").toUpperCase}:1.0"
    val token2Expectedf2 = s"\t${feature2.name}:1.0\tnext\\:${feature2.name}:1.0\tprevious\\:${feature2.name}:1.0"
    val token2Expectedf3 = s"\t${feature3.name}:3.0\tnext\\:${feature3.name}:3.0\tprevious\\:${feature3.name}:3.0"
    val token2Expectedf4 = s"\t${feature4.name}:7.0\tnext\\:${feature4.name}:7.0\tprevious\\:${feature4.name}:7.0\n"
    val token3Expectedf1 = s"\t${feature1.name}\\:${token3.replace(":", "\\:").toUpperCase}:1.0\tprevious\\:${feature1.name}\\:${token2.replace(":", "\\:").toUpperCase}:1.0"
    val token3Expectedf2 = s"\t${feature2.name}:1.0\tprevious\\:${feature2.name}:1.0"
    val token3Expectedf3 = s"\t${feature3.name}:3.0\tprevious\\:${feature3.name}:3.0"
    val token3Expectedf4 = s"\t${feature4.name}:7.0\tprevious\\:${feature4.name}:7.0\trawstring.end:1.0\n"
    val expected =
      token1Expectedf1 + token1Expectedf2 + token1Expectedf3 + token1Expectedf4 +
      token2Expectedf1 + token2Expectedf2 + token2Expectedf3 + token2Expectedf4 +
      token3Expectedf1 + token3Expectedf2 + token3Expectedf3 + token3Expectedf4
    actual shouldBe expected
  }

  it should "return the expected `CrfJniInput` for a combination of feature analysers (String, Boolean, Double, Int - (all supported types)) for `CrfTokens` size multiple (4)" in {
    val token1 = "token1"
    val token2 = "token2"
    val token3 = "token3"
    val token4 = "token4"
    val input = s"$token1, $token2, $token3, $token4"
    val feature1 = CrfFeatureTestImpl[String]("f1")(identity)
    val feature2 = CrfFeatureTestImpl[Boolean]("f2")(str => true)
    val feature3 = CrfFeatureTestImpl[Double]("f3")(str => 3d)
    val feature4 = CrfFeatureTestImpl[Int]("f4")(str => 7)
    val features = CrfFeaturesImpl(Seq(feature1, feature2, feature3, feature4))(Nil)
    val actual = CrfParserImpl.parse(input, features, Tokens)
    val token1Expectedf1 = s"\t${feature1.name}\\:${token1.replace(":", "\\:").toUpperCase}:1.0\tnext\\:${feature1.name}\\:${token2.replace(":", "\\:").toUpperCase}:1.0"
    val token1Expectedf2 = s"\t${feature2.name}:1.0\tnext\\:${feature2.name}:1.0"
    val token1Expectedf3 = s"\t${feature3.name}:3.0\tnext\\:${feature3.name}:3.0"
    val token1Expectedf4 = s"\t${feature4.name}:7.0\tnext\\:${feature4.name}:7.0\trawstring.start:1.0\n"
    val token2Expectedf1 = s"\t${feature1.name}\\:${token2.replace(":", "\\:").toUpperCase}:1.0\tnext\\:${feature1.name}\\:${token3.replace(":", "\\:").toUpperCase}:1.0\tprevious\\:${feature1.name}\\:${token1.replace(":", "\\:").toUpperCase}:1.0"
    val token2Expectedf2 = s"\t${feature2.name}:1.0\tnext\\:${feature2.name}:1.0\tprevious\\:${feature2.name}:1.0"
    val token2Expectedf3 = s"\t${feature3.name}:3.0\tnext\\:${feature3.name}:3.0\tprevious\\:${feature3.name}:3.0"
    val token2Expectedf4 = s"\t${feature4.name}:7.0\tnext\\:${feature4.name}:7.0\tprevious\\:${feature4.name}:7.0\n"
    val token3Expectedf1 = s"\t${feature1.name}\\:${token3.replace(":", "\\:").toUpperCase}:1.0\tnext\\:${feature1.name}\\:${token4.replace(":", "\\:").toUpperCase}:1.0\tprevious\\:${feature1.name}\\:${token2.replace(":", "\\:").toUpperCase}:1.0"
    val token3Expectedf2 = s"\t${feature2.name}:1.0\tnext\\:${feature2.name}:1.0\tprevious\\:${feature2.name}:1.0"
    val token3Expectedf3 = s"\t${feature3.name}:3.0\tnext\\:${feature3.name}:3.0\tprevious\\:${feature3.name}:3.0"
    val token3Expectedf4 = s"\t${feature4.name}:7.0\tnext\\:${feature4.name}:7.0\tprevious\\:${feature4.name}:7.0\n"
    val token4Expectedf1 = s"\t${feature1.name}\\:${token4.replace(":", "\\:").toUpperCase}:1.0\tprevious\\:${feature1.name}\\:${token3.replace(":", "\\:").toUpperCase}:1.0"
    val token4Expectedf2 = s"\t${feature2.name}:1.0\tprevious\\:${feature2.name}:1.0"
    val token4Expectedf3 = s"\t${feature3.name}:3.0\tprevious\\:${feature3.name}:3.0"
    val token4Expectedf4 = s"\t${feature4.name}:7.0\tprevious\\:${feature4.name}:7.0\trawstring.end:1.0\n"
    val expected =
      token1Expectedf1 + token1Expectedf2 + token1Expectedf3 + token1Expectedf4 +
      token2Expectedf1 + token2Expectedf2 + token2Expectedf3 + token2Expectedf4 +
      token3Expectedf1 + token3Expectedf2 + token3Expectedf3 + token3Expectedf4 +
      token4Expectedf1 + token4Expectedf2 + token4Expectedf3 + token4Expectedf4
    actual shouldBe expected
  }
}

object CrfParserImpl extends CrfParser
