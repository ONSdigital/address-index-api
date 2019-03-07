package uk.gov.ons.addressIndex.parsers

import org.scalatest.{FlatSpec, Matchers}

class TokensTest extends FlatSpec with Matchers {

  it should "produce `Tokens` for the given string `31 exeter close` splitting on whitespace" in {
    val input = "31 exeter close"
    val expected = Seq("31", "EXETER", "CLOSE")
    val actual = Tokens.preTokenize(input)
    actual shouldBe expected
  }

  it should "convert a string to tokens separating on ` `" in {
    val expected = List("ONE", "TWO", "THREE")
    val input = expected mkString " "
    val actual = Tokens.preTokenize(input)
    actual shouldBe expected
  }

  it should "produce tokens, splitting the char `,`" in {
    val input = "a,b,c"
    val expected = Seq("A", "B", "C")
    val actual = Tokens.preTokenize(input)
    actual shouldBe expected
  }

  it should "produce tokens, splitting the char `\\`" in {
    val input = "a\\b\\c"
    val expected = Seq("A", "B", "C")
    val actual = Tokens.preTokenize(input)
    actual shouldBe expected
  }

  it should "produce tokens, removing spaces around hyphen" in {
    val input = "1 -2 3 - 4 5- 6 A - B 7   - 8 9 -    1"
    val expected = Seq("1-2", "3-4", "5-6", "A", "B", "7-8", "9-1")
    val actual = Tokens.preTokenize(input)
    actual shouldBe expected
  }

  it should "produce tokens, removing spaces around hyphen (numbers with letters)" in {
    val input = "1A -2B 3A - 4 5- 6B 9 - 1"
    val expected = Seq("1A-2B", "3A-4", "5-6B", "9-1")
    val actual = Tokens.preTokenize(input)
    actual shouldBe expected
  }

  it should "produce tokens, replacing `to` with hyphen" in {
    val input = "1 TO 2 3 TO 4"
    val expected = Seq("1-2", "3-4")
    val actual = Tokens.preTokenize(input)
    actual shouldBe expected
  }

  it should "produce tokens without 'IN' tokens" in {
    val input = "1 IN 2 3 IN 4 IN 5"
    val expected = Seq("1", "2", "3", "4", "5")
    val actual = Tokens.preTokenize(input)
    actual shouldBe expected
  }

  it should "produce tokens, replacing `/` with hyphen" in {
    val input = "1/2 3/4"
    val expected = Seq("1-2", "3-4")
    val actual = Tokens.preTokenize(input)
    actual shouldBe expected
  }

  it should "replace synonyms at the beginning, middle or the end of the input" in {
    val input = "ENGLAND THIS WALES ENGLANDLONDON LONDONWALES THAT UNITEDKINGDOM"
    val expected = Seq("THIS", "ENGLANDLONDON", "LONDONWALES", "THAT")
    val actual = Tokens.preTokenize(input)
    actual shouldBe expected
  }

  it should "convert a string to tokens allowing special characters" in {
    val input = "SOMEINPUTIITHSOMECRAZYCASE.;'[#][{}#~|£$%^&*()`"
    val actual = Tokens.preTokenize(input)
    val expected = List(input)
    actual shouldBe expected
  }

  it should "convert '120 - 122 HIGH STREET OXFORD' into '120-122 HIGH STREET OXFORD'" in {
    val input = "120 - 122 HIGH STREET OXFORD"
    val actual = Tokens.preTokenize(input).mkString(" ")
    val expected = "120-122 HIGH STREET OXFORD"
    actual shouldBe expected
  }

  it should "convert '9 GRANVILLE ROAD ST. ALBANS HERTS AL1 5BE' into '9 GRANVILLE ROAD ST. ALBANS AL1 5BE'" in {
    val input = "9 GRANVILLE ROAD ST. ALBANS HERTS AL1 5BE"
    val actual = Tokens.preTokenize(input).mkString(" ")
    val expected = "9 GRANVILLE ROAD ST. ALBANS AL1 5BE"
    actual shouldBe expected
  }

