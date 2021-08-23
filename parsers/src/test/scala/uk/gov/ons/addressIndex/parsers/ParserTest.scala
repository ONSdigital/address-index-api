package uk.gov.ons.addressIndex.parsers

import org.scalatest._
import flatspec._
import matchers._
import uk.gov.ons.addressIndex.parsers.Parser.CrfLineData

class ParserTest extends AnyFlatSpec with should.Matchers {

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
      CrfLineData(None, "31", Some("EXETER")),
      CrfLineData(Some("31"), "EXETER", Some("CLOSE")),
      CrfLineData(Some("EXETER"), "CLOSE", Some("LONDON")),
      CrfLineData(Some("CLOSE"), "LONDON", Some("ONS")),
      CrfLineData(Some("LONDON"), "ONS", None)
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
      CrfLineData(None, "31", Some("ONS")),
      CrfLineData(Some("31"), "ONS", None)
    )

    // When
    val result = Parser.tokensToCrfLinesData(tokens)

    // Then
    result shouldBe expected
  }

  it should "transform one element tokens into one element tokens data" in {
    // Given
    val tokens = List("31")
    val expected = List(CrfLineData(None, "31", None))

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
    val lineData = CrfLineData(None, "31", None)
    val expected = "business:0.0\tcompany:0.0\tdigits\\:all_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:0.0\thyphenations:0.0\tlength\\:d\\:2:1.0\tlocational:0.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\tresidential:0.0\troad:0.0\tword:0.0"

    // When
    val result = Parser.constructCrfInputLine(lineData)

    // Then
    result shouldBe expected
  }

  it should "transform a token data (with previous and next token) into a CRF line" in {
    // Given
    val lineData = CrfLineData(Some("31"), "CURLEW", Some("WAY"))
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
    val lineData = CrfLineData(Some("31"), "CURLEW", None)
    val expected = "business:0.0\tcompany:0.0\tdigits\\:no_digits:1.0\tdirectional:0.0\tendsinpunc:0.0\tflat:0.0\thas.vowels:1.0\thyphenations:0.0\tlength\\:w\\:6:1.0\tlocational:0.0\tordinal:0.0\toutcode:0.0\tposttown:0.0\tresidential:0.0\troad:0.0\tword\\:CURLEW:1.0" +
      "\tprevious\\:business:0.0\tprevious\\:company:0.0\tprevious\\:digits\\:all_digits:1.0\tprevious\\:directional:0.0\tprevious\\:endsinpunc:0.0\tprevious\\:flat:0.0\tprevious\\:has.vowels:0.0\tprevious\\:hyphenations:0.0\tprevious\\:length\\:d\\:2:1.0\tprevious\\:locational:0.0\tprevious\\:ordinal:0.0\tprevious\\:outcode:0.0\tprevious\\:posttown:0.0\tprevious\\:residential:0.0\tprevious\\:road:0.0\tprevious\\:word:0.0"

    // When
    val result = Parser.constructCrfInputLine(lineData)

    // Then
    result shouldBe expected
  }

  it should "transform a token data (with only next token) into a CRF line" in {
    // Given
    val lineData = CrfLineData(None, "CURLEW", Some("WAY"))
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

}
