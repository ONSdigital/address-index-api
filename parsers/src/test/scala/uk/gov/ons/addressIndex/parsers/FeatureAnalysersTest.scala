package uk.gov.ons.addressIndex.parsers

import org.scalatest.{FlatSpec, Matchers}
import uk.gov.ons.addressIndex.crfscala.CrfScala._
import uk.gov.ons.addressIndex.parsers.FeatureAnalysers.Predef._

class FeatureAnalysersTest extends FlatSpec with Matchers {

  it should "have a `DigitsLiteral` for `allDigits` of `all_digits`" in {
    val expected = "all_digits"
    val actual = DigitsLiteral.allDigits
    expected shouldBe actual
  }

  it should "have a `DigitsLiteral` for `containsDigits` of `contains_digits`" in {
    val expected = "contains_digits"
    val actual = DigitsLiteral.containsDigits
    expected shouldBe actual
  }

  it should "have a `DigitsLiteral` for `noDigits` of `no_digits`" in {
    val expected = "no_digits"
    val actual = DigitsLiteral.noDigits
    expected shouldBe actual
  }

  it should "have all feature names" in {
    val expected : Seq[FeatureName] = Seq(
      "digits",
      "word",
      "length",
      "endsinpunc",
      "directional",
      "outcode",
      "posttown",
      "has.vowels",
      "flat",
      "company",
      "road",
      "residential",
      "business",
      "locational",
      "ordinal",
      "hyphenations"
    )
    val actual : Seq[FeatureName] = Seq[FeatureName](
      digits,
      word,
      FeatureAnalysers.Predef.length, //clash with `org.scalatest.words.MatcherWords.length`
      endsInPunctuation,
      directional,
      outcode,
      postTown,
      hasVowels,
      flat,
      company,
      road,
      residential,
      business,
      locational,
      ordinal,
      hyphenations
    )
    expected should contain theSameElementsAs actual
  }

  it should "produce the correct `word` feature output for token `Mansion`" in {
    val input = "Mansion"
    val expected = true
    val actual = wordAnalyser apply input
    expected shouldBe actual
  }

  ignore should "produce the correct `word` feature output for token `383`" in {
    val input = "383"
    val expected = true
    val actual = wordAnalyser apply input
    expected shouldBe actual
  }

  ignore should "produce the correct `word` feature output for token `3a83`" in {
    val input = "3a83"
    val expected = false
    val actual = wordAnalyser apply input
    expected shouldBe actual
  }

  it should "produce the correct `length` feature output for token `Mansion`" in {
    val input = "Mansion"
    val expected = "7"
    val actual = lengthAnalyser apply input
    expected shouldBe actual
  }

  it should "produce the correct `length` feature output for token ``" in {
    val input = ""
    val expected = "0"
    val actual = lengthAnalyser apply input
    expected shouldBe actual
  }

  it should "produce the correct `endsInPunctuation` feature output for token `House`" in {
    val input = "House"
    val expected = false
    val actual = endsInPunctuationAnalyser apply input
    expected shouldBe actual
  }

  it should "produce the correct `endsInPunctuation` feature output for token `House.`" in {
    val input = "House."
    val expected = true
    val actual = endsInPunctuationAnalyser apply input
    expected shouldBe actual
  }

  it should "produce the correct `endsInPunctuation` feature output for token `Ho.use`" in {
    val input = "Ho.use"
    val expected = false
    val actual = endsInPunctuationAnalyser apply input
    expected shouldBe actual
  }

  it should "produce the correct `directional` feature output for token ``" in {
    val inputs = Seq(
      "N",
      "S",
      "E",
      "W",
      "NE",
      "NW",
      "SE",
      "SW",
      "NORTH",
      "SOUTH",
      "EAST",
      "WEST",
      "NORTHEAST",
      "NORTHWEST",
      "SOUTHEAST",
      "SOUTHWEST"
    )
    val expected = Seq.fill(inputs.length)(true)
    val actual = inputs map directionalAnalyser.apply
    expected should contain theSameElementsAs actual
  }

  it should "produce the correct `outcode` feature output for expected inputs" in {
    val inputs = Seq.empty
    val expected = Seq.fill(inputs.length)(true)
    val actual = inputs map outcodeAnalyser.apply
    expected should contain theSameElementsAs actual
  }

  it should "produce the correct `post town` feature output for expected inputs" in {
    val inputs = Seq.empty
    val expected = Seq.fill(inputs.length)(true)
    val actual = inputs map postTownAnalyser.apply
    expected should contain theSameElementsAs actual
  }

  it should "produce the correct `has vowels` feature output for expected inputs" in {
    val inputs = Seq(
      "a", "e", "i", "o", "u"
    )
    val expected = Seq.fill(inputs.length)(true)
    val actual = inputs map hasVowelsAnalyser.apply
    expected should contain theSameElementsAs actual
  }

