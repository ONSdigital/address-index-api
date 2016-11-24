package uk.gov.ons.addressIndex.model

import java.util.UUID

case class AddressIndexUPRNRequest(
  format : AddressScheme,
  uprn   : BigInt,
  id     : UUID
)

case class AddressIndexSearchRequest(
  format : AddressScheme,
  input  : String,
  id     : UUID
)

sealed trait AddressScheme {
  override def toString: String
}
case class PostcodeAddressFile(override val toString : String) extends AddressScheme
case class BritishStandard7666(override val toString : String) extends AddressScheme
case class UnsupportedScheme(override val toString : String) extends AddressScheme

object AddressScheme {

  /**
    * Implicitly converts a `String` to an `AddressScheme`.
    * @param str - The string to be converted.
    */
  implicit class StringToAddressSchemeAugmenter(str: String) {
    def stringToScheme() : AddressScheme = str.toLowerCase match {
      case "paf" | "postcodeaddressfile" => PostcodeAddressFile(str)

      case "bs" | "bs7666" | "britishstandard7666" => BritishStandard7666(str)

      case scheme => UnsupportedScheme(scheme)
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