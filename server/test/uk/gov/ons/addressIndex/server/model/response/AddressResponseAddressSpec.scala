package uk.gov.ons.addressIndex.server.model.response

import org.scalatest.{Matchers, WordSpec}
import uk.gov.ons.addressIndex.model.db.index.{HybridAddress, NationalAddressGazetteerAddress, PostcodeAddressFileAddress, Relative}
import uk.gov.ons.addressIndex.model.server.response._

/**
  * Test conversion between ES reply and the model that will be send in the response
  */
class AddressResponseAddressSpec extends WordSpec with Matchers {

  val givenNag = NationalAddressGazetteerAddress(
    uprn = "n1",
    postcodeLocator = "n2",
    addressBasePostal = "n3",
    latitude = "50.7341677",
    longitude = "-3.540302",
    easting = "291398.00",
    northing = "093861.00",
    organisation = "n22",
    legalName = "n23",
    classificationCode = "n24",
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
    source = "source",
    usrnMatchIndicator = "usrnMatchIndicator",
    parentUprn = "parentUprn",
    crossReference = "crossReference",
    streetClassification = "streetClassification",
    multiOccCount = "multiOccCount",
    language = "language",
    classScheme = "classScheme",
    localCustodianCode = "localCustodianCode",
    localCustodianName = "localCustodianName",
    localCustodianGeogCode = "localCustodianGeogCode",
    rpc = "rpc",
    nagAll = "nagAll"
  )

  val givenRealisticNag = givenNag.copy(
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
    streetDescriptor = "BRIBERY ROAD",
    townName = "EXTER",
    locality = ""
  )

  val givenPaf = PostcodeAddressFileAddress(
    recordIdentifier = "1",
    changeType = "2",
    proOrder = "3",
    uprn = "4",
    udprn = "5",
    organizationName = "6",
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
    pafAll = "pafAll"
  )

  val givenRelative =  Relative (
    level = 1.toInt,
    siblings = Array(6L,7L),
    parents = Array(8L,9L)
  )

  val givenRelativeResponse = AddressResponseRelative.fromRelative(givenRelative)

