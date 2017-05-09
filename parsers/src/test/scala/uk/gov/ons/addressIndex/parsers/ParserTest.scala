package uk.gov.ons.addressIndex.parsers

import org.scalatest.{FlatSpec, Matchers}
import uk.gov.ons.addressIndex.crfscala.CrfParser
import uk.gov.ons.addressIndex.parsers.Parser.CrfLineData

class ParserTest extends FlatSpec with Matchers {

  it should "transform a single token into CRF input string" in {
    // Given
    val tokens = List("31")
    val expected = "\tRhysBradbury\tbusiness:0.0\tcompany:0.0\tdigits\\:all_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:0.0\thyphenations:0.0\tlength\\:d\\:2:1.0\tlocational:0.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\tresidential:0.0\troad:0.0\tsingleton:1.0\tword:0.0\n"

    // When
    val result = Parser.constructCrfInput(tokens)

    // Then
    result shouldBe expected
  }

  it should "transform a two tokens into CRF input string" in {
    // Given
    val tokens = List("31", "CURLEW")

    val expected =
      "\tRhysBradbury\tbusiness:0.0\tcompany:0.0\tdigits\\:all_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:0.0\thyphenations:0.0\tlength\\:d\\:2:1.0\tlocational:0.0\tnext\\:business:0.0\tnext\\:company:0.0\tnext\\:digits\\:no_digits:1.0\tnext\\:directional:0.0\tnext\\:endsinpunc:0.0\tnext\\:flat:0.0\tnext\\:has.vowels:1.0\tnext\\:hyphenations:0.0\tnext\\:length\\:w\\:6:1.0\tnext\\:locational:0.0\tnext\\:ordinal:0.0\tnext\\:outcode:0.0\tnext\\:posttown:0.0\tnext\\:rawstring.end:1.0\tnext\\:residential:0.0\tnext\\:road:0.0\tnext\\:word\\:CURLEW:1.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\trawstring.start:1.0\tresidential:0.0\troad:0.0\tword:0.0\n" +
        "\tRhysBradbury\tbusiness:0.0\tcompany:0.0\tdigits\\:no_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:1.0\thyphenations:0.0\tlength\\:w\\:6:1.0\tlocational:0.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\tprevious\\:business:0.0\tprevious\\:company:0.0\tprevious\\:digits\\:all_digits:1.0\tprevious\\:directional:0.0\tprevious\\:endsinpunc:0.0\tprevious\\:flat:0.0\tprevious\\:has.vowels:0.0\tprevious\\:hyphenations:0.0\tprevious\\:length\\:d\\:2:1.0\tprevious\\:locational:0.0\tprevious\\:ordinal:0.0\tprevious\\:outcode:0.0\tprevious\\:posttown:0.0\tprevious\\:rawstring.start:1.0\tprevious\\:residential:0.0\tprevious\\:road:0.0\tprevious\\:word:0.0\trawstring.end:1.0\tresidential:0.0\troad:0.0\tword\\:CURLEW:1.0\n"

    // When
    val result = Parser.constructCrfInput(tokens)

    // Then
    result shouldBe expected
  }

  it should "transform a three tokens into CRF input string" ignore { // This is ignored because we ne need to back-port a bug, so until it's fixed, this should be ignored
    // Given
    val tokens = List("31", "CURLEW", "WAY")

    val expected =
      "\tRhysBradbury\tbusiness:0.0\tcompany:0.0\tdigits\\:all_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:0.0\thyphenations:0.0\tlength\\:d\\:2:1.0\tlocational:0.0\tnext\\:business:0.0\tnext\\:company:0.0\tnext\\:digits\\:no_digits:1.0\tnext\\:directional:0.0\tnext\\:endsinpunc:0.0\tnext\\:flat:0.0\tnext\\:has.vowels:1.0\tnext\\:hyphenations:0.0\tnext\\:length\\:w\\:6:1.0\tnext\\:locational:0.0\tnext\\:ordinal:0.0\tnext\\:outcode:0.0\tnext\\:posttown:0.0\tnext\\:residential:0.0\tnext\\:road:0.0\tnext\\:word\\:CURLEW:1.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\trawstring.start:1.0\tresidential:0.0\troad:0.0\tword:0.0\n" +
        "\tRhysBradbury\tbusiness:0.0\tcompany:0.0\tdigits\\:no_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:1.0\thyphenations:0.0\tlength\\:w\\:6:1.0\tlocational:0.0\tnext\\:business:0.0\tnext\\:company:0.0\tnext\\:digits\\:no_digits:1.0\tnext\\:directional:0.0\tnext\\:endsinpunc:0.0\tnext\\:flat:0.0\tnext\\:has.vowels:1.0\tnext\\:hyphenations:0.0\tnext\\:length\\:w\\:3:1.0\tnext\\:locational:0.0\tnext\\:ordinal:0.0\tnext\\:outcode:0.0\tnext\\:posttown:0.0\tnext\\:rawstring.end:1.0\tnext\\:residential:0.0\tnext\\:road:1.0\tnext\\:word\\:WAY:1.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\tprevious\\:business:0.0\tprevious\\:company:0.0\tprevious\\:digits\\:all_digits:1.0\tprevious\\:directional:0.0\tprevious\\:endsinpunc:0.0\tprevious\\:flat:0.0\tprevious\\:has.vowels:0.0\tprevious\\:hyphenations:0.0\tprevious\\:length\\:d\\:2:1.0\tprevious\\:locational:0.0\tprevious\\:ordinal:0.0\tprevious\\:outcode:0.0\tprevious\\:posttown:0.0\tprevious\\:rawstring.start:1.0\tprevious\\:residential:0.0\tprevious\\:road:0.0\tprevious\\:word:0.0\tresidential:0.0\troad:0.0\tword\\:CURLEW:1.0\n" +
        "\tRhysBradbury\tbusiness:0.0\tcompany:0.0\tdigits\\:no_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:1.0\thyphenations:0.0\tlength\\:w\\:3:1.0\tlocational:0.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\tprevious\\:business:0.0\tprevious\\:company:0.0\tprevious\\:digits\\:no_digits:1.0\tprevious\\:directional:0.0\tprevious\\:endsinpunc:0.0\tprevious\\:flat:0.0\tprevious\\:has.vowels:1.0\tprevious\\:hyphenations:0.0\tprevious\\:length\\:w\\:6:1.0\tprevious\\:locational:0.0\tprevious\\:ordinal:0.0\tprevious\\:outcode:0.0\tprevious\\:posttown:0.0\tprevious\\:residential:0.0\tprevious\\:road:0.0\tprevious\\:word\\:CURLEW:1.0\trawstring.end:1.0\tresidential:0.0\troad:1.0\tword\\:WAY:1.0\n"

    // When
    val result = Parser.constructCrfInput(tokens)

    // Then
    result shouldBe expected
  }

