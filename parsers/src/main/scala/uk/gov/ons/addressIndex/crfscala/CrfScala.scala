package uk.gov.ons.addressIndex.crfscala

import uk.gov.ons.addressIndex.crfscala.jni.{CrfScalaJni, CrfScalaJniImpl}
import uk.gov.ons.addressIndex.parsers.Tokens
import uk.gov.ons.addressIndex.parsers.Tokens.Token
import scala.util.control.NonFatal
import uk.gov.ons.addressIndex.crfscala.jni.CrfScalaJni._

/**
  * scala wrapper of crfsuite
  *
  * todo describe this more
  */
object CrfScala {

  type Input = String
  type FeatureName = String
  type FeaturesResult = Map[FeatureName, _]
  type CrfFeatureAnalyser[T] = (Input => T)
  object CrfFeatureAnalyser {
    /**
      * Helper apply method for better syntax.
      * Constructs a function.
      * Eg:
      *
      *    FeatureAnalyser[String]("SplitOnSpaceCountAsStr") { str =>
      *       str.split(" ").length.toString
      *    }
      *
      * Or:
      *
      *    FeatureAnalyser[Int]("lengthOfString") { str =>
      *       str.length
      *    }
      *
      */
    def apply[T](analyser : CrfFeatureAnalyser[T]) : CrfFeatureAnalyser[T] = analyser
  }

  //todo scaladoc
  trait CrfType[T] {
    def value : T
  }

  //TODO scaladoc
  trait CrfParser {
    //TODO scaladoc
    def parse(i : Input, fas : CrfFeatures): List[ParseResult] = {
      val tokens = Tokens(i)
      val preprocessedTokens = Tokens normalise tokens

      //TODO
      val x = preprocessedTokens map fas.analyse
      val crfJniIput = ""
      val tokenResults = new CrfScalaJniImpl tag crfJniIput split CrfScalaJni.newLine
      x
      tokenResults.toList map { tr => ParseResult(tr, tr)}
    }
  }

  case class ParseResult(originalInput: Token, crfLabel: String)

  /**
    * scala wrapper of third_party.org.chokkan.crfsuite.Item
    */
  trait CrfFeatures {

    /**
      * @return all the features
      */
    def all : Seq[CrfFeature[_]]

    def toCrfJniInput(input: Token, next: Token, previous: Option[Token]): CrfJniInput = {
      all map(_.toCrfJniInput(input, next, previous)) mkString CrfScalaJni.lineEnd
    }

    /**
      * @param i the token to run against all feature analysers
      * @return the token and its results, as a pair
      */
    def analyse(i : Token) : TokenResult = TokenResult(
      token = i,
      results = all.map(f => f.name -> f.analyse(i)).toMap
    )
  }

  type CrfJniInput = String
  case class TokenResult(token: Token, results: FeaturesResult)

  /**
    * scala wrapper of third_party.org.chokkan.crfsuite.Attribute
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

    def toCrfJniInput(input: Token, next: Option[Token] = None, previous: Option[Token] = None): CrfJniInput = {
      new StringBuilder()
        .append(
          createCrfJniInput(
            prefix = name,
            someValue = analyse(input)
          )
        )
        .append(
          createCrfJniInput(
            prefix = CrfScalaJni.next,
            someValue = analyse(next.getOrElse(""))//TODO
          )
        )
        .append(
          createCrfJniInput(
            prefix = CrfScalaJni.previous,
            someValue = analyse(previous.getOrElse(""))//TODO
          )
        )
        .toString
    }

    def createCrfJniInput(prefix: String, someValue: Any): CrfJniInput = {
      someValue match {
        case _: String =>
          s"$name:$delimiter" //complicated TODO

        case _: Int =>
          s"$name:$someValue.0$delimiter"

        case _: Double =>
          s"$name:$someValue$delimiter"

        case _: Boolean =>
          s"$name:${if(someValue.asInstanceOf[Boolean]) "1.0" else "0.0"}$delimiter"

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
}