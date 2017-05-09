package uk.gov.ons.addressIndex.parsers

import uk.gov.ons.addressIndex.crfscala.CrfAggregateFeatureAnalyser.{apply => _, _}
import uk.gov.ons.addressIndex.crfscala.CrfFeatureAnalyser.CrfFeatureAnalyser
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfType
import uk.gov.ons.addressIndex.crfscala.{CrfAggregateFeatureAnalyser, CrfFeatureAnalyser}
import uk.gov.ons.addressIndex.parsers.FeatureAnalysers.ADT.Root
import uk.gov.ons.addressIndex.parsers.FeatureAnalysers.DigitsLiteral._
import uk.gov.ons.addressIndex.parsers.Implicits._

/**
  * FeatureAnalyser implementations for the AddressParser
  */
object FeatureAnalysers {

  /**
    * @return all of the predefined features
    */
  def allFeatures: FeaturesOld = {
    FeaturesOld(
      Seq(
        Feature[String](digits)(digitsAnalyser),
        Feature[Root[_]](word)(wordAnalyser),
        Feature[String](length)(lengthAnalyser),
        Feature[Boolean](ordinal)(ordinalAnalyser),
        Feature[Boolean](endsInPunctuation)(endsInPunctuationAnalyser),
        Feature[Boolean](directional)(directionalAnalyser),
        Feature[Boolean](outcode)(outcodeAnalyser),
        Feature[Boolean](postTown)(postTownAnalyser),
        Feature[Boolean](hasVowels)(hasVowelsAnalyser),
        Feature[Boolean](flat)(flatAnalyser),
        Feature[Boolean](company)(companyAnalyser),
        Feature[Boolean](road)(roadAnalyser),
        Feature[Boolean](residential)(residentialAnalyser),
        Feature[Boolean](business)(businessAnalyser),
        Feature[Boolean](locational)(locationalAnalyser),
        Feature[Int](hyphenations)(hyphenationsAnalyser)
      ).sortBy(_.name):_*
    )(
      FeatureAggregate[Boolean](rawStringStart)(rawStringStartAggrAnalyser),
      FeatureAggregate[Boolean](rawStringEnd)(rawStringEndAggrAnalyser),
      FeatureAggregate[Boolean](singleton)(singletonAggrAnalyser)
    )
  }

  /**
    * Implementation of CrfType type class, which allows mix return types.
    * This is needed to generate CrfJniInput.
    */
  object ADT {
    trait Root[T] extends CrfType[T] {
      def value: T
    }
    implicit class StringWrapper(override val value: String) extends Root[String]
    implicit class BooleanWrapper(override val value: Boolean) extends Root[Boolean]
  }

  val rawStringStart: String  = "rawstring.start"
  /**
    * @return true if the token is the first in the sequence.
    */
  def rawStringStartAggrAnalyser: CrfAggregateFeatureAnalyser[Boolean] = IndexAggregateFeatureAnalyser(_ => 0)

  val rawStringEnd: String  = "rawstring.end"
  /**
    * @return true is the token is the last in the sequence.
    */
  def rawStringEndAggrAnalyser: CrfAggregateFeatureAnalyser[Boolean] = IndexAggregateFeatureAnalyser(_.length - 1)

  val singleton: String  = "singleton"
  /**
    * @return true if the token is the only token.
    */
  def singletonAggrAnalyser: CrfAggregateFeatureAnalyser[Boolean] = CrfAggregateFeatureAnalyser[Boolean] { (tokens, token) =>
    tokens.length == 1 && tokens(0) == token
  }

  val word: String = "word"
  /**
    * @return true if the string is all digits, false if not
    */
  def wordAnalyser: CrfFeatureAnalyser[Root[_]] = CrfFeatureAnalyser[Root[_]] { str =>
    str.allDigits[Root[_]] { isAllDigits =>
      if (isAllDigits) {
        false
      } else {
        str
      }
    }
  }

  val length: String = "length"
  /**
    * @return the length of the string as a string
    *         with a prefix of "w:" if its a word (some or no digits) eg "Hello" or "Hello123" or
    *         with a prefix of "d:" if its a string which is all digits eg "123".
    */
  def lengthAnalyser: CrfFeatureAnalyser[String] = CrfFeatureAnalyser[String] { str =>
    val length = str.length
    str.allDigits[String] { isAllDigits =>
      if(isAllDigits) {
        s"d:$length"
      } else {
        s"w:$length"
      }
    }
  }

