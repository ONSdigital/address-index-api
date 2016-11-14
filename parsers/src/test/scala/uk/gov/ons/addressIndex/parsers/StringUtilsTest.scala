package uk.gov.ons.addressIndex.parsers

import org.scalatest.{FlatSpec, Matchers}
import uk.gov.ons.addressIndex.parsers.Implicits._

class StringUtilsTest extends FlatSpec with Matchers {

  it should "return true for `3232` when invoking `allDigits`" in {
    val expected = true
    val actual = "3232".allDigits[Boolean](identity)
    actual shouldBe expected
  }

  it should "return false for `hello` when invoking `allDigits`" in {
    val expected = false
    val actual = !"hello".allDigits[Boolean](identity)
    actual shouldBe expected
  }

  it should "return false for `hello123` when invoking `allDigits`" in {
    val expected = false
    val actual = !"hello123".allDigits[Boolean](identity)
    actual shouldBe expected
  }

  it should "return true for `hello123` when invoking `containsDigits`" in {
    val expected = true
    val actual = !"hello123".containsDigits[Boolean](identity)
    actual shouldBe expected
  }

  it should "return false for `hello` when invoking `containsDigits`" in {
    val expected = false
    val actual = !"hello".containsDigits[Boolean](identity)
    actual shouldBe expected
  }

  it should "return true for `hello` when invoking `containsVowels`" in {
    val expected = true
    val actual = !"hello".containsVowels[Boolean](identity)
    actual shouldBe expected
  }

  it should "return false for `hll` when invoking `containsVowels`" in {
    val expected = false
    val actual = !"hll".containsVowels[Boolean](identity)
    actual shouldBe expected
  }
}