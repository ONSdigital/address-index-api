package uk.gov.ons.addressIndex.parsers

import org.scalatest.{Assertion, FlatSpec, Matchers}
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult

class PostCodeValidationTest extends FlatSpec with Matchers {

  def testHelper(input: String, expected: String): Assertion = {
    val anInput = Map(
      Tokens.postcode -> Seq(
        CrfTokenResult(
          value = input,
          label = Tokens.postcode
        )
      )
    )
    val theExpected = Map(
      Tokens.postcode -> Seq(
        CrfTokenResult(
          value = expected,
          label = Tokens.postcode
        )
      )
    )
    val actual = Tokens.validatePostCode(anInput)
    actual shouldBe theExpected
  }

  it should "BD176QL -> BD17 6QL" in {
    testHelper(
      input = "BD176QL",
      expected = "BD17 6QL"
    )
  }

  it should "HA62YY -> HA6 2YY" in {
    testHelper(
      input = "HA62YY",
      expected = "HA6 2YY"
    )
  }

  it should "BH234TR -> BH23 4TR" in {
    testHelper(
      input = "BH234TR",
      expected = "BH23 4TR"
    )
  }

  it should "BA140HU -> BA14 0HU" in {
    testHelper(
      input = "BA140HU",
      expected = "BA14 0HU"
    )
  }

  it should "RH1 -> RH1" in {
    testHelper(
      input = "RH1",
      expected = "RH1"
    )
  }

  it should "L1 -> L1" in {
    testHelper(
      input = "L1",
      expected = "L1"
    )
  }

  it should "1AA -> 1AA" in {
    testHelper(
      input = "1AA",
      expected = "1AA"
    )
  }

  //These ingored tests are bounds tests for postcode, these occurrences will never happen
  //because the parser will pick them up as building number.
  //still interesting to keep.
  ignore should "17B -> " in {
    val input = Map(
      Tokens.postcode -> Seq(
        CrfTokenResult(
          value = "17B",
          label = Tokens.postcode
        )
      )
    )
    val expected = Map.empty
    val actual = Tokens.validatePostCode(input)
    actual shouldBe expected
  }

  ignore should "17 -> " in {
    val input = Map(
      Tokens.postcode -> Seq(
        CrfTokenResult(
          value = "17",
          label = Tokens.postcode
        )
      )
    )
    val expected = Map.empty
    val actual = Tokens.validatePostCode(input)
    actual shouldBe expected
  }

  ignore should "1A -> " in {
    val input = Map(
      Tokens.postcode -> Seq(
        CrfTokenResult(
          value = "1A",
          label = Tokens.postcode
        )
      )
    )
    val expected = Map.empty
    val actual = Tokens.validatePostCode(input)
    actual shouldBe expected
  }

  ignore should "12 -> " in {
    val input = Map(
      Tokens.postcode -> Seq(
        CrfTokenResult(
          value = "12",
          label = Tokens.postcode
        )
      )
    )
    val expected = Map.empty
    val actual = Tokens.validatePostCode(input)
    actual shouldBe expected
  }

  it should "L1234 -> " in {
    val input = Map(
      Tokens.postcode -> Seq(
        CrfTokenResult(
          value = "L1234",
          label = Tokens.postcode
        )
      )
    )
    val expected = Map.empty
    val actual = Tokens.validatePostCode(input)
    actual shouldBe expected
  }
}
