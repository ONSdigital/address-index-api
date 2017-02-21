package uk.gov.ons.addressIndex.crfscala

import uk.gov.ons.addressIndex.crfscala.CrfAggregateFeatureAnalyser.CrfAggregateFeatureAnalyser

/**
  * A feature analyser over multiple tokens.
  * @tparam T the type which is returned.
  */
trait CrfAggregateFeature[T] {

  /**
    * @return a function which returns an instance of T
    */
  def analyser: CrfAggregateFeatureAnalyser[T]

  /**
    * @return name
    */
  def name: String

  /**
    * A helper function which applys the input to this function and produces a type of T
    *
    * @param token the token
    * @param tokens the tokens
    * @return the result of the CrfAggregateFeatureAnalyser (function) of type T
    */
  def analyse(token: String, tokens: Array[String]): T = analyser apply(tokens, token)
}