package uk.gov.ons.addressIndex.model

import uk.gov.ons.addressIndex.model.AddressScheme.StringToAddressSchemeAugmenter
import org.scalatest.{FlatSpec, Matchers}
import uk.gov.ons.addressIndex.model.db.index.HybridIndex

class AddressIndexModelTest extends FlatSpec with Matchers {

  it should "produce the correct `AddressScheme` (`PostcodeAddressFile`) for the given string `paf`" in {
    val sample = "paf"
    sample.stringToScheme shouldBe Some(PostcodeAddressFile(HybridIndex.Fields.paf))
  }

  it should "produce the correct `AddressScheme` (`PostcodeAddressFile`) for the given string `postcodeAddressFile`" in {
    val sample = "postcodeAddressFile"
    sample.stringToScheme shouldBe Some(PostcodeAddressFile(HybridIndex.Fields.paf))
  }

  it should "produce the correct `AddressScheme` (`BritishStandard7666`) for the given string `britishstandard7666`" in {
    val sample = "britishstandard7666"
    sample.stringToScheme shouldBe Some(BritishStandard7666(HybridIndex.Fields.lpi))
  }

  it should "produce the correct `AddressScheme` (`BritishStandard7666`) for the given string `bs7666`" in {
    val sample = "bs7666"
    sample.stringToScheme shouldBe Some(BritishStandard7666(HybridIndex.Fields.lpi))
  }

  it should "produce the correct `AddressScheme` (`BritishStandard7666`) for the given string `bs`" in {
    val sample = "bs"
    sample.stringToScheme shouldBe Some(BritishStandard7666(HybridIndex.Fields.lpi))
  }

  it should "produce None for the given string `unsupported scheme`" in {
    val sample = "unsupported scheme"
    sample.stringToScheme shouldBe None
  }
}