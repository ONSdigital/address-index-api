package uk.gov.ons.addressIndex.parsers

object Tokens {

  type TokenIndicator = String
  type Token = String
  type Tokens = Array[Token]

  def apply(input : String) : Tokens = input.replaceAll(","," ").split(" ").filterNot(_.isEmpty)

  type TokenAnalyser[T] = (String => T)

  object TokenAnalyser {
    def apply[T](analyser: TokenAnalyser[T]) = analyser
  }

  val ALL : Seq[Token] = Seq(
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

  val DIRECTIONS : Seq[TokenIndicator] = Seq(
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

  val FLAT : Seq[TokenIndicator] = Seq(
    "FLAT", "FLT",
    "APARTMENT", "APPTS", "APPT", "APTS", "APT",
    "ROOM",
    "ANNEX",  "ANNEXE",
    "UNIT",
    "BLOCK", "BLK"
  )

  val COMPANY : Seq[TokenIndicator] = Seq(
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

  val ROAD : Seq[TokenIndicator] = Seq(
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
  val RESIDENTIAL : Seq[TokenIndicator] = Seq(
    "HOUSE", "HSE",
    "FARM",
    "LODGE",
    "COTTAGE", "COTTAGES",
    "VILLA", "VILLAS",
    "MAISONETTE",
    "MEWS"
  )

  val BUSINESS : Seq[TokenIndicator] = Seq(
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

  val LOCATIONAL : Seq[TokenIndicator] = Seq(
    "BASEMENT", "GROUND", "ATTIC",
    "UPPER", "ABOVE", "TOP", "LOWER", "FLOOR", "HIGHER",
    "LEFT", "RIGHT", "FRONT", "BACK", "REAR",
    "WHOLE", "PART", "SIDE"
  )

  val ORIDINAL : Seq[TokenIndicator] = Seq(
    "FIRST", "1ST",
    "SECOND", "2ND",
    "THIRD", "3RD",
    "FOURTH", "4TH",
    "FIFTH", "5TH",
    "SIXTH", "6TH",
    "SEVENTH", "7TH",
    "EIGHT", "8TH"
  )
  val OUTCODES : Seq[TokenIndicator] = Seq.empty //TODO
  val POST_TOWN : Seq[TokenIndicator] = Seq.empty //TODO
}