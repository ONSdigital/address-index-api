package uk.gov.ons.addressIndex.model.config

import org.scalatest.{FlatSpec, Matchers}

class AddressIndexConfigTest extends FlatSpec with Matchers {

  it should "produce the expected default config" in {
    val expected = AddressIndexConfigTest.default
    val actual = AddressIndexConfig.default
    actual shouldBe expected
  }
}

object AddressIndexConfigTest {
  val default = AddressIndexConfig(
    elasticSearch = ElasticSearchConfigTest.default
  )
}

class ElasticSearchConfigTest extends FlatSpec with Matchers {
  it should "produce the expected default config" in {
    val expected = ElasticSearchConfigTest.default
    val actual = ElasticSearchConfig.default
    actual shouldBe expected
  }
}

object ElasticSearchConfigTest {
  val default =ElasticSearchConfig(
    uri = "elasticsearch://localhost:9200",
    cluster = "ons-cluster",
    local = false,
    indexes = IndexesConfig(
      hybridIndex = "hybrid/address"
    ),
    shield = ShieldConfig(
      ssl = true,
      user = "admin",
      password = ""
    ),
    defaultLimit=10,
    defaultOffset=0,
    maximumLimit=100,
    maximumOffset=1000
  )
}