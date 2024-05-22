package uk.gov.ons.addressIndex.server.utils

import org.scalatest._
import flatspec._
import matchers._
import play.api.Logger

class DecodeHelperTest extends AnyFlatSpec with should.Matchers {

  val logger: Logger = Logger("DecodeHelperTest")

  it should "pass through an unencoded URL" in {
    // Given
    val inputURL = "&Big Wave Media"
    val expected ="&Big Wave Media"
    // When
    val actual = DecodeHelper.decodeUrl(inputURL)

    // Then
    actual shouldBe expected
  }

  it should "decode single encoded URL" in {
    // Given
    val inputURL = "%26Big%20Wave%20Media"
    val expected = "&Big Wave Media"
    // When
    val actual = DecodeHelper.decodeUrl(inputURL)

    // Then
    actual shouldBe expected
  }

  it should "decode a double encoded URL" in {
    // Given
    val inputURL = "%2526Big%2520Wave%2520Media"
    val expected = "&Big Wave Media"
    // When
    val actual = DecodeHelper.decodeUrl(inputURL)

    // Then
    actual shouldBe expected
  }
}
