package uk.gov.ons.addressIndex.parsers

import org.scalatest.{FlatSpec, Matchers}
import uk.gov.ons.addressIndex.crfscala.CrfScala.TokenResult

/**
  * This test file's expected values were created by using a Python Prototype.
  */
class AddressParserTest extends FlatSpec with Matchers {

  ignore should "create tokens and return the tokens with their results for `token1 token2`" in {
    val input = "token1 token2"
    val token = "Postcode"
    val expected = List(
      TokenResult(token, "token1"),
      TokenResult(token, "token2")
    )
    val actual = AddressParser.parse(input, FeatureAnalysers.Predef.all)
    expected should contain theSameElementsAs actual
  }

  ignore should "create tokens and return the tokens with their results for `token1,token2`" in {
    val input = "token1,token2"
    val token = "Postcode"
    val expected = List(
      TokenResult(token, "token1"),
      TokenResult(token, "token2")
    )
    val actual = AddressParser.parse(input, FeatureAnalysers.Predef.all)
    expected should contain theSameElementsAs actual
  }

  ignore should "create tokens and return the tokens with their results for `token1, token2`" in {
    val input = "token1, token2"
    val token = "Postcode"
    val expected = List(
      TokenResult(token, "token1"),
      TokenResult(token, "token2")
    )
    val actual = AddressParser.parse(input, FeatureAnalysers.Predef.all)
    expected should contain theSameElementsAs actual
  }

  it should "create tokens and return the tokens with their results for `token1`" in {
    val input = "token1"
    val token = "Postcode"
    val expected = List(
      TokenResult(token, "token1")
    )
    val actual = AddressParser.parse(input, FeatureAnalysers.Predef.all)
    expected should contain theSameElementsAs actual
  }
}