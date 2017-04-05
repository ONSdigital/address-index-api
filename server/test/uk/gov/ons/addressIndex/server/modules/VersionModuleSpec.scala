package uk.gov.ons.addressIndex.server.modules

import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.testkit._
import org.scalatest.WordSpec
import uk.gov.ons.addressIndex.model.config.AddressIndexConfig
import uk.gov.ons.addressIndex.server.model.dao.ElasticClientProvider


class VersionModuleSpec extends WordSpec with SearchMatchers with ElasticSugar {

  // this is necessary so that it can be injected in the provider (otherwise the method will call itself)
  val testClient = client

  // injections
  val elasticClientProvider = new ElasticClientProvider {
    override def client: ElasticClient = testClient
  }

  val testConfig = new AddressIndexConfigModule

  val invalidConfig = new ConfigModule {
    override def config: AddressIndexConfig = testConfig.config.copy(
      elasticSearch = testConfig.config.elasticSearch.copy(
        indexes = testConfig.config.elasticSearch.indexes.copy(hybridIndex = "invalid")
      )
    )
  }

  val hybridIndex1 = "hybrid_33_202020"
  val hybridIndex2 = "hybrid_34_202020"
  val hybridIndex3 = "hybrid_35_202020"
  val hybridAlias = testConfig.config.elasticSearch.indexes.hybridIndex

  client.execute(
    bulk(
      index into s"$hybridIndex1/address" id 11 fields ("name" -> "test1"),
      index into s"$hybridIndex2/address" id 12 fields ("name" -> "test2"),
      index into s"$hybridIndex3/address" id 13 fields ("name" -> "test3")
    )
  ).await

  blockUntilCount(1, hybridIndex1)
  blockUntilCount(1, hybridIndex2)
  blockUntilCount(1, hybridIndex3)

  client.execute {
    add alias hybridAlias on hybridIndex2
  }.await

  "Version module" should {

    "extract epoch version from a correct alias->index" in {
      // Given
      val versionModule = new AddressIndexVersionModule(testConfig, elasticClientProvider)
      val expected = "34"

      // When
      val result = versionModule.dataVersion

      // Then
      result shouldBe expected
    }

    "not extract epoch version from an incorrect alias" in {
      // Given
      val versionModule = new AddressIndexVersionModule(invalidConfig, elasticClientProvider)
      val expected = "develop"

      // When
      val result = versionModule.dataVersion

      // Then
      result shouldBe expected
    }

    "extract api version from file in resources" in {
      // Given
      val versionModule = new AddressIndexVersionModule(testConfig, elasticClientProvider)
      val expected = "test"

      // When
      val result = versionModule.apiVersion

      // Then
      result shouldBe expected
    }

  }
}

