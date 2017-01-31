package uk.gov.ons.addressIndex.crfscala


object CrfAggregateFeatureAnalyser {

  /**
    * An aggregate analyser
    * @tparam T
    */
  type CrfAggregateFeatureAnalyser[T] = ((Array[String], String) => T)

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