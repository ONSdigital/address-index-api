package uk.gov.ons.addressIndex.server.modules

import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.testkit._
import org.scalatest.WordSpec
import uk.gov.ons.addressIndex.model.db.index.{NationalAddressGazetteerAddress, NationalAddressGazetteerAddresses, PostcodeAddressFileAddress, PostcodeAddressFileAddresses}
import uk.gov.ons.addressIndex.model.server.response.AddressTokens

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
  println(nagIndex)

  testClient.execute {
    bulk(
      indexInto(pafIndexName / pafMappings).fields(
        "recordIdentifier" -> "1",
        "changeType" -> "2",
        "proOrder" -> "3",
        "uprn" -> "4",
        "udprn" -> "5",
        "organizationName" -> "6",
        "departmentName" -> "7",
        "subBuildingName" -> "8",
        "buildingName" -> "9",
        "buildingNumber" -> "10",
        "dependentThoroughfare" -> "11",
        "thoroughfare" -> "12",
        "doubleDependentLocality" -> "13",
        "dependentLocality" -> "14",
        "postTown" -> "15",
        "postcode" -> "16",
        "postcodeType" -> "17",
        "deliveryPointSuffix" -> "18",
        "welshDependentThoroughfare" -> "19",
        "welshThoroughfare" -> "20",
        "welshDoubleDependentLocality" -> "21",
        "welshDependentLocality" -> "22",
        "welshPostTown" -> "23",
        "poBoxNumber" -> "24",
        "processDate" -> "25",
        "startDate" -> "26",
        "endDate" -> "27",
        "lastUpdateDate" -> "28",
        "entryDate" -> "29"
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
        "uprn" -> "n1",
        "postcodeLocator" -> "n2",
        "addressBasePostal" -> "n3",
        "ursn" -> "n4",
        "lpiKey" -> "n5",
        "paoText" -> "n6",
        "paoStartNumber" -> "n7",
        "paoStartSuffix" -> "n8",
        "paoEndNumber" -> "n9",
        "paoEndSuffix" -> "n10",
        "saoText" -> "n11",
        "saoStartNumber" -> "n12",
        "saoStartSuffix" -> "n13",
        "saoEndNumber" -> "n14",
        "saoEndSuffix" -> "n15",
        "level" -> "n16",
        "officialFlag" -> "n17",
        "logicalStatus" -> "n18",
        "streetDescriptor" -> "n19",
        "townName" -> "n20",
        "locality" -> "n21",
        "organisation" -> "n22",
        "legalName" -> "n23",
        "lat" -> "1.0000000",
        "lon" -> "2.0000000"
      ),
      indexInto(nagIndexName / nagMappings). fields(
        "uprn" -> "1n1",
        "postcodeLocator" -> "1n2",
        "addressBasePostal" -> "1n3",
        "ursn" -> "1n4",
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
        "lon" -> "2.0000000"
      )
    )
  }.await

  blockUntilCount(2, pafIndexName)
  blockUntilCount(2, nagIndexName)

  "Elastic repository" should {

    "find PAF address by UPRN" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val expected = Some(PostcodeAddressFileAddress(
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
        "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", 1.0f
      ))

      // When
      val result = repository.queryPafUprn("4").await

      // Then
      result shouldBe expected
    }

    "find NAG address by UPRN" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val expected = Some(NationalAddressGazetteerAddress(
        "n1", "n2", "n3", "n4", "n5", "n6", "n7", "n8", "n9", "n10", "n11", "n12", "n13", "n14", "n15",
        "n16", "n17", "n18", "n19", "n20", "n21", "n22", "n23", "1.0000000", "2.0000000", 1.0f
      ))

      // When
      val result = repository.queryNagUprn("n1").await

      // Then
      result shouldBe expected
    }

    "find PAF addresses by building number and a postcode" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val tokens = AddressTokens(
        uprn = "4",
        buildingNumber = "10",
        postcode = "16"
      )
      val expectedScore = 1.4142135f
      val expected = PostcodeAddressFileAddress(
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
        "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", expectedScore
      )

      // When
      val PostcodeAddressFileAddresses(results, maxScore) = repository.queryPafAddresses(tokens).await

      // Then
      results.length shouldBe 1
      results.head shouldBe expected
      maxScore shouldBe expectedScore
    }

    "find NAG addresses by building number and a postcode" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val tokens = AddressTokens(
        uprn = "n1",
        buildingNumber = "n7",
        postcode = "n2"
      )
      val expectedScore = 1.4142135f
      val expected = NationalAddressGazetteerAddress(
        "n1", "n2", "n3", "n4", "n5", "n6", "n7", "n8", "n9", "n10", "n11", "n12", "n13", "n14", "n15",
        "n16", "n17", "n18", "n19", "n20", "n21", "n22", "n23", "1.0000000", "2.0000000", expectedScore
      )

      // When
      val NationalAddressGazetteerAddresses(results, maxScore) = repository.queryNagAddresses(tokens).await

      // Then
      results.length shouldBe 1
      results.head shouldBe expected
      maxScore shouldBe expectedScore
    }

  }

}
