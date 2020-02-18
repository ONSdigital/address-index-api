package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

case class AddressResponseHighlightHit(source: String,
                                       lang: String,
                                       distinctHitCount: Int,
                                       highLightedText: String
                                      )

object AddressResponseHighlightHit {
  implicit lazy val addressResponseHighlightHitFormat: Format[AddressResponseHighlightHit] = Json.format[AddressResponseHighlightHit]

  def fromHighlight(hit: (String,Seq[String])): Option[AddressResponseHighlightHit] = {
    val source = "L"
    val distinctHitCount = 2
    val lang = "E"
    val highLightedText = "Hello <em>World</em>"

   Some(AddressResponseHighlightHit(source,lang,distinctHitCount,highLightedText))
  }
}
