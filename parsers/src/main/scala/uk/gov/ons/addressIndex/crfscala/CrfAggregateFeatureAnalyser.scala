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
    *    CrfAggregateFeatureAnalyser[Int]("indexOfToken") { (tokens, token) =>
    *       tokens.indexOf(token)
    *    }
    */
  def apply[T](analyser: CrfAggregateFeatureAnalyser[T]): CrfAggregateFeatureAnalyser[T] = analyser
}