package uk.gov.ons.addressIndex.crfscala

import uk.gov.ons.addressIndex.crfscala.CrfAggregateFeatureAnalyser.CrfAggregateFeatureAnalyser

//todo scaladoc
trait CrfAggregateFeature[T] {

  /**
    * @return a function which returns an instance of T
    */
  def analyser: CrfAggregateFeatureAnalyser[T]

  /**
    * @return name
    */
  def name: String

  //todo scaladoc
  def analyse(token: String, tokens: Array[String]): T = analyser apply(tokens, token)
}