package uk.gov.ons.addressIndex.server.model.response

import org.scalatest.{Matchers, WordSpec}
import uk.gov.ons.addressIndex.model.db.index._
import uk.gov.ons.addressIndex.model.server.response.address._
import uk.gov.ons.addressIndex.model.server.response.eq.AddressByEqUprnResponse

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

  val givenPafEq: PostcodeAddressFileAddress = PostcodeAddressFileAddress(
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
    postTown = "Newport",
    postcode = "NP10 8XG",
    postcodeType = "17",
    deliveryPointSuffix = "18",
    welshDependentThoroughfare = "19",
    welshThoroughfare = "20",
    welshDoubleDependentLocality = "21",
    welshDependentLocality = "22",
    welshPostTown = "Casnewydd",
    poBoxNumber = "24",
    processDate = "25",
    startDate = "26",
    endDate = "27",
    lastUpdateDate = "28",
    entryDate = "29",
    pafAll = "pafAll",
    mixedPaf = "Office for National Statistics, Government Buildings, Cardiff Rd, Duffryn, Shire, Newport NP10 8XG",
    mixedWelshPaf = "Swyddfa Ystadegau Gwladol, Adeiladau'r Llywodraeth, Caerdydd Rd, Duffryn, Rhanbarth, Casnewydd NP10 8XG"
  )

  val givenNagEq: NationalAddressGazetteerAddress = NationalAddressGazetteerAddress(
    uprn = "99",
    postcodeLocator = "PO15 5RR",
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
    townName = "Fareham",
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
    mixedNag = "Office for National Statistics - Fareham, Segensworth, Titchfield, Mount Doom, Mordor, Fareham PO15 5RR",
    mixedWelshNag = "mixedWelshNag"
  )

  val givenWelshNagEq: NationalAddressGazetteerAddress = NationalAddressGazetteerAddress(
    uprn = "99",
    postcodeLocator = "NP10 8XG",
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
    townName = "Casnewydd",
    locality = "n21",
    lpiLogicalStatus = "lpiLogicalStatus",
    blpuLogicalStatus = "blpuLogicalStatus",
    usrnMatchIndicator = "usrnMatchIndicator",
    parentUprn = "parentUprn",
    streetClassification = "streetClassification",
    multiOccCount = "multiOccCount",
    language = NationalAddressGazetteerAddress.Languages.welsh,
    localCustodianCode = "localCustodianCode",
    localCustodianName = "localCustodianName",
    localCustodianGeogCode = "localCustodianGeogCode",
    rpc = "rpc",
    nagAll = "nagAll",
    lpiEndDate = "lpiEndDate",
    lpiStartDate = "lpiStartDate",
    mixedNag = "Office for National Statistics, Government Buildings, Cardiff Rd, Duffryn, Shire, Newport NP10 8XG",
    mixedWelshNag = "Swyddfa Ystadegau Gwladol, Adeiladau'r Llywodraeth, Caerdydd Rd, Duffryn, Rhanbarth, Casnewydd NP10 8XG"
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

  val givenNisraEq: NisraAddress = NisraAddress(
    organisationName = "1",
    subBuildingName = "2",
    buildingName = "3",
    buildingNumber = "4",
    thoroughfare = "5",
    altThoroughfare = "6",
    dependentThoroughfare = "7",
    locality = "8",
    townName = "Belfast",
    postcode = "BT4 3PP",
    uprn = "11",
    classificationCode = "12",
    udprn = "13",
    postTown = "Belfast",
    easting = "291398",
    northing = "93861",
    creationDate = "17",
    commencementDate = "18",
    archivedDate = "19",
    latitude = "50.7341677",
    mixedNisra = "Government Of Northern Ireland, Castle Buildings, Upper Newtownards Rd, Stormont, Belfast BT4 3PP",
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

  val validPafHighlight = Map(
    "paf.mixedPaf.partial" -> Seq("6 Long Lane Liverpool", "6 Longish Lane Liverpool")
  )

  val validPafHighlightWelsh = Map(
    "paf.mixedWelshPaf.partial" -> Seq("6 Long Lane Liverpoolhjy", "6 Longish Lane Liverpoolhjy")
  )

  val validNagHighlight = Map(
    "nag.mixedNag.partial" -> Seq("A Nag Address", "A Nagish Address")
  )

  val validNagHighlightWelsh = Map(
    "nag.mixedWelshNag.partial" -> Seq("A Naggly Address", "A Nagglyish Address")
  )

  val validPafHighlights = Seq(validPafHighlight,validPafHighlightWelsh)
  val validPafHighlightsWelsh = Seq(validPafHighlightWelsh, validPafHighlight)
  val validNagHighlights = Seq(validNagHighlight,validNagHighlightWelsh)
  val validNagHighlightsWelsh = Seq(validNagHighlightWelsh, validNagHighlight)

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
      val hybrid = HybridAddress(givenPaf.uprn, givenPaf.uprn, Some(Seq(givenRelative)), Some(Seq(givenCrossRef)), Some("postcodeIn"), Some("postcodeOut"), Seq(givenNag), Seq(givenPaf), Seq(givenNisra), 1, "classificationCode", "NA", "NA", "EW", "E", 0D, Seq() )
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
        confidenceScore = 100,
        underlyingScore = 1,
        countryCode = "E",
        censusAddressType = "NA",
        censusEstabType = "NA",
        highlights = None
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

    "strip off an unwanted concatenated postcode" in {
      // Given
      val inputFormattedAddress = "University Of Exeter, Devonshire House, Stocker Road, Exeter, EX4 4PZ EX44PZ"
      val expectedFormattedAddress ="University Of Exeter, Devonshire House, Stocker Road, Exeter, EX4 4PZ"

      // When
      val result = AddressResponseAddress.removeConcatenatedPostcode(inputFormattedAddress)

      // Then
      result shouldBe expectedFormattedAddress

    }

    "remove em tags from string" in {
      // Given
      val inputFormattedAddress = "<em>University</em> <em>Of</em> <em>Exeter</em>, Devonshire House, Stocker Road, <em>Exeter</em>, EX4 4PZ"
      val expectedFormattedAddress ="University Of Exeter, Devonshire House, Stocker Road, Exeter, EX4 4PZ"

      // When
      val result = AddressResponseAddress.removeEms(inputFormattedAddress)

      // Then
      result shouldBe expectedFormattedAddress

    }

    "create AddressResponseAddressEQ from Hybrid ES response for PAF and English" in {
      // Given
      val hybrid = HybridAddress(givenPaf.uprn, givenPaf.uprn, Some(Seq(givenRelative)), Some(Seq(givenCrossRef)), Some("postcodeIn"), Some("postcodeOut"), Seq(givenNag), Seq(givenPaf), Seq(), 1, "classificationCode", "NA", "NA", "EW", "E", 0D, validPafHighlights )
      val expected = AddressResponseAddressEQ(
        uprn = givenPaf.uprn,
        formattedAddress = "mixedPaf",
        underlyingScore = 1,
        confidenceScore = 100D,
        highlights = Some(AddressResponseHighlight("formattedAddress", "P", "E", Some(List(AddressResponseHighlightHit("P","E",0,"6 Long Lane Liverpool"), AddressResponseHighlightHit("P","E",0,"6 Longish Lane Liverpool")))))
      )

      // When
      val result = AddressResponseAddressEQ.fromHybridAddress(hybrid, favourPaf = true, favourWelsh = false, verbose = true)

      // Then
      result shouldBe expected
    }

    "create AddressResponseAddressEQ from Hybrid ES response for PAF and Welsh" in {
      // Given
      val hybrid = HybridAddress(givenPaf.uprn, givenPaf.uprn, Some(Seq(givenRelative)), Some(Seq(givenCrossRef)), Some("postcodeIn"), Some("postcodeOut"), Seq(givenNag), Seq(givenPaf), Seq(), 1, "classificationCode", "NA", "NA", "EW", "W", 0D, validPafHighlightsWelsh )
      val expected = AddressResponseAddressEQ(
        uprn = givenPaf.uprn,
        formattedAddress = "mixedWelshPaf",
        underlyingScore = 1,
        confidenceScore = 100D,
        highlights = Some(AddressResponseHighlight("formattedAddress", "P", "W", Some(List(AddressResponseHighlightHit("P","W",0,"6 Long Lane Liverpoolhjy"), AddressResponseHighlightHit("P","W",0,"6 Longish Lane Liverpoolhjy")))))
      )

      // When
      val result = AddressResponseAddressEQ.fromHybridAddress(hybrid, favourPaf = true, favourWelsh = true, verbose = true)

      // Then
      result shouldBe expected
    }

    "create AddressResponseAddressEQ from Hybrid ES response for NAG and English" in {
      // Given
      val hybrid = HybridAddress(givenPaf.uprn, givenPaf.uprn, Some(Seq(givenRelative)), Some(Seq(givenCrossRef)), Some("postcodeIn"), Some("postcodeOut"), Seq(givenNag), Seq(givenPaf), Seq(), 1, "classificationCode", "NA", "NA", "EW", "E", 0D, validNagHighlights )
      val expected = AddressResponseAddressEQ(
        uprn = givenPaf.uprn,
        formattedAddress = "mixedNag",
        underlyingScore = 1,
        confidenceScore = 100D,
        highlights = Some(AddressResponseHighlight("formattedAddress", "L", "E", Some(List(AddressResponseHighlightHit("L","E",0,"A Nag Address"), AddressResponseHighlightHit("L","E",0,"A Nagish Address")))))
      )

      // When
      val result = AddressResponseAddressEQ.fromHybridAddress(hybrid, favourPaf = false, favourWelsh = false, verbose = true)

      // Then
      result shouldBe expected
    }

    "create AddressResponseAddressEQ from Hybrid ES response for NAG and Welsh" in {
      // Given
      val hybrid = HybridAddress(givenPaf.uprn, givenPaf.uprn, Some(Seq(givenRelative)), Some(Seq(givenCrossRef)), Some("postcodeIn"), Some("postcodeOut"), Seq(givenNag.copy(language = NationalAddressGazetteerAddress.Languages.welsh)), Seq(givenPaf), Seq(), 1, "classificationCode", "NA", "NA", "EW", "W", 0D, validNagHighlightsWelsh )
      val expected = AddressResponseAddressEQ(
        uprn = givenPaf.uprn,
        formattedAddress = "mixedWelshNag",
        underlyingScore = 1,
        confidenceScore = 100D,
        highlights = Some(AddressResponseHighlight("formattedAddress", "L", "W", Some(List(AddressResponseHighlightHit("L","W",0,"A Naggly Address"), AddressResponseHighlightHit("L","W",0,"A Nagglyish Address")))))
      )

      // When
      val result = AddressResponseAddressEQ.fromHybridAddress(hybrid, favourPaf = false, favourWelsh = true, verbose = true)

      // Then
      result shouldBe expected
    }

    "create AddressResponseAddressEQ from Hybrid ES response for NISRA" in {
      // Given
      val hybrid = HybridAddress(givenPaf.uprn, givenPaf.uprn, Some(Seq(givenRelative)), Some(Seq(givenCrossRef)), Some("postcodeIn"), Some("postcodeOut"), Seq(givenNag.copy(language = NationalAddressGazetteerAddress.Languages.welsh)), Seq(givenPaf), Seq(givenNisra), 1, "classificationCode", "NA", "NA", "EW", "E", 0D, validNagHighlightsWelsh )
      val expected = AddressResponseAddressEQ(
        uprn = givenPaf.uprn,
        formattedAddress = "mixedNisra",
        underlyingScore = 1,
        confidenceScore = 100D,
        highlights = Some(AddressResponseHighlight("formattedAddress", "L", "W", Some(List(AddressResponseHighlightHit("L","W",0,"A Naggly Address"), AddressResponseHighlightHit("L","W",0,"A Nagglyish Address")))))
      )

      // When
      val result = AddressResponseAddressEQ.fromHybridAddress(hybrid, favourPaf = false, favourWelsh = false, verbose = true)

      // Then
      result shouldBe expected
    }

    "create AddressResponseAddressCustomEQ from Hybrid ES response for PAF" in {
      // Given
      val hybrid = HybridAddress(givenPaf.uprn, givenPaf.uprn, Some(Seq(givenRelative)), Some(Seq(givenCrossRef)), Some("postcodeIn"), Some("postcodeOut"), Seq(givenNag), Seq(givenPaf), Seq(), 1, "classificationCode", "NA", "NA", "EW", "E", 0D, validPafHighlights )
      val expected = AddressResponseAddressCustomEQ(
        uprn = givenPaf.uprn,
        bestMatchAddress = "formattedAddress", // This is determined by some controller logic
        bestMatchAddressType = "PAF"
     )

      // When
      val result = AddressResponseAddressCustomEQ.fromAddressResponseAddressEQ(AddressResponseAddressEQ.fromHybridAddress(hybrid, favourPaf = true, favourWelsh = true, verbose = true))

      // Then
      result shouldBe expected
    }

    "create Welsh AddressResponseAddressCustomEQ from Hybrid ES response for Welsh PAF" in {
      // Given
      val hybrid = HybridAddress(givenPaf.uprn, givenPaf.uprn, Some(Seq(givenRelative)), Some(Seq(givenCrossRef)), Some("postcodeIn"), Some("postcodeOut"), Seq(givenNag), Seq(givenPaf), Seq(), 1, "classificationCode", "NA", "NA", "EW", "W", 0D, validPafHighlightsWelsh )
      val expected = AddressResponseAddressCustomEQ(
        uprn = givenPaf.uprn,
        bestMatchAddress = "formattedAddress", // This is determined by some controller logic
        bestMatchAddressType = "WELSHPAF"
      )

      // When
      val result = AddressResponseAddressCustomEQ.fromAddressResponseAddressEQ(AddressResponseAddressEQ.fromHybridAddress(hybrid, favourPaf = true, favourWelsh = true, verbose = true))

      // Then
      result shouldBe expected
    }

    "create AddressResponseAddressCustomEQ from Hybrid ES response for NAG" in {
      // Given
      val hybrid = HybridAddress(givenPaf.uprn, givenPaf.uprn, Some(Seq(givenRelative)), Some(Seq(givenCrossRef)), Some("postcodeIn"), Some("postcodeOut"), Seq(givenNag), Seq(givenPaf), Seq(), 1, "classificationCode", "NA", "NA", "EW", "E", 0D, validNagHighlights )
      val expected = AddressResponseAddressCustomEQ(
        uprn = givenPaf.uprn,
        bestMatchAddress = "formattedAddress", // This is determined by some controller logic
        bestMatchAddressType = "NAG"
      )

      // When
      val result = AddressResponseAddressCustomEQ.fromAddressResponseAddressEQ(AddressResponseAddressEQ.fromHybridAddress(hybrid, favourPaf = true, favourWelsh = true, verbose = true))

      // Then
      result shouldBe expected
    }

    "create Welsh AddressResponseAddressCustomEQ from Hybrid ES response for Welsh NAG" in {
      // Given
      val hybrid = HybridAddress(givenPaf.uprn, givenPaf.uprn, Some(Seq(givenRelative)), Some(Seq(givenCrossRef)), Some("postcodeIn"), Some("postcodeOut"), Seq(givenNag), Seq(givenPaf), Seq(), 1, "classificationCode", "NA", "NA", "EW", "W", 0D, validNagHighlightsWelsh )
      val expected = AddressResponseAddressCustomEQ(
        uprn = givenPaf.uprn,
        bestMatchAddress = "formattedAddress", // This is determined by some controller logic
        bestMatchAddressType = "WELSHNAG"
      )

      // When
      val result = AddressResponseAddressCustomEQ.fromAddressResponseAddressEQ(AddressResponseAddressEQ.fromHybridAddress(hybrid, favourPaf = true, favourWelsh = true, verbose = true))

      // Then
      result shouldBe expected
    }

    "create AddressByEqUprnResponse from Hybrid ES response for PAF" in {
      // Given
      val hybrid = HybridAddress(givenPafEq.uprn, givenPafEq.uprn, Some(Seq(givenRelative)), Some(Seq(givenCrossRef)), Some("postcodeIn"), Some("postcodeOut"), Seq(givenNag), Seq(givenPafEq), Seq(givenNisra), 1, "classificationCode", "NA", "NA", "EW", "E", 0D, Seq() )

      val expected = AddressResponseAddressUPRNEQ(
        uprn = givenPafEq.uprn,
        formattedAddress = "Office for National Statistics, Government Buildings, Cardiff Rd, Duffryn, Shire, Newport NP10 8XG",
        addressLine1 = "Office for National Statistics",
        addressLine2 = "Government Buildings",
        addressLine3 = "Cardiff Rd, Duffryn, Shire",
        townName = "Newport",
        postcode = "NP10 8XG"
      )

      // When
      val result = AddressByEqUprnResponse.fromHybridAddress(hybrid, verbose = true, "PAF")

      // Then
      result shouldBe expected
    }

    "create AddressByEqUprnResponse from Hybrid ES response for WELSHPAF" in {
      // Given
      val hybrid = HybridAddress(givenPafEq.uprn, givenPafEq.uprn, Some(Seq(givenRelative)), Some(Seq(givenCrossRef)), Some("postcodeIn"), Some("postcodeOut"), Seq(givenNag), Seq(givenPafEq), Seq(givenNisra), 1, "classificationCode", "NA", "NA", "EW", "W", 0D, Seq() )

      val expected = AddressResponseAddressUPRNEQ(
        uprn = givenPafEq.uprn,
        formattedAddress = "Swyddfa Ystadegau Gwladol, Adeiladau'r Llywodraeth, Caerdydd Rd, Duffryn, Rhanbarth, Casnewydd NP10 8XG",
        addressLine1 = "Swyddfa Ystadegau Gwladol",
        addressLine2 = "Adeiladau'r Llywodraeth",
        addressLine3 = "Caerdydd Rd, Duffryn, Rhanbarth",
        townName = "Casnewydd",
        postcode = "NP10 8XG"
      )

      // When
      val result = AddressByEqUprnResponse.fromHybridAddress(hybrid, verbose = true, "WELSHPAF")

      // Then
      result shouldBe expected
    }

    "create AddressByEqUprnResponse from Hybrid ES response for NAG" in {
      // Given
      val hybrid = HybridAddress(givenNagEq.uprn, givenNagEq.uprn, Some(Seq(givenRelative)), Some(Seq(givenCrossRef)), Some("postcodeIn"), Some("postcodeOut"), Seq(givenNagEq), Seq(givenPafEq), Seq(givenNisra), 1, "classificationCode", "NA", "NA", "EW", "E", 0D, Seq() )

      val expected = AddressResponseAddressUPRNEQ(
        uprn = givenNagEq.uprn,
        formattedAddress = "Office for National Statistics - Fareham, Segensworth, Titchfield, Mount Doom, Mordor, Fareham PO15 5RR",
        addressLine1 = "Office for National Statistics - Fareham",
        addressLine2 = "Segensworth",
        addressLine3 = "Titchfield, Mount Doom, Mordor",
        townName = "Fareham",
        postcode = "PO15 5RR"
      )

      // When
      val result = AddressByEqUprnResponse.fromHybridAddress(hybrid, verbose = true, "NAG")

      // Then
      result shouldBe expected
    }

    "create AddressByEqUprnResponse from Hybrid ES response for WELSHNAG" in {
      // Given
      val hybrid = HybridAddress(givenWelshNagEq.uprn, givenWelshNagEq.uprn, Some(Seq(givenRelative)), Some(Seq(givenCrossRef)), Some("postcodeIn"), Some("postcodeOut"), Seq(givenWelshNagEq), Seq(givenPafEq), Seq(givenNisra), 1, "classificationCode", "NA", "NA", "EW", "W", 0D, Seq() )

      val expected = AddressResponseAddressUPRNEQ(
        uprn = givenWelshNagEq.uprn,
        formattedAddress = "Swyddfa Ystadegau Gwladol, Adeiladau'r Llywodraeth, Caerdydd Rd, Duffryn, Rhanbarth, Casnewydd NP10 8XG",
        addressLine1 = "Swyddfa Ystadegau Gwladol",
        addressLine2 = "Adeiladau'r Llywodraeth",
        addressLine3 = "Caerdydd Rd, Duffryn, Rhanbarth",
        townName = "Casnewydd",
        postcode = "NP10 8XG"
      )

      // When
      val result = AddressByEqUprnResponse.fromHybridAddress(hybrid, verbose = true, "WELSHNAG")

      // Then
      result shouldBe expected
    }

    "create AddressByEqUprnResponse from Hybrid ES response for NISRA" in {
      // Given
      val hybrid = HybridAddress(givenNisraEq.uprn, givenNisraEq.uprn, Some(Seq(givenRelative)), Some(Seq(givenCrossRef)), Some("postcodeIn"), Some("postcodeOut"), Seq(givenWelshNagEq), Seq(givenPafEq), Seq(givenNisraEq), 1, "classificationCode", "NA", "NA", "NI", "E", 0D, Seq() )

      val expected = AddressResponseAddressUPRNEQ(
        uprn = givenNisraEq.uprn,
        formattedAddress = "Government Of Northern Ireland, Castle Buildings, Upper Newtownards Rd, Stormont, Belfast BT4 3PP",
        addressLine1 = "Government Of Northern Ireland",
        addressLine2 = "Castle Buildings",
        addressLine3 = "Upper Newtownards Rd, Stormont",
        townName = "Belfast",
        postcode = "BT4 3PP"
      )

      // When
      val result = AddressByEqUprnResponse.fromHybridAddress(hybrid, verbose = true, "NISRA")

      // Then
      result shouldBe expected
    }

    "create AddressResponseAddressPostcodeEQ from Hybrid ES response for NISRA" in {
      // Given
      val hybrid = HybridAddress(givenNisraEq.uprn, givenNisraEq.uprn, Some(Seq(givenRelative)), Some(Seq(givenCrossRef)), Some("postcodeIn"), Some("postcodeOut"), Seq(givenWelshNagEq), Seq(givenPafEq), Seq(givenNisraEq), 1, "classificationCode", "NA", "NA", "NI", "E", 0D, Seq() )

      val expected = AddressResponseAddressPostcodeEQ(
        uprn = givenNisraEq.uprn,
        formattedAddress = "Government Of Northern Ireland, Castle Buildings, Upper Newtownards Rd, Stormont, Belfast BT4 3PP",
        addressType = "NISRA"
      )

      // When
      val result = AddressResponseAddressPostcodeEQ.fromHybridAddress(hybrid, favourPaf = false, favourWelsh = true, verbose = false)

      // Then
      result shouldBe expected
    }

    "create AddressResponseAddressPostcodeEQ from Hybrid ES response for PAF" in {
      // Given
      val hybrid = HybridAddress(givenPafEq.uprn, givenPafEq.uprn, Some(Seq(givenRelative)), Some(Seq(givenCrossRef)), Some("postcodeIn"), Some("postcodeOut"), Seq(givenWelshNagEq), Seq(givenPafEq), Seq(givenNisraEq), 1, "classificationCode", "NA", "NA", "NI", "E", 0D, Seq() )

      val expected = AddressResponseAddressPostcodeEQ(
        uprn = givenPafEq.uprn,
        formattedAddress = "Office for National Statistics, Government Buildings, Cardiff Rd, Duffryn, Shire, Newport NP10 8XG",
        addressType = "PAF"
      )

      // When
      val result = AddressResponseAddressPostcodeEQ.fromHybridAddress(hybrid, favourPaf = true, favourWelsh = false, verbose = true)

      // Then
      result shouldBe expected
    }

    "create AddressResponseAddressPostcodeEQ from Hybrid ES response for WELSHPAF" in {
      // Given
      val hybrid = HybridAddress(givenPafEq.uprn, givenPafEq.uprn, Some(Seq(givenRelative)), Some(Seq(givenCrossRef)), Some("postcodeIn"), Some("postcodeOut"), Seq(givenWelshNagEq), Seq(givenPafEq), Seq(givenNisraEq), 1, "classificationCode", "NA", "NA", "NI", "E", 0D, Seq() )

      val expected = AddressResponseAddressPostcodeEQ(
        uprn = givenPafEq.uprn,
        formattedAddress = "Swyddfa Ystadegau Gwladol, Adeiladau'r Llywodraeth, Caerdydd Rd, Duffryn, Rhanbarth, Casnewydd NP10 8XG",
        addressType = "WELSHPAF"
      )

      // When
      val result = AddressResponseAddressPostcodeEQ.fromHybridAddress(hybrid, favourPaf = true, favourWelsh = true, verbose = true)

      // Then
      result shouldBe expected
    }

    "create AddressResponseAddressPostcodeEQ from Hybrid ES response for NAG" in {
      // Given
      val hybrid = HybridAddress(givenNagEq.uprn, givenNagEq.uprn, Some(Seq(givenRelative)), Some(Seq(givenCrossRef)), Some("postcodeIn"), Some("postcodeOut"), Seq(givenNagEq), Seq(givenPafEq), Seq(), 1, "classificationCode", "NA", "NA", "NI", "E", 0D, Seq() )

      val expected = AddressResponseAddressPostcodeEQ(
        uprn = givenNagEq.uprn,
        formattedAddress = "Office for National Statistics - Fareham, Segensworth, Titchfield, Mount Doom, Mordor, Fareham PO15 5RR",
        addressType = "NAG"
      )

      // When
      val result = AddressResponseAddressPostcodeEQ.fromHybridAddress(hybrid, favourPaf = false, favourWelsh = false, verbose = true)

      // Then
      result shouldBe expected
    }

    "create AddressResponseAddressPostcodeEQ from Hybrid ES response for WELSHNAG" in {
      // Given
      val hybrid = HybridAddress(givenWelshNagEq.uprn, givenWelshNagEq.uprn, Some(Seq(givenRelative)), Some(Seq(givenCrossRef)), Some("postcodeIn"), Some("postcodeOut"), Seq(givenWelshNagEq), Seq(givenPafEq), Seq(), 1, "classificationCode", "NA", "NA", "NI", "E", 0D, Seq() )

      val expected = AddressResponseAddressPostcodeEQ(
        uprn = givenWelshNagEq.uprn,
        formattedAddress = "Swyddfa Ystadegau Gwladol, Adeiladau'r Llywodraeth, Caerdydd Rd, Duffryn, Rhanbarth, Casnewydd NP10 8XG",
        addressType = "WELSHNAG"
      )

      // When
      val result = AddressResponseAddressPostcodeEQ.fromHybridAddress(hybrid, favourPaf = false, favourWelsh = true, verbose = true)

      // Then
      result shouldBe expected
    }

    "create AddressResponseAddressPostcodeEQ from Hybrid ES response for WELSHNAG when favouring PAF and Welsh when no PAF exists" in {
      // Given
      val hybrid = HybridAddress(givenWelshNagEq.uprn, givenWelshNagEq.uprn, Some(Seq(givenRelative)), Some(Seq(givenCrossRef)), Some("postcodeIn"), Some("postcodeOut"), Seq(givenWelshNagEq), Seq(), Seq(), 1, "classificationCode", "NA", "NA", "NI", "E", 0D, Seq() )

      val expected = AddressResponseAddressPostcodeEQ(
        uprn = givenWelshNagEq.uprn,
        formattedAddress = "Swyddfa Ystadegau Gwladol, Adeiladau'r Llywodraeth, Caerdydd Rd, Duffryn, Rhanbarth, Casnewydd NP10 8XG",
        addressType = "WELSHNAG"
      )

      // When
      val result = AddressResponseAddressPostcodeEQ.fromHybridAddress(hybrid, favourPaf = true, favourWelsh = true, verbose = true)

      // Then
      result shouldBe expected
    }
  }
}
