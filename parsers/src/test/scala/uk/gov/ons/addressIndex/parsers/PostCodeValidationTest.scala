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
    val actual = Tokens.postTokenizeTreatmentPostCode(anInput)
    actual shouldBe theExpected
  }

  it should "BD176QL -> BD17 6QL" in {
    // Given
    val input = Map(
      Tokens.postcode -> Seq(
        CrfTokenResult(
          value = "BD176QL",
          label = Tokens.postcode
        )
      )
    )

    val expected = Map(
      Tokens.postcode -> Seq(
        CrfTokenResult(
          value = "BD17 6QL",
          label = Tokens.postcode
        )
      ),
      Tokens.postcodeOut -> Seq(
        CrfTokenResult(
          value = "BD17",
          label = Tokens.postcodeOut
        )
      ),
      Tokens.postcodeIn -> Seq(
        CrfTokenResult(
          value = "6QL",
          label = Tokens.postcodeIn
        )
      )
    )

    // When
    val actual = Tokens.postTokenizeTreatmentPostCode(input)

    // Then
    actual shouldBe expected
  }

  it should "HA62YY -> HA6 2YY" in {
    // Given
    val input = Map(
      Tokens.postcode -> Seq(
        CrfTokenResult(
          value = "HA62YY",
          label = Tokens.postcode
        )
      )
    )

    val expected = Map(
      Tokens.postcode -> Seq(
        CrfTokenResult(
          value = "HA6 2YY",
          label = Tokens.postcode
        )
      ),
      Tokens.postcodeOut -> Seq(
        CrfTokenResult(
          value = "HA6",
          label = Tokens.postcodeOut
        )
      ),
      Tokens.postcodeIn -> Seq(
        CrfTokenResult(
          value = "2YY",
          label = Tokens.postcodeIn
        )
      )
    )

    // When
    val actual = Tokens.postTokenizeTreatmentPostCode(input)

    // Then
    actual shouldBe expected
  }

  it should "BH234TR -> BH23 4TR" in {
    // Given
    val input = Map(
      Tokens.postcode -> Seq(
        CrfTokenResult(
          value = "BH234TR",
          label = Tokens.postcode
        )
      )
    )

    val expected = Map(
      Tokens.postcode -> Seq(
        CrfTokenResult(
          value = "BH23 4TR",
          label = Tokens.postcode
        )
      ),
      Tokens.postcodeOut -> Seq(
        CrfTokenResult(
          value = "BH23",
          label = Tokens.postcodeOut
        )
      ),
      Tokens.postcodeIn -> Seq(
        CrfTokenResult(
          value = "4TR",
          label = Tokens.postcodeIn
        )
      )
    )

    // When
    val actual = Tokens.postTokenizeTreatmentPostCode(input)

    // Then
    actual shouldBe expected
  }

  it should "BA140HU -> BA14 0HU" in {
    // Given
    val input = Map(
      Tokens.postcode -> Seq(
        CrfTokenResult(
          value = "BA140HU",
          label = Tokens.postcode
        )
      )
    )

    val expected = Map(
      Tokens.postcode -> Seq(
        CrfTokenResult(
          value = "BA14 0HU",
          label = Tokens.postcode
        )
      ),
      Tokens.postcodeOut -> Seq(
        CrfTokenResult(
          value = "BA14",
          label = Tokens.postcodeOut
        )
      ),
      Tokens.postcodeIn -> Seq(
        CrfTokenResult(
          value = "0HU",
          label = Tokens.postcodeIn
        )
      )
    )

    // When
    val actual = Tokens.postTokenizeTreatmentPostCode(input)

    // Then
    actual shouldBe expected
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

  it should "X1AA -> X 1AA" in {
    // Given
    val input = Map(
      Tokens.postcode -> Seq(
        CrfTokenResult(
          value = "X1AA",
          label = Tokens.postcode
        )
      )
    )

    val expected = Map(
      Tokens.postcode -> Seq(
        CrfTokenResult(
          value = "X 1AA",
          label = Tokens.postcode
        )
      ),
      Tokens.postcodeOut -> Seq(
        CrfTokenResult(
          value = "X",
          label = Tokens.postcodeOut
        )
      ),
      Tokens.postcodeIn -> Seq(
        CrfTokenResult(
          value = "1AA",
          label = Tokens.postcodeIn
        )
      )
    )

    // When
    val actual = Tokens.postTokenizeTreatmentPostCode(input)

    // Then
    actual shouldBe expected
  }

  it should "L1234 -> " in {
    // Given
    val input = Map(
      Tokens.postcode -> Seq(
        CrfTokenResult(
          value = "L1234",
          label = Tokens.postcode
        )
      )
    )
    val expected = Map.empty

    // When
    val actual = Tokens.postTokenizeTreatmentPostCode(input)

    // Then
    actual shouldBe expected
  }
}
