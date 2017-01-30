package uk.gov.ons.addressIndex.crfscala

import uk.gov.ons.addressIndex.crfscala.CrfFeatureAnalyser.CrfFeatureAnalyser
import uk.gov.ons.addressIndex.crfscala.CrfScala._
import scala.util.control.NonFatal

/** todo scaladoc
  *
  * @tparam T the return type of the FeatureAnalyser
  */
trait CrfFeature[T] {

  /**
    * @return a function which returns an instance of T
    */
  def analyser: CrfFeatureAnalyser[T]

  /**
    * @return name
    */
  def name: String

  /**
    * @param input
    * @return apply the analyser to i
    */
  def analyse(input: String): T = analyser apply input

  //TODO scaladoc
  /**
    * @param input
    * @param opNext
    * @param opPrevious
    * @return
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

  //TODO scaladoc
  /**
    *
    * @param prefix
    * @param value
    * @return
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