  it should "transform a four tokens into CRF input string" in {
    // Given
    val tokens = List("31", "CURLEW", "WAY", "STREET")

    val expected =
      "\tRhysBradbury\tbusiness:0.0\tcompany:0.0\tdigits\\:all_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:0.0\thyphenations:0.0\tlength\\:d\\:2:1.0\tlocational:0.0\tnext\\:business:0.0\tnext\\:company:0.0\tnext\\:digits\\:no_digits:1.0\tnext\\:directional:0.0\tnext\\:endsinpunc:0.0\tnext\\:flat:0.0\tnext\\:has.vowels:1.0\tnext\\:hyphenations:0.0\tnext\\:length\\:w\\:6:1.0\tnext\\:locational:0.0\tnext\\:ordinal:0.0\tnext\\:outcode:0.0\tnext\\:posttown:0.0\tnext\\:residential:0.0\tnext\\:road:0.0\tnext\\:word\\:CURLEW:1.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\trawstring.start:1.0\tresidential:0.0\troad:0.0\tword:0.0\n" +
        "\tRhysBradbury\tbusiness:0.0\tcompany:0.0\tdigits\\:no_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:1.0\thyphenations:0.0\tlength\\:w\\:6:1.0\tlocational:0.0\tnext\\:business:0.0\tnext\\:company:0.0\tnext\\:digits\\:no_digits:1.0\tnext\\:directional:0.0\tnext\\:endsinpunc:0.0\tnext\\:flat:0.0\tnext\\:has.vowels:1.0\tnext\\:hyphenations:0.0\tnext\\:length\\:w\\:3:1.0\tnext\\:locational:0.0\tnext\\:ordinal:0.0\tnext\\:outcode:0.0\tnext\\:posttown:0.0\tnext\\:residential:0.0\tnext\\:road:1.0\tnext\\:word\\:WAY:1.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\tprevious\\:business:0.0\tprevious\\:company:0.0\tprevious\\:digits\\:all_digits:1.0\tprevious\\:directional:0.0\tprevious\\:endsinpunc:0.0\tprevious\\:flat:0.0\tprevious\\:has.vowels:0.0\tprevious\\:hyphenations:0.0\tprevious\\:length\\:d\\:2:1.0\tprevious\\:locational:0.0\tprevious\\:ordinal:0.0\tprevious\\:outcode:0.0\tprevious\\:posttown:0.0\tprevious\\:rawstring.start:1.0\tprevious\\:residential:0.0\tprevious\\:road:0.0\tprevious\\:word:0.0\tresidential:0.0\troad:0.0\tword\\:CURLEW:1.0\n" +
        "\tRhysBradbury\tbusiness:0.0\tcompany:0.0\tdigits\\:no_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:1.0\thyphenations:0.0\tlength\\:w\\:3:1.0\tlocational:0.0\tnext\\:business:0.0\tnext\\:company:0.0\tnext\\:digits\\:no_digits:1.0\tnext\\:directional:0.0\tnext\\:endsinpunc:0.0\tnext\\:flat:0.0\tnext\\:has.vowels:1.0\tnext\\:hyphenations:0.0\tnext\\:length\\:w\\:6:1.0\tnext\\:locational:0.0\tnext\\:ordinal:0.0\tnext\\:outcode:0.0\tnext\\:posttown:1.0\tnext\\:rawstring.end:1.0\tnext\\:residential:0.0\tnext\\:road:1.0\tnext\\:word\\:STREET:1.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\tprevious\\:business:0.0\tprevious\\:company:0.0\tprevious\\:digits\\:no_digits:1.0\tprevious\\:directional:0.0\tprevious\\:endsinpunc:0.0\tprevious\\:flat:0.0\tprevious\\:has.vowels:1.0\tprevious\\:hyphenations:0.0\tprevious\\:length\\:w\\:6:1.0\tprevious\\:locational:0.0\tprevious\\:ordinal:0.0\tprevious\\:outcode:0.0\tprevious\\:posttown:0.0\tprevious\\:residential:0.0\tprevious\\:road:0.0\tprevious\\:word\\:CURLEW:1.0\tresidential:0.0\troad:1.0\tword\\:WAY:1.0\n" +
        "\tRhysBradbury\tbusiness:0.0\tcompany:0.0\tdigits\\:no_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:1.0\thyphenations:0.0\tlength\\:w\\:6:1.0\tlocational:0.0\tordinal:0.0\toutcode:0.0\tposttown:1.0\tprevious\\:business:0.0\tprevious\\:company:0.0\tprevious\\:digits\\:no_digits:1.0\tprevious\\:directional:0.0\tprevious\\:endsinpunc:0.0\tprevious\\:flat:0.0\tprevious\\:has.vowels:1.0\tprevious\\:hyphenations:0.0\tprevious\\:length\\:w\\:3:1.0\tprevious\\:locational:0.0\tprevious\\:ordinal:0.0\tprevious\\:outcode:0.0\tprevious\\:posttown:0.0\tprevious\\:residential:0.0\tprevious\\:road:1.0\tprevious\\:word\\:WAY:1.0\trawstring.end:1.0\tresidential:0.0\troad:1.0\tword\\:STREET:1.0\n"

    // When
    val result = Parser.constructCrfInput(tokens)

    // Then
    result shouldBe expected
  }

  it should "transform a five tokens into CRF input string" in {
    // Given
    val tokens = List("31", "CURLEW", "TOWN", "WAY", "STREET")

    val expected =
      "\tRhysBradbury\tbusiness:0.0\tcompany:0.0\tdigits\\:all_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:0.0\thyphenations:0.0\tlength\\:d\\:2:1.0\tlocational:0.0\tnext\\:business:0.0\tnext\\:company:0.0\tnext\\:digits\\:no_digits:1.0\tnext\\:directional:0.0\tnext\\:endsinpunc:0.0\tnext\\:flat:0.0\tnext\\:has.vowels:1.0\tnext\\:hyphenations:0.0\tnext\\:length\\:w\\:6:1.0\tnext\\:locational:0.0\tnext\\:ordinal:0.0\tnext\\:outcode:0.0\tnext\\:posttown:0.0\tnext\\:residential:0.0\tnext\\:road:0.0\tnext\\:word\\:CURLEW:1.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\trawstring.start:1.0\tresidential:0.0\troad:0.0\tword:0.0\n" +
        "\tRhysBradbury\tbusiness:0.0\tcompany:0.0\tdigits\\:no_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:1.0\thyphenations:0.0\tlength\\:w\\:6:1.0\tlocational:0.0\tnext\\:business:0.0\tnext\\:company:0.0\tnext\\:digits\\:no_digits:1.0\tnext\\:directional:0.0\tnext\\:endsinpunc:0.0\tnext\\:flat:0.0\tnext\\:has.vowels:1.0\tnext\\:hyphenations:0.0\tnext\\:length\\:w\\:4:1.0\tnext\\:locational:0.0\tnext\\:ordinal:0.0\tnext\\:outcode:0.0\tnext\\:posttown:0.0\tnext\\:residential:0.0\tnext\\:road:0.0\tnext\\:word\\:TOWN:1.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\tprevious\\:business:0.0\tprevious\\:company:0.0\tprevious\\:digits\\:all_digits:1.0\tprevious\\:directional:0.0\tprevious\\:endsinpunc:0.0\tprevious\\:flat:0.0\tprevious\\:has.vowels:0.0\tprevious\\:hyphenations:0.0\tprevious\\:length\\:d\\:2:1.0\tprevious\\:locational:0.0\tprevious\\:ordinal:0.0\tprevious\\:outcode:0.0\tprevious\\:posttown:0.0\tprevious\\:rawstring.start:1.0\tprevious\\:residential:0.0\tprevious\\:road:0.0\tprevious\\:word:0.0\tresidential:0.0\troad:0.0\tword\\:CURLEW:1.0\n" +
        "\tRhysBradbury\tbusiness:0.0\tcompany:0.0\tdigits\\:no_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:1.0\thyphenations:0.0\tlength\\:w\\:4:1.0\tlocational:0.0\tnext\\:business:0.0\tnext\\:company:0.0\tnext\\:digits\\:no_digits:1.0\tnext\\:directional:0.0\tnext\\:endsinpunc:0.0\tnext\\:flat:0.0\tnext\\:has.vowels:1.0\tnext\\:hyphenations:0.0\tnext\\:length\\:w\\:3:1.0\tnext\\:locational:0.0\tnext\\:ordinal:0.0\tnext\\:outcode:0.0\tnext\\:posttown:0.0\tnext\\:residential:0.0\tnext\\:road:1.0\tnext\\:word\\:WAY:1.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\tprevious\\:business:0.0\tprevious\\:company:0.0\tprevious\\:digits\\:no_digits:1.0\tprevious\\:directional:0.0\tprevious\\:endsinpunc:0.0\tprevious\\:flat:0.0\tprevious\\:has.vowels:1.0\tprevious\\:hyphenations:0.0\tprevious\\:length\\:w\\:6:1.0\tprevious\\:locational:0.0\tprevious\\:ordinal:0.0\tprevious\\:outcode:0.0\tprevious\\:posttown:0.0\tprevious\\:residential:0.0\tprevious\\:road:0.0\tprevious\\:word\\:CURLEW:1.0\tresidential:0.0\troad:0.0\tword\\:TOWN:1.0\n" +
        "\tRhysBradbury\tbusiness:0.0\tcompany:0.0\tdigits\\:no_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:1.0\thyphenations:0.0\tlength\\:w\\:3:1.0\tlocational:0.0\tnext\\:business:0.0\tnext\\:company:0.0\tnext\\:digits\\:no_digits:1.0\tnext\\:directional:0.0\tnext\\:endsinpunc:0.0\tnext\\:flat:0.0\tnext\\:has.vowels:1.0\tnext\\:hyphenations:0.0\tnext\\:length\\:w\\:6:1.0\tnext\\:locational:0.0\tnext\\:ordinal:0.0\tnext\\:outcode:0.0\tnext\\:posttown:1.0\tnext\\:rawstring.end:1.0\tnext\\:residential:0.0\tnext\\:road:1.0\tnext\\:word\\:STREET:1.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\tprevious\\:business:0.0\tprevious\\:company:0.0\tprevious\\:digits\\:no_digits:1.0\tprevious\\:directional:0.0\tprevious\\:endsinpunc:0.0\tprevious\\:flat:0.0\tprevious\\:has.vowels:1.0\tprevious\\:hyphenations:0.0\tprevious\\:length\\:w\\:4:1.0\tprevious\\:locational:0.0\tprevious\\:ordinal:0.0\tprevious\\:outcode:0.0\tprevious\\:posttown:0.0\tprevious\\:residential:0.0\tprevious\\:road:0.0\tprevious\\:word\\:TOWN:1.0\tresidential:0.0\troad:1.0\tword\\:WAY:1.0\n" +
        "\tRhysBradbury\tbusiness:0.0\tcompany:0.0\tdigits\\:no_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:1.0\thyphenations:0.0\tlength\\:w\\:6:1.0\tlocational:0.0\tordinal:0.0\toutcode:0.0\tposttown:1.0\tprevious\\:business:0.0\tprevious\\:company:0.0\tprevious\\:digits\\:no_digits:1.0\tprevious\\:directional:0.0\tprevious\\:endsinpunc:0.0\tprevious\\:flat:0.0\tprevious\\:has.vowels:1.0\tprevious\\:hyphenations:0.0\tprevious\\:length\\:w\\:3:1.0\tprevious\\:locational:0.0\tprevious\\:ordinal:0.0\tprevious\\:outcode:0.0\tprevious\\:posttown:0.0\tprevious\\:residential:0.0\tprevious\\:road:1.0\tprevious\\:word\\:WAY:1.0\trawstring.end:1.0\tresidential:0.0\troad:1.0\tword\\:STREET:1.0\n"

    // When
    val result = Parser.constructCrfInput(tokens)

    // Then
    result shouldBe expected
  }

