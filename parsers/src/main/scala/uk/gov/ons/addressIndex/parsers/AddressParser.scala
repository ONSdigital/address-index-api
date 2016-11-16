package uk.gov.ons.addressIndex.parsers

import Tokens._
import uk.gov.ons.addressIndex.crfscala.CrfScala._
import uk.gov.ons.addressIndex.parsers.Implicits._

object AddressParser extends CrfParser {

  object FeatureAnalysers {

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
      def wordAnalyser() : FeatureAnalyser[Boolean] = FeatureAnalyser[Boolean](_.allDigits[Boolean](!_))

      val length : FeatureName = "length"
      def lengthAnalyser() : FeatureAnalyser[String] = FeatureAnalyser[String](_.length.toString)

      val endsInPunctuation : FeatureName = "endsinpunc"
      def endsInPunctuationAnalyser() : FeatureAnalyser[Boolean] = FeatureAnalyser[Boolean](_.last == '.')

      val directional : FeatureName = "directional"
      def directionalAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.DIRECTIONS)

      val outcode : FeatureName = "outcode"
      def outcodeAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.OUTCODES)

      val postTown : FeatureName = "posttown"
      def postTownAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.POST_TOWN)

      val hasVowels : FeatureName = "has.vowels"
      def hasVowelsAnalyser() : FeatureAnalyser[Boolean] = FeatureAnalyser[Boolean](_.containsVowels[Boolean](identity))

      val flat : FeatureName = "flat"
      def flatAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.FLAT)

      val company : FeatureName = "company"
      def companyAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.COMPANY)

      val road : FeatureName = "road"
      def roadAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.ROAD)

      val residential : FeatureName = "residential"
      def residentialAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.RESIDENTIAL)

      val business : FeatureName = "business"
      def businessAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.BUSINESS)

      val locational : FeatureName = "locational"
      def locationalAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.LOCATIONAL)

      val ordinal : FeatureName = "ordinal"
      def ordinalAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.ORIDINAL)

      val hyphenations : FeatureName = "hyphenations"
      def hyphenationsAnalyser(): FeatureAnalyser[Int] = FeatureAnalyser[Int](_ count(_ == '-'))

      val digits : FeatureName = "digits"

      /**
        * @return a DigitLiteral String
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
      *     ContainsAnalyser(Seq("oneThingToLookFoor", "AnotherThingToLookFor"))
      */
    object ContainsAnalyser {
      def apply(tis : Seq[TokenIndicator]) : FeatureAnalyser[Boolean] = FeatureAnalyser[Boolean](tis contains _)
    }
  }
}

/**
  * Feature collection
  *
  * scala wrapper on third_party.org.chokkan.crfsuite.Item
  *
  * @param all the features of this feature collection
  */
case class Features(override val all : Feature[_]*) extends CrfFeatures

/**
  * scala wrapper on third_party.org.chokkan.crfsuite.Attribute
  *
  * @param name the feature's key which is referenced in them jcrfsuite model
  *
  * @param analyser feature analyser
  *
  * @tparam T the return type of this analyser; used for the conversion to an Item
  */
case class Feature[T](override val name : String)(override val analyser : FeatureAnalyser[T]) extends CrfFeature[T]