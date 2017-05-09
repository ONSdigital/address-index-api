package uk.gov.ons.addressIndex.parsers

import com.typesafe.config.ConfigFactory
import org.apache.commons.lang3.StringUtils
import uk.gov.ons.addressIndex.crfscala.CrfAggregateFeatureAnalyser.CrfAggregateFeatureAnalyser
import uk.gov.ons.addressIndex.crfscala.CrfFeatureAnalyser.CrfFeatureAnalyser
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.crfscala.{CrfAggregateFeature, CrfFeature, CrfFeatures, CrfParser}

/**
  * AddressParser
  */
object AddressParser extends CrfParser {

  val currentDirectory = new java.io.File(".").getCanonicalPath
  val modelPath = s"$currentDirectory/$parserLibPath/addressCRFA.crfsuite"

  tagger.loadModel(modelPath)

  def tag(input: String): Seq[CrfTokenResult]  = {
    super.tag(StringUtils.stripAccents(input), FeatureAnalysers.allFeatures, Tokens)
  }
  def parse(input: String): String = {
    super.parse(input, FeatureAnalysers.allFeatures, Tokens)
  }

  override def parserLibPath: String = ConfigFactory.load.getString("addressIndex.parserLibPath")
}

case class FeaturesOld(override val features : Feature[_]*)(override val aggregateFeatures: FeatureAggregate[_]*) extends CrfFeatures

case class Feature[T](override val name: String)(override val analyser: CrfFeatureAnalyser[T]) extends CrfFeature[T]

case class FeatureAggregate[T](override val name: String)(override val analyser: CrfAggregateFeatureAnalyser[T]) extends CrfAggregateFeature[T]