package uk.gov.ons.addressIndex.parsers

import com.github.jcrfsuite.CrfTagger
import Tokens._
import third_party.org.chokkan.crfsuite.{Attribute, Item, ItemSequence}
import uk.gov.ons.addressIndex.parsers.AddressParser.{FeatureAnalyser, Input}

object AddressParser {

  type Input = String
  type FeatureName = String
  type FeatureAnalyser[T] = (Input => T)

  object FeatureAnalyser {

    /**
      * For example:
      *
      *   import FeatureAnalyser.Predef._
      *
      *   Feature[String](DIGITS)(digitsAnalyser())
      *
      * @param analyser
      * @tparam T
      * @return
      */
    def apply[T](analyser : FeatureAnalyser[T]) = analyser

    object Predef {

      /**
        * @return all of the predefined features
        */
      def features() : Features = {
        Features(
          Feature[String](DIGITS)(digitsAnalyser()),
          Feature[Boolean](WORD)(wordAnalyser()),
          Feature[String](LENGTH)(lengthAnalyser()),
          Feature[Boolean](ENDS_IN_PUNCTUATION)(endsInPunctuationAnalyser()),
          Feature[Boolean](DIRECTIONAL)(directionalAnalyser()),
          Feature[Boolean](OUTCODE)(outcodeAnalyser()),
          Feature[Boolean](POST_TOWN)(postTownAnalyser()),
          Feature[Boolean](HAS_VOWELS)(hasVowelsAnalyser()),
          Feature[Boolean](FLAT)(flatAnalyser()),
          Feature[Boolean](COMPANY)(companyAnalyser()),
          Feature[Boolean](ROAD)(roadAnalyser()),
          Feature[Boolean](RESIDENTIAL)(residentialAnalyser()),
          Feature[Boolean](BUSINESS)(businessAnalyser()),
          Feature[Boolean](LOCATIONAL)(locationalAnalyser()),
          Feature[Int](HYPHENATIONS)(hyphenationsAnalyser())
        )
      }

      val WORD : FeatureName = "word"
      def wordAnalyser() : FeatureAnalyser[Boolean] = FeatureAnalyser[Boolean](_.allDigits[Boolean](!_))

      val LENGTH : FeatureName = "length"
      def lengthAnalyser() : FeatureAnalyser[String] = FeatureAnalyser[String](_.length.toString)

      val ENDS_IN_PUNCTUATION : FeatureName = "endsinpunc"
      def endsInPunctuationAnalyser() : FeatureAnalyser[Boolean] = FeatureAnalyser[Boolean](_.last == '.')

      val DIRECTIONAL : FeatureName = "directional"
      def directionalAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.DIRECTIONS)

      val OUTCODE : FeatureName = "outcode"
      def outcodeAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.OUTCODES)

