package uk.gov.ons.addressIndex.crfscala

import com.github.jcrfsuite.CrfTagger
import third_party.org.chokkan.crfsuite.{Attribute, Item}
import uk.gov.ons.addressIndex.parsers.Tokens

import scala.util.control.NonFatal

/**
  * scala wrapper of crfsuite
  */
object CrfScala {

  type Input = String
  type FeatureName = String
  type FeatureSequence = third_party.org.chokkan.crfsuite.ItemSequence
  type Tagger = CrfTagger

  type FeatureAnalyser[T] = (Input => T)
  object FeatureAnalyser {
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
    def apply[T](analyser : FeatureAnalyser[T]) : FeatureAnalyser[T] = analyser
  }

  /**
    *
    */
  trait CrfParser {
    //TODO still defining output
    def parse(i : Input, fa : CrfFeatures) = {
      val tagger = new Tagger("/Users/rhysbradbury/Downloads/addressCRF.crfsuite")
      val tokens = Tokens(i)
      val fs = new FeatureSequence()

      for (token <- tokens) {
        fs add(fa toItem token)
      }
      tagger tag fs
    }
  }

  /**
    * scala wrapper of third_party.org.chokkan.crfsuite.Item
    */
  trait CrfFeatures {

    /**
      * @return all the features
      */
    def all : Seq[CrfFeature[_]]

    /**
      * @param i input
      * @return the features as an Item
      */
    def toItem(i : Input) : Item = {
      val item = new Item()
      for(feature <- all) {
        item.add(feature toAttribute i)
      }
      item
    }
  }

  /**
    * scala wrapper of third_party.org.chokkan.crfsuite.Attribute
    *
    * @tparam T the return type of the FeatureAnalyser
    */
  trait CrfFeature[T] {

    /**
      * @return a function which returns an instance of T
      */
    def analyser() : FeatureAnalyser[T]

    /**
      * @return name
      */
    def name() : String

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
      * scala wrapper of third_party.org.chokkan.crfsuite.Attribute
      *
      * do not change this without speaking to Rhys Bradbury
      * please be aware that the Attribute source has no JavaDoc
      *
      * depending on T (the return type fo the analyser) we construct Attribute differently
      *
      * @param i input
      * @return wrapper of third_party.org.chokkan.crfsuite.Attribute
      */
    def toAttribute(i : Input) : Attribute = {
      val v = analyse(i)
      v match {
        case _ : String =>
          new Attribute(s"$name=$v")

        case _ : Double =>
          new Attribute(name, v.asInstanceOf[Double])

        case _ : Int =>
          new Attribute(name, Int int2double v.asInstanceOf[Int])

        case _ : Boolean =>
          if(v.asInstanceOf[Boolean]) {
            new Attribute(name, 1d)
          } else {
            new Attribute(name, 0d)
          }

        case NonFatal(e) =>
          throw e

        case _ =>
          throw new UnsupportedOperationException(
            s"Unsupported input to crf Attribute: ${analyse(i).getClass.toString} or Feature with name: $name")
      }
    }
  }
}