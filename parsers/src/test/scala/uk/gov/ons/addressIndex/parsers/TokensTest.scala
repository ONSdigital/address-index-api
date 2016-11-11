package uk.gov.ons.addressIndex.parsers

import org.scalatest.{FlatSpec, Matchers}
import uk.gov.ons.addressIndex.parsers.Tokens.{Token, TokenIndicator}

class TokensTest extends FlatSpec with Matchers {

  it should "produce `Tokens` for the given string `31 exeter close` splitting on whitespace" in {
    val input : String = "31 exeter close"
    val expected : Seq[Token] = Seq("31", "exeter", "close")
    val actual : Seq[Token]  = Tokens(input)
    expected should contain theSameElementsAs actual
  }

  it should "produce all tokens" in {
    val expected : Seq[Token] = Seq(
      "OrganisationName",
      "DepartmentName",
      "SubBuildingName",
      "BuildingName",
      "BuildingNumber",
      "StreetName",
      "Locality",
      "TownName",
      "Postcode"
    )
    val actual : Seq[Token]  = Tokens.ALL
    expected should contain theSameElementsAs actual
  }

  it should "produce all directions" in {
    val expected : Seq[TokenIndicator] = Seq(
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
    val actual : Seq[TokenIndicator] = Tokens.DIRECTIONS
    expected should contain theSameElementsAs actual
  }

  it should "produce all flats" in {
    val expected : Seq[TokenIndicator] = Seq(
      "FLAT", "FLT",
      "APARTMENT", "APPTS", "APPT", "APTS", "APT",
      "ROOM",
      "ANNEX",  "ANNEXE",
      "UNIT",
      "BLOCK", "BLK"
    )
    val actual : Seq[TokenIndicator] = Tokens.FLAT
    expected should contain theSameElementsAs actual
  }

  it should "produce all companies" in {
    val expected : Seq[TokenIndicator] = Seq(
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
    val actual : Seq[TokenIndicator] = Tokens.COMPANY
    expected should contain theSameElementsAs actual
  }

  it should "produce all roads" in {
    val expected : Seq[TokenIndicator] = Seq(
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
    val actual : Seq[TokenIndicator] = Tokens.ROAD
    expected should contain theSameElementsAs actual
  }

  it should "produce all residentials" in {
    val expected : Seq[TokenIndicator] = Seq(
      "HOUSE", "HSE",
      "FARM",
      "LODGE",
      "COTTAGE", "COTTAGES",
      "VILLA", "VILLAS",
      "MAISONETTE",
      "MEWS"
    )
    val actual : Seq[TokenIndicator] = Tokens.RESIDENTIAL
    expected should contain theSameElementsAs actual
  }

  it should "produce all businesses" in {
    val expected : Seq[TokenIndicator] = Seq(
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
    val actual : Seq[TokenIndicator] = Tokens.BUSINESS
    expected should contain theSameElementsAs actual
  }

  it should "produce all locationals" in {
    val expected : Seq[TokenIndicator] = Seq(
      "BASEMENT", "GROUND", "ATTIC",
      "UPPER", "ABOVE", "TOP", "LOWER", "FLOOR", "HIGHER",
      "LEFT", "RIGHT", "FRONT", "BACK", "REAR",
      "WHOLE", "PART", "SIDE"
    )
    val actual : Seq[TokenIndicator] = Tokens.LOCATIONAL
    expected should contain theSameElementsAs actual
  }

  it should "produce all ordinals" in {
    val expected : Seq[TokenIndicator] = Seq(
      "FIRST", "1ST",
      "SECOND", "2ND",
      "THIRD", "3RD",
      "FOURTH", "4TH",
      "FIFTH", "5TH",
      "SIXTH", "6TH",
      "SEVENTH", "7TH",
      "EIGHT", "8TH"
    )
    val actual : Seq[TokenIndicator] = Tokens.ORIDINAL
    expected should contain theSameElementsAs actual
  }

  it should "produce all outcodes" in {
    val expected : Seq[TokenIndicator] = Seq.empty
    val actual : Seq[TokenIndicator] = Tokens.OUTCODES
    expected should contain theSameElementsAs actual
  }

  it should "produce all post towns" in {
    val expected : Seq[TokenIndicator] = Seq.empty
    val actual : Seq[TokenIndicator] = Tokens.POST_TOWN
    expected should contain theSameElementsAs actual
  }
}