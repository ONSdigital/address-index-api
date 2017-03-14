package uk.gov.ons.addressIndex.parsers

import org.scalatest.{FlatSpec, Matchers}

class PostCodeValidationTest extends FlatSpec with Matchers {

  it should "BD176QL -> BD17 6QL" in {
    // Given
    val input = Map(Tokens.postcode -> "BD176QL")

    val expected = Map(
      Tokens.postcode -> "BD17 6QL",
      Tokens.postcodeOut -> "BD17",
      Tokens.postcodeIn -> "6QL"
    )

    // When
    val actual = Tokens.postTokenizeTreatmentPostCode(input)

    // Then
    actual shouldBe expected
  }

  it should "BD17 6QL -> BD17 6QL (if the postcode is splitted by space)" in {
    // Given
    val input = Map(
      Tokens.postcode -> "BD17 6QL"
    )

    val expected = Map(
      Tokens.postcode -> "BD17 6QL",
      Tokens.postcodeOut -> "BD17",
      Tokens.postcodeIn -> "6QL"
    )

    // When
    val actual = Tokens.postTokenizeTreatmentPostCode(input)

    // Then
    actual shouldBe expected
  }

  it should "HA62YY -> HA6 2YY" in {
    // Given
    val input = Map(
      Tokens.postcode -> "HA62YY"
    )

    val expected = Map(
      Tokens.postcode -> "HA6 2YY",
      Tokens.postcodeOut -> "HA6",
      Tokens.postcodeIn -> "2YY"
    )

    // When
    val actual = Tokens.postTokenizeTreatmentPostCode(input)

    // Then
    actual shouldBe expected
  }

  it should "BH234TR -> BH23 4TR" in {
    // Given
    val input = Map(
      Tokens.postcode -> "BH234TR"
    )

    val expected = Map(
      Tokens.postcode -> "BH23 4TR",
      Tokens.postcodeOut -> "BH23",
      Tokens.postcodeIn -> "4TR"
    )

    // When
    val actual = Tokens.postTokenizeTreatmentPostCode(input)

    // Then
    actual shouldBe expected
  }

  it should "BA140HU -> BA14 0HU" in {
    // Given
    val input = Map(
      Tokens.postcode -> "BA140HU"
    )

    val expected = Map(
      Tokens.postcode -> "BA14 0HU",
      Tokens.postcodeOut -> "BA14",
      Tokens.postcodeIn -> "0HU"
    )

    // When
    val actual = Tokens.postTokenizeTreatmentPostCode(input)

    // Then
    actual shouldBe expected
  }

  it should "RH1 -> RH1" in {
    // Given
    val input = Map(
      Tokens.postcode -> "RH1"
    )

    val expected = Map(
      Tokens.postcode -> "RH1",
      Tokens.postcodeOut -> "RH1"
    )

    // When
    val actual = Tokens.postTokenizeTreatmentPostCode(input)

    // Then
    actual shouldBe expected
  }

  it should "L1 -> L1" in {
    // Given
    val input = Map(
      Tokens.postcode -> "L1"
    )

    val expected = Map(
      Tokens.postcode -> "L1",
      Tokens.postcodeOut -> "L1"
    )

    // When
    val actual = Tokens.postTokenizeTreatmentPostCode(input)

    // Then
    actual shouldBe expected
  }

  it should "1AA -> 1AA" in {
    // Given
    val input = Map(
      Tokens.postcode -> "1AA"
    )

    val expected = Map(
      Tokens.postcode -> "1AA",
      Tokens.postcodeOut -> "1AA"
    )

    // When
    val actual = Tokens.postTokenizeTreatmentPostCode(input)

    // Then
    actual shouldBe expected
  }

  it should "X1AA -> X 1AA" in {
    // Given
    val input = Map(
      Tokens.postcode -> "X1AA"
    )

    val expected = Map(
      Tokens.postcode -> "X1AA",
      Tokens.postcodeOut -> "X1AA"
    )

    // When
    val actual = Tokens.postTokenizeTreatmentPostCode(input)

    // Then
    actual shouldBe expected
  }

  it should "L1234 -> L1 234" in {
    // even if the inCode does not look valid, it is still an input from the user
    // Given
    val input = Map(
      Tokens.postcode -> "L1234"
    )

    val expected = Map(
      Tokens.postcode -> "L1 234",
      Tokens.postcodeOut -> "L1",
      Tokens.postcodeIn -> "234"
    )

    // When
    val actual = Tokens.postTokenizeTreatmentPostCode(input)

    // Then
    actual shouldBe expected
  }
}