  it should "convert '9 GRANVILLE ROAD ST. ALBANS HERTFORDSHIRE AL1 5BE' into '9 GRANVILLE ROAD ST. ALBANS AL1 5BE'" in {
    val input = "9 GRANVILLE ROAD ST. ALBANS HERTFORDSHIRE AL1 5BE"
    val actual = Tokens.preTokenize(input).mkString(" ")
    val expected = "9 GRANVILLE ROAD ST. ALBANS AL1 5BE"
    actual shouldBe expected
  }

  it should "convert '27 AMBERLEY WAY STREETLY SUTTON COLDFIELD WEST MIDLANDS B74 3RN' into '27 AMBERLEY WAY STREETLY SUTTON COLDFIELD B74 3RN'" in {
    val input = "27 AMBERLEY WAY STREETLY SUTTON COLDFIELD WEST MIDLANDS B74 3RN"
    val actual = Tokens.preTokenize(input).mkString(" ")
    val expected = "27 AMBERLEY WAY STREETLY SUTTON COLDFIELD B74 3RN"
    actual shouldBe expected
  }

  it should "convert 'HILL VIEW CARE HOME 5 ESSEX CLOSE FRIMLEY CAMBERLEY GU16 9FH ENGLAND' into 'HILL VIEW CARE HOME 5 ESSEX CLOSE FRIMLEY CAMBERLEY GU16 9FH'" in {
    val input = "HILL VIEW CARE HOME 5 ESSEX CLOSE FRIMLEY CAMBERLEY GU16 9FH ENGLAND"
    val actual = Tokens.preTokenize(input).mkString(" ")
    val expected = "HILL VIEW CARE HOME 5 ESSEX CLOSE FRIMLEY CAMBERLEY GU16 9FH"
    actual shouldBe expected
  }

  it should "convert 'ESSEX HOUSE TYNE AND WEAR ROAD MIDDX S01 1ZZ' into 'ESSEX HOUSE TYNE AND WEAR ROAD S01 1ZZ'" in {
    val input = "ESSEX HOUSE TYNE AND WEAR ROAD MIDDX S01 1ZZ"
    val actual = Tokens.preTokenize(input).mkString(" ")
    val expected = "ESSEX HOUSE TYNE AND WEAR ROAD S01 1ZZ"
    actual shouldBe expected
  }

  it should "convert 'ESSEX HOUSE TYNE AND WEAR ROAD MIDDLESEX S01 1ZZ' into 'ESSEX HOUSE TYNE AND WEAR ROAD S01 1ZZ'" in {
    val input = "ESSEX HOUSE TYNE AND WEAR ROAD MIDDLESEX S01 1ZZ"
    val actual = Tokens.preTokenize(input).mkString(" ")
    val expected = "ESSEX HOUSE TYNE AND WEAR ROAD S01 1ZZ"
    actual shouldBe expected
  }

  it should "convert 'DEVON VIEW CUMBRIA LANE OXFORD TYNE AND WEAR OX14 1ZZ' into 'DEVON VIEW CUMBRIA LANE OXFORD OX14 1ZZ'" in {
    val input = "DEVON VIEW CUMBRIA LANE OXFORD TYNE AND WEAR OX14 1ZZ"
    val actual = Tokens.preTokenize(input).mkString(" ")
    val expected = "DEVON VIEW CUMBRIA LANE OXFORD OX14 1ZZ"
    actual shouldBe expected
  }

  it should "convert 'SUSSEX ROAD SURREY W SUSSEX' into 'SUSSEX ROAD'" in {
    val input = "SUSSEX ROAD SURREY W SUSSEX"
    val actual = Tokens.preTokenize(input).mkString(" ")
    val expected = "SUSSEX ROAD"
    actual shouldBe expected
  }

