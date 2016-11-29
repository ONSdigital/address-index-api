package uk.gov.ons.addressIndex.server.model.response

import org.scalatest.{Matchers, WordSpec}
import uk.gov.ons.addressIndex.model.db.index.PostcodeAddressFileAddress

class AddressResponseAddressSpec extends WordSpec with Matchers {

  "Address response Address model" should {
    "be creatable from Elastic response" in {
      // Given
      val given = PostcodeAddressFileAddress(
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
      val result = AddressResponseAddress.fromPafAddress(given)

      // Then
      result shouldBe expected
    }
  }

}
