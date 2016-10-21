package uk.gov.ons.addressIndex.model

import uk.gov.ons.addressIndex.model.AddressScheme.StringToAddressSchemeAugmenter
import org.scalatest.{FlatSpec, Matchers}

class AddressIndexModelTest extends FlatSpec with Matchers {

  it should "produce the correct `AddressScheme` (`PostcodeAddressFile`) for the given string `paf`" in {
    val sample = "paf"
    val expected = PostcodeAddressFile()
    sample.toAddressScheme map (_ shouldBe expected)
  }

  it should "produce the correct `AddressScheme` (`BritishStandard7666`) for the given string `britishstandard7666`" in {
    val sample = "britishstandard7666"
    val expected = BritishStandard7666()
    sample.toAddressScheme map(_ shouldBe expected)
  }

  it should "produce the correct `AddressScheme` (`BritishStandard7666`) for the given string `bs7666`" in {
    val sample = "bs7666"
    val expected = BritishStandard7666()
    sample.toAddressScheme map(_ shouldBe expected)
  }

  it should "produce a `InvalidAddressSchemeException` for the given string `someRandomString`" in {
    val sample = "someRandomString"
    sample.toAddressScheme recover {
      case InvalidAddressSchemeException(msg) => assert(true)
      case _ => assert(false)
    }
  }
}