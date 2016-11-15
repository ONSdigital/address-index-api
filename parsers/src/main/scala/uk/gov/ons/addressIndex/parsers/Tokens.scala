package uk.gov.ons.addressIndex.parsers

import uk.gov.ons.addressIndex.crfscala.CrfScala.Input

object Tokens {

  type TokenIndicator = String
  type Token = String
  type Tokens = Array[Token]

  def apply(input : Input) : Tokens = input.replaceAll(","," ").split(" ").filterNot(_.isEmpty)


  val all : Seq[Token] = Seq(
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

  //TODO TokenIndicators encode to int for better computation of features. (All possible spelling permutations.)

  val directions : Seq[TokenIndicator] = Seq(
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

  val flat : Seq[TokenIndicator] = Seq(
    "FLAT", "FLT",
    "APARTMENT", "APPTS", "APPT", "APTS", "APT",
    "ROOM",
    "ANNEX",  "ANNEXE",
    "UNIT",
    "BLOCK", "BLK"
  )

  val company : Seq[TokenIndicator] = Seq(
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

  val road : Seq[TokenIndicator] = Seq(
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
  val residential : Seq[TokenIndicator] = Seq(
    "HOUSE", "HSE",
    "FARM",
    "LODGE",
    "COTTAGE", "COTTAGES",
    "VILLA", "VILLAS",
    "MAISONETTE",
    "MEWS"
  )

  val business : Seq[TokenIndicator] = Seq(
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

  val locational : Seq[TokenIndicator] = Seq(
    "BASEMENT", "GROUND", "ATTIC",
    "UPPER", "ABOVE", "TOP", "LOWER", "FLOOR", "HIGHER",
    "LEFT", "RIGHT", "FRONT", "BACK", "REAR",
    "WHOLE", "PART", "SIDE"
  )

  val ordinal : Seq[TokenIndicator] = Seq(
    "FIRST", "1ST",
    "SECOND", "2ND",
    "THIRD", "3RD",
    "FOURTH", "4TH",
    "FIFTH", "5TH",
    "SIXTH", "6TH",
    "SEVENTH", "7TH",
    "EIGHT", "8TH"
  )
  val outcodes : Seq[TokenIndicator] = Seq.empty //TODO
  val postTown : Seq[TokenIndicator] = Seq.empty //TODO
}