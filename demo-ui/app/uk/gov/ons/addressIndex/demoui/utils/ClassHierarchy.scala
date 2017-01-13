package uk.gov.ons.addressIndex.demoui.utils

import play.api.i18n._
import javax.inject.{Inject, Singleton}

@Singleton
class ClassHierarchy @Inject()(val messagesApi: MessagesApi)  {

  def analyseClassCode(code: String): String = {

    if (messagesApi.isDefinedAt("category." + code)) {
      val primary = "^[A-Z]{1}".r
      val secondary = "^[A-Z]{2}".r
      val tertiary = "^[A-Z]{2}[0-9]{2}".r
      val quaternary = "^[A-Z]{2}[0-9]{2}[A-Z]{2}".r

      val patterns = Seq(primary, secondary, tertiary, quaternary)
      var classifications = Seq[Option[String]]()
      var outputHierarchy = Seq("[" + code + "]")

      patterns.foreach{pattern =>
        val classification = pattern.findFirstIn(code)
        classifications :+= classification
      }

      classifications.flatten.foreach{ code =>
        // Sometimes a full classification hierarchy doesn't exist so check first before adding to Sequence
        if (messagesApi.isDefinedAt("category." + code)) outputHierarchy :+= "[" + messagesApi("category." + code) + "]"
      }

      outputHierarchy.mkString
    } else {
      "[" + code + "]"
    }
  }
}