package uk.gov.ons.addressIndex.crfscala

import uk.gov.ons.addressIndex.crfscala.CrfScala._
import uk.gov.ons.addressIndex.crfscala.jni.CrfScalaJni

//TODO scaladoc
trait CrfFeatures {

  /**
    * @return all the features
    */
  def features: Seq[CrfFeature[_]]

  def aggregateFeatures: Seq[CrfAggregateFeature[_]]

  //TODO scaladoc
  def toCrfJniInput(input: CrfToken, next: Option[CrfToken] = None, previous: Option[CrfToken] = None): CrfJniInput = {
    (features map(_.toCrfJniInput(input, next, previous)) mkString) + CrfScalaJni.lineEnd
  }

  /**
    * @param i the token to run against all feature analysers
    * @return the token and its results, as a pair
    */
  def analyse(i : CrfToken, next: Option[CrfToken] = None, previous: Option[CrfToken] = None): CrfTokenResult = {
    CrfTokenResult(
      token = i,
      next = next,
      previous = previous,
      results = features.map(f => f.name -> f.analyse(i)).toMap
    )
  }
}
