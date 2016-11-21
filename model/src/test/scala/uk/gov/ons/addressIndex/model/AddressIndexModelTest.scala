package uk.gov.ons.addressIndex.model

import uk.gov.ons.addressIndex.model.AddressScheme.StringToAddressSchemeAugmenter
import org.scalatest.{FlatSpec, Matchers}

class AddressIndexModelTest extends FlatSpec with Matchers {

  it should "produce the correct `AddressScheme` (`PostcodeAddressFile`) for the given string `paf`" in {
    val sample = "paf"
    sample.toAddressScheme  shouldBe a [PostcodeAddressFile]
  }

  it should "produce the correct `AddressScheme` (`PostcodeAddressFile`) for the given string `postcodeAddressFile`" in {
    val sample = "postcodeAddressFile"
    sample.toAddressScheme shouldBe a [PostcodeAddressFile]
  }

  it should "produce the correct `AddressScheme` (`BritishStandard7666`) for the given string `britishstandard7666`" in {
    val sample = "britishstandard7666"
    sample.toAddressScheme shouldBe a [BritishStandard7666]
  }

  it should "produce the correct `AddressScheme` (`BritishStandard7666`) for the given string `bs7666`" in {
    val sample = "bs7666"
    sample.toAddressScheme shouldBe a [BritishStandard7666]
  }

  it should "produce the correct `AddressScheme` (`BritishStandard7666`) for the given string `bs`" in {
    val sample = "bs"
    sample.toAddressScheme shouldBe a [BritishStandard7666]
  }

}