package uk.gov.ons.addressIndex.model

import uk.gov.ons.addressIndex.model.AddressScheme.StringToAddressSchemeAugmenter
import uk.gov.ons.addressIndex.model.{BritishStandard7666, PostcodeAddressFile, UnrecognisedAddressScheme}
import org.scalatest.{FlatSpec, Matchers, OptionValues}

class AddressIndexModelTest
  extends FlatSpec
  with Matchers
  with OptionValues {

 it should "produce the correct `AddressScheme` (`PostcodeAddressFile`) for the given string `paf`" in {
   val sample   = "paf"
   val expected = PostcodeAddressFile()
   val actual   = sample.toAddressScheme

   actual shouldBe expected
 }

  it should "produce the correct `AddressScheme` (`BritishStandard7666`) for the given string `britishstandard7666`" in {
    val sample   = "britishstandard7666"
    val expected = BritishStandard7666()
    val actual   = sample.toAddressScheme

    actual shouldBe expected
  }

  it should "produce the correct `AddressScheme` (`BritishStandard7666`) for the given string `bs7666`" in {
    val sample   = "bs7666"
    val expected = BritishStandard7666()
    val actual   = sample.toAddressScheme

    actual shouldBe expected
  }

  it should "produce the correct `AddressScheme` (`UnrecognisedAddressScheme`) for the given string `someRandomString`" in {
    val sample   = "someRandomString"
    val expected = UnrecognisedAddressScheme()
    val actual   = sample.toAddressScheme

    actual shouldBe expected
  }
}