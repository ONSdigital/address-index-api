package uk.gov.ons.addressIndex.server.utils
import javax.inject.Inject
import play.api.i18n.MessagesApi
import org.scalatest.{FlatSpec, Matchers}
import play.api.Logger
import uk.gov.ons.addressIndex.model.db.index.Relative
import uk.gov.ons.addressIndex.model.server.response._
import uk.gov.ons.addressIndex.parsers.Tokens

/**
  * Unit tests for all the methods in the hopperScore calculation class
  */
class HopperScoreHelperTest extends FlatSpec with Matchers {

  val logger = Logger("HopperScoreHelperTest")

  val mockAddressTokens = Map (
    Tokens.buildingNumber -> "7",
    Tokens.paoStartNumber -> "7",
    Tokens.streetName -> "GATE REACH",
    Tokens.townName -> "EXETER",
    Tokens.postcode -> "PO7 6GA",
    Tokens.postcodeIn -> "6GA",
    Tokens.postcodeOut -> "PO7"
  )

  val mockPafAddress1 = AddressResponsePaf(
    udprn = "",
    organisationName = "",
    departmentName = "",
    subBuildingName = "",
    buildingName = "",
    buildingNumber = "7",
    dependentThoroughfare = "GATE REACH",
    thoroughfare = "",
    doubleDependentLocality = "",
    dependentLocality = "",
    postTown = "EXETER",
    postcode = "PO7 6GA",
    postcodeType = "",
    deliveryPointSuffix = "",
    welshDependentThoroughfare = "",
    welshThoroughfare = "",
    welshDoubleDependentLocality = "",
    welshDependentLocality = "",
    welshPostTown = "",
    poBoxNumber = "",
    startDate = "",
    endDate = ""
  )

  val mockNagAddress1 = AddressResponseNag(
    uprn = "",
    postcodeLocator = "PO7 6GA",
    addressBasePostal = "",
    usrn = "",
    lpiKey = "",
    pao = AddressResponsePao(
      paoText = "",
      paoStartNumber = "7",
      paoStartSuffix = "",
      paoEndNumber = "",
      paoEndSuffix = ""
    ),
    sao = AddressResponseSao(
      saoText = "",
      saoStartNumber = "",
      saoStartSuffix = "",
      saoEndNumber = "",
      saoEndSuffix = ""
    ),
    level = "",
    officialFlag = "",
    logicalStatus = "1",
    streetDescriptor = "",
    townName = "EXETER",
    locality = "",
    organisation = "",
    legalName = "",
    classificationCode = "R",
    localCustodianCode = "435",
    localCustodianName = "MILTON KEYNES",
    localCustodianGeogCode = "E06000042",
    lpiEndDate = ""
  )

  val mockRelative = Relative(
    level = 1,
    siblings = Array(6L, 7L),
    parents = Array(8L, 9L)
  )

  val mockRelativeResponse = AddressResponseRelative.fromRelative(mockRelative)

  val mockBespokeScoreEmpty = AddressResponseScore(
    objectScore = 0d,
    structuralScore = 0d,
    buildingScore = 0d,
    localityScore = 0d,
    unitScore = 0d,
    buildingScoreDebug = "0",
    localityScoreDebug = "0",
    unitScoreDebug = "0")

  val mockBespokeScore = AddressResponseScore(
    objectScore = -1.0d,
    structuralScore = 1.0d,
    buildingScore = 1.0d,
    localityScore = 1.0d,
    unitScore = -1.0d,
    buildingScoreDebug = "91",
    localityScoreDebug = "9111",
    unitScoreDebug = "0999")

  val mockAddressResponseAddress = AddressResponseAddress(
    uprn = "",
    parentUprn = "",
    relatives = Seq(mockRelativeResponse),
    formattedAddress = "7, GATE REACH, EXETER, EX2 9GA",
    formattedAddressNag = "7, GATE REACH, EXETER, EX2 9GA",
    formattedAddressPaf = "7, GATE REACH, EXETER, EX2 9GA",
    paf = Some(mockPafAddress1),
    nag = Some(mockNagAddress1),
    geo = None,
    underlyingScore = 1.0f,
    bespokeScore = Some(mockBespokeScoreEmpty)
  )

  val mockAddressResponseAddressWithScores = AddressResponseAddress(
    uprn = "",
    parentUprn = "",
    relatives = Seq(mockRelativeResponse),
    formattedAddress = "7, GATE REACH, EXETER, EX2 9GA",
    formattedAddressNag = "7, GATE REACH, EXETER, EX2 9GA",
    formattedAddressPaf = "7, GATE REACH, EXETER, EX2 9GA",
    paf = Some(mockPafAddress1),
    nag = Some(mockNagAddress1),
    geo = None,
    underlyingScore = 1.0f,
    bespokeScore = Some(mockBespokeScore)
  )

