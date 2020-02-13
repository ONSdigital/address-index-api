package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}
//import uk.gov.ons.addressIndex.model.db.index.{NationalAddressGazetteerAddress, NisraAddress}

//import scala.util.Try

case class AddressResponseHighlight(bestMatchField: String, highlight: Map[String,Seq[String]])

object AddressResponseHighlight {
  implicit lazy val addressResponseHighlightFormat: Format[AddressResponseHighlight] = Json.format[AddressResponseHighlight]

  def fromHighlight(bestMatchField: String, other: Map[String,Seq[String]]): Option[AddressResponseHighlight] = {
   Some(AddressResponseHighlight(bestMatchField,other))
 }

 }