  "Address response Address model" should {

    "create PAF from Elastic PAF response" in {
      // Given
      val paf = givenPaf

      val expected = AddressResponsePaf(
        udprn = paf.udprn,
        organisationName = paf.organizationName,
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

    "create formatted address from PAF" in {
      // Given
      val paf = givenPaf

      val expected = "7, 6, 8, 9, PO BOX 24, 10 11, 12, 13, 14, 15, 16"

      // When
      val result = AddressResponsePaf.generateFormattedAddress(paf)

      // Then
      result shouldBe expected
    }

    "handle absent dependentThoroughfare in the formatted address" in {
      // Given
      val paf = givenPaf.copy(dependentThoroughfare = "")

      val expected = "7, 6, 8, 9, PO BOX 24, 10 12, 13, 14, 15, 16"

      // When
      val result = AddressResponsePaf.generateFormattedAddress(paf)

      // Then
      result shouldBe expected
    }

    "handle absent PO box in the formatted address" in {
      // Given
      val paf = givenPaf.copy(poBoxNumber = "")

      val expected = "7, 6, 8, 9, 10 11, 12, 13, 14, 15, 16"

      // When
      val result = AddressResponsePaf.generateFormattedAddress(paf)

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
        nag.classificationCode,
        nag.localCustodianCode,
        nag.localCustodianName,
        nag.localCustodianGeogCode
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
        latitude = 50.7341677d,
        longitude = -3.540302d,
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
      val hybrid = HybridAddress(givenPaf.uprn, givenPaf.uprn, Seq(givenRelative), "postcodeIn", "postcodeOut", Seq(givenNag), Seq(givenPaf), 1)
      val expectedPaf = AddressResponsePaf.fromPafAddress(givenPaf)
      val expectedNag = AddressResponseNag.fromNagAddress(givenNag)
      val expected = AddressResponseAddress(
        uprn = givenPaf.uprn,
        parentUprn = givenPaf.uprn,
        relatives = Seq(givenRelativeResponse),
        formattedAddress = "n22, n12n13-n14n15, n11, n6, n7n8-n9n10 n19, n21, n20, n2",
        formattedAddressNag = "n22, n12n13-n14n15, n11, n6, n7n8-n9n10 n19, n21, n20, n2",
        formattedAddressPaf = "7, 6, 8, 9, PO BOX 24, 10 11, 12, 13, 14, 15, 16",
        paf = Some(expectedPaf),
        nag = Some(expectedNag),
        geo = Some(AddressResponseGeo(
          latitude = 50.7341677d,
          longitude = -3.540302d,
          easting = 291398,
          northing = 93861
        )),
        underlyingScore = 1,
        objectScore = 0,
        structuralScore = 0,
        buildingScore = 0,
        localityScore = 0,
        unitScore = 0,
        buildingScoreDebug = "0",
        localityScoreDebug = "0",
        unitScoreDebug = "0"
      )

      // When
      val result = AddressResponseAddress.fromHybridAddress(hybrid)

      // Then
      result shouldBe expected
    }

    "create NAG with expected formatted address (sao empty)" in {
      // Given
      val nag = givenRealisticNag.copy(saoStartNumber = "")
      val expected = "MAJESTIC, 1 BRIBERY ROAD, EXTER, EXO 808"

      // When
      val result = AddressResponseNag.generateFormattedAddress(nag)

      // Then
      result shouldBe expected
    }

    "create NAG with expected formatted address (pao empty)" in {
      // Given
      val nag = givenRealisticNag.copy(paoStartNumber = "")
      val expected = "MAJESTIC, 1 BRIBERY ROAD, EXTER, EXO 808"

      // When
      val result = AddressResponseNag.generateFormattedAddress(nag)

      // Then
      result shouldBe expected
    }

    "create NAG with expected formatted address (saoText field)" in {
      // Given
      val nag = givenRealisticNag.copy(
        paoStartNumber = "",
        saoText = "UNIT",
        saoStartNumber = ""
      )
      val expected = "MAJESTIC, UNIT, BRIBERY ROAD, EXTER, EXO 808"

      // When
      val result = AddressResponseNag.generateFormattedAddress(nag)

      // Then
      result shouldBe expected
    }

    "create NAG with expected formatted address (paoText field)" in {
      // Given
      val nag = givenRealisticNag.copy(
        paoStartNumber = "",
        paoText = "UNIT",
        saoStartNumber = ""
      )
      val expected = "MAJESTIC, UNIT, BRIBERY ROAD, EXTER, EXO 808"

      // When
      val result = AddressResponseNag.generateFormattedAddress(nag)

      // Then
      result shouldBe expected
    }

    "create NAG with expected formatted address (saoText and paoText fields)" in {
      // Given
      val nag = givenRealisticNag.copy(
        paoStartNumber = "",
        saoText = "UNIT",
        paoText = "BUNIT",
        saoStartNumber = ""
      )
      val expected = "MAJESTIC, UNIT, BUNIT, BRIBERY ROAD, EXTER, EXO 808"

      // When
      val result = AddressResponseNag.generateFormattedAddress(nag)

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
      val nagAddresses = Seq(givenNag, expectedNag , givenNag.copy(lpiLogicalStatus = "6"))

      // When
      val result = AddressResponseAddress.chooseMostRecentNag(nagAddresses)

      // Then
      result shouldBe Some(expectedNag)
    }

    "choose the nag with a legal status equal to 6 if it exists and the one with legal status 1 doesn't exist" in {
      // Given
      val expectedNag = givenNag.copy(lpiLogicalStatus = "6")
      val nagAddresses = Seq(givenNag, expectedNag , givenNag.copy(lpiLogicalStatus = "8"))

      // When
      val result = AddressResponseAddress.chooseMostRecentNag(nagAddresses)

      // Then
      result shouldBe Some(expectedNag)
    }

    "choose the nag with a legal status equal to 8 if it exists and the one with legal status 1 or 6 doesn't exist" in {
      // Given
      val expectedNag = givenNag.copy(lpiLogicalStatus = "8")
      val nagAddresses = Seq(givenNag, expectedNag , givenNag.copy(lpiLogicalStatus = "11"))

      // When
      val result = AddressResponseAddress.chooseMostRecentNag(nagAddresses)

      // Then
      result shouldBe Some(expectedNag)
    }

    "choose the first nag with if a nag with a legal status 1, 6 or 8 doesn't exist" in {
      // Given
      val expectedNag = givenNag
      val nagAddresses = Seq(expectedNag, expectedNag.copy(lpiLogicalStatus = "10") , expectedNag.copy(lpiLogicalStatus = "11"))

      // When
      val result = AddressResponseAddress.chooseMostRecentNag(nagAddresses)

      // Then
      result shouldBe Some(expectedNag)
    }

  }

}