  val mockLocalityParams: Seq[(String, String)] =
    Seq(("locality.9111", "EX2 6"), ("locality.9615", "EX1 1"), ("locality.9615", "EX1 1"),
      ("locality.9615", "EX1 1"), ("locality.9615", "EX1 1"), ("locality.9615", "EX1 1"), ("locality.9615", "EX1 1"),
      ("locality.9615", "EX1 3"), ("locality.9615", "EX1 3"), ("locality.9615", "EX1 3"))

  it should "calculate the ambiguity penalty for a given locality " in {
    // Given
    val localityScoreToTest = "locality.9615"
    val expected = 2.0d

    // When
    val actual = HopperScoreHelper.calculateAmbiguityPenalty(localityScoreToTest, mockLocalityParams)

    // Then
    actual shouldBe expected
  }

  it should "capture the outcode from a postcode " in {
    // Given
    val postcodeToTest = "PO15 5RR"
    val expected = "PO15"

    // When
    val actual = HopperScoreHelper.getOutcode(postcodeToTest)

    // Then
    actual shouldBe expected
  }

  it should "capture the sector from a postcode " in {
    // Given
    val postcodeToTest = "PO15 5RR"
    val expected = "PO15 5"

    // When
    val actual = HopperScoreHelper.getSector(postcodeToTest)

    // Then
    actual shouldBe expected
  }

  it should "swap two digits in the incode of a postcode " in {
    // Given
    val postcodeToTest = "5RG"
    val expected = "5GR"

    // When
    val actual = HopperScoreHelper.swap(postcodeToTest, 1, 2)

    // Then
    actual shouldBe expected
  }

  it should "match two streets according to the rules " in {
    // Given
    val street1 = "HOPPER STREET"
    val street2 = "CHOPPER COURT"
    val expected = 2

    // When
    val actual = HopperScoreHelper.matchStreets(street1, street2)

    // Then
    actual shouldBe expected
  }

  it should "match two building names according to the rules " in {
    // Given
    val building1 = "TONY'S TYRES"
    val building2 = "TONYS EXHAUSTS AND TYRES"
    val expected = 1

    // When
    val actual = HopperScoreHelper.matchNames(building1, building2)

    // Then
    actual shouldBe expected
  }

  it should "capture the start suffix from a building name or number " in {
    // Given
    val building1 = "72C-84E"
    val expected = "C"

    // When
    val actual = HopperScoreHelper.getStartSuffix(building1)

    // Then
    actual shouldBe expected
  }

  it should "capture the end suffix from a building name or number " in {
    // Given
    val building1 = "72C-84E"
    val expected = "E"

    // When
    val actual = HopperScoreHelper.getEndSuffix(building1)

    // Then
    actual shouldBe expected
  }

  it should "capture the top of the range of building numbers " in {
    // Given
    val building1 = "72C-84E"
    val expected = 84

    // When
    val actual = HopperScoreHelper.getRangeTop(building1)

    // Then
    actual shouldBe expected
  }

  it should "capture the bottom of the range of building numbers " in {
    // Given
    val building1 = "72C-84E"
    val expected = 72

    // When
    val actual = HopperScoreHelper.getRangeBottom(building1)

    // Then
    actual shouldBe expected
  }

  it should "calculate the minium of a list of numbers " in {
    // Given
    val expected = 42

    // When
    val actual = HopperScoreHelper.min(123, 42, 976, 996996)

    // Then
    actual shouldBe expected
  }

  it should "return the levenshtein edit distance of two strings " in {
    // Given
    val string1 = "BONGOES"
    val string2 = "BINGO"
    val expected = 3

    // When
    val actual = HopperScoreHelper.levenshtein(string1, string2)

    // Then
    actual shouldBe expected
  }

  /**
    * getScoresForAddresses
    * addScoresToAddress
    */

  it should "calculate the unit score for an address " in {
    // Given
    val expected = "unit.0888"

    // When
    val actual = HopperScoreHelper.calculateUnitScore(
      mockAddressResponseAddress,
      "UNIT 7",
      "@",
      "@",
      "@",
      "@",
      "@",
      "GATES")

    // Then
    actual shouldBe expected
  }

