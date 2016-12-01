package uk.gov.ons.addressIndex.crfscala.jni

import uk.gov.ons.addressIndex.crfscala.CrfScala.{FeaturesResult, CrfTokenResult}

/**
  * CrfScalaJni is an interface which should be implemented with native methods.
  */
trait CrfScalaJni {
  /**
    * @param modelPath String path to model
    * @param items the input to tag
    * @return a crfsuite specific string which we can interpret as the results of tagging.
    */
  def tag(modelPath: String, items : String) : String
}
//todo scaladoc
object Implicits {
  implicit class FeaturesResultToInputAugmenter(res : FeaturesResult) {
    implicit def toTagInput() : String = {
      res.keys.mkString(" ")
    }
  }
  implicit class CRFSuiteSpecificStringToScalaAugmenter(taggingResult : String) {
    implicit def toTokenResult() : Seq[CrfTokenResult] = {
      Seq.empty[CrfTokenResult]
    }
  }
}

//todo scaladoc
object CrfScalaJni {
  val tab = "\t"
  val newLine = "\n"
  val lineStart = tab
  val delimiter = tab
  val lineEnd = newLine
  val previous = "previous\\:"
  val next = "next\\:"
}

/**
  * This is the native implementation of CrfScalaJni.
  */
class CrfScalaJniImpl extends CrfScalaJni {
  @native def tag(modelPath: String, items : String) : String
}