  it should "transform a six (and more) tokens into CRF input string" in {
    // Given
    val tokens = List("31", "CURLEW", "TOWN", "TOWN", "WAY", "STREET")

    val expected =
      "\tRhysBradbury\tbusiness:0.0\tcompany:0.0\tdigits\\:all_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:0.0\thyphenations:0.0\tlength\\:d\\:2:1.0\tlocational:0.0\tnext\\:business:0.0\tnext\\:company:0.0\tnext\\:digits\\:no_digits:1.0\tnext\\:directional:0.0\tnext\\:endsinpunc:0.0\tnext\\:flat:0.0\tnext\\:has.vowels:1.0\tnext\\:hyphenations:0.0\tnext\\:length\\:w\\:6:1.0\tnext\\:locational:0.0\tnext\\:ordinal:0.0\tnext\\:outcode:0.0\tnext\\:posttown:0.0\tnext\\:residential:0.0\tnext\\:road:0.0\tnext\\:word\\:CURLEW:1.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\trawstring.start:1.0\tresidential:0.0\troad:0.0\tword:0.0\n" +
        "\tRhysBradbury\tbusiness:0.0\tcompany:0.0\tdigits\\:no_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:1.0\thyphenations:0.0\tlength\\:w\\:6:1.0\tlocational:0.0\tnext\\:business:0.0\tnext\\:company:0.0\tnext\\:digits\\:no_digits:1.0\tnext\\:directional:0.0\tnext\\:endsinpunc:0.0\tnext\\:flat:0.0\tnext\\:has.vowels:1.0\tnext\\:hyphenations:0.0\tnext\\:length\\:w\\:4:1.0\tnext\\:locational:0.0\tnext\\:ordinal:0.0\tnext\\:outcode:0.0\tnext\\:posttown:0.0\tnext\\:residential:0.0\tnext\\:road:0.0\tnext\\:word\\:TOWN:1.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\tprevious\\:business:0.0\tprevious\\:company:0.0\tprevious\\:digits\\:all_digits:1.0\tprevious\\:directional:0.0\tprevious\\:endsinpunc:0.0\tprevious\\:flat:0.0\tprevious\\:has.vowels:0.0\tprevious\\:hyphenations:0.0\tprevious\\:length\\:d\\:2:1.0\tprevious\\:locational:0.0\tprevious\\:ordinal:0.0\tprevious\\:outcode:0.0\tprevious\\:posttown:0.0\tprevious\\:rawstring.start:1.0\tprevious\\:residential:0.0\tprevious\\:road:0.0\tprevious\\:word:0.0\tresidential:0.0\troad:0.0\tword\\:CURLEW:1.0\n" +
        "\tRhysBradbury\tbusiness:0.0\tcompany:0.0\tdigits\\:no_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:1.0\thyphenations:0.0\tlength\\:w\\:4:1.0\tlocational:0.0\tnext\\:business:0.0\tnext\\:company:0.0\tnext\\:digits\\:no_digits:1.0\tnext\\:directional:0.0\tnext\\:endsinpunc:0.0\tnext\\:flat:0.0\tnext\\:has.vowels:1.0\tnext\\:hyphenations:0.0\tnext\\:length\\:w\\:4:1.0\tnext\\:locational:0.0\tnext\\:ordinal:0.0\tnext\\:outcode:0.0\tnext\\:posttown:0.0\tnext\\:residential:0.0\tnext\\:road:0.0\tnext\\:word\\:TOWN:1.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\tprevious\\:business:0.0\tprevious\\:company:0.0\tprevious\\:digits\\:no_digits:1.0\tprevious\\:directional:0.0\tprevious\\:endsinpunc:0.0\tprevious\\:flat:0.0\tprevious\\:has.vowels:1.0\tprevious\\:hyphenations:0.0\tprevious\\:length\\:w\\:6:1.0\tprevious\\:locational:0.0\tprevious\\:ordinal:0.0\tprevious\\:outcode:0.0\tprevious\\:posttown:0.0\tprevious\\:residential:0.0\tprevious\\:road:0.0\tprevious\\:word\\:CURLEW:1.0\tresidential:0.0\troad:0.0\tword\\:TOWN:1.0\n" +
        "\tRhysBradbury\tbusiness:0.0\tcompany:0.0\tdigits\\:no_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:1.0\thyphenations:0.0\tlength\\:w\\:4:1.0\tlocational:0.0\tnext\\:business:0.0\tnext\\:company:0.0\tnext\\:digits\\:no_digits:1.0\tnext\\:directional:0.0\tnext\\:endsinpunc:0.0\tnext\\:flat:0.0\tnext\\:has.vowels:1.0\tnext\\:hyphenations:0.0\tnext\\:length\\:w\\:3:1.0\tnext\\:locational:0.0\tnext\\:ordinal:0.0\tnext\\:outcode:0.0\tnext\\:posttown:0.0\tnext\\:residential:0.0\tnext\\:road:1.0\tnext\\:word\\:WAY:1.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\tprevious\\:business:0.0\tprevious\\:company:0.0\tprevious\\:digits\\:no_digits:1.0\tprevious\\:directional:0.0\tprevious\\:endsinpunc:0.0\tprevious\\:flat:0.0\tprevious\\:has.vowels:1.0\tprevious\\:hyphenations:0.0\tprevious\\:length\\:w\\:4:1.0\tprevious\\:locational:0.0\tprevious\\:ordinal:0.0\tprevious\\:outcode:0.0\tprevious\\:posttown:0.0\tprevious\\:residential:0.0\tprevious\\:road:0.0\tprevious\\:word\\:TOWN:1.0\tresidential:0.0\troad:0.0\tword\\:TOWN:1.0\n" +
        "\tRhysBradbury\tbusiness:0.0\tcompany:0.0\tdigits\\:no_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:1.0\thyphenations:0.0\tlength\\:w\\:3:1.0\tlocational:0.0\tnext\\:business:0.0\tnext\\:company:0.0\tnext\\:digits\\:no_digits:1.0\tnext\\:directional:0.0\tnext\\:endsinpunc:0.0\tnext\\:flat:0.0\tnext\\:has.vowels:1.0\tnext\\:hyphenations:0.0\tnext\\:length\\:w\\:6:1.0\tnext\\:locational:0.0\tnext\\:ordinal:0.0\tnext\\:outcode:0.0\tnext\\:posttown:1.0\tnext\\:rawstring.end:1.0\tnext\\:residential:0.0\tnext\\:road:1.0\tnext\\:word\\:STREET:1.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\tprevious\\:business:0.0\tprevious\\:company:0.0\tprevious\\:digits\\:no_digits:1.0\tprevious\\:directional:0.0\tprevious\\:endsinpunc:0.0\tprevious\\:flat:0.0\tprevious\\:has.vowels:1.0\tprevious\\:hyphenations:0.0\tprevious\\:length\\:w\\:4:1.0\tprevious\\:locational:0.0\tprevious\\:ordinal:0.0\tprevious\\:outcode:0.0\tprevious\\:posttown:0.0\tprevious\\:residential:0.0\tprevious\\:road:0.0\tprevious\\:word\\:TOWN:1.0\tresidential:0.0\troad:1.0\tword\\:WAY:1.0\n" +
        "\tRhysBradbury\tbusiness:0.0\tcompany:0.0\tdigits\\:no_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:1.0\thyphenations:0.0\tlength\\:w\\:6:1.0\tlocational:0.0\tordinal:0.0\toutcode:0.0\tposttown:1.0\tprevious\\:business:0.0\tprevious\\:company:0.0\tprevious\\:digits\\:no_digits:1.0\tprevious\\:directional:0.0\tprevious\\:endsinpunc:0.0\tprevious\\:flat:0.0\tprevious\\:has.vowels:1.0\tprevious\\:hyphenations:0.0\tprevious\\:length\\:w\\:3:1.0\tprevious\\:locational:0.0\tprevious\\:ordinal:0.0\tprevious\\:outcode:0.0\tprevious\\:posttown:0.0\tprevious\\:residential:0.0\tprevious\\:road:1.0\tprevious\\:word\\:WAY:1.0\trawstring.end:1.0\tresidential:0.0\troad:1.0\tword\\:STREET:1.0\n"

    // When
    val result = Parser.constructCrfInput(tokens)

    // Then
    result shouldBe expected
  }

