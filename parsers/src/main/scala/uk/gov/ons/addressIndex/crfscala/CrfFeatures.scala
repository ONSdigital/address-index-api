package uk.gov.ons.addressIndex.crfscala

import uk.gov.ons.addressIndex.crfscala.CrfScala._

//TODO scaladoc
trait CrfFeatures {

  /**
    * @return all the features
    */
  def features: Seq[CrfFeature[_]]

  //todo scaladoc
  def aggregateFeatures: Seq[CrfAggregateFeature[_]]

  //TODO scaladoc
  def toCrfJniInput(input: String, next: Option[String] = None, previous: Option[String] = None): String = {
    CrfScala.arbitraryString + (features map(_.toCrfJniInput(input, next, previous)) mkString) + CrfScalaJni.lineEnd
  }

  /**
    * @param i the token to run against all feature analysers
    * @return the token and its results, as a pair
    */
  def analyse(i : String, next: Option[String] = None, previous: Option[String] = None): Unit = {
//    CrfTokenResult(
//      token = i,
//      next = next,
//      previous = previous,
//      results = features.map(f => f.name -> f.analyse(i)).toMap
//    )
    ()
  }
}