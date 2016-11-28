package uk.gov.ons.addressIndex.crfscala.jni

import uk.gov.ons.addressIndex.crfscala.CrfScala.{FeaturesResult, TokenResult}

trait CrfScalaJni {
  /**
    * @param location url of the .crfsuite file
    * @return true if the model file was successfully opened,
    *         false if not.
    */
  def modelOpen(location: String) : Boolean

  /**
    * @param input the input to tag
    * @return a crfsuite specific string which we can interpret as the results of tagging.
    */
  def tag(input : String) : String

  /**
    * @return all the labels available in the crfsuite model.
    */
  def modelLabels() : Array[String]
}

object Implicits {
  implicit class FeaturesResultToInputAugmenter(res : FeaturesResult) {
    def toTagInput() : String = {
      res.keys.mkString(" ")
    }
  }
  implicit class CRFSuiteSpecificStringToScalaAugmenter(taggingResult : String) {
    def toTagInput() : Seq[TokenResult] = {
      Seq(
        TokenResult(
          token = "exampleToken",
          input = "exampleInput"
        )
      )
    }
  }
}

class CrfScalaJniImpl extends CrfScalaJni {
  @native def modelOpen(location : String) : Boolean
  @native def tag(input : String) : String
  @native def modelLabels() : Array[String]
}