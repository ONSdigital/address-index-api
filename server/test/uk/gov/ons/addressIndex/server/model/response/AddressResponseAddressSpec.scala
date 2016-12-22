package uk.gov.ons.addressIndex.server.model.response

import org.scalatest.{Matchers, WordSpec}
import uk.gov.ons.addressIndex.model.db.index.{NationalAddressGazetteerAddress, PostcodeAddressFileAddress}
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
    logicalStatus = "n18",
    streetDescriptor = "n19",
    townName = "n20",
    locality = "n21",
    score = 1.0f
  )

  "Address response Address model" should {

    "be creatable from Elastic PAF response" in {
      // Given
      val paf = PostcodeAddressFileAddress(
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
        score = 1.0f
      )

      val expected = AddressResponseAddress(
        uprn = paf.uprn,
        formattedAddress = "7 6 8 9 10 PO BOX 24 11 12 13 14 15 16",
        paf = Some(AddressResponsePaf(
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
        )),
        nag = None,
        geo = None,
        underlyingScore = 1,
        underlyingMaxScore = 1
      )

      // When
      val result = AddressResponseAddress.fromPafAddress(paf)

      // Then
      result shouldBe expected
    }

    "be creatable from Elastic NAG response" in {
      // Given
      val nag = givenNag

      val expected = AddressResponseAddress(
        uprn = nag.uprn,
        formattedAddress = "n22 n12n13-n14n15 n11 n6 n7n8-n9n10 n19 n21 n20 n2",
        paf = None,
        nag = Some(AddressResponseNag(
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
          nag.logicalStatus,
          nag.streetDescriptor,
          nag.townName,
          nag.locality,
          nag.organisation,
          nag.legalName,
          nag.classificationCode
        )),
        geo = Some(AddressResponseGeo(
          latitude = 50.7341677d,
          longitude = -3.540302d,
          easting = 291398,
          northing = 93861
        )),
        underlyingScore = 1,
        underlyingMaxScore = 1)

      // When
      val result = AddressResponseAddress.fromNagAddress(1)(nag)

      // Then
      result shouldBe expected
    }

    "be creatable (with empty geo field) from Elastic NAG response with invalid latitude" in {
      // Given
      val nag = givenNag.copy(latitude = "invalid")

      // When
      val result = AddressResponseAddress.fromNagAddress(1)(nag).geo

      // Then
      result shouldBe None
    }

    "be creatable (with empty geo field) from Elastic NAG response with invalid longitude" in {
      // Given
      val nag = givenNag.copy(longitude = "invalid")

      // When
      val result = AddressResponseAddress.fromNagAddress(1)(nag).geo

      // Then
      result shouldBe None
    }

    "be creatable (with empty geo field) from Elastic NAG response with invalid easting" in {
      // Given
      val nag = givenNag.copy(easting = "invalid")

      // When
      val result = AddressResponseAddress.fromNagAddress(1)(nag).geo

      // Then
      result shouldBe None
    }

    "be creatable (with empty geo field) from Elastic NAG response with invalid northing" in {
      // Given
      val nag = givenNag.copy(northing = "invalid")

      // When
      val result = AddressResponseAddress.fromNagAddress(1)(nag).geo

      // Then
      result shouldBe None
    }

  }

}