  it should "transform tokens into tokens data" in {
    // Given
    val tokens = List("31", "EXETER", "CLOSE", "LONDON", "ONS")
    val expected = List(
      CrfLineData("31", Some("EXETER"), None),
      CrfLineData("EXETER", Some("CLOSE"), Some("31")),
      CrfLineData("CLOSE", Some("LONDON"), Some("EXETER")),
      CrfLineData("LONDON", Some("ONS"), Some("CLOSE")),
      CrfLineData("ONS", None, Some("LONDON"))
    )

    // When
    val result = Parser.tokensToCrfLinesData(tokens)

    // Then
    result shouldBe expected
  }

  it should "transform two elements tokens into two elements tokens data" in {
    // Given
    val tokens = List("31", "ONS")
    val expected = List(
      CrfLineData("31", Some("ONS"), None),
      CrfLineData("ONS", None, Some("31"))
    )

    // When
    val result = Parser.tokensToCrfLinesData(tokens)

    // Then
    result shouldBe expected
  }

  it should "transform one element tokens into one element tokens data" in {
    // Given
    val tokens = List("31")
    val expected = List(CrfLineData("31", None, None))

    // When
    val result = Parser.tokensToCrfLinesData(tokens)

    // Then
    result shouldBe expected
  }

  it should "transform empty tokens into empty tokens data" in {
    // Given
    val tokens = List.empty
    val expected = List.empty

    // When
    val result = Parser.tokensToCrfLinesData(tokens)

    // Then
    result shouldBe expected
  }

  it should "transform a lonely token data into a CRF line" in {
    // Given
    val lineData = CrfLineData("31", None, None)
    val expected = "business:0.0\tcompany:0.0\tdigits\\:all_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:0.0\thyphenations:0.0\tlength\\:d\\:2:1.0\tlocational:0.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\tresidential:0.0\troad:0.0\tword:0.0"

    // When
    val result = Parser.constructCrfInputLine(lineData)

    // Then
    result shouldBe expected
  }

  it should "transform a token data (with previous and next token) into a CRF line" in {
    // Given
    val lineData = CrfLineData("CURLEW", Some("WAY"), Some("31"))
    val expected = "business:0.0\tcompany:0.0\tdigits\\:no_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:1.0\thyphenations:0.0\tlength\\:w\\:6:1.0\tlocational:0.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\tresidential:0.0\troad:0.0\tword\\:CURLEW:1.0" +
      "\tnext\\:business:0.0\tnext\\:company:0.0\tnext\\:digits\\:no_digits:1.0\tnext\\:directional:0.0\tnext\\:endsinpunc:0.0\tnext\\:flat:0.0\tnext\\:has.vowels:1.0\tnext\\:hyphenations:0.0\tnext\\:length\\:w\\:3:1.0\tnext\\:locational:0.0\tnext\\:ordinal:0.0\tnext\\:outcode:0.0\tnext\\:posttown:0.0\tnext\\:residential:0.0\tnext\\:road:1.0\tnext\\:word\\:WAY:1.0" +
      "\tprevious\\:business:0.0\tprevious\\:company:0.0\tprevious\\:digits\\:all_digits:1.0\tprevious\\:directional:0.0\tprevious\\:endsinpunc:0.0\tprevious\\:flat:0.0\tprevious\\:has.vowels:0.0\tprevious\\:hyphenations:0.0\tprevious\\:length\\:d\\:2:1.0\tprevious\\:locational:0.0\tprevious\\:ordinal:0.0\tprevious\\:outcode:0.0\tprevious\\:posttown:0.0\tprevious\\:residential:0.0\tprevious\\:road:0.0\tprevious\\:word:0.0"

    // When
    val result = Parser.constructCrfInputLine(lineData)

    // Then
    result shouldBe expected
  }

  it should "transform a token data (with only previous token) into a CRF line" in {
    // Given
    val lineData = CrfLineData("CURLEW", None, Some("31"))
    val expected = "business:0.0\tcompany:0.0\tdigits\\:no_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:1.0\thyphenations:0.0\tlength\\:w\\:6:1.0\tlocational:0.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\tresidential:0.0\troad:0.0\tword\\:CURLEW:1.0" +
      "\tprevious\\:business:0.0\tprevious\\:company:0.0\tprevious\\:digits\\:all_digits:1.0\tprevious\\:directional:0.0\tprevious\\:endsinpunc:0.0\tprevious\\:flat:0.0\tprevious\\:has.vowels:0.0\tprevious\\:hyphenations:0.0\tprevious\\:length\\:d\\:2:1.0\tprevious\\:locational:0.0\tprevious\\:ordinal:0.0\tprevious\\:outcode:0.0\tprevious\\:posttown:0.0\tprevious\\:residential:0.0\tprevious\\:road:0.0\tprevious\\:word:0.0"

    // When
    val result = Parser.constructCrfInputLine(lineData)

    // Then
    result shouldBe expected
  }

  it should "transform a token data (with only next token) into a CRF line" in {
    // Given
    val lineData = CrfLineData("CURLEW", Some("WAY"), None)
    val expected = "business:0.0\tcompany:0.0\tdigits\\:no_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:1.0\thyphenations:0.0\tlength\\:w\\:6:1.0\tlocational:0.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\tresidential:0.0\troad:0.0\tword\\:CURLEW:1.0" +
      "\tnext\\:business:0.0\tnext\\:company:0.0\tnext\\:digits\\:no_digits:1.0\tnext\\:directional:0.0\tnext\\:endsinpunc:0.0\tnext\\:flat:0.0\tnext\\:has.vowels:1.0\tnext\\:hyphenations:0.0\tnext\\:length\\:w\\:3:1.0\tnext\\:locational:0.0\tnext\\:ordinal:0.0\tnext\\:outcode:0.0\tnext\\:posttown:0.0\tnext\\:residential:0.0\tnext\\:road:1.0\tnext\\:word\\:WAY:1.0"

    // When
    val result = Parser.constructCrfInputLine(lineData)

    // Then
    result shouldBe expected
  }

  it should "transform a token '31' into features line" in {
    // Given
    val token = "31"
    val expected = "business:0.0\tcompany:0.0\tdigits\\:all_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:0.0\thyphenations:0.0\tlength\\:d\\:2:1.0\tlocational:0.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\tresidential:0.0\troad:0.0\tword:0.0"

    // When
    val result = Parser.tokenToFeatures(token)

    // Then
    result shouldBe expected
  }

  it should "transform a token 'CURLEW' into features line" in {
    // Given
    val token = "CURLEW"
    val expected = "business:0.0\tcompany:0.0\tdigits\\:no_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:1.0\thyphenations:0.0\tlength\\:w\\:6:1.0\tlocational:0.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\tresidential:0.0\troad:0.0\tword\\:CURLEW:1.0"

    // When
    val result = Parser.tokenToFeatures(token)

    // Then
    result shouldBe expected
  }

  it should "transform a token 'CURLEW' with prefix 'previous' into features line" in {
    // Given
    val token = "CURLEW"
    val expected = "previous\\:business:0.0\tprevious\\:company:0.0\tprevious\\:digits\\:no_digits:1.0\tprevious\\:directional:0.0\tprevious\\:endsinpunc:0.0\tprevious\\:flat:0.0\tprevious\\:has.vowels:1.0\tprevious\\:hyphenations:0.0\tprevious\\:length\\:w\\:6:1.0\tprevious\\:locational:0.0\tprevious\\:ordinal:0.0\tprevious\\:outcode:0.0\tprevious\\:posttown:0.0\tprevious\\:residential:0.0\tprevious\\:road:0.0\tprevious\\:word\\:CURLEW:1.0"

    // When
    val result = Parser.tokenToFeatures(token, "previous\\:")

    // Then
    result shouldBe expected
  }

