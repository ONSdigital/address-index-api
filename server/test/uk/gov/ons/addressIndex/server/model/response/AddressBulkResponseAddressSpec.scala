package uk.gov.ons.addressIndex.server.model.response

import org.scalatest.matchers._
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.ons.addressIndex.model.db.BulkAddress
import uk.gov.ons.addressIndex.model.db.index._
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.bulk.AddressBulkResponseAddress

/**
  * Test conversion between ES reply and the model that will be send in the response
  */

class AddressBulkResponseAddressSpec extends AnyWordSpec with should.Matchers {

  val givenNag: NationalAddressGazetteerAddress = NationalAddressGazetteerAddress(
    uprn = "n1",
    postcodeLocator = "n2",
    addressBasePostal = "n3",
    latitude = "50.7341677",
    longitude = "-3.540302",
    easting = "291398.00",
    northing = "093861.00",
    organisation = "n22",
    legalName = "n23",
    usrn = "n4",
    lpiKey = "n5",
    paoText = "n6",
    paoStartNumber = "n7",
    paoStartSuffix = "n8",
    paoEndNumber = "n9",
    paoEndSuffix = "n10",
    saoText = "n11",
    saoStartNumber = "n12",
    saoStartSuffix = "n13",
    saoEndNumber = "n14",
    saoEndSuffix = "n15",
    level = "n16",
    officialFlag = "n17",
    streetDescriptor = "n19",
    townName = "n20",
    locality = "n21",
    lpiLogicalStatus = "lpiLogicalStatus",
    blpuLogicalStatus = "blpuLogicalStatus",
    usrnMatchIndicator = "usrnMatchIndicator",
    parentUprn = "5",
    streetClassification = "streetClassification",
    multiOccCount = "multiOccCount",
    language = NationalAddressGazetteerAddress.Languages.english,
    localCustodianCode = "localCustodianCode",
    localCustodianName = "localCustodianName",
    localCustodianGeogCode = "localCustodianGeogCode",
    rpc = "rpc",
    nagAll = "nagAll",
    lpiEndDate = "lpiEndDate",
    lpiStartDate = "lpiStartDate",
    mixedNag = "mixedNag",
    mixedWelshNag = "mixedWelshNag"
  )

  val givenWelshNag: NationalAddressGazetteerAddress = givenNag.copy(
    townName = "wn20",
    locality = "wn21",
    language = NationalAddressGazetteerAddress.Languages.welsh,
    mixedNag = "welshMixedNag"
  )

  val givenRealisticNag: NationalAddressGazetteerAddress = givenNag.copy(
    postcodeLocator = "EXO 808",
    addressBasePostal = "D",
    organisation = "MAJESTIC",
    legalName = "",
    paoText = "",
    paoStartNumber = "1",
    paoStartSuffix = "",
    paoEndNumber = "",
    paoEndSuffix = "",
    saoText = "",
    saoStartNumber = "1",
    saoStartSuffix = "",
    saoEndNumber = "",
    saoEndSuffix = "",
    level = "",
    language = NationalAddressGazetteerAddress.Languages.english,
    streetDescriptor = "BRIBERY ROAD",
    townName = "EXTER",
    locality = ""
  )

  val givenPaf: PostcodeAddressFileAddress = PostcodeAddressFileAddress(
    recordIdentifier = "1",
    changeType = "2",
    proOrder = "3",
    uprn = "4",
    udprn = "5",
    organisationName = "6",
    departmentName = "7",
    subBuildingName = "8",
    buildingName = "9",
    buildingNumber = "10",
    dependentThoroughfare = "11",
    thoroughfare = "12",
    doubleDependentLocality = "13",
    dependentLocality = "14",
    postTown = "15",
    postcode = "16",
    postcodeType = "17",
    deliveryPointSuffix = "18",
    welshDependentThoroughfare = "19",
    welshThoroughfare = "20",
    welshDoubleDependentLocality = "21",
    welshDependentLocality = "22",
    welshPostTown = "23",
    poBoxNumber = "24",
    processDate = "25",
    startDate = "26",
    endDate = "27",
    lastUpdateDate = "28",
    entryDate = "29",
    pafAll = "pafAll",
    mixedPaf = "mixedPaf",
    mixedWelshPaf = "mixedWelshPaf"
  )

  val givenRelative: Relative = Relative (
    level = 1,
    siblings = Array(6L, 7L),
    parents = Array(8L, 9L)
  )

  val givenCrossRef: CrossRef = CrossRef(
    crossReference = "E05011011",
    source = "7666OW"
  )

  def transformToNonIDS(addressIn: AddressResponseAddress): AddressResponseAddressNonIDS = {
    AddressResponseAddressNonIDS.fromAddress(addressIn)
  }

  "Address response Bulk Address model" should {

    "return a valid bulk response when paf is present" in {
      // Given
      val hybrid = HybridAddress("", "", givenPaf.uprn, givenNag.parentUprn, Some(Seq(givenRelative)), Some(Seq(givenCrossRef)), Some("postcodeIn"), Some("postcodeOut"), Seq(givenNag), Seq(givenPaf), 1, "classificationCode", "NA", "NA", "EW", "E", 0D, Seq() )
      val expected = AddressBulkResponseAddress(
        id = "1",
        inputAddress = "some input",
        uprn = givenPaf.uprn,
        parentUprn = givenNag.parentUprn,
        udprn = givenPaf.udprn,
        matchedFormattedAddress = "mixedNag",
        confidenceScore = 100,
        underlyingScore = 1,
        matchedAddress  = None,
        tokens = Map.empty[String, String],
        matchtype = "",
        recommendationCode = ""
      )

      // When
      val bulk = new BulkAddress("1", "some input", tokens = Map.empty[String, String], hybrid)
      val result = AddressBulkResponseAddress.fromBulkAddress(bulk,transformToNonIDS(AddressResponseAddress.fromHybridAddress(hybrid, verbose = false, pafdefault = false)),includeFullAddress = false)

      // Then
      result shouldBe expected
    }

    "return a valid bulk response when paf is not present" in {
      // Given
      val hybrid = HybridAddress("", "", givenNag.uprn, givenNag.parentUprn, Some(Seq(givenRelative)), Some(Seq(givenCrossRef)), Some("postcodeIn"), Some("postcodeOut"), Seq(givenNag), Seq(), 1, "classificationCode", "NA", "NA", "EW", "E", 0D, Seq())
      val expected = AddressBulkResponseAddress(
        id = "1",
        inputAddress = "some input",
        uprn = givenNag.uprn,
        parentUprn = givenNag.parentUprn,
        udprn = "",
        matchedFormattedAddress = "mixedNag",
        confidenceScore = 100,
        underlyingScore = 1,
        matchedAddress = None,
        tokens = Map.empty[String, String],
        matchtype = "",
        recommendationCode = ""
      )

      // When
      val bulk = new BulkAddress("1", "some input", tokens = Map.empty[String, String], hybrid)
      val result = AddressBulkResponseAddress.fromBulkAddress(bulk,transformToNonIDS(AddressResponseAddress.fromHybridAddress(hybrid, verbose = false, pafdefault = false)),includeFullAddress = false)

      // Then
      result shouldBe expected
    }

  }
}
