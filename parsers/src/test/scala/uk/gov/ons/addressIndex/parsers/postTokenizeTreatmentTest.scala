package uk.gov.ons.addressIndex.parsers

import org.scalatest.{FlatSpec, Matchers}
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult

class postTokenizeTreatmentTest extends FlatSpec with Matchers {

  it should "transform buildingName into paoStartNumber and paoStartSuffix" in {
    // Given
    val input = Map(
      Tokens.buildingName -> "65B"
    )

    val expected = Map(
      Tokens.buildingName -> "65B",
      Tokens.paoStartNumber -> "65",
      Tokens.paoStartSuffix -> "B"
    )

    // When
    val actual = Tokens.postTokenizeTreatmentBuildingName(input)

    // Then
    actual shouldBe expected
  }

  it should "transform buildingNumber into paoStartNumber" in {
    // Given
    val input = Map(
      Tokens.buildingNumber -> "65"
    )

    val expected = Map(
      Tokens.buildingNumber -> "65",
      Tokens.paoStartNumber -> "65"
    )

    // When
    val actual = Tokens.postTokenizeTreatmentBuildingNumber(input)

    // Then
    actual shouldBe expected
  }

  it should "NOT transform buildingNumber if it is not a number" in {
    // Given
    val input = List(
      CrfTokenResult(
        value = "not a number",
        label = Tokens.buildingNumber
      )
    )

    val expected = Map.empty[String, Seq[CrfTokenResult]]

    // When
    val actual = Tokens.postTokenizeTreatment(input)

    // Then
    actual shouldBe expected
  }

