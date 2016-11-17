package uk.gov.ons.addressIndex.parsers

import uk.gov.ons.addressIndex.crfscala.CrfScala.{FeatureAnalyser, _}
import uk.gov.ons.addressIndex.parsers.Implicits._
import Tokens._

/**
  * FeatureAnalyser implementations for the AddressParser
  */
object FeatureAnalysers {

  /**
    * Predefined items
    */
  object Predef {

    /**
      * @return all of the predefined features
      */
    def all() : Features = {
      Features(
        Feature[String](digits)(digitsAnalyser()),
        Feature[Boolean](word)(wordAnalyser()),
        Feature[String](length)(lengthAnalyser()),
        Feature[Boolean](endsInPunctuation)(endsInPunctuationAnalyser()),
        Feature[Boolean](directional)(directionalAnalyser()),
        Feature[Boolean](outcode)(outcodeAnalyser()),
        Feature[Boolean](postTown)(postTownAnalyser()),
        Feature[Boolean](hasVowels)(hasVowelsAnalyser()),
        Feature[Boolean](flat)(flatAnalyser()),
        Feature[Boolean](company)(companyAnalyser()),
        Feature[Boolean](road)(roadAnalyser()),
        Feature[Boolean](residential)(residentialAnalyser()),
        Feature[Boolean](business)(businessAnalyser()),
        Feature[Boolean](locational)(locationalAnalyser()),
        Feature[Int](hyphenations)(hyphenationsAnalyser())
      )
    }

    val word : FeatureName = "word"
    /**
      * @return true if the string is all digits, false if not
      */
    def wordAnalyser() : FeatureAnalyser[Boolean] = FeatureAnalyser[Boolean](_.allDigits[Boolean](!_))

    val length : FeatureName = "length"
    /**
      * @return the length of the string as a string
      */
    def lengthAnalyser() : FeatureAnalyser[String] = FeatureAnalyser[String](_.length.toString)

    val endsInPunctuation : FeatureName = "endsinpunc"
    /**
      * @return true if the last character of the string is a '.', false if not
      */
    def endsInPunctuationAnalyser() : FeatureAnalyser[Boolean] = FeatureAnalyser[Boolean](_.last == '.')

    val hasVowels : FeatureName = "has.vowels"
    /**
      * @return true if the string is in the Tokens.postTown collection, false if not
      */
    def hasVowelsAnalyser() : FeatureAnalyser[Boolean] = FeatureAnalyser[Boolean](_.containsVowels[Boolean](identity))

    val directional : FeatureName = "directional"
    /**
      * @return true if the string is in the Tokens.directions collection, false if not
      */
    def directionalAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.directions)

    val outcode : FeatureName = "outcode"
    /**
      * @return true if the string is in the Tokens.outcodes collection, false if not
      */
    def outcodeAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.outcodes)

    val postTown : FeatureName = "posttown"
    /**
      * @return true if the string is in the Tokens.postTown collection, false if not
      */
    def postTownAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.postTown)

    val flat : FeatureName = "flat"
    /**
      * @return true if the string is in the Tokens.flat collection, false if not
      */
    def flatAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.flat)

    val company : FeatureName = "company"
    /**
      * @return true if the string is in the Tokens.company collection, false if not
      */
    def companyAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.company)

    val road : FeatureName = "road"
    /**
      * @return true if the string is in the Tokens.road collection, false if not
      */
    def roadAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.road)

    val residential : FeatureName = "residential"
    /**
      * @return true if the string is in the Tokens.residential collection, false if not
      */
    def residentialAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.residential)

    val business : FeatureName = "business"
    /**
      * @return true if the string is in the Tokens.business collection, false if not
      */
    def businessAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.business)

    val locational : FeatureName = "locational"
    /**
      * @return true if the string is in the Tokens.locational collection, false if not
      */
    def locationalAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.locational)

    val ordinal : FeatureName = "ordinal"
    /**
      * @return true if the string is in the Tokens.ordinal collection, false if not
      */
    def ordinalAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.ordinal)

    val hyphenations : FeatureName = "hyphenations"
    /**
      * @return the count of characters which are '-'
      */
    def hyphenationsAnalyser(): FeatureAnalyser[Int] = FeatureAnalyser[Int](_ count(_ == '-'))

    val digits : FeatureName = "digits"
    /**
      * @return a DigitLiteral String, which indicates if the string has all digits, contains digits or no digits
      */
    def digitsAnalyser() : FeatureAnalyser[String] = {
      import DigitsLiteral._
      FeatureAnalyser[String] { str =>
        str.allDigits[String] { rs =>
          if(rs) {
            allDigits
          } else {
            str.containsDigits[String] { rs =>
              if(rs) {
                containsDigits
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
      val containsDigits = "contains_digits"
      val noDigits = "no_digits"
    }
  }

  /**
    * Helper FeatureAnalyser implementation
    * Use this analyser for using contains on a Sequence
    *
    * Eg:
    *     ContainsAnalyser(Seq("oneThingToLookFor", "AnotherThingToLookFor"))
    */
  object ContainsAnalyser {
    def apply(tis : Seq[TokenIndicator]) : FeatureAnalyser[Boolean] = FeatureAnalyser[Boolean](tis contains _)
  }
}