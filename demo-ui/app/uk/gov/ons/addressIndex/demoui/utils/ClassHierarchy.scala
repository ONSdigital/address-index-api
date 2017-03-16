package uk.gov.ons.addressIndex.demoui.utils

import javax.inject.{Inject, Singleton}

import play.api.i18n._

@Singleton
class ClassHierarchy @Inject()(val messagesApi: MessagesApi)  {

  def analyseClassCode(code: String): String = {

    if (messagesApi.isDefinedAt("category." + code)) {
      val primary = "^[A-Z]{1}".r
      val secondary = "^[A-Z]{2}".r
      val tertiary = "^[A-Z]{2}[0-9]{2}".r
      val quaternary = "^[A-Z]{2}[0-9]{2}[A-Z]{2}".r

      val patterns = Seq(primary, secondary, tertiary, quaternary)
      val classifications = patterns.flatMap(_.findFirstIn(code))

      val codes = classifications
        .map(categoryCode => s"category.$categoryCode")
        .filter(messagesApi.isDefinedAt)
        .map(categoryCode => s" [ ${messagesApi(categoryCode)} ]")

      s" [ $code ]${codes.mkString}"
    } else {
      s" [ $code ]"
    }
  }
}