  it should "calculate the locality score for an address " in {
    // Given
    val expected = "locality.9111"

    // When
    val actual = HopperScoreHelper.calculateLocalityScore(
      mockAddressResponseAddress,
      "PO7 6GA",
      "PO7",
      "6GA",
      "@",
      "EXETER",
      "GATE REACH",
      "@",
      "@")

    // Then
    actual shouldBe expected
  }

  it should "calculate the building score for an address " in {
    // Given
    val expected = "building.71"

    // When
    val actual = HopperScoreHelper.calculateBuildingScore(
      mockAddressResponseAddress,
      "ONS",
      "7",
      "7",
      "@",
      "@",
      "@",
      "@")

    // Then
    actual shouldBe expected
  }

  it should "calculate the structural score for an address " in {
    // Given
    val buildingScore = 0.9d
    val localityScore = 0.25d
    val expected = 0.2250d

    // When
    val actual = HopperScoreHelper.calculateStructuralScore(buildingScore,localityScore)

    // Then
    actual shouldBe expected
  }

  it should "calculate the object score for an address " in {
    // Given
    val buildingScore = 0.9d
    val localityScore = 0.25d
    val unitScore = 0.5d
    val expected = 0.1125d

    // When
    val actual = HopperScoreHelper.calculateObjectScore(buildingScore,localityScore,unitScore)

    // Then
    actual shouldBe expected
  }

  it should "create a locality param List of Tuples from an address and tokens " in {
    // Given
    val expected = ("locality.9111","PO7 6")

    // When
    val actual = HopperScoreHelper.getLocalityParams(mockAddressResponseAddress,mockAddressTokens)

    // Then
    actual shouldBe expected
  }

  it should "get the scores for a address " in {
    // Given
    val expected = Seq(mockAddressResponseAddressWithScores)

    // When
    val actual = HopperScoreHelper.getScoresForAddresses(Seq(mockAddressResponseAddress),mockAddressTokens)

    // Then
    actual shouldBe expected
  }

  it should "add the scores for addresses to the response object " in {
    // Given
    val expected = mockAddressResponseAddressWithScores

    // When
    val actual = HopperScoreHelper.addScoresToAddress(mockAddressResponseAddress,mockAddressTokens,mockLocalityParams)

    // Then
    actual shouldBe expected
  }

  it should "calculate the detailed organisation building name paf score for an address " in {
    // Given
    val buildingName = "MESSAGE TOWERS"
    val organisationName = "NICE MESSAGES"
    val pafBuildingName = "MASSAGE TOWERS"
    val pafOrganisationName = "NICE MASSAGES"
    val expected = 2

    // When
    val actual = HopperScoreHelper.calculateDetailedOrganisationBuildingNamePafScore (
      buildingName,
      pafBuildingName,
      organisationName,
      pafOrganisationName)

    // Then
    actual shouldBe expected
  }

  it should "calculate the detailed organisation building name nag score for an address " in {
    // Given
    val buildingName = "MESSAGE TOWERS"
    val organisationName = "NICE MESSAGES"
    val nagPaoText = "MASSAGE TOWERS UNIT 3"
    val nagOrganisationName = "NICE MASSAGES"
    val expected = 2

    // When
    val actual = HopperScoreHelper.calculateDetailedOrganisationBuildingNameNagScore (
      buildingName,
      nagPaoText,
      organisationName,
      nagOrganisationName)

    // Then
    actual shouldBe expected
  }


  it should "calculate the building number paf score for an address " in {
    // Given
    val buildingName = "UNIT 3A"
    val pafBuildingName = "UNIT 3A"
    val pafBuildingNumber ="42"
    val paoStartSuffix = "A"
    val paoEndSuffix = ""
    val buildingNumber = "42"
    val paoStartNumber = "42"
    val paoEndNumber = ""
    val expected = 1

    // When
    val actual = HopperScoreHelper.calculateBuildingNumPafScore (
      buildingName,
      pafBuildingName,
      pafBuildingNumber,
      paoStartSuffix,
      paoEndSuffix,
      buildingNumber,
      paoStartNumber,
      paoEndNumber)

    // Then
    actual shouldBe expected
  }

  it should "calculate the building number nag score for an address " in {
    // Given
    val buildingName = "UNIT 3A"
    val nagPaoStartSuffix = "A"
    val nagPaoEndSuffix = ""
    val paoStartSuffix = "A"
    val paoEndSuffix = ""
    val buildingNumber = "42"
    val nagPaoStartNumber = "42"
    val nagPaoEndNumber = ""
    val paoStartNumber = "42"
    val paoEndNumber = ""
    val expected = 1

    // When
    val actual = HopperScoreHelper.calculateBuildingNumNagScore (
      buildingName,
      nagPaoStartNumber,
      nagPaoEndNumber,
      nagPaoStartSuffix,
      nagPaoEndSuffix,
      paoEndSuffix,
      paoStartSuffix,
      buildingNumber,
      paoStartNumber,
      paoEndNumber)

    // Then
    actual shouldBe expected
  }

