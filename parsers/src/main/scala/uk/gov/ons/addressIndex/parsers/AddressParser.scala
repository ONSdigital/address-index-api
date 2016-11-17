package uk.gov.ons.addressIndex.parsers

import uk.gov.ons.addressIndex.crfscala.CrfScala._

//TODO scaladoc
/**
  * AddressParser
  */
object AddressParser extends CrfParser {
  def parse(i : Input, fa : CrfFeatures) : List[TokenResult] = {
    implicit val tagger = new Tagger("/Users/rhysbradbury/Downloads/addressCRF.crfsuite")
    super.parse(i, fa)
  }
}

/**
  * Feature collection
  *
  * scala wrapper on third_party.org.chokkan.crfsuite.Item
  *
  * @param all the features of this feature collection
  */
case class Features(override val all : Feature[_]*) extends CrfFeatures

/**
  * scala wrapper on third_party.org.chokkan.crfsuite.Attribute
  *
  * @param name the feature's key which is referenced in them jcrfsuite model
  *
  * @param analyser feature analyser
  *
  * @tparam T the return type of this analyser; used for the conversion to an Item
  */
case class Feature[T](override val name : String)(override val analyser : CrfFeatureAnalyser[T]) extends CrfFeature[T]