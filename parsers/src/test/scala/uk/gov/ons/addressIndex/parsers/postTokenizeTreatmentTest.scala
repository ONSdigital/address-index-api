package uk.gov.ons.addressIndex.parsers

import org.scalatest.{Assertion, FlatSpec, Matchers}
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult

class postTokenizeTreatmentTest extends FlatSpec with Matchers {

  it should "transform buildingName into paoStartNumber and paoStartSuffix" in {
    // Given
    val input = List(
        CrfTokenResult(
          value = "65b",
          label = Tokens.buildingName
        )
      )

    val expected = Map(
      Tokens.buildingName -> "65b",
      Tokens.paoStartNumber -> "65",
      Tokens.paoStartSuffix -> "b"
    )

    // When
    val actual = Tokens.postTokenizeTreatment(input)

    // Then
    actual shouldBe expected
  }

  it should "transform buildingNumber into paoStartNumber" in {
    // Given
    val input = List(
      CrfTokenResult(
        value = "65",
        label = Tokens.buildingNumber
      )
    )

    val expected = Map(
      Tokens.buildingNumber -> "65",
      Tokens.paoStartNumber -> "65"
    )

    // When
    val actual = Tokens.postTokenizeTreatment(input)

    // Then
    actual shouldBe expected
  }

  it should "transform buildingNumber and buildingName into paoStartNumber and paoStartSuffix" in {
    // Given
    val input = List(
      CrfTokenResult(
        value = "65b",
        label = Tokens.buildingName
      ),
      CrfTokenResult(
        value = "12",
        label = Tokens.buildingNumber
      )
    )

    val expected = Map(
      Tokens.buildingName -> "65b",
      Tokens.buildingNumber -> "12",
      Tokens.paoStartNumber -> "12",
      Tokens.paoStartSuffix -> "b"
    )

    // When
    val actual = Tokens.postTokenizeTreatment(input)

    // Then
    actual shouldBe expected
  }

  it should "transform buildingName into paoStartNumber and paoEndSuffix" in {
    // Given
    val input = List(
      CrfTokenResult(
        value = "120-122",
        label = Tokens.buildingName
      )
    )

    val expected = Map(
      Tokens.buildingName -> "120-122",
      Tokens.paoStartNumber -> "120",
      Tokens.paoEndNumber -> "122"
    )

    // When
    val actual = Tokens.postTokenizeTreatment(input)

    // Then
    actual shouldBe expected
  }

  it should "NOT transform buildingName if it doesn't follow the pattern" in {
    // Given
    val input = List(
      CrfTokenResult(
        value = "120-A",
        label = Tokens.buildingName
      )
    )

    val expected = Map(
      Tokens.buildingName -> "120-A"
    )

    // When
    val actual = Tokens.postTokenizeTreatment(input)

    // Then
    actual shouldBe expected
  }

  it should "NOT transform buildingName if it is not a building number" in {
    // Given
    val input = List(
      CrfTokenResult(
        value = "65 120-122",
        label = Tokens.buildingName
      )
    )

    val expected = Map(
      Tokens.buildingName -> "65 120-122"
    )

    // When
    val actual = Tokens.postTokenizeTreatment(input)

    // Then
    actual shouldBe expected
  }

}
