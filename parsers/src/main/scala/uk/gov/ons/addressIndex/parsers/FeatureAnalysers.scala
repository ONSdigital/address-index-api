package uk.gov.ons.addressIndex.parsers

import uk.gov.ons.addressIndex.parsers.Implicits._
import Tokens._
import uk.gov.ons.addressIndex.crfscala.CrfAggregateFeatureAnalyser.{apply => _, _}
import uk.gov.ons.addressIndex.crfscala.{CrfAggregateFeatureAnalyser, CrfFeatureAnalyser}
import uk.gov.ons.addressIndex.crfscala.CrfFeatureAnalyser.CrfFeatureAnalyser
import uk.gov.ons.addressIndex.crfscala.CrfScala.{CrfTokens, CrfType, FeatureName}
import uk.gov.ons.addressIndex.parsers.FeatureAnalysers.ADT.Root

/**
  * FeatureAnalyser implementations for the AddressParser
  */
object FeatureAnalysers {

  /**
    * @return all of the predefined features
    */
  def allFeatures(): Features = {
    Features(
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
      //TODO Impl aggr feature to crfJniInput
      FeatureAggregate[Boolean](rawStringStart)(rawStringStartAggrAnalyser),
      FeatureAggregate[Boolean](rawStringEnd)(rawStringEndAggrAnalyser),
      FeatureAggregate[Boolean](singleton)(singletonAggr)
    )
  }

  object ADT {
    trait Root[T] extends CrfType[T] {
      def value: T
    }
    implicit class MyString(override val value: String) extends Root[String]
    implicit class MyBoolean(override val value: Boolean) extends Root[Boolean]
  }

  //TODO scaladoc
  val rawStringStart: FeatureName  = "rawstring.start"
  def rawStringStartAggrAnalyser: CrfAggregateFeatureAnalyser[Boolean] = IndexAggregateFeatureAnalyser(_ => 0)

  val rawStringEnd: FeatureName  = "rawstring.end"
  def rawStringEndAggrAnalyser: CrfAggregateFeatureAnalyser[Boolean] = IndexAggregateFeatureAnalyser(_.length - 1)

  val singleton: FeatureName  = "singleton"
  def singletonAggr: CrfAggregateFeatureAnalyser[Boolean] = CrfAggregateFeatureAnalyser[Boolean] { (tokens, token) =>
    tokens.length == 1 && tokens(0) == token
  }

  val word: FeatureName = "word"
  /**
    * @return true if the string is all digits, false if not
    */
  def wordAnalyser: CrfFeatureAnalyser[Root[_]] = CrfFeatureAnalyser[Root[_]] { str =>
    str.allDigits[Root[_]] { isAllDigits =>
      if(isAllDigits) {
        false
      } else {
        str
      }
    }
  }

  val length: FeatureName = "length"
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

  val endsInPunctuation: FeatureName = "endsinpunc"
  /**
    * @return true if the last character of the string is a '.', false if not
    */
  def endsInPunctuationAnalyser: CrfFeatureAnalyser[Boolean] = CrfFeatureAnalyser[Boolean](_.last == '.')

  val hasVowels: FeatureName = "has.vowels"
  /**
    * @return true if the string contains vowels, false if not
    */
  def hasVowelsAnalyser: CrfFeatureAnalyser[Boolean] = CrfFeatureAnalyser[Boolean](_.containsVowels[Boolean](identity))

  val directional: FeatureName = "directional"
  /**
    * @return true if the string is in the Tokens.directions collection, false if not
    */
  def directionalAnalyser: CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.directions)

  val outcode: FeatureName = "outcode"
  /**
    * @return true if the string is in the Tokens.outcodes collection, false if not
    */
  def outcodeAnalyser: CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.outcodes)

  val postTown: FeatureName = "posttown"
  /**
    * @return true if the string is in the Tokens.postTown collection, false if not
    */
  def postTownAnalyser: CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.postTown)

  val flat: FeatureName = "flat"
  /**
    * @return true if the string is in the Tokens.flat collection, false if not
    */
  def flatAnalyser: CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.flat)

  val company: FeatureName = "company"
  /**
    * @return true if the string is in the Tokens.company collection, false if not
    */
  def companyAnalyser: CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.company)

  val road: FeatureName = "road"
  /**
    * @return true if the string is in the Tokens.road collection, false if not
    */
  def roadAnalyser: CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.road)

  val residential: FeatureName = "residential"
  /**
    * @return true if the string is in the Tokens.residential collection, false if not
    */
  def residentialAnalyser: CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.residential)

  val business: FeatureName = "business"
  /**
    * @return true if the string is in the Tokens.business collection, false if not
    */
  def businessAnalyser: CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.business)

  val locational: FeatureName = "locational"
  /**
    * @return true if the string is in the Tokens.locational collection, false if not
    */
  def locationalAnalyser: CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.locational)

  val ordinal: FeatureName = "ordinal"
  /**
    * @return true if the string is in the Tokens.ordinal collection, false if not
    */
  def ordinalAnalyser: CrfFeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.ordinal)

  val hyphenations: FeatureName = "hyphenations"
  /**
    * @return the count of characters which are '-'
    */
  def hyphenationsAnalyser: CrfFeatureAnalyser[Int] = CrfFeatureAnalyser[Int](_ count(_ == '-'))

  val digits: FeatureName = "digits"
  /**
    * @return a DigitLiteral String, which indicates if the string has all digits, contains digits or no digits
    */
  def digitsAnalyser: CrfFeatureAnalyser[String] = {
    import DigitsLiteral._
    CrfFeatureAnalyser[String] { str =>
      str.allDigits[String] { rs =>
        if(rs) {
          allDigits
        } else {
          str.containsDigits[String] { rs =>
            if(rs) {
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
    * Helper FeatureAnalyser implementation
    * Use this analyser for using contains on a Sequence
    *
    * Eg:
    *     ContainsAnalyser(Seq("oneThingToLookFor", "AnotherThingToLookFor"))
    */
  object ContainsAnalyser {
    def apply(tis: Seq[TokenIndicator]): CrfFeatureAnalyser[Boolean] = CrfFeatureAnalyser[Boolean](tis contains _)
  }

  //TODO Scaladoc
  object IndexAggregateFeatureAnalyser {
    def apply(fn: CrfTokens => Int): CrfAggregateFeatureAnalyser[Boolean] = CrfAggregateFeatureAnalyser[Boolean] { (tokens, token) =>
      tokens.indexOf(token) == fn(tokens)
    }
  }
}