  it should "transform response from the tagger into a map with label->values" in {
    // Given
    val tokens = List("1", "CURLEW", "WAY", "EXETER", "EX4", "4SW")
    val response = "BuildingNumber: 0.999236\nStreetName: 1.000000\nStreetName: 0.999999\nTownName: 0.999996\nPostcode: 1.000000\nPostcode: 1.000000"
    val expected = Map(
      Tokens.buildingNumber -> "1",
      Tokens.streetName -> "CURLEW WAY",
      Tokens.townName -> "EXETER",
      Tokens.postcode -> "EX4 4SW"
    )

    // When
    val result = Parser.parseTaggerResult(response, tokens)

    // Then
    result shouldBe expected
  }

  //===========================

  object CrfParserImpl extends CrfParser {
    override def parserLibPath: String = "parsers/src/main/resources"
  }

  it should "have same results as previous one" in {
    // Given
    val addresses = List(
      "31",
      "CUrlew",
      "North",
      "31 CURLEW",
      "31 CURLEW WAY",
      "31 CURLEW WAY 31",
      "31 Cse22URLEW WAY 31",
      "2 Dagmar Rd, Exmouth EX8 2AN, UK",
      " 2 Dagmar Rd ",
      " 51 Oakland Dr ",
      " 11 Market St ",
      " Unnamed Road ",
      " Hardaway Head ",
      " 39 Hele Rd ",
      " 4 Knights Cres ",
      " 8 Ley Meadow Dr ",
      " 7 Little Normans ",
      " Unnamed Road ",
      " 2 Batts Park ",
      " 4 Sovereign Cl ",
      " 3 Lyme St ",
      " 2 S Whimple Farmhouse ",
      " 20-34 Church St ",
      " 9 Stafford Ln ",
      " 8 College Green ",
      " 1 Castle Ln ",
      " 22 Glebelands Rd ",
      " Monkleigh Mill Ln ",
      " 55 Longbrook St ",
      " 2 The Tudors ",
      " 4 York Rd ",
      " 16 The Village ",
      " 4 Alphin Brook Rd ",
      " 33 New St ",
      " 16 Park St ",
      " 14 Lowman Way ",
      " 6 Brickhouse Dr ",
      " 13 Ropewalk ",
      " Bishops Tawton Rd ",
      " Quarry Ln ",
      " 12 Orchard Cl ",
      " 21 Stucley Rd ",
      " 31 B3175 ",
      " Mill Ln ",
      " 27 Downeshead Ln ",
      " Combe Hill ",
      " 23 B3178 ",
      " 2 Walls Cl ",
      " 58 Lloyds Cres ",
      " 9 Parson's Cl ",
      " 11B Broad St ",
      " 3 College Rd ",
      " Unnamed Road ",
      " Marley Rd ",
      " 55 Long Barn ",
      " 1 Barton Cl ",
      " 7 Shorland Cl ",
      " 12 Manor Rd ",
      " 2 Devonshire Ct ",
      " Culmcott Cottage ",
      " 3 Sea View ",
      " 9 North Rd ",
      " 5 A388 ",
      " 2 Turners Cottages ",
      " 1 Mill Ln ",
      " 2 Barrack Rd ",
      " 2 Gamberlake ",
      " Stepps Ln ",
      " 18 Dewberry Dr ",
      " B3248 ",
      " Station Rd ",
      " 9 Mill Ln ",
      " George Teign Rd ",
      " 40 Priory Rd ",
      " Jutland Way ",
      " 7 Rye Park Cl ",
      " 39 Victoria St ",
      " Crow Ln ",
      " 36 Falcon Rd ",
      " 32 River Plate Rd ",
      " 6 Sedgeclaire Cl ",
      " Commons Hill ",
      " 30A Spicer Rd ",
      " 23 Broad St ",
      " 197 Pinhoe Rd ",
      " 62 Tollards Rd ",
      " 9 High St ",
      " 29 Pulchrass St ",
      " Unnamed Road ",
      " 2 Farwood Cottages ",
      " A3124 ",
      " A3052 ",
      " Stoke Canon ",
      " 15 Clovelly Cl ",
      " B3227 ",
      " 3B Grosvenor Pl ",
      " London Rd ",
      " Old Hill ",
      " 52-116 Rosebery Rd ",
      " 2 Vicarage St ",
      " 52 Tennacott Heights ",
      " 1 Higher Rock Cottages ",
      " B3181 ",
      " A377 ",
      " Longpark Dr ",
      " 6 Yeo Business Park ",
      " 33 Duckworth Rd ",
      " 2 Halse Hill ",
      " B3248 ",
      " Unnamed Road ",
      " 9 Green Ln ",
      " 2 Hope Cottages ",
      " Genora Cottage ",
      " Nadder Bottom ",
      " 1 Fore St ",
      " Washfield Ln ",
      " 6 Yeo Business Park ",
      " B3181 ",
      " 50 Kings Ln ",
      " 109 A38 ",
      " Unnamed Road ",
      " Primrose Ln ",
      " 117 Woolbrook Rd ",
      " 3-4 Silver St ",
      " 2 Alma Terrace ",
      " A377 ",
      " Woodland Head Yeoford ",
      " 1 Church Parks ",
      " 10 Metcombe Rise ",
      " 4 Sheddymoor Heights ",
      " 10 Orchards Ln ",
      " Unnamed Road ",
      " 20 Great Links Tor Rd ",
      " 8 Foretown Estate ",
      " 56 Parr St ",
      " 4 Shelly Rd ",
      " 9 Tremletts Cl ",
      " 2 Fairview ",
      " 20 Wellswood Gardens ",
      " 5 Hospital Ln ",
      " 2 Exeter Rd ",
      " 1 Clapper Ln ",
      " S Moor Ln ",
      " 23 Bampton St ",
      " 25 Longbrook Terrace ",
      " 1 Cott Ln ",
      " 3 Ashcombe Rd ",
      " 114 Pound Ln ",
      " 2 Southley Rd ",
      " Caller's Ln ",
      " B3232 ",
      " Long Ln ",
      " 2 New Buildings ",
      " 30-32 Torridge Rd ",
      " 4 Causeway End ",
      " 6 Elderberry Way ",
      " 4 Brixington Ln ",
      " 2 Dagmar Rd ",
      " 51 Oakland Dr ",
      " 11 Market St ",
      " Unnamed Road ",
      " Hardaway Head ",
      " 39 Hele Rd ",
      " 4 Knights Cres ",
      " 8 Ley Meadow Dr ",
      " 7 Little Normans ",
      " Unnamed Road ",
      " 2 Batts Park ",
      " 4 Sovereign Cl ",
      " 3 Lyme St ",
      " 2 S Whimple Farmhouse ",
      " 20-34 Church St ",
      " 9 Stafford Ln ",
      " 8 College Green ",
      " 1 Castle Ln ",
      " 22 Glebelands Rd ",
      " Monkleigh Mill Ln",
      " 55 Longbrook St",
      " 2 The Tudors",
      " 4 York Rd",
      " 16 The Village",
      " 4 Alphin Brook Rd",
      " 33 New St",
      " 16 Park St",
      " 14 Lowman Way",
      " 6 Brickhouse Dr",
      " 13 Ropewalk",
      " Bishops Tawton Rd",
      " Quarry Ln",
      " 12 Orchard Cl",
      " 21 Stucley Rd",
      " 31 B3175",
      " Mill Ln",
      " 27 Downeshead Ln",
      " Combe Hill",
      " 23 B3178",
      " 2 Walls Cl",
      " 58 Lloyds Cres",
      " 9 Parson's Cl",
      " 11B Broad St",
      " 3 College Rd",
      " Unnamed Road",
      " Marley Rd",
      " 55 Long Barn",
      " 1 Barton Cl",
      " 7 Shorland Cl",
      " 12 Manor Rd",
      " 2 Devonshire Ct",
      " Culmcott Cottage",
      " 3 Sea View",
      " 9 North Rd",
      " 5 A388",
      " 2 Turners Cottages",
      " 1 Mill Ln",
      " 2 Barrack Rd",
      " 2 Gamberlake",
      " Stepps Ln",
      " 18 Dewberry Dr",
      " B3248",
      " Station Rd",
      " 9 Mill Ln",
      " George Teign Rd",
      " 40 Priory Rd",
      " Jutland Way",
      " 7 Rye Park Cl",
      " 39 Victoria St",
      " Crow Ln",
      " 36 Falcon Rd",
      " 32 River Plate Rd",
      " 6 Sedgeclaire Cl",
      " Commons Hill",
      " 30A Spicer Rd",
      " 23 Broad St",
      " 197 Pinhoe Rd",
      " 62 Tollards Rd",
      " 9 High St",
      " 29 Pulchrass St",
      " Unnamed Road",
      " 2 Farwood Cottages",
      " A3124",
      " A3052",
      " Stoke Canon",
      " 15 Clovelly Cl",
      " B3227",
      " 3B Grosvenor Pl",
      " London Rd",
      " Old Hill",
      " 52-116 Rosebery Rd",
      " 2 Vicarage St",
      " 52 Tennacott Heights",
      " 1 Higher Rock Cottages",
      " B3181",
      " A377",
      " Longpark Dr",
      " 6 Yeo Business Park",
      " 33 Duckworth Rd",
      " 2 Halse Hill",
      " B3248",
      " Unnamed Road",
      " 9 Green Ln",
      " 2 Hope Cottages",
      " Genora Cottage",
      " Nadder Bottom",
      " 1 Fore St",
      " Washfield Ln",
      " 6 Yeo Business Park",
      " B3181",
      " 50 Kings Ln",
      " 109 A38",
      " Unnamed Road",
      " Primrose Ln",
      " 117 Woolbrook Rd",
      " 3-4 Silver St",
      " 2 Alma Terrace",
      " A377",
      " Woodland Head Yeoford",
      " 1 Church Parks",
      " 10 Metcombe Rise",
      " 4 Sheddymoor Heights",
      " 10 Orchards Ln",
      " Unnamed Road",
      " 20 Great Links Tor Rd",
      " 8 Foretown Estate",
      " 56 Parr St",
      " 4 Shelly Rd",
      " 9 Tremletts Cl",
      " 2 Fairview",
      " 20 Wellswood Gardens",
      " 5 Hospital Ln",
      " 2 Exeter Rd",
      " 1 Clapper Ln",
      " S Moor Ln",
      " 23 Bampton St",
      " 25 Longbrook Terrace",
      " 1 Cott Ln",
      " 3 Ashcombe Rd",
      " 114 Pound Ln",
      " 2 Southley Rd",
      " Caller's Ln",
      " B3232",
      " Long Ln",
      " 2 New Buildings",
      " 30-32 Torridge Rd",
      " 4 Causeway End",
      " 6 Elderberry Way",
      " 4 Brixington Ln",
      " 2 Dagmar Rd",
      " 51 Oakland Dr",
      " 11 Market St",
      " Unnamed Road",
      " Hardaway Head",
      " 39 Hele Rd",
      " 4 Knights Cres",
      " 8 Ley Meadow Dr",
      " 7 Little Normans",
      " Unnamed Road",
      " 2 Batts Park",
      " 4 Sovereign Cl",
      " 3 Lyme St",
      " 2 S Whimple Farmhouse",
      " 20-34 Church St",
      " 9 Stafford Ln",
      " 8 College Green",
      " 1 Castle Ln",
      " 22 Glebelands Rd",
      " Monkleigh Mill Ln",
      " 55 Longbrook St",
      " 2 The Tudors",
      " 4 York Rd",
      " 16 The Village",
      " 4 Alphin Brook Rd",
      " 33 New St",
      " 16 Park St",
      " 14 Lowman Way",
      " 6 Brickhouse Dr",
      " 13 Ropewalk",
      " Bishops Tawton Rd",
      " Quarry Ln",
      " 12 Orchard Cl",
      " 21 Stucley Rd",
      " 31 B3175",
      " Mill Ln",
      " 27 Downeshead Ln",
      " Combe Hill",
      " 23 B3178",
      " 2 Walls Cl",
      " 58 Lloyds Cres",
      " 9 Parson's Cl",
      " 11B Broad St",
      " 3 College Rd",
      " Unnamed Road",
      " Marley Rd",
      " 55 Long Barn",
      " 1 Barton Cl",
      " 7 Shorland Cl",
      " 12 Manor Rd",
      " 2 Devonshire Ct",
      " Culmcott Cottage",
      " 3 Sea View",
      " 9 North Rd",
      " 5 A388",
      " 2 Turners Cottages",
      " 1 Mill Ln",
      " 2 Barrack Rd",
      " 2 Gamberlake",
      " Stepps Ln",
      " 18 Dewberry Dr",
      " B3248",
      " Station Rd",
      " 9 Mill Ln",
      " George Teign Rd",
      " 40 Priory Rd",
      " Jutland Way",
      " 7 Rye Park Cl",
      " 39 Victoria St",
      " Crow Ln",
      " 36 Falcon Rd",
      " 32 River Plate Rd",
      " 6 Sedgeclaire Cl",
      " Commons Hill",
      " 30A Spicer Rd",
      " 23 Broad St",
      " 197 Pinhoe Rd",
      " 62 Tollards Rd",
      " 9 High St",
      " 29 Pulchrass St",
      " Unnamed Road",
      " 2 Farwood Cottages",
      " A3124",
      " A3052",
      " Stoke Canon",
      " 15 Clovelly Cl",
      " B3227",
      " 3B Grosvenor Pl",
      " London Rd",
      " Old Hill",
      " 52-116 Rosebery Rd",
      " 2 Vicarage St",
      " 52 Tennacott Heights",
      " 1 Higher Rock Cottages",
      " B3181",
      " A377",
      " Longpark Dr",
      " 6 Yeo Business Park",
      " 33 Duckworth Rd",
      " 2 Halse Hill",
      " B3248",
      " Unnamed Road",
      " 9 Green Ln",
      " 2 Hope Cottages",
      " Genora Cottage",
      " Nadder Bottom",
      " 1 Fore St",
      " Washfield Ln",
      " 6 Yeo Business Park",
      " B3181",
      " 50 Kings Ln",
      " 109 A38",
      " Unnamed Road",
      " Primrose Ln",
      " 117 Woolbrook Rd",
      " 3-4 Silver St",
      " 2 Alma Terrace",
      " A377",
      " Woodland Head Yeoford",
      " 1 Church Parks",
      " 10 Metcombe Rise",
      " 4 Sheddymoor Heights",
      " 10 Orchards Ln",
      " Unnamed Road",
      " 20 Great Links Tor Rd",
      " 8 Foretown Estate",
      " 56 Parr St",
      " 4 Shelly Rd",
      " 9 Tremletts Cl",
      " 2 Fairview",
      " 20 Wellswood Gardens",
      " 5 Hospital Ln",
      " 2 Exeter Rd",
      " 1 Clapper Ln",
      " S Moor Ln",
      " 23 Bampton St",
      " 25 Longbrook Terrace",
      " 1 Cott Ln",
      " 3 Ashcombe Rd",
      " 114 Pound Ln",
      " 2 Southley Rd",
      " Caller's Ln",
      " B3232",
      " Long Ln",
      " 2 New Buildings",
      " 30-32 Torridge Rd",
      " 4 Causeway End",
      " 6 Elderberry Way",
      " 4 Brixington Ln",
      " 2 Dagmar Rd",
      " 51 Oakland Dr",
      " 11 Market St",
      " Unnamed Road",
      " Hardaway Head",
      " 39 Hele Rd",
      " 4 Knights Cres",
      " 8 Ley Meadow Dr",
      " 7 Little Normans",
      " Unnamed Road",
      " 2 Batts Park",
      " 4 Sovereign Cl",
      " 3 Lyme St",
      " 2 S Whimple Farmhouse",
      " 20-34 Church St",
      " 9 Stafford Ln",
      " 8 College Green",
      " 1 Castle Ln",
      " 22 Glebelands Rd",
      " Monkleigh Mill Ln",
      " 55 Longbrook St",
      " 2 The Tudors",
      " 4 York Rd",
      " 16 The Village",
      " 4 Alphin Brook Rd",
      " 33 New St",
      " 16 Park St",
      " 14 Lowman Way",
      " 6 Brickhouse Dr",
      " 13 Ropewalk",
      " Bishops Tawton Rd",
      " Quarry Ln",
      " 12 Orchard Cl",
      " 21 Stucley Rd",
      " 31 B3175",
      " Mill Ln",
      " 27 Downeshead Ln",
      " Combe Hill",
      " 23 B3178",
      " 2 Walls Cl",
      " 58 Lloyds Cres",
      " 9 Parson's Cl",
      " 11B Broad St",
      " 3 College Rd",
      " Unnamed Road",
      " Marley Rd",
      " 55 Long Barn",
      " 1 Barton Cl",
      " 7 Shorland Cl",
      " 12 Manor Rd",
      " 2 Devonshire Ct",
      " Culmcott Cottage",
      " 3 Sea View",
      " 9 North Rd",
      " 5 A388",
      " 2 Turners Cottages",
      " 1 Mill Ln",
      " 2 Barrack Rd",
      " 2 Gamberlake",
      " Stepps Ln",
      " 18 Dewberry Dr",
      " B3248",
      " Station Rd",
      " 9 Mill Ln",
      " George Teign Rd",
      " 40 Priory Rd",
      " Jutland Way",
      " 7 Rye Park Cl",
      " 39 Victoria St",
      " Crow Ln",
      " 36 Falcon Rd",
      " 32 River Plate Rd",
      " 6 Sedgeclaire Cl",
      " Commons Hill",
      " 30A Spicer Rd",
      " 23 Broad St",
      " 197 Pinhoe Rd",
      " 62 Tollards Rd",
      " 9 High St",
      " 29 Pulchrass St",
      " Unnamed Road",
      " 2 Farwood Cottages",
      " A3124",
      " A3052",
      " Stoke Canon",
      " 15 Clovelly Cl",
      " B3227",
      " 3B Grosvenor Pl",
      " London Rd",
      " Old Hill",
      " 52-116 Rosebery Rd",
      " 2 Vicarage St",
      " 52 Tennacott Heights",
      " 1 Higher Rock Cottages",
      " B3181",
      " A377",
      " Longpark Dr",
      " 6 Yeo Business Park",
      " 33 Duckworth Rd",
      " 2 Halse Hill",
      " B3248",
      " Unnamed Road",
      " 9 Green Ln",
      " 2 Hope Cottages",
      " Genora Cottage",
      " Nadder Bottom",
      " 1 Fore St",
      " Washfield Ln",
      " 6 Yeo Business Park",
      " B3181",
      " 50 Kings Ln",
      " 109 A38",
      " Unnamed Road",
      " Primrose Ln",
      " 117 Woolbrook Rd",
      " 3-4 Silver St",
      " 2 Alma Terrace",
      " A377",
      " Woodland Head Yeoford",
      " 1 Church Parks",
      " 10 Metcombe Rise",
      " 4 Sheddymoor Heights",
      " 10 Orchards Ln",
      " Unnamed Road",
      " 20 Great Links Tor Rd",
      " 8 Foretown Estate",
      " 56 Parr St",
      " 4 Shelly Rd",
      " 9 Tremletts Cl",
      " 2 Fairview",
      " 20 Wellswood Gardens",
      " 5 Hospital Ln",
      " 2 Exeter Rd",
      " 1 Clapper Ln",
      " S Moor Ln",
      " 23 Bampton St",
      " 25 Longbrook Terrace",
      " 1 Cott Ln",
      " 3 Ashcombe Rd",
      " 114 Pound Ln",
      " 2 Southley Rd",
      " Caller's Ln",
      " B3232",
      " Long Ln",
      " 2 New Buildings",
      " 30-32 Torridge Rd",
      " 4 Causeway End",
      " 6 Elderberry Way",
      " 4 Brixington Ln",
      " 2 Dagmar Rd",
      " 51 Oakland Dr",
      " 11 Market St",
      " Unnamed Road",
      " Hardaway Head",
      " 39 Hele Rd",
      " 4 Knights Cres",
      " 8 Ley Meadow Dr",
      " 7 Little Normans",
      " Unnamed Road",
      " 2 Batts Park",
      " 4 Sovereign Cl",
      " 3 Lyme St",
      " 2 S Whimple Farmhouse",
      " 20-34 Church St",
      " 9 Stafford Ln",
      " 8 College Green",
      " 1 Castle Ln",
      " 22 Glebelands Rd",
      " Monkleigh Mill Ln",
      " 55 Longbrook St",
      " 2 The Tudors",
      " 4 York Rd",
      " 16 The Village",
      " 4 Alphin Brook Rd",
      " 33 New St",
      " 16 Park St",
      " 14 Lowman Way",
      " 6 Brickhouse Dr",
      " 13 Ropewalk",
      " Bishops Tawton Rd",
      " Quarry Ln",
      " 12 Orchard Cl",
      " 21 Stucley Rd",
      " 31 B3175",
      " Mill Ln",
      " 27 Downeshead Ln",
      " Combe Hill",
      " 23 B3178",
      " 2 Walls Cl",
      " 58 Lloyds Cres",
      " 9 Parson's Cl",
      " 11B Broad St",
      " 3 College Rd",
      " Unnamed Road",
      " Marley Rd",
      " 55 Long Barn",
      " 1 Barton Cl",
      " 7 Shorland Cl",
      " 12 Manor Rd",
      " 2 Devonshire Ct",
      " Culmcott Cottage",
      " 3 Sea View",
      " 9 North Rd",
      " 5 A388",
      " 2 Turners Cottages",
      " 1 Mill Ln",
      " 2 Barrack Rd",
      " 2 Gamberlake",
      " Stepps Ln",
      " 18 Dewberry Dr",
      " B3248",
      " Station Rd",
      " 9 Mill Ln",
      " George Teign Rd",
      " 40 Priory Rd",
      " Jutland Way",
      " 7 Rye Park Cl",
      " 39 Victoria St",
      " Crow Ln",
      " 36 Falcon Rd",
      " 32 River Plate Rd",
      " 6 Sedgeclaire Cl",
      " Commons Hill",
      " 30A Spicer Rd",
      " 23 Broad St",
      " 197 Pinhoe Rd",
      " 62 Tollards Rd",
      " 9 High St",
      " 29 Pulchrass St",
      " Unnamed Road",
      " 2 Farwood Cottages",
      " A3124",
      " A3052",
      " Stoke Canon",
      " 15 Clovelly Cl",
      " B3227",
      " 3B Grosvenor Pl",
      " London Rd",
      " Old Hill",
      " 52-116 Rosebery Rd",
      " 2 Vicarage St",
      " 52 Tennacott Heights",
      " 1 Higher Rock Cottages",
      " B3181",
      " A377",
      " Longpark Dr",
      " 6 Yeo Business Park",
      " 33 Duckworth Rd",
      " 2 Halse Hill",
      " B3248",
      " Unnamed Road",
      " 9 Green Ln",
      " 2 Hope Cottages",
      " Genora Cottage",
      " Nadder Bottom",
      " 1 Fore St",
      " Washfield Ln",
      " 6 Yeo Business Park",
      " B3181",
      " 50 Kings Ln",
      " 109 A38",
      " Unnamed Road",
      " Primrose Ln",
      " 117 Woolbrook Rd",
      " 3-4 Silver St",
      " 2 Alma Terrace",
      " A377",
      " Woodland Head Yeoford",
      " 1 Church Parks",
      " 10 Metcombe Rise",
      " 4 Sheddymoor Heights",
      " 10 Orchards Ln",
      " Unnamed Road",
      " 20 Great Links Tor Rd",
      " 8 Foretown Estate",
      " 56 Parr St",
      " 4 Shelly Rd",
      " 9 Tremletts Cl",
      " 2 Fairview",
      " 20 Wellswood Gardens",
      " 5 Hospital Ln",
      " 2 Exeter Rd",
      " 1 Clapper Ln",
      " S Moor Ln",
      " 23 Bampton St",
      " 25 Longbrook Terrace",
      " 1 Cott Ln",
      " 3 Ashcombe Rd",
      " 114 Pound Ln",
      " 2 Southley Rd",
      " Caller's Ln",
      " B3232",
      " Long Ln",
      " 2 New Buildings",
      " 30-32 Torridge Rd",
      " 4 Causeway End",
      " 6 Elderberry Way",
      " 4 Brixington Ln",
      " 2 Dagmar Rd",
      " 51 Oakland Dr",
      " 11 Market St",
      " Unnamed Road",
      " Hardaway Head",
      " 39 Hele Rd",
      " 4 Knights Cres",
      " 8 Ley Meadow Dr",
      " 7 Little Normans",
      " Unnamed Road",
      " 2 Batts Park",
      " 4 Sovereign Cl",
      " 3 Lyme St",
      " 2 S Whimple Farmhouse",
      " 20-34 Church St",
      " 9 Stafford Ln",
      " 8 College Green",
      " 1 Castle Ln",
      " 22 Glebelands Rd",
      " Monkleigh Mill Ln",
      " 55 Longbrook St",
      " 2 The Tudors",
      " 4 York Rd",
      " 16 The Village",
      " 4 Alphin Brook Rd",
      " 33 New St",
      " 16 Park St",
      " 14 Lowman Way",
      " 6 Brickhouse Dr",
      " 13 Ropewalk",
      " Bishops Tawton Rd",
      " Quarry Ln",
      " 12 Orchard Cl",
      " 21 Stucley Rd",
      " 31 B3175",
      " Mill Ln",
      " 27 Downeshead Ln",
      " Combe Hill",
      " 23 B3178",
      " 2 Walls Cl",
      " 58 Lloyds Cres",
      " 9 Parson's Cl",
      " 11B Broad St",
      " 3 College Rd",
      " Unnamed Road",
      " Marley Rd",
      " 55 Long Barn",
      " 1 Barton Cl",
      " 7 Shorland Cl",
      " 12 Manor Rd",
      " 2 Devonshire Ct",
      " Culmcott Cottage",
      " 3 Sea View",
      " 9 North Rd",
      " 5 A388",
      " 2 Turners Cottages",
      " 1 Mill Ln",
      " 2 Barrack Rd",
      " 2 Gamberlake",
      " Stepps Ln",
      " 18 Dewberry Dr",
      " B3248",
      " Station Rd",
      " 9 Mill Ln",
      " George Teign Rd",
      " 40 Priory Rd",
      " Jutland Way",
      " 7 Rye Park Cl",
      " 39 Victoria St",
      " Crow Ln",
      " 36 Falcon Rd",
      " 32 River Plate Rd",
      " 6 Sedgeclaire Cl",
      " Commons Hill",
      " 30A Spicer Rd",
      " 23 Broad St",
      " 197 Pinhoe Rd",
      " 62 Tollards Rd",
      " 9 High St",
      " 29 Pulchrass St",
      " Unnamed Road",
      " 2 Farwood Cottages",
      " A3124",
      " A3052",
      " Stoke Canon",
      " 15 Clovelly Cl",
      " B3227",
      " 3B Grosvenor Pl",
      " London Rd",
      " Old Hill",
      " 52-116 Rosebery Rd",
      " 2 Vicarage St",
      " 52 Tennacott Heights",
      " 1 Higher Rock Cottages",
      " B3181",
      " A377",
      " Longpark Dr",
      " 6 Yeo Business Park",
      " 33 Duckworth Rd",
      " 2 Halse Hill",
      " B3248",
      " Unnamed Road",
      " 9 Green Ln",
      " 2 Hope Cottages",
      " Genora Cottage",
      " Nadder Bottom",
      " 1 Fore St",
      " Washfield Ln",
      " 6 Yeo Business Park",
      " B3181",
      " 50 Kings Ln",
      " 109 A38",
      " Unnamed Road",
      " Primrose Ln",
      " 117 Woolbrook Rd",
      " 3-4 Silver St",
      " 2 Alma Terrace",
      " A377",
      " Woodland Head Yeoford",
      " 1 Church Parks",
      " 10 Metcombe Rise",
      " 4 Sheddymoor Heights",
      " 10 Orchards Ln",
      " Unnamed Road",
      " 20 Great Links Tor Rd",
      " 8 Foretown Estate",
      " 56 Parr St",
      " 4 Shelly Rd",
      " 9 Tremletts Cl",
      " 2 Fairview",
      " 20 Wellswood Gardens",
      " 5 Hospital Ln",
      " 2 Exeter Rd",
      " 1 Clapper Ln",
      " S Moor Ln",
      " 23 Bampton St",
      " 25 Longbrook Terrace",
      " 1 Cott Ln",
      " 3 Ashcombe Rd",
      " 114 Pound Ln",
      " 2 Southley Rd",
      " Caller's Ln",
      " B3232",
      " Long Ln",
      " 2 New Buildings",
      " 30-32 Torridge Rd",
      " 4 Causeway End",
      " 6 Elderberry Way",
      " 4 Brixington Ln",
      " 2 Dagmar Rd",
      " 51 Oakland Dr",
      " 11 Market St",
      " Unnamed Road",
      " Hardaway Head",
      " 39 Hele Rd",
      " 4 Knights Cres",
      " 8 Ley Meadow Dr",
      " 7 Little Normans",
      " Unnamed Road",
      " 2 Batts Park",
      " 4 Sovereign Cl",
      " 3 Lyme St",
      " 2 S Whimple Farmhouse",
      " 20-34 Church St",
      " 9 Stafford Ln",
      " 8 College Green",
      " 1 Castle Ln",
      " 22 Glebelands Rd",
      " Monkleigh Mill Ln",
      " 55 Longbrook St",
      " 2 The Tudors",
      " 4 York Rd",
      " 16 The Village",
      " 4 Alphin Brook Rd",
      " 33 New St",
      " 16 Park St",
      " 14 Lowman Way",
      " 6 Brickhouse Dr",
      " 13 Ropewalk",
      " Bishops Tawton Rd",
      " Quarry Ln",
      " 12 Orchard Cl",
      " 21 Stucley Rd",
      " 31 B3175",
      " Mill Ln",
      " 27 Downeshead Ln",
      " Combe Hill",
      " 23 B3178",
      " 2 Walls Cl",
      " 58 Lloyds Cres",
      " 9 Parson's Cl",
      " 11B Broad St",
      " 3 College Rd",
      " Unnamed Road",
      " Marley Rd",
      " 55 Long Barn",
      " 1 Barton Cl",
      " 7 Shorland Cl",
      " 12 Manor Rd",
      " 2 Devonshire Ct",
      " Culmcott Cottage",
      " 3 Sea View",
      " 9 North Rd",
      " 5 A388",
      " 2 Turners Cottages",
      " 1 Mill Ln",
      " 2 Barrack Rd",
      " 2 Gamberlake",
      " Stepps Ln",
      " 18 Dewberry Dr",
      " B3248",
      " Station Rd",
      " 9 Mill Ln",
      " George Teign Rd",
      " 40 Priory Rd",
      " Jutland Way",
      " 7 Rye Park Cl",
      " 39 Victoria St",
      " Crow Ln",
      " 36 Falcon Rd",
      " 32 River Plate Rd",
      " 6 Sedgeclaire Cl",
      " Commons Hill",
      " 30A Spicer Rd",
      " 23 Broad St",
      " 197 Pinhoe Rd",
      " 62 Tollards Rd",
      " 9 High St",
      " 29 Pulchrass St",
      " Unnamed Road",
      " 2 Farwood Cottages",
      " A3124",
      " A3052",
      " Stoke Canon",
      " 15 Clovelly Cl",
      " B3227",
      " 3B Grosvenor Pl",
      " London Rd",
      " Old Hill",
      " 52-116 Rosebery Rd",
      " 2 Vicarage St",
      " 52 Tennacott Heights",
      " 1 Higher Rock Cottages",
      " B3181",
      " A377",
      " Longpark Dr",
      " 6 Yeo Business Park",
      " 33 Duckworth Rd",
      " 2 Halse Hill",
      " B3248",
      " Unnamed Road",
      " 9 Green Ln",
      " 2 Hope Cottages",
      " Genora Cottage",
      " Nadder Bottom",
      " 1 Fore St",
      " Washfield Ln",
      " 6 Yeo Business Park",
      " B3181",
      " 50 Kings Ln",
      " 109 A38",
      " Unnamed Road",
      " Primrose Ln",
      " 117 Woolbrook Rd",
      " 3-4 Silver St",
      " 2 Alma Terrace",
      " A377",
      " Woodland Head Yeoford",
      " 1 Church Parks",
      " 10 Metcombe Rise",
      " 4 Sheddymoor Heights",
      " 10 Orchards Ln",
      " Unnamed Road",
      " 20 Great Links Tor Rd",
      " 8 Foretown Estate",
      " 56 Parr St",
      " 4 Shelly Rd",
      " 9 Tremletts Cl",
      " 2 Fairview",
      " 20 Wellswood Gardens",
      " 5 Hospital Ln",
      " 2 Exeter Rd",
      " 1 Clapper Ln",
      " S Moor Ln",
      " 23 Bampton St",
      " 25 Longbrook Terrace",
      " 1 Cott Ln",
      " 3 Ashcombe Rd",
      " 114 Pound Ln",
      " 2 Southley Rd",
      " Caller's Ln",
      " B3232",
      " Long Ln",
      " 2 New Buildings",
      " 30-32 Torridge Rd",
      " 4 Causeway End",
      " 6 Elderberry Way",
      " 4 Brixington Ln",
      "Glace Grove, Exeter EX2 8DU, UK",
      "199 Toures Westall Rd, Exeter EX2 6UH, UK",
      "8 The DAYZ, Exeter EX2 4DA, UK",
      "199 Rutherford St, Exeter EX2 5UY, UK",
      "100 Cludens Cl, Exeter EX2 8UX, UK",
      "Matfard Rd, Exeter EU2, UK",
      "50 Pinces Rd, Exeter EX2 9UL, UK",
      "A329, Exeter EX2 9UJ, UK",
      "23 Hope Rd, Exeter EX2, UK",
      "127A Burnthouse Ln, Exeter EX2 6UF, UK",
      "38 The Yellow, Ide, Exeter EX2, UK"
    ).map(Tokens.preTokenize)

    // When
    val mine = addresses.map(Parser.constructCrfInput)
    val notMine = addresses.map { input =>
      val actual = CrfParserImpl.parse(input.mkString(" "), FeatureAnalysers.allFeatures, Tokens)
      CrfParserImpl.augmentCrfJniInput(actual)
    }

    // Then

    mine.zip(notMine).foreach { case (m, n) =>
      if (m != n) {
        println("======")
        println(m)
        println("======")
        println(n)
        println("======")
      }
      m shouldBe n
    }
  }
}

