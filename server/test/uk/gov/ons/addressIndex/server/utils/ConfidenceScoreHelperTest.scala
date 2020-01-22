package uk.gov.ons.addressIndex.server.utils

import org.scalatest.{FlatSpec, Matchers}
import play.api.Logger
import uk.gov.ons.addressIndex.parsers.Tokens


/**
  * Unit tests for all the methods in the Confidence Score calculation class
  */
class ConfidenceScoreHelperTest extends FlatSpec with Matchers {

  val logger: Logger = Logger("HopperScoreHelperTest")

  val mockAddressTokens: Map[String, String] = Map(
    Tokens.buildingNumber -> "7",
    Tokens.paoStartNumber -> "7",
    Tokens.streetName -> "GATE REACH",
    Tokens.townName -> "EXETER",
    Tokens.postcode -> "PO7 6GA",
    Tokens.postcodeIn -> "6GA",
    Tokens.postcodeOut -> "PO7"
  )

  val mockAddressTokens2: Map[String, String] = Map(
    Tokens.buildingNumber -> "7",
    Tokens.subBuildingName -> "MY SHED",
    Tokens.paoStartNumber -> "7",
    Tokens.streetName -> "GATE REACH",
    Tokens.townName -> "EXETER",
    Tokens.postcode -> "PO7 6GA",
    Tokens.postcodeIn -> "6GA",
    Tokens.postcodeOut -> "PO7"
  )

  it should "calculate the confidence score for a single address without structure tokens " in {
    // Given
    val tokens = mockAddressTokens
    val structuralScore = 1d
    val unitScore = -1d
    val elasticRatio = 1.3
    val expected = 99.92d

    // When
    val actual = ConfidenceScoreHelper.calculateConfidenceScore(tokens, structuralScore, unitScore, elasticRatio)

    // Then
    actual shouldBe expected
  }

  it should "calculate the confidence score for a single address with structure tokens " in {
    // Given
    val tokens = mockAddressTokens2
    val structuralScore = 1d
    val unitScore = 0.5d
    val elasticRatio = 0.98
    val expected = 51.3258d

    // When
    val actual = ConfidenceScoreHelper.calculateConfidenceScore(tokens, structuralScore, unitScore, elasticRatio)

    // Then
    actual shouldBe expected
  }

  it should "calculate the confidence score for a single address from a batch with only one match " in {
    // Given
    val tokens = mockAddressTokens2
    val structuralScore = 1d
    val unitScore = 0.5d
    val elasticRatio = 1.2
    val expected = 99.2077d

    // When
    val actual = ConfidenceScoreHelper.calculateConfidenceScore(tokens, structuralScore, unitScore, elasticRatio)

    // Then
    actual shouldBe expected
  }

  it should "calculate the elastic denominator for a list of scores with equal top two " in {
    // Given
    val scores = Seq(10F, 10F, 5F, 3F)
    val expected = 10d

    // When
    val actual = ConfidenceScoreHelper.calculateElasticDenominator(scores)

    // Then
    actual shouldBe expected
  }

  it should "calculate the elastic denominator for a list of scores with different top two " in {
    // Given
    val scores = Seq(10F, 5F, 3F, 2F)
    val expected = 7.5d

    // When
    val actual = ConfidenceScoreHelper.calculateElasticDenominator(scores)

    // Then
    actual shouldBe expected
  }

  it should "calculate the elastic denominator for a list with only one " in {
    // Given
    val scores = Seq(10F)
    val expected = -1d

    // When
    val actual = ConfidenceScoreHelper.calculateElasticDenominator(scores)

    // Then
    actual shouldBe expected
  }

  it should "calculate the elastic ratio for a list with only one " in {
    // Given
    val scores = Seq(4.6F)
    val expected = 1.2d

    // When
    val elasticDenominator = ConfidenceScoreHelper.calculateElasticDenominator(scores)
    val safeDenominator = if (elasticDenominator == 0) 1 else elasticDenominator
    val actual = if (elasticDenominator == -1D) 1.2D else 4.6F / safeDenominator

    // Then
    actual shouldBe expected
  }

}
