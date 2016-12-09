package uk.gov.ons.addressIndex.server.model.response

import org.scalatest.{Matchers, WordSpec}
import uk.gov.ons.addressIndex.model.db.index.{NationalAddressGazetteerAddress, PostcodeAddressFileAddress}
import uk.gov.ons.addressIndex.model.server.response.{AddressResponseAddress, AddressResponseGeo, AddressResponseNag, AddressResponsePaf}

class AddressResponseAddressSpec extends WordSpec with Matchers {

  "Address response Address model" should {

    "be creatable from Elastic PAF response" in {
      // Given
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
        score = 1.0f
      )

      val expected = AddressResponseAddress(
        uprn = "4",
        formattedAddress = "",
        paf = Some(AddressResponsePaf(
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
          startDate = "26",
          endDate = "27"
        )),
        nag = None,
        geo = None,
        underlyingScore = 1,
        underlyingMaxScore = 1
      )

      // When
      val result = AddressResponseAddress.fromPafAddress(givenPaf)

      // Then
      result shouldBe expected
    }

    "be creatable from Elastic NAG response" in {
      // Given
      val givenNag = NationalAddressGazetteerAddress(
        uprn = "n1",
        postcodeLocator = "n2",
        addressBasePostal = "n3",
        ursn = "n4",
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
        organisation = "n22",
        legalName = "n23",
        latitude = "1.0000000",
        longitude = "2.0000000",
        score = 1.0f
      )

      val expected = AddressResponseAddress(
        uprn = "n1",
        formattedAddress = "",
        paf = None,
        nag = Some(AddressResponseNag(
          uprn = "n1",
          postcodeLocator = "n2",
          addressBasePostal = "n3",
          ursn = "n4",
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
          logicalStatus = "n18",
          streetDescriptor = "n19",
          townName = "n20",
          locality = "n21",
          organisation = "n22",
          legalName = "n23"
        )),
        geo = Some(AddressResponseGeo(
          latitude = 1.0d,
          longitude = 2.0d,
          easting = 0,
          northing = 0
        )),
        underlyingScore = 1,
        underlyingMaxScore = 1
      )

      // When
      val result = AddressResponseAddress.fromNagAddress(1)(givenNag)

      // Then
      result shouldBe expected
    }

    "be creatable from Elastic NAG response with invalid latitude" in {
      // Given
      val givenNag = NationalAddressGazetteerAddress(
        uprn = "n1",
        postcodeLocator = "n2",
        addressBasePostal = "n3",
        ursn = "n4",
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
        organisation = "n22",
        legalName = "n23",
        latitude = "something wrong",
        longitude = "2.0000000",
        score = 1.0f
      )

      val expected = AddressResponseAddress(
        uprn = "n1",
        formattedAddress = "",
        paf = None,
        nag = Some(AddressResponseNag(
          uprn = "n1",
          postcodeLocator = "n2",
          addressBasePostal = "n3",
          ursn = "n4",
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
          logicalStatus = "n18",
          streetDescriptor = "n19",
          townName = "n20",
          locality = "n21",
          organisation = "n22",
          legalName = "n23"
        )),
        geo = None,
        underlyingScore = 1,
        underlyingMaxScore = 1
      )

      // When
      val result = AddressResponseAddress.fromNagAddress(1)(givenNag)

      // Then
      result shouldBe expected
    }

    "be creatable from Elastic NAG response with invalid longitude" in {
      // Given
      val givenNag = NationalAddressGazetteerAddress(
        uprn = "n1",
        postcodeLocator = "n2",
        addressBasePostal = "n3",
        ursn = "n4",
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
        organisation = "n22",
        legalName = "n23",
        latitude = "1.0000000",
        longitude = "something wrong",
        score = 1.0f
      )

      val expected = AddressResponseAddress(
        uprn = "n1",
        formattedAddress = "",
        paf = None,
        nag = Some(AddressResponseNag(
          uprn = "n1",
          postcodeLocator = "n2",
          addressBasePostal = "n3",
          ursn = "n4",
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
          logicalStatus = "n18",
          streetDescriptor = "n19",
          townName = "n20",
          locality = "n21",
          organisation = "n22",
          legalName = "n23"
        )),
        geo = None,
        underlyingScore = 1,
        underlyingMaxScore = 1
      )

      // When
      val result = AddressResponseAddress.fromNagAddress(1)(givenNag)

      // Then
      result shouldBe expected
    }

  }

}
