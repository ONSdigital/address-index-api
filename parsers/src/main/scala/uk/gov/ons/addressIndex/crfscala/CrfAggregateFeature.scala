package uk.gov.ons.addressIndex.crfscala

import uk.gov.ons.addressIndex.crfscala.CrfAggregateFeatureAnalyser.CrfAggregateFeatureAnalyser
import uk.gov.ons.addressIndex.crfscala.CrfScala.{CrfToken, CrfTokens}

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

  def analyse(token: CrfToken, tokens: CrfTokens): T = analyser apply(tokens, token)
}