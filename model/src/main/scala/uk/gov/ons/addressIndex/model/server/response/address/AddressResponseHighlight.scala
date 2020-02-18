package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}
case class AddressResponseHighlight(bestMatchAddress: String,
                                    hits: Option[Seq[AddressResponseHighlightHit]])

object AddressResponseHighlight {
  implicit lazy val addressResponseHighlightFormat: Format[AddressResponseHighlight] = Json.format[AddressResponseHighlight]

  def fromHighlight(bestMatchAddress: String, other: Map[String,Seq[String]]): Option[AddressResponseHighlight] = {
   val hitList = other.flatMap{hit =>
     AddressResponseHighlightHit.fromHighlight(hit)}
    val optList = Option(hitList.toSeq)
   Some(AddressResponseHighlight(bestMatchAddress,optList))
 }
}



