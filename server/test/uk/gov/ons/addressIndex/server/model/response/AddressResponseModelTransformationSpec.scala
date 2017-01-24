package uk.gov.ons.addressIndex.server.model.response

import org.scalatest.{Matchers, WordSpec}
import uk.gov.ons.addressIndex.model.db.index.{NationalAddressGazetteer, PostcodeAddressFile}
import uk.gov.ons.addressIndex.model.server.response._

/**
  * Test conversion between ES reply and the model that will be send in the response
  */
class AddressResponseModelTransformationSpec extends WordSpec with Matchers {

  val givenNag = NationalAddressGazetteer(
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
    locality = "n21"
  )

  val givenPaf = PostcodeAddressFile(
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
    entryDate = "29"
  )

  "Address response Address model" should {

    "be creatable from Elastic PAF response" in {
      // Given
      val paf = givenPaf

      val expected = AddressInformation(
        uprn = paf.uprn,
        paf = Some(
          Seq(
            PAFWithFormat(
              formattedAddress = "7, 6, 8, 9, PO BOX 24, 10 11, 12, 13, 14, 15, 16",
              paf = PAF(
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
            )
          )
        ),
        nag = None,
        underlyingScore = 1,
        underlyingMaxScore = 1
      )

      // When
      val actual = Seq(paf.toPAFWithFormat)

      // Then
      expected.paf.getOrElse(Seq.empty) should contain theSameElementsAs actual
    }

    "handle absent dependentThoroughfare in the formatted address" in {
      // Given
      val paf = givenPaf.copy(dependentThoroughfare = "")

      val expected = "7, 6, 8, 9, PO BOX 24, 10 12, 13, 14, 15, 16"

      // When
      val actual = paf.toPAFWithFormat.formattedAddress

      // Then
      actual shouldBe expected
    }

    "handle absent PO box in the formatted address" in {
      // Given
      val paf = givenPaf.copy(poBoxNumber = "")

      val expected = "7, 6, 8, 9, 10 11, 12, 13, 14, 15, 16"

      // When
      val actual = paf.toPAFWithFormat.formattedAddress

      // Then
      actual shouldBe expected
    }

    "be creatable from Elastic NAG response" in {
      // Given
      val nag = givenNag

      val expected = AddressInformation(
        uprn = nag.uprn,
        paf = None,
        nag = Some(
          Seq(
            NAGWithFormat(
              formattedAddress = "n22, n12n13-n14n15, n11, n6, n7n8-n9n10 n19, n21, n20, n2",
              nag = NAG(
                nag.uprn,
                nag.postcodeLocator,
                nag.addressBasePostal,
                nag.usrn,
                nag.lpiKey,
                pao = PAO(
                  nag.paoText,
                  nag.paoStartNumber,
                  nag.paoStartSuffix,
                  nag.paoEndNumber,
                  nag.paoEndSuffix
                ),
                sao = SAO(
                  nag.saoText,
                  nag.saoStartNumber,
                  nag.saoStartSuffix,
                  nag.saoEndNumber,
                  nag.saoEndSuffix
                ),
                geo = GEO(
                  nag.latitude.toDouble,
                  nag.longitude.toDouble,
                  nag.easting.toDouble,
                  nag.northing.toDouble
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
              )
            )
          )
        ),
        underlyingScore = 1,
        underlyingMaxScore = 1
      )

      // When
      val actual = Seq(nag.toNagWithFormat)

      // Then
      expected.nag.getOrElse(Seq.empty) should contain theSameElementsAs actual
    }
  }
}
