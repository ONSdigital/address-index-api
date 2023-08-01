package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}
case class AddressResponseHighlight(bestMatchAddress: String,
                                    source: String,
                                    lang: String,
                                    hits: Option[Seq[AddressResponseHighlightHit]])

object AddressResponseHighlight {
  implicit lazy val addressResponseHighlightFormat: Format[AddressResponseHighlight] = Json.format[AddressResponseHighlight]

  def fromHighlight(bestMatchAddress: String, other: Map[String,Seq[String]]): Option[AddressResponseHighlight] = {
   val hitList = other.flatMap{hit =>
     hit._2.flatMap {lin =>
     AddressResponseHighlightHit.fromHighlight(hit,lin)}}
    val optList = Option(hitList.toSeq)
   Some(AddressResponseHighlight(bestMatchAddress,
     hitList.headOption.map(hit => hit.source).getOrElse(""),
     hitList.headOption.map(hit => hit.lang).getOrElse(""),
     optList))
 }

  def fromCombinedHighlight(bestMatchAddress: String,
                            other: Map[String,Seq[String]],
                            formattedAddressPaf: String,
                            welshFormattedAddressPaf: String,
                            formattedAddressNag: String,
                            welshFormattedAddressNag: String): Option[AddressResponseHighlight] = {
    val hitList1 = if (formattedAddressPaf.isEmpty) List.empty else other.flatMap{hit =>
      hit._2.flatMap {lin =>
        AddressResponseHighlightHit.fromCombinedHighlight(hit,lin,"formattedAddressPaf",formattedAddressPaf)}}
    val hitList2 = if (welshFormattedAddressPaf.isEmpty) List.empty else other.flatMap{hit =>
      hit._2.flatMap {lin =>
        AddressResponseHighlightHit.fromCombinedHighlight(hit,lin,"welshFormattedAddressPaf",welshFormattedAddressPaf)}}
    val hitList3 = if (formattedAddressNag.isEmpty) List.empty else other.flatMap{hit =>
      hit._2.flatMap {lin =>
        AddressResponseHighlightHit.fromCombinedHighlight(hit,lin,"formattedAddressNag",formattedAddressNag)}}
    val hitList4 = if (welshFormattedAddressNag.isEmpty) List.empty else other.flatMap{hit =>
      hit._2.flatMap {lin =>
        AddressResponseHighlightHit.fromCombinedHighlight(hit,lin,"welshFormattedAddressNag",welshFormattedAddressNag)}}

    val hitList = hitList1 ++ hitList2 ++ hitList3 ++ hitList4
    val optList = Option(hitList.toSeq)
    Some(AddressResponseHighlight(bestMatchAddress,
      hitList.headOption.map(hit => hit.source).getOrElse(""),
      hitList.headOption.map(hit => hit.lang).getOrElse(""),
      optList))
  }
}



