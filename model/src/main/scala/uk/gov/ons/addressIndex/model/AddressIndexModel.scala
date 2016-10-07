package main.scala.uk.gov.ons.addressIndex.model

import java.util.UUID

case class AddressIndexUPRNRequest(
  uprn   : BigInt,
  format : AddressScheme
)

case class AddressIndexSearchRequest(
  input  : String,
  id     : UUID,
  format : AddressScheme
)

sealed trait AddressScheme
case class PostcodeAddressFile()       extends AddressScheme
case class BritishStandard7666()       extends AddressScheme
case class UnrecognisedAddressScheme() extends AddressScheme

object AddressScheme {

  /**
    * Implicitly converts a `String` to an `AddressScheme`.
    * @param str - The string to be converted.
    */
  implicit class StringToAddressSchemeAugmenter(str: String) {
    def toAddressScheme(): AddressScheme = str toLowerCase match {
      case "paf"
        => PostcodeAddressFile()

      case "bs7666" | "britishstandard7666"
        => BritishStandard7666()

      case _
        => UnrecognisedAddressScheme()
    }
  }
}

case class TokenisedAddressResult(
  tokenisedAddress : TokenisedAddress,
  notParsed        : Option[List[Unparsed]],
  uprn             : Int
)

case class AddressIndexResponse(
  results : List[TokenisedAddressResult],
  input   : AddressIndexSearchRequest,
  id      : UUID,
  uprn    : Int
)

case class Postcode(
  inCode  : String,
  outCode : String
)

case class Unparsed(
  untokenised : String,
  subString   : SubString
)

case class SubString(
  startIndex : Int,
  endIndex   : Int
)

case class TokenisedAddress(
  postcode    : Option[Postcode],
  houseNumber : Option[String],
  houseName   : Option[String],
  flatNumber  : Option[String],
  flatName    : Option[String],
  county      : Option[String],
  city        : Option[String],
  locality    : Option[String],
  estate      : Option[String],
  streetName  : Option[String],
  leadingFrom : Option[String],
  floor       : Option[String]
)