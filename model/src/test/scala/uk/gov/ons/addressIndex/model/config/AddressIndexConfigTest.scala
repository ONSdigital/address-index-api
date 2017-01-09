package uk.gov.ons.addressIndex.model.config

import org.scalatest.{FlatSpec, Matchers}

class AddressIndexConfigTest extends FlatSpec with Matchers {

  ignore should "produce the expected default config" in {
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
  ignore should "produce the expected default config" in {
    val expected = ElasticSearchConfigTest.default
    val actual = ElasticSearchConfig.default
    actual shouldBe expected
  }
}

object ElasticSearchConfigTest {
  val default = ElasticSearchConfig(
    uri = "elasticsearch://localhost:9200",
    cluster = "ons-cluster",
    local = false,
    shield = ShieldConfig(
      user = "",
      password = "",
      ssl = true
    ),
    indexes = IndexesConfig(
      pafIndex = "paf/address",
      nagIndex = "nag/address"
    ),
    defaultLimit=10,
    defaultOffset=0,
    maximumLimit=100,
    maximumOffset=1000
  )
}