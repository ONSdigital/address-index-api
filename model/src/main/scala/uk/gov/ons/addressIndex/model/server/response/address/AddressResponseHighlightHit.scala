package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

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
    val distinctHitCount = Math.round((highLightedText.mkString.replace(",","").split(" ").distinct.mkString.count(_ == '<') / 2).toFloat)

   Some(AddressResponseHighlightHit(source,lang,distinctHitCount,highLightedText))
  }

  def fromCombinedHighlight(hit: (String,Seq[String]), overrideText:String = "", testFieldName:String, testFieldVal:String): Option[AddressResponseHighlightHit] = {

    val highlightedTextIn = if (overrideText != "") overrideText else hit._2.mkString
    val highlightedTextOut = highlightSingleFromCombined(testFieldVal,highlightedTextIn)
    val searchField = testFieldName
    val lang = if (searchField.contains("elsh")) "W" else "E"
    val source = if (searchField.contains("Nag")) "L" else if (searchField.contains("Nisra")) "N" else "P"
    val distinctHitCount = Math.round((highlightedTextOut.mkString.replace(",","").split(" ").distinct.mkString.count(_ == '<') / 2).toFloat)

    Some(AddressResponseHighlightHit(source,lang,distinctHitCount,highlightedTextOut))
  }

  def highlightSingleFromCombined(singleIn: String,combinedIn: String) : String = {
    val comboBits = combinedIn.mkString.replace(",","").split(" ").distinct
    val comboHits: Array[String] = comboBits.map{ bit => if (bit.startsWith("<")) bit.replaceAll("<em>","").replaceAll("</em>","") else ""}
    val singleBits = singleIn.replace(","," ,").split(" ")
    val newSingle = singleBits.map{ bit => if (comboHits contains bit) "<em>"+bit+"</em>" else bit }
    newSingle.mkString(" ").replaceAll(" ,",",")
  }
}