  val endsInPunctuation: String = "endsinpunc"
  /**
    * @return true if the last character of the string is a '.', false if not
    */
  def endsInPunctuationAnalyser: CrfFeatureAnalyser[Boolean] = CrfFeatureAnalyser[Boolean]{ str =>
    val arbitrary: Char = 'x'
    str.lastOption.getOrElse(arbitrary) == '.'
  }

  val hasVowels: String = "has.vowels"
  /**
    * @return true if the string contains vowels, false if not
    */
  def hasVowelsAnalyser: CrfFeatureAnalyser[Boolean] = CrfFeatureAnalyser[Boolean](_.containsVowels[Boolean](identity))

  val directional: String = "directional"
  /**
    * @return true if the string is in the Tokens.directions collection, false if not
    */
  def directionalAnalyser: CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.directions)

  val outcode: String = "outcode"
  /**
    * @return true if the string is in the Tokens.outcodes collection, false if not
    */
  def outcodeAnalyser: CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.outcodes)

  val postTown: String = "posttown"
  /**
    * @return true if the string is in the Tokens.postTown collection, false if not
    */
  def postTownAnalyser: CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.postTown)

  val flat: String = "flat"
  /**
    * @return true if the string is in the Tokens.flat collection, false if not
    */
  def flatAnalyser: CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.flat)

  val company: String = "company"
  /**
    * @return true if the string is in the Tokens.company collection, false if not
    */
  def companyAnalyser: CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.company)

  val road: String = "road"
  /**
    * @return true if the string is in the Tokens.road collection, false if not
    */
  def roadAnalyser: CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.road)

  val residential: String = "residential"
  /**
    * @return true if the string is in the Tokens.residential collection, false if not
    */
  def residentialAnalyser: CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.residential)

  val business: String = "business"
  /**
    * @return true if the string is in the Tokens.business collection, false if not
    */
  def businessAnalyser: CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.business)

  val locational: String = "locational"
  /**
    * @return true if the string is in the Tokens.locational collection, false if not
    */
  def locationalAnalyser: CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.locational)

  val ordinal: String = "ordinal"
  /**
    * @return true if the string is in the Tokens.ordinal collection, false if not
    */
  def ordinalAnalyser: CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.ordinal)

  val hyphenations: String = "hyphenations"
  /**
    * @return the count of characters which are '-'
    */
  def hyphenationsAnalyser: CrfFeatureAnalyser[Int] = CrfFeatureAnalyser[Int](_ count(_ == '-'))

  val digits: String = "digits"
  /**
    * @return a DigitLiteral String, which indicates if the string has all digits, contains digits or no digits
    */
  def digitsAnalyser: CrfFeatureAnalyser[String] = {
    CrfFeatureAnalyser[String] { str =>
      str.allDigits[String] { isAllDigits =>
        if (isAllDigits) {
          allDigits
        } else {
          str.containsDigits[String] { hasSomeDigits =>
            if (hasSomeDigits) {
              someDigits
            } else {
              noDigits
            }
          }
        }
      }
    }
  }

  /**
    * ref digitsAnalyser
    */
  object DigitsLiteral {
    val allDigits = "all_digits"
    val someDigits = "some_digits"
    val noDigits = "no_digits"
  }

  /**
    * Helper CrfFeatureAnalyser implementation
    * Use this analyser for using contains on a Sequence
    *
    * Eg:
    *     ContainsAnalyser(Seq("oneThingToLookFor", "AnotherThingToLookFor"))
    */
  object ContainsAnalyser {
    def apply(seq: Seq[String]): CrfFeatureAnalyser[Boolean] = CrfFeatureAnalyser[Boolean](seq contains _)
  }

  /**
    * Helper CrfAggregateFeatureAnalyser implementation
    * Use this analyser for aggregates.
    *
    * Eg:
    *    IndexAggregateFeatureAnalyser(_ => 0)
    */
  object IndexAggregateFeatureAnalyser {
    def apply(fn: Array[String] => Int): CrfAggregateFeatureAnalyser[Boolean] = CrfAggregateFeatureAnalyser[Boolean] { (tokens, token) =>
      tokens.indexOf(token) == fn(tokens)
    }
  }
}