  it should "produce the correct `flat` feature output for expected inputs" in {
    val inputs = Seq(
      "FLAT", "FLT",
      "APARTMENT", "APPTS", "APPT", "APTS", "APT",
      "ROOM",
      "ANNEX",  "ANNEXE",
      "UNIT",
      "BLOCK", "BLK"
    )
    val expected = Seq.fill(inputs.length)(true)
    val actual = inputs map flatAnalyser.apply
    expected should contain theSameElementsAs actual
  }

  it should "produce the correct `company` feature output for expected inputs" in {
    val inputs = Seq(
      "CIC",
      "CIO",
      "LLP",
      "LP",
      "LTD",
      "LIMITED",
      "CYF",
      "PLC",
      "CCC",
      "UNLTD",
      "ULTD"
    )
    val expected = Seq.fill(inputs.length)(true)
    val actual = inputs map companyAnalyser.apply
    expected should contain theSameElementsAs actual
  }

  it should "produce the correct `road` feature output for expected inputs" in {
    val inputs = Seq(
      "ROAD", "RAOD", "RD",
      "DRIVE", "DR",
      "STREET", "STRT",
      "AVENUE", "AVENEU",
      "SQUARE",
      "LANE", "LNE", "LN",
      "COURT", "CRT", "CT",
      "PARK", "PK",
      "GRDN", "GARDEN",
      "CRESCENT",
      "CLOSE", "CL",
      "WALK",
      "WAY",
      "TERRACE",
      "BVLD",
      "HEOL",
      "FFORDD",
      "PLACE",
      "GARDENS",
      "GROVE",
      "VIEW",
      "HILL",
      "GREEN"
    )
    val expected = Seq.fill(inputs.length)(true)
    val actual = inputs map roadAnalyser.apply
    expected should contain theSameElementsAs actual
  }

  it should "produce the correct `residential` feature output for expected inputs" in {
    val inputs = Seq(
      "HOUSE", "HSE",
      "FARM",
      "LODGE",
      "COTTAGE", "COTTAGES",
      "VILLA", "VILLAS",
      "MAISONETTE",
      "MEWS"
    )
    val expected = Seq.fill(inputs.length)(true)
    val actual = inputs map residentialAnalyser.apply
    expected should contain theSameElementsAs actual
  }

  it should "produce the correct `business` feature output for expected inputs" in {
    val inputs = Seq(
      "OFFICE",
      "HOSPITAL",
      "CARE",
      "CLUB",
      "BANK",
      "BAR",
      "UK",
      "SOCIETY",
      "PRISON",
      "HMP",
      "RC",
      "UWE",
      "UEA",
      "LSE",
      "KCL",
      "UCL",
      "UNI", "UNIV", "UNIVERSITY", "UNIVERISTY"
    )
    val expected = Seq.fill(inputs.length)(true)
    val actual = inputs map businessAnalyser.apply
    expected should contain theSameElementsAs actual
  }

  it should "produce the correct `locational` feature output for expected inputs" in {
    val inputs = Seq(
      "BASEMENT", "GROUND", "ATTIC",
      "UPPER", "ABOVE", "TOP", "LOWER", "FLOOR", "HIGHER",
      "LEFT", "RIGHT", "FRONT", "BACK", "REAR",
      "WHOLE", "PART", "SIDE"
    )
    val expected = Seq.fill(inputs.length)(true)
    val actual = inputs map locationalAnalyser.apply
    expected should contain theSameElementsAs actual
  }

  it should "produce the correct `ordinal` feature output for expected inputs" in {
    val inputs = Seq(
      "FIRST", "1ST",
      "SECOND", "2ND",
      "THIRD", "3RD",
      "FOURTH", "4TH",
      "FIFTH", "5TH",
      "SIXTH", "6TH",
      "SEVENTH", "7TH",
      "EIGHT", "8TH"
    )
    val expected = Seq.fill(inputs.length)(true)
    val actual = inputs map ordinalAnalyser.apply
    expected should contain theSameElementsAs actual
  }

  it should "produce the correct `hyphenations` feature output for token `my-road`" in {
    val input = "my-road"
    val expected = 1
    val actual = hyphenationsAnalyser apply input
    expected shouldBe actual
  }

  it should "produce the correct `hyphenations` feature output for token `my-road-my-street`" in {
    val input = "my-road-my-street"
    val expected = 3
    val actual = hyphenationsAnalyser apply input
    expected shouldBe actual
  }

  it should "produce the correct `hyphenations` feature output for token `myroad`" in {
    val input = "myroad"
    val expected = 0
    val actual = hyphenationsAnalyser apply input
    expected shouldBe actual
  }
}