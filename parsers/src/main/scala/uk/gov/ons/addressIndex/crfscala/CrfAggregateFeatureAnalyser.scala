package uk.gov.ons.addressIndex.crfscala

import uk.gov.ons.addressIndex.crfscala.CrfScala.{CrfToken, CrfTokens}

object CrfAggregateFeatureAnalyser {

  /**
    * An aggregate analyser
    * @tparam T
    */
  type CrfAggregateFeatureAnalyser[T] = ((CrfTokens, CrfToken) => T)

  /**
    * Helper apply method for better syntax.
    * Constructs a function.
    * Eg Impl:
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
  def apply[T](analyser : CrfAggregateFeatureAnalyser[T]) : CrfAggregateFeatureAnalyser[T] = analyser
}