  it should "calculate the organisation building name paf score for an address " in {
    // Given
    val buildingName = "THE PRIORY"
    val pafBuildingName = "THE OLD PRIORY"
    val organisationName = "BOBS BANANA RIPENERS"
    val pafOrganisationName =  "BIBS AND BANDANAS"
    val expected = 1

    // When
    val actual = HopperScoreHelper.calculateOrganisationBuildingNamePafScore (
      buildingName,
      pafBuildingName,
      organisationName,
      pafOrganisationName)

    // Then
    actual shouldBe expected
  }

  it should "calculate the organisation building name nag score for an address " in {
    // Given
    val buildingName = "THE PRIORY"
    val nagPaoText = "THE OLD PRIORY"
    val organisationName = "BOBS BANANA RIPENERS"
    val nagOrganisationName =  "BIBS AND BANDANAS"
    val expected = 1

    // When
    val actual = HopperScoreHelper.calculateOrganisationBuildingNamePafScore (
      buildingName,
      nagPaoText,
      organisationName,
      nagOrganisationName)

    // Then
    actual shouldBe expected
  }

  it should "calculate the street name paf score for an address " in {
    // Given
    val streetName = "WOMBAT STREET"
    val pafThoroughfare = "AARDVARK AVENUE"
    val pafDependentThoroughfare = "WOMBAT STREET"
    val pafWelshThoroughfare= ""
    val pafWelshDependentThoroughfare = ""
    val expected = 1

    // When
    val actual = HopperScoreHelper.calculateStreetPafScore (
      streetName,
      pafThoroughfare,
      pafDependentThoroughfare,
      pafWelshThoroughfare,
      pafWelshDependentThoroughfare)

    // Then
    actual shouldBe expected
  }

  it should "calculate the street name nag score for an address " in {
    // Given
    val streetName = "WOMBAT STREET"
    val nagStreetDescriptor = "AARDVARK AVENUE"
    val expected = 6

    // When
    val actual = HopperScoreHelper.calculateStreetNagScore(streetName, nagStreetDescriptor)

    // Then
    actual shouldBe expected
  }

  it should "calculate the town locality paf score for an address " in {
    // Given
    val townName = "LUTON"
    val locality = ""
    val pafPostTown = "LOOTON"
    val pafWelshPostTown = ""
    val pafDependentLocality = ""
    val pafWelshDependentLocality = ""
    val pafDoubleDependentLocality = ""
    val pafWelshDoubleDependentLocality = ""
    val streetName = "WOMBAT STREET"
    val expected = 1

    // When
    val actual = HopperScoreHelper.calculateTownLocalityPafScore (
      townName,
      locality,
      pafPostTown,
      pafWelshPostTown,
      pafDependentLocality,
      pafWelshDependentLocality,
      pafDoubleDependentLocality,
      pafWelshDoubleDependentLocality,
      streetName)

    // Then
    actual shouldBe expected
  }

  it should "calculate the town locality nag score for an address " in {
    // Given
    val townName = "PARK GATE"
    val locality = "SOUTHAMPTON"
    val nagTownName = "PORKY GATER"
    val streetName = ""
    val nagLocality = "SOTON"
    val expected = 6

    // When
    val actual = HopperScoreHelper.calculateTownLocalityNagScore (
        townName,
        nagTownName,
        locality,
        nagLocality,
        streetName)

    // Then
    actual shouldBe expected
  }

  it should "calculate the postcode paf score for an address " in {
    // Given
    val postcode = "PO15 5RR"
    val pafPostcode = "PO15 5RR"
    val postcodeOut = "PO15"
    val postcodeWithInvertedIncode = "PO15 5RR"
    val postcodeSector = "PO15 5"
    val postcodeArea = "PO"
    val expected = 1

    // When
    val actual = HopperScoreHelper.calculatePostcodePafScore (
      postcode,
      pafPostcode,
      postcodeOut,
      postcodeWithInvertedIncode,
      postcodeSector,
      postcodeArea)

    // Then
    actual shouldBe expected
  }

