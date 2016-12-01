package uk.gov.ons.addressIndex.crfscala

import uk.gov.ons.addressIndex.crfscala.CrfScala._

/**
  * scala wrapper of third_party.org.chokkan.crfsuite.Item
  */
trait CrfFeatures {

  /**
    * @return all the features
    */
  def all: Seq[CrfFeature[_]]

  //TODO scaladoc
  def toCrfJniInput(input: CrfToken, next: Option[CrfToken] = None, previous: Option[CrfToken] = None): CrfJniInput = {
    all map(_.toCrfJniInput(input, next, previous)) mkString
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
      results = all.map(f => f.name -> f.analyse(i)).toMap
    )
  }
}
