package uk.gov.ons.addressIndex.crfscala

import com.github.jcrfsuite.CrfTagger
import third_party.org.chokkan.crfsuite.{Attribute, Item}
import uk.gov.ons.addressIndex.parsers.Tokens
import uk.gov.ons.addressIndex.parsers.Tokens.Token

import collection.JavaConverters._
import scala.util.control.NonFatal

/**
  * scala wrapper of crfsuite
  *
  * todo describe this more
  */
object CrfScala {
  type Input = String
  type FeatureName = String
  type FeatureSequence = third_party.org.chokkan.crfsuite.ItemSequence
  type Tagger = CrfTagger

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
  abstract trait CrfType[T] {
    def value : T
  }

  //TODO scaladoc
  trait CrfParser {
    //TODO scaladoc
    def parse(i : Input, fa : CrfFeatures)(implicit tagger : Tagger) : List[TokenResult] = {
      val tokens = Tokens(i)
      val fs = new FeatureSequence()
      val preprocessedTokens = Tokens normalise tokens
      for (token <- preprocessedTokens) {
        fs add(fa toItem token)
      }
      val r = tagger tag fs
      r.asScala.zipWithIndex.map { p =>
        TokenResult(
          token = p._1.first,
          input = tokens(p._2)
        )
      } toList
    }
  }


  /**
    * @param token the token which the crfsuite identified
    * @param input the input which produced this token
    */
  case class TokenResult(token : Token, input : Input)

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
      createAttribute(v)
    }

    def createAttribute(someValue : Any) : Attribute = {
      someValue match {
        case _ : String =>
          new Attribute(s"$name=$someValue")

        case _ : Double =>
          new Attribute(name, someValue.asInstanceOf[Double])

        case _ : Int =>
          new Attribute(name, Int int2double someValue.asInstanceOf[Int])

        case _ : Boolean =>
          if(someValue.asInstanceOf[Boolean]) {
            new Attribute(name, 1d)
          } else {
            new Attribute(name, 0d)
          }

        case t : CrfType[_] =>
          createAttribute(t.value)

        case NonFatal(e) =>
          throw e

        case _ =>
          throw new UnsupportedOperationException(
            s"Unsupported input to crf Attribute: ${someValue.getClass.toString} or Feature with name: $name")
      }
    }
  }

}