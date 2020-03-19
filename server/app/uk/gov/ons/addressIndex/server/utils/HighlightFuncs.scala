package uk.gov.ons.addressIndex.server.utils

import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseAddress, AddressResponseAddressEQ, AddressResponseHighlight, AddressResponseHighlightHit}

object HighlightFuncs {

  def getBestMatchAddress(highlights: Option[AddressResponseHighlight], favourPaf: Boolean = true, favourWelsh: Boolean = false): String =
  {

    highlights match {
      case Some(value) => AddressResponseAddress.removeConcatenatedPostcode(AddressResponseAddress.removeEms(determineBestMatchAddress(value, favourPaf, favourWelsh)))
      case None => ""
    }
  }

  def getSource(highlights: Option[AddressResponseHighlight], favourPaf: Boolean = true, favourWelsh: Boolean = false): String =
  {

    highlights match {
      case Some(value) => AddressResponseAddress.removeConcatenatedPostcode(AddressResponseAddress.removeEms(determineSource(value, favourPaf, favourWelsh)))
      case None => ""
    }
  }

  def getLang(highlights: Option[AddressResponseHighlight], favourPaf: Boolean = true, favourWelsh: Boolean = false): String =
  {

    highlights match {
      case Some(value) => AddressResponseAddress.removeConcatenatedPostcode(AddressResponseAddress.removeEms(determineLang(value, favourPaf, favourWelsh)))
      case None => ""
    }
  }

  def determineBestMatchAddress(highlight: AddressResponseHighlight, favourPaf: Boolean, favourWelsh: Boolean): String =
  {
    val highs = sortHighs(highlight.hits.getOrElse(Seq()), favourPaf, favourWelsh)
    highs.headOption.map(_.highLightedText).getOrElse("")
  }

  def determineSource(highlight: AddressResponseHighlight, favourPaf: Boolean, favourWelsh: Boolean): String =
  {
    val highs = sortHighs(highlight.hits.getOrElse(Seq()), favourPaf, favourWelsh)
    highs.headOption.map(_.source).getOrElse("")
  }

  def determineLang(highlight: AddressResponseHighlight, favourPaf: Boolean, favourWelsh: Boolean): String =
  {
    val highs = sortHighs(highlight.hits.getOrElse(Seq()), favourPaf, favourWelsh)
    highs.headOption.map(_.lang).getOrElse("")
  }

  def sortHighs(hits: Seq[AddressResponseHighlightHit], favourPaf: Boolean, favourWelsh: Boolean): Seq[AddressResponseHighlightHit] =
  {
    if (favourPaf) {
      if (favourWelsh)
        hits.sortBy(_.source)(Ordering[String].reverse).sortBy(_.lang)(Ordering[String].reverse).sortBy(_.distinctHitCount)(Ordering[Int].reverse)
      else
        hits.sortBy(_.source)(Ordering[String].reverse).sortBy(_.lang).sortBy(_.distinctHitCount)(Ordering[Int].reverse)
    } else {
      if (favourWelsh)
        hits.sortBy(_.source).sortBy(_.lang)(Ordering[String].reverse).sortBy(_.distinctHitCount)(Ordering[Int].reverse)
      else
        hits.sortBy(_.source).sortBy(_.lang).sortBy(_.distinctHitCount)(Ordering[Int].reverse)
    }
  }

  def boostAddress(add: AddressResponseAddress, input: String, favourPaf: Boolean, favourWelsh: Boolean, highVerbose: Boolean): AddressResponseAddress = {
    if (add.formattedAddress.toUpperCase().replaceAll("[,]", "").startsWith(input.toUpperCase().replaceAll("[,]", ""))) {
      add.copy(
        confidenceScore = (math.round(add.underlyingScore)*5).min(100),
        underlyingScore = add.underlyingScore,
        highlights = if (add.highlights.isEmpty) None else Option(add.highlights.get.copy(
          bestMatchAddress = getBestMatchAddress(add.highlights, favourPaf, favourWelsh),
          source = getSource(add.highlights, favourPaf, favourWelsh),
          lang = getLang(add.highlights, favourPaf, favourWelsh),
          hits = if (highVerbose) Option(sortHighs(add.highlights.get.hits.getOrElse(Seq()), favourPaf, favourWelsh)) else None)))
    } else add.copy(
      confidenceScore = (math.round(add.underlyingScore)*5).min(100),
      underlyingScore = add.underlyingScore,
      highlights = if (add.highlights.isEmpty) None else Option(add.highlights.get.copy(
        bestMatchAddress = getBestMatchAddress(add.highlights, favourPaf, favourWelsh),
        source = getSource(add.highlights, favourPaf, favourWelsh),
        lang = getLang(add.highlights, favourPaf, favourWelsh),
        hits = if (highVerbose) Option(sortHighs(add.highlights.get.hits.getOrElse(Seq()), favourPaf, favourWelsh)) else None)))
  }


  def boostAddress(add: AddressResponseAddressEQ, input: String, favourPaf: Boolean, favourWelsh: Boolean, highVerbose: Boolean): AddressResponseAddressEQ = {
    if (add.formattedAddress.toUpperCase().replaceAll("[,]", "").startsWith(input.toUpperCase().replaceAll("[,]", ""))) {
      add.copy(
        confidenceScore = (math.round(add.underlyingScore)*5).min(100),
        underlyingScore = add.underlyingScore,
        highlights = if (add.highlights.isEmpty) None else Option(add.highlights.get.copy(
          bestMatchAddress = getBestMatchAddress(add.highlights, favourPaf, favourWelsh),
          source = getSource(add.highlights, favourPaf, favourWelsh),
          lang = getLang(add.highlights, favourPaf, favourWelsh),
          hits = if (highVerbose) Option(sortHighs(add.highlights.get.hits.getOrElse(Seq()), favourPaf, favourWelsh)) else None)))
    } else add.copy(
      confidenceScore = (math.round(add.underlyingScore)*5).min(100),
      underlyingScore = add.underlyingScore,
      highlights = if (add.highlights.isEmpty) None else Option(add.highlights.get.copy(
        bestMatchAddress = getBestMatchAddress(add.highlights, favourPaf, favourWelsh),
        source = getSource(add.highlights, favourPaf, favourWelsh),
        lang = getLang(add.highlights, favourPaf, favourWelsh),
        hits = if (highVerbose) Option(sortHighs(add.highlights.get.hits.getOrElse(Seq()), favourPaf, favourWelsh)) else None)))
  }
}