  it should "calculate the postcode nag score for an address " in {
    // Given
    val postcode = "PO15 5RS"
    val nagPostcode = "PO15 5SR"
    val postcodeOut = "PO15"
    val postcodeWithInvertedIncode = "PO15 5SR"
    val postcodeSector = "PO15 5"
    val postcodeArea = "PO"
    val expected = 3

    // When
    val actual = HopperScoreHelper.calculatePostcodeNagScore (
      postcode: String,
      nagPostcode: String,
      postcodeOut: String,
      postcodeWithInvertedIncode: String,
      postcodeSector: String,
      postcodeArea: String)

    // Then
    actual shouldBe expected
  }

  it should "calculate the orgainisation name nag score for an address " in {
    // Given
    val organisationName = "BONGO WONGO"
    val nagPaoText = "WINGO BINGO"
    val nagSaoText = "GAMBLING DEN 2"
    val nagOrganisationName = "WINGO BINGO"
    val expected = 1

    // When
    val actual = HopperScoreHelper.calculateOrganisationNameNagScore(
      organisationName,
      nagPaoText,
      nagSaoText,
      nagOrganisationName)

    // Then
    actual shouldBe expected
  }

  it should "calculate the sub building name paf score for an address " in {
    // Given
    val subBuildingName = "ANNEX THREE"
    val pafSubBuildingName = "THE ANNEX"
    val expected = 3

    // When
    val actual = HopperScoreHelper.calculateSubBuildingNamePafScore(
      subBuildingName,
      pafSubBuildingName)

    // Then
    actual shouldBe expected
  }

  it should "calculate the sub building name nag score for an address " in {
    // Given
    val subBuildingName = "ANNEX THREE"
    val nagSaoText = "ANNEX 3"
    val expected = 6

    // When
    val actual = HopperScoreHelper.calculateSubBuildingNameNagScore(
      subBuildingName,
      nagSaoText)

    // Then
    actual shouldBe expected
  }

  it should "calculate the sub building number paf score for an address " in {
    // Given
    val subBuildingName = "UNIT 3A"
    val pafSubBuildingName = "UNIT 3A"
    val pafBuildingName = "JENGA TOWERS"
    val pafBuildingNumber ="42"
    val saoStartSuffix = "A"
    val saoEndSuffix = ""
    val saoStartNumber = "42"
    val saoEndNumber = ""
    val expected = 1

    // When
    val actual = HopperScoreHelper.calculateSubBuildingNumberPafScore (
      subBuildingName,
      pafSubBuildingName,
      pafBuildingName,
      saoStartSuffix,
      saoEndSuffix,
      saoStartNumber,
      saoEndNumber,
      pafBuildingNumber)

    // Then
    actual shouldBe expected
  }

  it should "calculate the sub building number nag score for an address " in {
    // Given
    val subBuildingName = "UNIT 3A"
    val nagSaoStartNumber = "3"
    val nagSaoEndNumber = ""
    val nagSaoStartSuffix = "A"
    val nagSaoEndSuffix = ""
    val saoStartSuffix = "A"
    val saoEndSuffix = ""
    val saoStartNumber = "42"
    val saoEndNumber = ""
    val expected = 1

    // When
    val actual = HopperScoreHelper.calculateSubBuildingNumberNagScore (
      subBuildingName,
      nagSaoStartNumber,
      nagSaoEndNumber,
      nagSaoStartSuffix,
      nagSaoEndSuffix,
      saoStartSuffix,
      saoEndSuffix,
      saoStartNumber,
      saoEndNumber)

    // Then
    actual shouldBe expected
  }

  it should "determine that a string contains a number when it does " in {
    // Given
    val stringWithNum = "Level42"
    val expected = true

    // When
    val actual = HopperScoreHelper.containsNumber(stringWithNum)

    // Then
    actual shouldBe expected

  }

  it should "determine that a string does not contain a number when it doesn't " in {
    // Given
    val stringWithoutNum = "LevelFortyTwo"
    val expected = false

    // When
    val actual = HopperScoreHelper.containsNumber(stringWithoutNum)

    // Then
    actual shouldBe expected
  }

  it should "extract the parts containing numbers from a multipart string " in {
    // Given
    val parts = "2B OR NOT 2B"
    val expected = "2B 2B"

    // When
    val actual = HopperScoreHelper.getNumberPartsFromName(parts)

    // Then
    actual shouldBe expected
  }

  it should "extract the number-free parts from a multipart string " in {
    // Given
    val parts = "2B OR NOT 2B"
    val expected = "OR NOT"

    // When
    val actual = HopperScoreHelper.getNonNumberPartsFromName(parts)

    // Then
    actual shouldBe expected
  }
}

