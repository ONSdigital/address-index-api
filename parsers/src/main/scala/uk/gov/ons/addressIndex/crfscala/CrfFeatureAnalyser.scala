package uk.gov.ons.addressIndex.crfscala

import uk.gov.ons.addressIndex.crfscala.CrfScala._

object CrfFeatureAnalyser {
  type CrfFeatureAnalyser[T] = (Input => T)

  /**
    * Helper apply method for better syntax.
    * Constructs a function.
    * Eg:
    *
    *    CrfFeatureAnalyser[String]("SplitOnSpaceCountAsStr") { str =>
    *       str.split(" ").length.toString
    *    }
    *
    * Or:
    *
    *    CrfFeatureAnalyser[Int]("lengthOfString") { str =>
    *       str.length
    *    }
    *
    */
  def apply[T](analyser : CrfFeatureAnalyser[T]) : CrfFeatureAnalyser[T] = analyser
}