package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json};

case class AddressResponseHighlightHit(source: String,
                                       lang: String,
                                       distinctHitCount: Int,
                                       highLightedText: String
                                      )

object AddressResponseHighlightHit {
  implicit lazy val addressResponseHighlightHitFormat: Format[AddressResponseHighlightHit] = Json.format[AddressResponseHighlightHit]

  def fromHighlight(hit: (String,Seq[String]), overrideText:String = ""): Option[AddressResponseHighlightHit] = {

    val highLightedText = if (overrideText != "") overrideText else hit._2.mkString
    val searchField = hit._1.mkString
    val lang = if (searchField.contains("Welsh")) "W" else "E"
    val source = if (searchField.contains("Nag")) "L" else if (searchField.contains("Nisra")) "N" else "P"
    val distinctHitCount = Math.round(highLightedText.mkString.replace(",","").split(" ").distinct.mkString.count(_ == '<') / 2)

   Some(AddressResponseHighlightHit(source,lang,distinctHitCount,highLightedText))
  }

  def fromCombinedHighlight(hit: (String,Seq[String]), overrideText:String = "", testFieldName:String, testFieldVal:String): Option[AddressResponseHighlightHit] = {

    val highlightedTextIn = if (overrideText != "") overrideText else hit._2.mkString
    val highlightedTextOut = testFieldVal
    val searchField = testFieldName
    val lang = if (searchField.contains("welsh")) "W" else "E"
    val source = if (searchField.contains("Nag")) "L" else if (searchField.contains("Nisra")) "N" else "P"
    val distinctHitCount = Math.round(highlightedTextOut.mkString.replace(",","").split(" ").distinct.mkString.count(_ == '<') / 2)

    Some(AddressResponseHighlightHit(source,lang,distinctHitCount,highlightedTextOut))
  }
}
