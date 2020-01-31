package uk.gov.ons.addressIndex.server.model.response

import org.scalatest.{Matchers, WordSpec}
import uk.gov.ons.addressIndex.model.db.index._
import uk.gov.ons.addressIndex.model.server.response.address._

/**
  * Test conversion between ES reply and the model that will be send in the response
  */
class AddressResponseAddressSpec extends WordSpec with Matchers {

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
    parentUprn = "parentUprn",
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
    mixedNag = "mixedNag"
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

  val givenNisra: NisraAddress = NisraAddress(
    organisationName = "1",
    subBuildingName = "2",
    buildingName = "3",
    buildingNumber = "4",
    thoroughfare = "5",
    altThoroughfare = "6",
    dependentThoroughfare = "7",
    locality = "8",
    townName = "10",
    postcode = "BT36 5SN",
    uprn = "11",
    classificationCode = "12",
    udprn = "13",
    postTown = "14",
    easting = "291398",
    northing = "93861",
    creationDate = "17",
    commencementDate = "18",
    archivedDate = "19",
    latitude = "50.7341677",
    mixedNisra = "mixedNisra",
    longitude = "-3.540302",
    paoText = "",
    paoStartNumber = "4",
    paoStartSuffix = "",
    paoEndNumber = "",
    paoEndSuffix = "",
    saoText = "",
    saoStartNumber = "4",
    saoStartSuffix = "",
    saoEndNumber = "",
    saoEndSuffix = "",
    addressStatus = "APPROVED",
    buildingStatus = "DEMOLISHED",
    localCouncil = "BELFAST",
    LGDCode = "N09000003"
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

  val givenBespokeScore: AddressResponseScore = AddressResponseScore(
    objectScore = 0,
    structuralScore = 0,
    buildingScore = 0,
    localityScore = 0,
    unitScore = 0,
    buildingScoreDebug = "0",
    localityScoreDebug = "0",
    unitScoreDebug = "0",
    ambiguityPenalty = 1d)

  val givenRelativeResponse: AddressResponseRelative = AddressResponseRelative.fromRelative(givenRelative)

  val givenCrossRefResponse: AddressResponseCrossRef = AddressResponseCrossRef.fromCrossRef(givenCrossRef)

  "Address response Address model" should {

    "create PAF from Elastic PAF response" in {
      // Given
      val paf = givenPaf

      val expected = AddressResponsePaf(
        udprn = paf.udprn,
        organisationName = paf.organisationName,
        departmentName = paf.departmentName,
        subBuildingName = paf.subBuildingName,
        buildingName = paf.buildingName,
        buildingNumber = paf.buildingNumber,
        dependentThoroughfare = paf.dependentThoroughfare,
        thoroughfare = paf.thoroughfare,
        doubleDependentLocality = paf.doubleDependentLocality,
        dependentLocality = paf.dependentLocality,
        postTown = paf.postTown,
        postcode = paf.postcode,
        postcodeType = paf.postcodeType,
        deliveryPointSuffix = paf.deliveryPointSuffix,
        welshDependentThoroughfare = paf.welshDependentThoroughfare,
        welshThoroughfare = paf.welshThoroughfare,
        welshDoubleDependentLocality = paf.welshDoubleDependentLocality,
        welshDependentLocality = paf.welshDependentLocality,
        welshPostTown = paf.welshPostTown,
        poBoxNumber = paf.poBoxNumber,
        startDate = paf.startDate,
        endDate = paf.endDate
      )

      // When
      val result = AddressResponsePaf.fromPafAddress(paf)

      // Then
      result shouldBe expected
    }

    "create NAG from Elastic NAG response" in {
      // Given
      val nag = givenNag

      val expected = AddressResponseNag(
        nag.uprn,
        nag.postcodeLocator,
        nag.addressBasePostal,
        nag.usrn,
        nag.lpiKey,
        pao = AddressResponsePao(
          nag.paoText,
          nag.paoStartNumber,
          nag.paoStartSuffix,
          nag.paoEndNumber,
          nag.paoEndSuffix
        ),
        sao = AddressResponseSao(
          nag.saoText,
          nag.saoStartNumber,
          nag.saoStartSuffix,
          nag.saoEndNumber,
          nag.saoEndSuffix
        ),
        nag.level,
        nag.officialFlag,
        nag.lpiLogicalStatus,
        nag.streetDescriptor,
        nag.townName,
        nag.locality,
        nag.organisation,
        nag.legalName,
        nag.localCustodianCode,
        nag.localCustodianName,
        nag.localCustodianGeogCode,
        nag.lpiEndDate,
        nag.lpiStartDate
      )

      // When
      val result = AddressResponseNag.fromNagAddress(nag)

      // Then
      result shouldBe expected
    }

    "create GEO from NAG elastic response" in {
      // Given
      val nag = givenRealisticNag
      val expected = Some(AddressResponseGeo(
        latitude = 50.7341677,
        longitude = -3.540302,
        easting = 291398,
        northing = 93861
      ))

      // When
      val result = AddressResponseGeo.fromNagAddress(nag)

      // Then
      result shouldBe expected
    }

    "be creatable from Hybrid ES response" in {
      // Given
      val hybrid = HybridAddress(givenPaf.uprn, givenPaf.uprn, Some(Seq(givenRelative)), Some(Seq(givenCrossRef)), Some("postcodeIn"), Some("postcodeOut"), Seq(givenNag), Seq(givenPaf), Seq(givenNisra), 1, "classificationCode", "NA", "NA", "47","E")
      val expectedPaf = AddressResponsePaf.fromPafAddress(givenPaf)
      val expectedNag = AddressResponseNag.fromNagAddress(givenNag)
      val expectedNisra = AddressResponseNisra.fromNisraAddress(givenNisra)
      val expected = AddressResponseAddress(
        uprn = givenPaf.uprn,
        parentUprn = givenPaf.uprn,
        relatives = Some(Seq(givenRelativeResponse)),
        crossRefs = Some(Seq(givenCrossRefResponse)),
        formattedAddress = "mixedNisra",
        formattedAddressNag = "mixedNag",
        formattedAddressPaf = "mixedPaf",
        formattedAddressNisra = "mixedNisra",
        welshFormattedAddressNag = "",
        welshFormattedAddressPaf = "mixedWelshPaf",
        paf = Some(expectedPaf),
        nag = Some(Seq(expectedNag)),
        nisra = Some(expectedNisra),
        geo = Some(AddressResponseGeo(
          latitude = 50.7341677,
          longitude = -3.540302,
          easting = 291398,
          northing = 93861
        )),
        classificationCode = "classificationCode",
        lpiLogicalStatus = givenNag.lpiLogicalStatus,
        fromSource = "47",
        confidenceScore = 100,
        underlyingScore = 1,
        countryCode = "E",
        censusAddressType = "NA",
        censusEstabType = "NA"
      )

      // When
      val result = AddressResponseAddress.fromHybridAddress(hybrid, verbose = true)

      // Then
      result shouldBe expected
    }

    "be creatable (with empty geo field) from Elastic NAG response with invalid latitude" in {
      // Given
      val nag = givenNag.copy(latitude = "invalid")

      // When
      val result = AddressResponseGeo.fromNagAddress(nag)

      // Then
      result shouldBe None
    }

    "be creatable (with empty geo field) from Elastic NAG response with invalid longitude" in {
      // Given
      val nag = givenNag.copy(longitude = "invalid")

      // When
      val result = AddressResponseGeo.fromNagAddress(nag)

      // Then
      result shouldBe None
    }

    "be creatable (with empty geo field) from Elastic NAG response with invalid easting" in {
      // Given
      val nag = givenNag.copy(easting = "invalid")

      // When
      val result = AddressResponseGeo.fromNagAddress(nag)

      // Then
      result shouldBe None
    }

    "be creatable (with empty geo field) from Elastic NAG response with invalid northing" in {
      // Given
      val nag = givenNag.copy(northing = "invalid")

      // When
      val result = AddressResponseGeo.fromNagAddress(nag)

      // Then
      result shouldBe None
    }

    "choose the nag with a legal status equal to 1 if it exists" in {
      // Given
      val expectedNag = givenNag.copy(lpiLogicalStatus = "1")
      val nagAddresses = Seq(givenNag, expectedNag, givenNag.copy(lpiLogicalStatus = "6"))

      // When
      val result = AddressResponseAddress.chooseMostRecentNag(nagAddresses, NationalAddressGazetteerAddress.Languages.english)

      // Then
      result shouldBe Some(expectedNag)
    }

    "choose the nag with a legal status equal to 6 if it exists and the one with legal status 1 doesn't exist" in {
      // Given
      val expectedNag = givenNag.copy(lpiLogicalStatus = "6")
      val nagAddresses = Seq(givenNag, expectedNag, givenNag.copy(lpiLogicalStatus = "8"))

      // When
      val result = AddressResponseAddress.chooseMostRecentNag(nagAddresses, NationalAddressGazetteerAddress.Languages.english)

      // Then
      result shouldBe Some(expectedNag)
    }

    "choose the nag with a legal status equal to 8 if it exists and the one with legal status 1 or 6 doesn't exist" in {
      // Given
      val expectedNag = givenNag.copy(lpiLogicalStatus = "8")
      val nagAddresses = Seq(givenNag, expectedNag, givenNag.copy(lpiLogicalStatus = "11"))

      // When
      val result = AddressResponseAddress.chooseMostRecentNag(nagAddresses, NationalAddressGazetteerAddress.Languages.english)

      // Then
      result shouldBe Some(expectedNag)
    }

    "choose the first nag with if a nag with a legal status 1, 6 or 8 doesn't exist" in {
      // Given
      val expectedNag = givenNag
      val nagAddresses = Seq(expectedNag, expectedNag.copy(lpiLogicalStatus = "10"), expectedNag.copy(lpiLogicalStatus = "11"))

      // When
      val result = AddressResponseAddress.chooseMostRecentNag(nagAddresses, NationalAddressGazetteerAddress.Languages.english)

      // Then
      result shouldBe Some(expectedNag)
    }

    "choose the nag with a specified language if it exists" in {
      // Given
      val expectedNag = givenNag.copy(lpiLogicalStatus = "1", language = NationalAddressGazetteerAddress.Languages.welsh)
      val nagAddresses = Seq(givenNag, expectedNag, givenNag.copy(lpiLogicalStatus = "6"))

      // When
      val result = AddressResponseAddress.chooseMostRecentNag(nagAddresses, NationalAddressGazetteerAddress.Languages.welsh)

      // Then
      result shouldBe Some(expectedNag)
    }

  }

}