  it should "transform buildingNumber and buildingName into paoStartNumber and paoStartSuffix" in {
    // Given
    val input = List(
      CrfTokenResult(
        value = "65B",
        label = Tokens.buildingName
      ),
      CrfTokenResult(
        value = "12",
        label = Tokens.buildingNumber
      )
    )

    val expected = Map(
      Tokens.buildingName -> "65B",
      Tokens.buildingNumber -> "12",
      Tokens.paoStartNumber -> "12",
      Tokens.saoStartNumber -> "65",
      Tokens.saoStartSuffix -> "B"
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

  it should "transform buildingName even if it doesn't follow the pattern" in {
    // Given
    val input = List(
      CrfTokenResult(
        value = "120-A",
        label = Tokens.buildingName
      )
    )

    val expected = Map(
      Tokens.buildingName -> "120-A",
      Tokens.paoStartNumber -> "120"
    )

    // When
    val actual = Tokens.postTokenizeTreatment(input)

    // Then
    actual shouldBe expected
  }

  it should "transform buildingName if it is a number + a range" in {
    // Given
    val input = List(
      CrfTokenResult(
        value = "65 120-122",
        label = Tokens.buildingName
      )
    )

    val expected = Map(
      Tokens.buildingName -> "65 120-122",
      Tokens.paoStartNumber -> "120",
      Tokens.paoEndNumber -> "122",
      Tokens.saoStartNumber -> "65"
    )

    // When
    val actual = Tokens.postTokenizeTreatment(input)

    // Then
    actual shouldBe expected
  }

  it should "transform subBuildingName into saoStartNumber and buildingName into paoStartNumber and paoEndNumber" in {
    // Given
    val input = List(
      CrfTokenResult(
        value = "FLAT 2",
        label = Tokens.subBuildingName
      ),
      CrfTokenResult(
        value = "SHIRE HOUSE 75-77",
        label = Tokens.buildingName
      )
    )

    val expected = Map(
      Tokens.subBuildingName -> "FLAT 2",
      Tokens.saoStartNumber -> "2",
      Tokens.buildingName -> "SHIRE HOUSE 75-77",
      Tokens.paoStartNumber -> "75",
      Tokens.paoEndNumber -> "77"
    )

    // When
    val actual = Tokens.postTokenizeTreatment(input)

    // Then
    actual shouldBe expected
  }

  it should "transform subBuildingName into sao fields and buildingName into paoStartNumber and paoStartSuffix" in {
    // Given
    val input = List(
      CrfTokenResult(
        value = "11A-111A FIRST FLOOR FLAT",
        label = Tokens.subBuildingName
      ),
      CrfTokenResult(
        value = "237A",
        label = Tokens.buildingName
      )
    )

    val expected = Map(
      Tokens.subBuildingName -> "11A-111A FIRST FLOOR FLAT",
      Tokens.saoStartNumber -> "11",
      Tokens.saoStartSuffix -> "A",
      Tokens.saoEndNumber -> "111",
      Tokens.saoEndSuffix -> "A",
      Tokens.buildingName -> "237A",
      Tokens.paoStartNumber -> "237",
      Tokens.paoStartSuffix -> "A"
    )

    // When
    val actual = Tokens.postTokenizeTreatment(input)

    // Then
    actual shouldBe expected
  }

  it should "transform subBuildingName into sao fields and buildingName into paoStartNumber and paoEndNumber" in {
    // Given
    val input = List(
      CrfTokenResult(
        value = "1B-1C",
        label = Tokens.subBuildingName
      ),
      CrfTokenResult(
        value = "COLEMAN HOUSE 1-3",
        label = Tokens.buildingName
      )
    )

    val expected = Map(
      Tokens.subBuildingName -> "1B-1C",
      Tokens.saoStartNumber -> "1",
      Tokens.saoStartSuffix -> "B",
      Tokens.saoEndNumber -> "1",
      Tokens.saoEndSuffix -> "C",
      Tokens.buildingName -> "COLEMAN HOUSE 1-3",
      Tokens.paoStartNumber -> "1",
      Tokens.paoEndNumber -> "3"
    )

    // When
    val actual = Tokens.postTokenizeTreatment(input)

    // Then
    actual shouldBe expected
  }

  it should "transform complex building name (with many ranges) into pao and sao fields" in {
    // Given
    val input = Map(
      Tokens.buildingName -> "311C-311B 311A-311"
    )

    val expected = Map(
      Tokens.buildingName -> "311C-311B 311A-311",
      Tokens.paoStartNumber -> "311",
      Tokens.paoStartSuffix -> "A",
      Tokens.paoEndNumber -> "311",
      Tokens.saoStartNumber -> "311",
      Tokens.saoStartSuffix -> "C",
      Tokens.saoEndNumber -> "311",
      Tokens.saoEndSuffix -> "B"
    )

    // When
    val actual = Tokens.postTokenizeTreatmentBuildingName(input)

    // Then
    actual shouldBe expected
  }

  it should "transform building name with a range even if there is already a building number" in {
    // Given
    val input = List(
      CrfTokenResult(
        value = "4-5D ASHFORD BUSINESS COMPLEX",
        label = Tokens.buildingName
      ),
      CrfTokenResult(
        value = "166",
        label = Tokens.buildingNumber
      )
    )

    val expected = Map(
      Tokens.buildingName -> "4-5D ASHFORD BUSINESS COMPLEX",
      Tokens.buildingNumber -> "166",
      Tokens.paoStartNumber -> "166",
      Tokens.saoStartNumber -> "4",
      Tokens.saoEndNumber -> "5",
      Tokens.saoEndSuffix -> "D"
    )

    // When
    val actual = Tokens.postTokenizeTreatment(input)

    // Then
    actual shouldBe expected
  }

  it should "transform building name with a range, a number and everything has a suffix" in {
    // Given
    val input = Map(
      Tokens.buildingName -> "8B-8C THE MEWS 15A"
    )

    val expected = Map(
      Tokens.buildingName -> "8B-8C THE MEWS 15A",
      Tokens.paoStartNumber -> "15",
      Tokens.paoStartSuffix -> "A",
      Tokens.saoStartNumber -> "8",
      Tokens.saoStartSuffix -> "B",
      Tokens.saoEndNumber -> "8",
      Tokens.saoEndSuffix -> "C"
    )

    // When
    val actual = Tokens.postTokenizeTreatmentBuildingName(input)

    // Then
    actual shouldBe expected
  }

  it should "transform building name with 2 ranges and some siffixes" in {
    // Given
    val input = List(
      CrfTokenResult(
        value = "44A-44F PICCADILLY COURT 457-463",
        label = Tokens.buildingName
      )
    )

    val expected = Map(
      Tokens.buildingName -> "44A-44F PICCADILLY COURT 457-463",
      Tokens.paoStartNumber -> "457",
      Tokens.paoEndNumber -> "463",
      Tokens.saoStartNumber -> "44",
      Tokens.saoStartSuffix -> "A",
      Tokens.saoEndNumber -> "44",
      Tokens.saoEndSuffix -> "F"
    )

    // When
    val actual = Tokens.postTokenizeTreatment(input)

    // Then
    actual shouldBe expected
  }

  it should "NOT transform building name into numbers if they are not Short" in {
    // Given
    val input = List(
      CrfTokenResult(
        value = "123456A-123456F PICCADILLY COURT 123456-123456",
        label = Tokens.buildingName
      )
    )

    val expected = Map(
      Tokens.buildingName -> "123456A-123456F PICCADILLY COURT 123456-123456",
      Tokens.saoStartSuffix -> "A",
      Tokens.saoEndSuffix -> "F"
    )

    // When
    val actual = Tokens.postTokenizeTreatment(input)

    // Then
    actual shouldBe expected
  }

  it should "replace borough if it is in the street name and the city is London" in {
    // Given
    val input = List(
      CrfTokenResult(
        value = "ALBANY ROAD MANOR PARK",
        label = Tokens.streetName
      ),
      CrfTokenResult(
        value = "LONDON",
        label = Tokens.townName
      )
    )

    val expected = Map(
      Tokens.streetName -> "ALBANY ROAD",
      Tokens.townName -> "LONDON",
      Tokens.locality -> "MANOR PARK"
    )

    // When
    val actual = Tokens.postTokenizeTreatment(input)

    // Then
    actual shouldBe expected
  }

  it should "NOT replace borough if it is in the street name and the city is NOT London" in {
    // Given
    val input = List(
      CrfTokenResult(
        value = "ALBANY ROAD MANOR PARK",
        label = Tokens.streetName
      ),
      CrfTokenResult(
        value = "MANCHESTER",
        label = Tokens.townName
      )
    )

    val expected = Map(
      Tokens.streetName -> "ALBANY ROAD MANOR PARK",
      Tokens.townName -> "MANCHESTER"
    )

    // When
    val actual = Tokens.postTokenizeTreatment(input)

    // Then
    actual shouldBe expected
  }

}
