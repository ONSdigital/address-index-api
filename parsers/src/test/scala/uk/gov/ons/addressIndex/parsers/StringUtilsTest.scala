package uk.gov.ons.addressIndex.parsers

import org.scalatest.FlatSpec
import uk.gov.ons.addressIndex.parsers.Implicits._

class StringUtilsTest extends FlatSpec{
  it should "return true for `3232` when invoking `allDigits`" in {
    assert("3232".allDigits[Boolean](identity))
  }
}