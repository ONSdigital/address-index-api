package uk.gov.ons.addressIndex.parsers

import org.scalatest.{FlatSpec, Matchers}
/**
  * This test file's expected values were created by using a Python Prototype.
  */
class AddressParserTest extends FlatSpec with Matchers {

  ignore should "create the correct `CrfJniInput` for input `mySingleton`" in {
    val input = "mySingleton"
    val actual = AddressParser.parse(input, FeatureAnalysers.allFeatures, Tokens)
    val expected = "\tbusiness:0.0\tcompany:0.0\tdigits\\:no_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:1.0\thyphenations:0.0\tlength\\:w\\:11:1.0\tlocational:0.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\tresidential:0.0\troad:0.0 \tsingleton:1.0\tword\\:MYSINGLETON:1.0\n"



  }

  ignore should "create the correct `CrfJniInput` for input `my pair`" in {
    val input = "my pair"
    val actual = AddressParser.parse(input, FeatureAnalysers.allFeatures, Tokens)
    val expected = "\tbusiness:0.0\tcompany:0.0\tdigits\\:no_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:0.0\thyphenations:0.0\tlength\\:w\\:2:1.0\tlocational:0.0\tnext\\:business:0.0\tnext\\:company:0.0\tnext\\:digits\\:no_digits:1.0\tnext\\:directional:0.0\tnext\\:endsinpunc:0.0\tnext\\:flat:0.0\tnext\\:has.vowels:1.0\tnext\\:hyphenations:0.0\tnext\\:length\\:w\\:4:1.0\tnext\\:locational:0.0\tnext\\:ordinal:0.0\tnext\\:outcode:0.0\tnext\\:posttown:0.0\tnext\\:rawstring.end:1.0\tnext\\:residential:0.0\tnext\\:road:0.0\tnext\\:word\\:PAIR:1.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\trawstring.start:1.0\tresidential:0.0\troad:0.0\tword\\:MY:1.0\n\tbusiness:0.0\tcompany:0.0\tdigits\\:no_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:1.0\thyphenations:0.0\tlength\\:w\\:4:1.0\tlocational:0.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\tprevious\\:business:0.0\tprevious\\:company:0.0\tprevious\\:digits\\:no_digits:1.0\tprevious\\:directional:0.0\tprevious\\:endsinpunc:0.0\tprevious\\:flat:0.0\tprevious\\:has.vowels:0.0\tprevious\\:hyphenations:0.0\tprevious\\:length\\:w\\:2:1.0\tprevious\\:locational:0.0\tprevious\\:ordinal:0.0\tprevious\\:outcode:0.0\tprevious\\:posttown:0.0\tprevious\\:rawstring.start:1.0\tprevious\\:residential:0.0\tprevious\\:road:0.0\tprevious\\:word\\:MY:1.0\trawstring.end:1.0\tresidential:0.0\troad:0.0\tword\\:PAIR:1.0"

    println(s"Actual:\n$actual\nExpected:\n$expected")

    assert(false)
  }
}