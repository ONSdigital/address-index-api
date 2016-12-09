package uk.gov.ons.addressIndex.parsers

import uk.gov.ons.addressIndex.crfscala.CrfAggregateFeatureAnalyser.CrfAggregateFeatureAnalyser
import uk.gov.ons.addressIndex.crfscala.CrfFeatureAnalyser.CrfFeatureAnalyser
import uk.gov.ons.addressIndex.crfscala.{CrfAggregateFeature, CrfFeature, CrfFeatures, CrfParser}

//TODO scaladoc
/**
  * AddressParser
  */
object AddressParser extends CrfParser {
  def tag(i: String): String = {
    super.tag(i, FeatureAnalysers.allFeatures, Tokens)
  }
  def parse(i: String): String = {
    super.parse(i, FeatureAnalysers.allFeatures, Tokens)
  }
}

case class Features(override val features : Feature[_]*)(override val aggregateFeatures: FeatureAggregate[_]*) extends CrfFeatures

case class Feature[T](override val name: String)(override val analyser: CrfFeatureAnalyser[T]) extends CrfFeature[T]

case class FeatureAggregate[T](override val name: String)(override val analyser: CrfAggregateFeatureAnalyser[T]) extends CrfAggregateFeature[T]