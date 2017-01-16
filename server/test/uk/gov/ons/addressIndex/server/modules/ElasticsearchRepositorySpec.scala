package uk.gov.ons.addressIndex.server.modules

import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.testkit._
import org.scalatest.WordSpec
import uk.gov.ons.addressIndex.model.db.index.{NationalAddressGazetteerAddress, NationalAddressGazetteerAddresses, PostcodeAddressFileAddress, PostcodeAddressFileAddresses}
import uk.gov.ons.addressIndex.model.server.response.AddressTokens
import uk.gov.ons.addressIndex.server.modules.Model.Pagination

import scala.concurrent.ExecutionContext.Implicits.global

class ElasticsearchRepositorySpec extends WordSpec with SearchMatchers with ElasticSugar {

  // this is necessary so that it can be injected in the provider (otherwise the method will call itself)
  val testClient = client

  // injections
  val elasticClientProvider = new ElasticClientProvider {
    override def client: ElasticClient = testClient
  }
  val config = new AddressIndexConfigModule

  val pafIndex = config.config.elasticSearch.indexes.pafIndex
  val Array(pafIndexName, pafMappings) = pafIndex.split("/")

  val nagIndex = config.config.elasticSearch.indexes.nagIndex
  val Array(nagIndexName, nagMappings) = nagIndex.split("/")

  val pafRecordIdentifier = "1"
  val pafChangeType = "2"
  val pafProOrder = "3"
  val pafUprn = "4"
  val pafUdprn = "5"
  val pafOrganizationName = "6"
  val pafDepartmentName = "7"
  val pafSubBuildingName = "8"
  val pafBuildingName = "9"
  val pafBuildingNumber = "10"
  val pafDependentThoroughfare = "11"
  val pafThoroughfare = "12"
  val pafDoubleDependentLocality = "13"
  val pafDependentLocality = "14"
  val pafPostTown = "15"
  val pafPostcode = "16"
  val pafPostcodeType = "17"
  val pafDeliveryPointSuffix = "18"
  val pafWelshDependentThoroughfare = "19"
  val pafWelshThoroughfare = "20"
  val pafWelshDoubleDependentLocality = "21"
  val pafWelshDependentLocality = "22"
  val pafWelshPostTown = "23"
  val pafPoBoxNumber = "24"
  val pafProcessDate = "25"
  val pafStartDate = "26"
  val pafEndDate = "27"
  val pafLastUpdateDate = "28"
  val pafEntryDate = "29"


  val nagUprn = "n1"
  val nagPostcodeLocator = "n2"
  val nagAddressBasePostal = "n3"
  val nagUsrn = "n4"
  val nagLpiKey = "n5"
  val nagPaoText = "n6"
  val nagPaoStartNumber = "n7"
  val nagPaoStartSuffix = "n8"
  val nagPaoEndNumber = "n9"
  val nagPaoEndSuffix = "n10"
  val nagSaoText = "n11"
  val nagSaoStartNumber = "n12"
  val nagSaoStartSuffix = "n13"
  val nagSaoEndNumber = "n14"
  val nagSaoEndSuffix = "n15"
  val nagLevel = "n16"
  val nagOfficialFlag = "n17"
  val nagLogicalStatus = "n18"
  val nagStreetDescriptor = "n19"
  val nagTownName = "n20"
  val nagLocality = "n21"
  val nagOrganisation = "n22"
  val nagLegalName = "n23"
  val nagLatitude = "1.0000000"
  val nagLongitude = "2.0000000"
  val nagNorthing = "3"
  val nagEasting = "4"
  val nagClassificationCode = "n24"

