package uk.gov.ons.addressIndex.server.modules

import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.testkit._
import org.scalatest.WordSpec
import uk.gov.ons.addressIndex.model.db.index.PostcodeAddressFileAddress
import uk.gov.ons.addressIndex.server.model.response.AddressTokens

import scala.concurrent.ExecutionContext.Implicits.global

class ElasticsearchRepositorySpec extends WordSpec with SearchMatchers with ElasticSugar {

  // this is necessary so that it can be injected in the provider (otherwise the method will call itself)
  val testClient = client

  // injections
  val elasticClientProvider = new ElasticClientProvider {
    override def client: ElasticClient = testClient
  }
  val config = new AddressIndexConfigModule

  val index = config.config.elasticSearch.indexes.pafIndex
  val Array(indexName, mappings) = index.split("/")

  testClient.execute {
    bulk(
      indexInto(indexName / mappings).fields(
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
      indexInto(indexName / mappings).fields(
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
      )
    )
  }.await

  blockUntilCount(2, indexName)

  "Elastic repository" should {

    "find address by UPRN" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val expected = Some(PostcodeAddressFileAddress(
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
        "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", 1.0f
      ))

      // When
      val result = repository.queryUprn("4").await

      // Then

      result shouldBe expected
    }

    "find address by building number and a postcode" in {
      // Given
      val repository = new AddressIndexRepository(config, elasticClientProvider)
      val tokens = AddressTokens(
        uprn = "4",
        buildingNumber = "10",
        postcode = "16"
      )
      val expected = PostcodeAddressFileAddress(
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15",
        "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", 1.4142135f
      )

      // When
      val (results, maxScore) = repository.queryAddress(tokens).await

      // Then
      results.length shouldBe 1
      results.head shouldBe expected
      maxScore shouldBe 1.4142135f
    }

  }

}