      val POST_TOWN : FeatureName = "posttown"
      def postTownAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.POST_TOWN)

      val HAS_VOWELS : FeatureName = "has.vowels"
      def hasVowelsAnalyser() : FeatureAnalyser[Boolean] = FeatureAnalyser[Boolean](_.containsVowels[Boolean](identity))

      val FLAT : FeatureName = "flat"
      def flatAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.FLAT)

      val COMPANY : FeatureName = "company"
      def companyAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.COMPANY)

      val ROAD : FeatureName = "road"
      def roadAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.ROAD)

      val RESIDENTIAL : FeatureName = "residential"
      def residentialAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.RESIDENTIAL)

      val BUSINESS : FeatureName = "business"
      def businessAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.BUSINESS)

      val LOCATIONAL : FeatureName = "locational"
      def locationalAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.LOCATIONAL)

      val ORDINAL : FeatureName = "ordinal"
      def ordinalAnalyser() : FeatureAnalyser[Boolean] = ContainsAnalyser(Tokens.ORIDINAL)

      val HYPHENATIONS : FeatureName = "hyphenations"
      def hyphenationsAnalyser() : FeatureAnalyser[Int] = FeatureAnalyser[Int](_ count(_ == '-'))

      val DIGITS : FeatureName = "digits"

      /**
        * @return a DigitLiteral String
        */
      def digitsAnalyser(): FeatureAnalyser[String] = {
        import DigitsLiteral._
        FeatureAnalyser[String] { str =>
          str.allDigits[String] { rs =>
            if(rs) {
              ALL_DIGITS
            } else {
              str.containsDigits[String] { rs =>
                if(rs) {
                  CONTAINS_DIGITS
                } else {
                  NO_DIGITS
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
        val ALL_DIGITS = "all_digits"
        val CONTAINS_DIGITS = "contains_digits"
        val NO_DIGITS = "no_digits"
      }
    }
  }

  object ContainsAnalyser {
    def apply(tis : Seq[TokenIndicator]) : FeatureAnalyser[Boolean] = FeatureAnalyser[Boolean](tis contains _)
  }

  //TODO move to string utils on utils proj?
  implicit class StringUtils(str : String) {

    val DIGITS : List[String] = (0 to 9).toList map(_.toString)

    def allDigits[T](fn : (Boolean => T)) : T = {
      //TODO rename me
      val length1eqTrueIfAllDigits : List[Boolean] = DIGITS.map(str contains _).distinct
      val allDigits : Boolean = length1eqTrueIfAllDigits.length == 1 && length1eqTrueIfAllDigits.head
      fn(allDigits)
    }

    def containsDigits[T](fn : (Boolean => T)) : T = fn(DIGITS exists(str contains _))

    val VOWELS : List[Char] = List('a', 'e', 'i', 'o', 'u')

    def containsVowels[T](fn : (Boolean => T)) : T = fn(VOWELS exists(str contains _))
  }

  def parse(i : Input) = {
    val tagger = new CrfTagger("/Users/rhysbradbury/Downloads/addressCRF.crfsuite")
    val tokens = Tokens(i)

    val itemSeq = new ItemSequence()
    val features = FeatureAnalyser.Predef.features

    for (token <- tokens) {
      itemSeq.add(features toItem token)
    }

    val res = tagger.tag(itemSeq)

    pprint.pprintln(res)
  }
}

/**
  * Feature collection
  *
  * scala wrapper on third_party.org.chokkan.crfsuite.Item
  *
  * @param features the features of this feature collection
  */
case class Features(features : Feature[_]*) {

  /**
    * @param i
    * @return the features as an Item
    *
    */
  def toItem(i : Input) : Item = {
    val item = new Item()
    for(feature <- features) {
      item.add(feature toAttribute i)
    }
    item
  }
}


trait CrfFeature {
  
}

/**
  * scala wrapper on third_party.org.chokkan.crfsuite.Attribute
  *
  * @param key the feature's key which is referenced in them jcrfsuite model
  *
  * @param analyser feature analyser
  *
  * @tparam T the return type of this analyser; used for the conversion to an Item
  */
case class Feature[T](key : String)(analyser : FeatureAnalyser[T]) {

  /**
    * The return type of this features analyser
    */
  type value = T

  /**
    * @param i input
    * @return apply the analyser to i
    */
  def analyse(i : Input) : T = analyser apply i

  /**
    * Do not change this without speaking to Rhys Bradbury
    * Please be aware that the Attribute source has no JavaDoc
    *
    * Depending on T (the return type fo the analyser) we construct
    * the Attribute differently
    *
    * @param i input
    * @return
    */
  def toAttribute(i : Input) : Attribute = {
    val v = analyse(i)
    v match {

      case _ : String =>
        new Attribute(s"$key=$v")

      case _ : Double =>
        new Attribute(key, v.asInstanceOf[Double])

      case _ : Int =>
        new Attribute(key, Int int2double v.asInstanceOf[Int])

      case _ : Boolean =>
        if(v.asInstanceOf[Boolean]) {
          new Attribute(key, 1d)
        } else {
          new Attribute(key, 0d)
        }

      case _ =>
        throw new UnsupportedOperationException(
          s"Unsupported input to crf Attribute: ${analyse(i).getClass.toString} " +
            s"for Feature with key $key"
        )
    }
  }
}