  testClient.execute {
    bulk(
      indexInto(pafIndexName / pafMappings).fields(
        "recordIdentifier" -> pafRecordIdentifier,
        "changeType" -> pafChangeType,
        "proOrder" -> pafProOrder,
        "uprn" -> pafUprn,
        "udprn" -> pafUdprn,
        "organizationName" -> pafOrganizationName,
        "departmentName" -> pafDepartmentName,
        "subBuildingName" -> pafSubBuildingName,
        "buildingName" -> pafBuildingName,
        "buildingNumber" -> pafBuildingNumber,
        "dependentThoroughfare" -> pafDependentThoroughfare,
        "thoroughfare" -> pafThoroughfare,
        "doubleDependentLocality" -> pafDoubleDependentLocality,
        "dependentLocality" -> pafDependentLocality,
        "postTown" -> pafPostTown,
        "postcode" -> pafPostcode,
        "postcodeType" -> pafPostcodeType,
        "deliveryPointSuffix" -> pafDeliveryPointSuffix,
        "welshDependentThoroughfare" -> pafWelshDependentThoroughfare,
        "welshThoroughfare" -> pafWelshThoroughfare,
        "welshDoubleDependentLocality" -> pafWelshDoubleDependentLocality,
        "welshDependentLocality" -> pafWelshDependentLocality,
        "welshPostTown" -> pafWelshPostTown,
        "poBoxNumber" -> pafPoBoxNumber,
        "processDate" -> pafProcessDate,
        "startDate" -> pafStartDate,
        "endDate" -> pafEndDate,
        "lastUpdateDate" -> pafLastUpdateDate,
        "entryDate" ->pafEntryDate
      ),
      indexInto(pafIndexName / pafMappings).fields(
        "recordIdentifier" -> "a1",
        "changeType" -> "a2",
        "proOrder" -> "a3",
        "uprn" -> "a4",
        "udprn" -> "a5",
        "organizationName" -> "a6",
        "departmentName" -> "a7",
        "subBuildingName" -> "a8",
        "buildingName" -> "a9",
        "buildingNumber" -> "a10",
        "dependentThoroughfare" -> "a11",
        "thoroughfare" -> "a12",
        "doubleDependentLocality" -> "a13",
        "dependentLocality" -> "a14",
        "postTown" -> "a15",
        "postcode" -> "a16",
        "postcodeType" -> "a17",
        "deliveryPointSuffix" -> "a18",
        "welshDependentThoroughfare" -> "a19",
        "welshThoroughfare" -> "a20",
        "welshDoubleDependentLocality" -> "a21",
        "welshDependentLocality" -> "a22",
        "welshPostTown" -> "a23",
        "poBoxNumber" -> "a24",
        "processDate" -> "a25",
        "startDate" -> "a26",
        "endDate" -> "a27",
        "lastUpdateDate" -> "a28",
        "entryDate" -> "a29"
      ),
      indexInto(nagIndexName / nagMappings). fields(
        "uprn" -> nagUprn,
        "postcodeLocator" -> nagPostcodeLocator,
        "addressBasePostal" -> nagAddressBasePostal,
        "usrn" -> nagUsrn,
        "lpiKey" -> nagLpiKey,
        "paoText" -> nagPaoText,
        "paoStartNumber" -> nagPaoStartNumber,
        "paoStartSuffix" -> nagPaoStartSuffix,
        "paoEndNumber" -> nagPaoEndNumber,
        "paoEndSuffix" -> nagPaoEndSuffix,
        "saoText" -> nagSaoText,
        "saoStartNumber" -> nagSaoStartNumber,
        "saoStartSuffix" -> nagSaoStartSuffix,
        "saoEndNumber" -> nagSaoEndNumber,
        "saoEndSuffix" -> nagSaoEndSuffix,
        "level" -> nagLevel,
        "officialFlag" -> nagOfficialFlag,
        "logicalStatus" -> nagLogicalStatus,
        "streetDescriptor" -> nagStreetDescriptor,
        "townName" -> nagTownName,
        "locality" -> nagLocality,
        "organisation" -> nagOrganisation,
        "legalName" -> nagLegalName,
        "latitude" -> nagLatitude,
        "longitude" -> nagLongitude,
        "northing" -> nagNorthing,
        "easting" -> nagEasting,
        "classificationCode" -> nagClassificationCode

      ),
      indexInto(nagIndexName / nagMappings). fields(
        "uprn" -> "1n1",
        "postcodeLocator" -> "1n2",
        "addressBasePostal" -> "1n3",
        "usrn" -> "1n4",
        "lpiKey" -> "1n5",
        "paoText" -> "1n6",
        "paoStartNumber" -> "1n7",
        "paoStartSuffix" -> "1n8",
        "paoEndNumber" -> "1n9",
        "paoEndSuffix" -> "1n10",
        "saoText" -> "1n11",
        "saoStartNumber" -> "1n12",
        "saoStartSuffix" -> "1n13",
        "saoEndNumber" -> "1n14",
        "saoEndSuffix" -> "1n15",
        "level" -> "1n16",
        "officialFlag" -> "1n17",
        "logicalStatus" -> "1n18",
        "streetDescriptor" -> "1n19",
        "townName" -> "1n20",
        "locality" -> "1n21",
        "organisation" -> "1n22",
        "legalName" -> "1n23",
        "lat" -> "1.0000000",
        "lon" -> "2.0000000",
        "northing" -> "3",
        "easting" -> "4",
        "classificationCode" -> "n24"
      )
    )
  }.await

