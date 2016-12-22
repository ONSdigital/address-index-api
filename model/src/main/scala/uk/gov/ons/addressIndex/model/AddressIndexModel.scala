package uk.gov.ons.addressIndex.model

import java.util.UUID

case class AddressIndexUPRNRequest(
  format: AddressScheme,
  uprn: BigInt,
  id: UUID
)

case class AddressIndexSearchRequest(
  format: AddressScheme,
  input: String,
  id: UUID
)

sealed trait AddressScheme {
  override def toString: String
}
case class PostcodeAddressFile(override val toString: String) extends AddressScheme
case class BritishStandard7666(override val toString: String) extends AddressScheme

object AddressScheme {

  /**
    * Implicitly converts a `String` to an `AddressScheme`.
    * @param str - The string to be converted.
    */
  implicit class StringToAddressSchemeAugmenter(str: String) {
    def stringToScheme(): Option[AddressScheme] = str.toLowerCase match {
      case "paf" | "postcodeaddressfile" => Some(PostcodeAddressFile(str))

      case "bs" | "bs7666" | "britishstandard7666" => Some(BritishStandard7666(str))

      case _ => None
    }
  }

}

