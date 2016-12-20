package uk.gov.ons.addressIndex.model.db.index

import org.scalatest.{FlatSpec, Matchers}

class PostcodeAddressFileAddressTest extends FlatSpec with Matchers {

  it should "have an expected fixed name" in {
    val expected = PostcodeAddressFileAddressTest.name
    val actual = PostcodeAddressFileAddress.Name
    expected shouldBe actual
  }
}

object PostcodeAddressFileAddressTest {
  val name = "PostcodeAddressFile"
}