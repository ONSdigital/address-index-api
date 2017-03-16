package uk.gov.ons.addressIndex.crfscala

import uk.gov.ons.addressIndex.crfscala.CrfFeatureAnalyser.CrfFeatureAnalyser
import uk.gov.ons.addressIndex.crfscala.CrfScala._
import scala.util.control.NonFatal

/**
  * @tparam T the return type of the FeatureAnalyser
  */
trait CrfFeature[T] {

  /**
    * @return a function which returns an instance of T
    */
  def analyser: CrfFeatureAnalyser[T]

  /**
    * @return name of the feature.
    */
  def name: String

  /**
    * Helper method which applys the feature analyser (function) to it's input
    *
    * @param input the input
    * @return apply the analyser to i, and return the result of type T
    */
  def analyse(input: String): T = analyser apply input

  /**
    * Produces an IWA string
    *
    * @param input the current input token
    * @param opNext the optional next token
    * @param opPrevious the optional previous token
    * @return an IWA string
    */
  def toCrfJniInput(input: String, opNext: Option[String] = None, opPrevious: Option[String] = None): String = {
    val currentCrfJni: String = createCrfJniInput(
      prefix = name,
      value = analyse(input)
    )
    val nextCrfJni: String = {
      opNext map { next =>
        CrfScalaJni.delimiter +
          createCrfJniInput(
            prefix = CrfScalaJni.next,
            value = analyse(next)
          )
      } getOrElse ""
    }
    val previousCrfJni: String = {
      opPrevious map { previous =>
        CrfScalaJni.delimiter +
          createCrfJniInput(
            prefix = CrfScalaJni.previous,
            value = analyse(previous)
          )
      } getOrElse ""
    }
    CrfScalaJni.lineStart + currentCrfJni + nextCrfJni + previousCrfJni
  }

  /**
    * Produces an IWA string. This is the most granular level where we
    * create IWA strings. The string will look different on FeatureAnalysers
    * return type and the feature analysers name.
    *
    * @param prefix the prefix, eg, next, previous
    * @param value the value of the feature analyser
    * @return the IWA string part
    */
  def createCrfJniInput(prefix: String, value: Any): String = {
    def qualify(str: String): String = str.replace(":", "\\:")
    val qName = qualify(name)
    val qPrefix = if (prefix == name) "" else prefix

    value match {
      case _: String =>
        s"$qPrefix$qName\\:${qualify(value.asInstanceOf[String])}:1.0"

      case _: Int =>
        s"$qPrefix$qName:$value.0"

      case _: Double =>
        s"$qPrefix$qName:$value"

      case _: Boolean =>
        s"$qPrefix$qName:${if (value.asInstanceOf[Boolean]) "1.0" else "0.0"}"

      case t : CrfType[_] =>
        createCrfJniInput(prefix, t.value)

      case NonFatal(e) =>
        throw e

      case _ =>
        throw new UnsupportedOperationException(
          s"Unsupported input to CrfJniInput: ${value.getClass.toString} or Feature with name: $name"
        )
    }
  }
}