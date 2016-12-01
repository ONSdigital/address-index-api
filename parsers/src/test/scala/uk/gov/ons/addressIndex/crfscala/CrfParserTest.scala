package uk.gov.ons.addressIndex.crfscala

import org.scalatest.{FlatSpec, Matchers}
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokens
import uk.gov.ons.addressIndex.parsers.Tokens

class CrfParserTest extends FlatSpec with Matchers {

  ignore should "return the expected `CrfJniInput` for a combination of feature analysers (String, Boolean, Double, Int - (all supported types)) for `CrfTokens`" in {
    val token1 = "token1"
    val input: CrfTokens = Array(token1)

    val feature1 = CrfFeatureTestImpl[String]("name1")(str => str)
    val feature2 = CrfFeatureTestImpl[Boolean]("name2")(str => true)
    val feature3 = CrfFeatureTestImpl[Double]("name3")(str => 0d)
    val feature4 = CrfFeatureTestImpl[Int]("name4")(str => 0)
    val features = CrfFeaturesImpl(Seq(feature1, feature2, feature3, feature4))


//    val actual = test toCrfJniInput token1

    val actual = CrfParserImpl.parse(token1, features, Tokens)

    val expected = s"\t${feature1.name}\\:${token1.replace(":", "\\:")}:1.0\t${feature2.name}:1.0\t${feature3.name}:0.0\t${feature4.name}:0.0\n"
    actual shouldBe expected
  }
}

object CrfParserImpl extends CrfParser