  it should "convert 'SALOP HOUSE CAERPHILLY LANE SIR Y FFLINT S11 1SS' into 'SALOP HOUSE CAERPHILLY LANE S11 1SS'" in {
    val input = "SALOP HOUSE CAERPHILLY LANE SIR Y FFLINT S11 1SS"
    val actual = Tokens.preTokenize(input).mkString(" ")
    val expected = "SALOP HOUSE CAERPHILLY LANE S11 1SS"
    actual shouldBe expected
  }

  it should "convert '5 HAMPSHIRE ROAD BRADFORD ON AVON AVON NORTH SOMERSET' into '5 HAMPSHIRE ROAD BRADFORD ON AVON'" in {
    val input = "5 HAMPSHIRE ROAD BRADFORD ON AVON AVON NORTH SOMERSET"
    val expected = Seq("5", "HAMPSHIRE", "ROAD", "BRADFORD", "ON", "AVON")
    val actual = Tokens.preTokenize(input)
    actual shouldBe expected
  }

  it should "convert '15 EAST SUSSEX STREET STRATFORD UPON AVON AVON WEST DUNBARTONSHIRE' into '15 EAST SUSSEX STREET STRATFORD UPON AVON'" in {
    val input = "15 EAST SUSSEX STREET STRATFORD UPON AVON AVON WEST DUNBARTONSHIRE"
    val expected = Seq("15", "EAST", "SUSSEX", "STREET", "STRATFORD", "UPON", "AVON")
    val actual = Tokens.preTokenize(input)
    actual shouldBe expected
  }

  it should "convert '12 WORCESTERSHIRE HOUSE DINAS POWYS POWYS' into '12 WORCESTERSHIRE HOUSE DINAS POWYS'" in {
    val input = "12 WORCESTERSHIRE HOUSE DINAS POWYS POWYS"
    val expected = Seq("12", "WORCESTERSHIRE", "HOUSE", "DINAS", "POWYS")
    val actual = Tokens.preTokenize(input)
    actual shouldBe expected
  }

  it should "convert '99 ABERDEENSHIRE GARDENS BIDFORD ON AVON AVON PEMBROKESHIRE' into '99 ABERDEENSHIRE GARDENS BIDFORD ON AVON'" in {
    val input = "99 ABERDEENSHIRE GARDENS BIDFORD ON AVON AVON PEMBROKESHIRE"
    val expected = Seq("99", "ABERDEENSHIRE", "GARDENS", "BIDFORD", "ON", "AVON")
    val actual = Tokens.preTokenize(input)
    actual shouldBe expected
  }

  it should "convert '19 AVON COTTAGES WELFORD ON AVON GREATER LONDON UK ENGLAND' into '19 AVON COTTAGES WELFORD ON AVON'" in {
    val input = "19 AVON COTTAGES WELFORD ON AVON GREATER LONDON UK ENGLAND"
    val expected = Seq("19", "AVON", "COTTAGES", "WELFORD", "ON", "AVON")
    val actual = Tokens.preTokenize(input)
    actual shouldBe expected
  }

  it should "convert '21 SHETLAND PLACE WESTON ON AVON SUTHERLAND UNITED KINGDOM UK' into '21 SHETLAND PLACE WESTON ON AVON'" in {
    val input = "21 SHETLAND PLACE WESTON ON AVON SUTHERLAND UNITED KINGDOM UK"

    val expected = Seq("21", "SHETLAND", "PLACE", "WESTON", "ON", "AVON")
    val actual = Tokens.preTokenize(input)
    actual shouldBe expected
  }

  it should "convert 'CALLERTON HOUSE CALLERTON PLACE STANLEY CO DURHAM DH9' into 'CALLERTON HOUSE CALLERTON PLACE STANLEY DH9'" in {
    val input = "CALLERTON HOUSE CALLERTON PLACE STANLEY CO DURHAM DH9"
    val expected = "CALLERTON HOUSE CALLERTON PLACE STANLEY DH9"
    val actual = Tokens.preTokenize(input).mkString(" ")
    actual shouldBe expected
  }
}