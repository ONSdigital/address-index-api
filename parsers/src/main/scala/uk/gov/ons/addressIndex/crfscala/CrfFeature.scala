package uk.gov.ons.addressIndex.crfscala

import uk.gov.ons.addressIndex.crfscala.CrfFeatureAnalyser.CrfFeatureAnalyser
import uk.gov.ons.addressIndex.crfscala.CrfScala._
import uk.gov.ons.addressIndex.crfscala.jni.CrfScalaJni

import scala.util.control.NonFatal

/** todo scaladoc
  *
  * @tparam T the return type of the FeatureAnalyser
  */
trait CrfFeature[T] {

  /**
    * @return a function which returns an instance of T
    */
  def analyser() : CrfFeatureAnalyser[T]

  /**
    * @return name
    */
  def name() : String

  /**
    * @param i input
    * @return apply the analyser to i
    */
  def analyse(i : Input) : T = analyser apply i

  //TODO scaladoc
  /**
    *
    * @param input
    * @param next
    * @param previous
    * @return
    */
  def toCrfJniInput(input: CrfToken, next: Option[CrfToken] = None, previous: Option[CrfToken] = None): CrfJniInput = {
    new StringBuilder()
      .append(CrfScalaJni.lineStart)
      .append(
        createCrfJniInput(
          prefix = name,
          someValue = analyse(input)
        )
      )
      .append(
        next map { next =>
          createCrfJniInput(
            prefix = CrfScalaJni.next,
            someValue = analyse(next)
          )
        } getOrElse ""
      )
      .append(
        previous map { previous =>
          createCrfJniInput(
            prefix = CrfScalaJni.previous,
            someValue = analyse(previous)
          )
        } getOrElse ""
      )
      .append(CrfScalaJni.lineEnd)
      .toString
  }

  //TODO scaladoc
  /**
    *
    * @param prefix
    * @param someValue
    * @return
    */
  def createCrfJniInput(prefix: String, someValue: Any): CrfJniInput = {
    def qualify(str: String): String = str.replace(":", "\\:")
    val qName = qualify(name)

    someValue match {
      case _: String =>
        s"$qName\\:${qualify(someValue.asInstanceOf[String])}:1.0"

      case _: Int =>
        s"$qName:$someValue.0"

      case _: Double =>
        s"$qName:$someValue"

      case _: Boolean =>
        s"$qName:${if(someValue.asInstanceOf[Boolean]) "1.0" else "0.0"}"

      case t : CrfType[_] =>
        createCrfJniInput(prefix, t.value)

      case NonFatal(e) =>
        throw e

      case _ =>
        throw new UnsupportedOperationException(
          s"Unsupported input to CrfJniInput: ${someValue.getClass.toString} or Feature with name: $name"
        )
    }
  }
}