  blockUntilCount(2, pafIndexName)
  blockUntilCount(2, nagIndexName)

  val expectedPaf = PostcodeAddressFileAddress(
    pafRecordIdentifier,
    pafChangeType,
    pafProOrder,
    pafUprn,
    pafUdprn,
    pafOrganizationName,
    pafDepartmentName,
    pafSubBuildingName,
    pafBuildingName,
    pafBuildingNumber,
    pafDependentThoroughfare,
    pafThoroughfare,
    pafDoubleDependentLocality,
    pafDependentLocality,
    pafPostTown,
    pafPostcode,
    pafPostcodeType,
    pafDeliveryPointSuffix,
    pafWelshDependentThoroughfare,
    pafWelshThoroughfare,
    pafWelshDoubleDependentLocality,
    pafWelshDependentLocality,
    pafWelshPostTown,
    pafPoBoxNumber,
    pafProcessDate,
    pafStartDate,
    pafEndDate,
    pafLastUpdateDate,
    pafEntryDate,
    1.0f
  )

  val expectedNag = NationalAddressGazetteerAddress(
    nagUprn,
    nagPostcodeLocator,
    nagAddressBasePostal,
    nagLatitude,
    nagLongitude,
    nagEasting,
    nagNorthing,
    nagOrganisation,
    nagLegalName,
    nagClassificationCode,
    nagUsrn,
    nagLpiKey,
    nagPaoText,
    nagPaoStartNumber,
    nagPaoStartSuffix,
    nagPaoEndNumber,
    nagPaoEndSuffix,
    nagSaoText,
    nagSaoStartNumber,
    nagSaoStartSuffix,
    nagSaoEndNumber,
    nagSaoEndSuffix,
    nagLevel,
    nagOfficialFlag,
    nagLogicalStatus,
    nagStreetDescriptor,
    nagTownName,
    nagLocality,
    1.0f
  )

  "Elastic repository" should {

    implicit val pagination = Pagination(
      offset = 1,
      limit = 10
    )

    "find PAF address by UPRN" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val expected = Some(expectedPaf)

      // When
      val result = repository.queryPafUprn("4").await

      // Then
      result shouldBe expected
    }

    "find NAG address by UPRN" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val expected = Some(expectedNag)

      // When
      val result = repository.queryNagUprn("n1").await

      // Then
      result shouldBe expected
    }

    "find PAF addresses by building number and a postcode" ignore {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val tokens = Seq.empty

//        AddressTokens(
//        uprn = "4",
//        buildingNumber = "10",
//        postcode = "16"
//      )
      val expectedScore = 1.4142135f
      val expected = expectedPaf.copy(score = expectedScore)



      // When
      val PostcodeAddressFileAddresses(results, maxScore) = repository.queryPafAddresses(tokens).await

      // Then
      results.length shouldBe 1
      results.head shouldBe expected
      maxScore shouldBe expectedScore
    }

    "find NAG addresses by building number and a postcode" ignore {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val tokens = Seq.empty
//        AddressTokens(
//        uprn = "n1",
//        buildingNumber = "n7",
//        postcode = "n2"
//      )
      val expectedScore = 1.4142135f
      val expected = expectedNag.copy(score = expectedScore)

      // When
      val NationalAddressGazetteerAddresses(results, maxScore) = repository.queryNagAddresses(tokens).await

      // Then
      results.length shouldBe 1
      results.head shouldBe expected
      maxScore shouldBe expectedScore
    }

  }

}
