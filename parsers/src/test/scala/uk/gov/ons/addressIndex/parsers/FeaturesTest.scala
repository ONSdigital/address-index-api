package uk.gov.ons.addressIndex.parsers

import org.scalatest.{FlatSpec, Matchers}

class FeaturesTest extends FlatSpec with Matchers {

  it should "produce the correct `length` feature output for token `MANSION`" in {
    // Given
    val input = "MANSION"
    val expected = "length\\:w\\:7:1.0"

    // When
    val actual = Features.lengthFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `length` feature output for token `` (empty)" in {
    // Given
    val input = ""
    val expected = "length\\:w\\:0:1.0"

    // When
    val actual = Features.lengthFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `length` feature output for token `31`" in {
    // Given
    val input = "31"
    val expected = "length\\:d\\:2:1.0"

    // When
    val actual = Features.lengthFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `endsInPunctuation` feature output for token `HOUSE`" in {
    // Given
    val input = "HOUSE"
    val expected = "endsinpunc:0.0"

    // When
    val actual = Features.endsInPuncFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `endsInPunctuation` feature output for token `ST.`" in {
    // Given
    val input = "ST."
    val expected = "endsinpunc:1.0"

    // When
    val actual = Features.endsInPuncFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `endsInPunctuation` feature output for token `HOU.SE`" in {
    // Given
    val input = "HOU.SE"
    val expected = "endsinpunc:0.0"

    // When
    val actual = Features.endsInPuncFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `has.vowels` feature output for token `HELLO`" in {
    // Given
    val input = "HELLO"
    val expected = "has.vowels:1.0"

    // When
    val actual = Features.hasVowelsFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `has.vowels` feature output for token `HLL`" in {
    // Given
    val input = "HLL"
    val expected = "has.vowels:0.0"

    // When
    val actual = Features.hasVowelsFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `digits` feature output for token `31`" in {
    // Given
    val input = "31"
    val expected = "digits\\:all_digits:1.0"

    // When
    val actual = Features.digitsFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `digits` feature output for token `31A`" in {
    // Given
    val input = "31A"
    val expected = "digits\\:some_digits:1.0"

    // When
    val actual = Features.digitsFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `digits` feature output for token `HOUSE`" in {
    // Given
    val input = "HOUSE"
    val expected = "digits\\:no_digits:1.0"

    // When
    val actual = Features.digitsFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `hyphenations` feature output for token `HOUSE`" in {
    // Given
    val input = "HOUSE"
    val expected = "hyphenations:0.0"

    // When
    val actual = Features.hyphenationsFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `hyphenations` feature output for token `ST-ROCK`" in {
    // Given
    val input = "ST-ROCK"
    val expected = "hyphenations:1.0"

    // When
    val actual = Features.hyphenationsFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `hyphenations` feature output for token `ST-MARIE-ROCK`" in {
    // Given
    val input = "ST-MARIE-ROCK"
    val expected = "hyphenations:2.0"

    // When
    val actual = Features.hyphenationsFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `word` feature output for token `HELLO`" in {
    // Given
    val input = "HELLO"
    val expected = "word\\:HELLO:1.0"

    // When
    val actual = Features.wordFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `word` feature output for token `31`" in {
    // Given
    val input = "31"
    val expected = "word:0.0"

    // When
    val actual = Features.wordFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `word` feature output for token `HE31LLO`" in {
    // Given
    val input = "HE31LLO"
    val expected = "word\\:HE31LLO:1.0"

    // When
    val actual = Features.wordFeature(input)

    // Then
    expected shouldBe actual
  }

  /**
    * IMPORTANT: following tests are for the features that say whether or not the
    * token is in a specific list. We test that the feature uses a specific list
    * by providing 2 tokens: one from the list and the other not
    */

  it should "produce the correct `directional` feature output for token `NORTH`" in {
    // Given
    val input = "NORTH"
    val expected = "directional:1.0"

    // When
    val actual = Features.directionalFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `directional` feature output for token `AB10`" in {
    // Given
    val input = "AB10"
    val expected = "directional:0.0"

    // When
    val actual = Features.directionalFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `outcode` feature output for token `AB10`" in {
    // Given
    val input = "AB10"
    val expected = "outcode:1.0"

    // When
    val actual = Features.outCodeFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `outcode` feature output for token `ABERDEEN`" in {
    // Given
    val input = "ABERDEEN"
    val expected = "outcode:0.0"

    // When
    val actual = Features.outCodeFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `posttown` feature output for token `ABERDEEN`" in {
    // Given
    val input = "ABERDEEN"
    val expected = "posttown:1.0"

    // When
    val actual = Features.postTownFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `posttown` feature output for token `OFFICE`" in {
    // Given
    val input = "OFFICE"
    val expected = "posttown:0.0"

    // When
    val actual = Features.postTownFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `business` feature output for token `OFFICE`" in {
    // Given
    val input = "OFFICE"
    val expected = "business:1.0"

    // When
    val actual = Features.businessFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `business` feature output for token `CIC`" in {
    // Given
    val input = "CIC"
    val expected = "business:0.0"

    // When
    val actual = Features.businessFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `company` feature output for token `CIC`" in {
    // Given
    val input = "CIC"
    val expected = "company:1.0"

    // When
    val actual = Features.companyFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `company` feature output for token `FLAT`" in {
    // Given
    val input = "FLAT"
    val expected = "company:0.0"

    // When
    val actual = Features.companyFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `flat` feature output for token `FLAT`" in {
    // Given
    val input = "FLAT"
    val expected = "flat:1.0"

    // When
    val actual = Features.flatFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `flat` feature output for token `BASEMENT`" in {
    // Given
    val input = "BASEMENT"
    val expected = "flat:0.0"

    // When
    val actual = Features.flatFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `locational` feature output for token `BASEMENT`" in {
    // Given
    val input = "BASEMENT"
    val expected = "locational:1.0"

    // When
    val actual = Features.locationalFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `locational` feature output for token `FIRST`" in {
    // Given
    val input = "FIRST"
    val expected = "locational:0.0"

    // When
    val actual = Features.locationalFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `ordinal` feature output for token `FIRST`" in {
    // Given
    val input = "FIRST"
    val expected = "ordinal:1.0"

    // When
    val actual = Features.ordinalFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `ordinal` feature output for token `HOUSE`" in {
    // Given
    val input = "HOUSE"
    val expected = "ordinal:0.0"

    // When
    val actual = Features.ordinalFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `residential` feature output for token `HOUSE`" in {
    // Given
    val input = "HOUSE"
    val expected = "residential:1.0"

    // When
    val actual = Features.residentialFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `residential` feature output for token `ROAD`" in {
    // Given
    val input = "ROAD"
    val expected = "residential:0.0"

    // When
    val actual = Features.residentialFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `road` feature output for token `ROAD`" in {
    // Given
    val input = "ROAD"
    val expected = "road:1.0"

    // When
    val actual = Features.roadFeature(input)

    // Then
    expected shouldBe actual
  }

  it should "produce the correct `road` feature output for token `HOUSE`" in {
    // Given
    val input = "HOUSE"
    val expected = "road:0.0"

    // When
    val actual = Features.roadFeature(input)

    // Then
    expected shouldBe actual
  }
}
