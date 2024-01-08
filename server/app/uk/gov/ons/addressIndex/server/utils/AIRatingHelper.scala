package uk.gov.ons.addressIndex.server.utils

import uk.gov.ons.addressIndex.model.server.response.address.{AddressResponseAddress, AddressResponseScoreSummary}
import uk.gov.ons.addressIndex.model.server.response.bulk.AddressBulkResponseAddress

import scala.util.Try

object AIRatingHelper {

  //todo: the code below will be tidied and/or refactored when output is finalised

  def makeScoreSummary(maxConfidenceScore:Double, secondConfidenceScore: Double, unambiguityScore:Double): AddressResponseScoreSummary ={

    val topMatchConfidenceZone = maxConfidenceScore match {
      case i if (i < 50) => "L"
      case i if (i > 66) => "H"
      case _ => "M"
    }

    val topMatchUnambiguityZone = unambiguityScore match {
      case i if (i < 20) => "L"
      case i if (i > 40) => "H"
      case _ => "M"
    }

    val recommendationCode = {
      if (topMatchConfidenceZone == ("H") && topMatchUnambiguityZone != "L") "A"
      else if (topMatchConfidenceZone == ("M") && topMatchUnambiguityZone == "H") "A"
      else "I"
    }

    val recommendationText = {
      if (topMatchConfidenceZone == ("H") && topMatchUnambiguityZone != "L") "Accept result"
      else if (topMatchConfidenceZone == ("M") && topMatchUnambiguityZone == "H") "Accept result"
      else "Requires clerical intervention"
    }

    AddressResponseScoreSummary(
      maxConfidenceScore = maxConfidenceScore,
      topMatchConfidenceZone = topMatchConfidenceZone,
      unambiguityScore = unambiguityScore,
      topMatchUnambiguityZone = topMatchUnambiguityZone,
      recommendationCode = recommendationCode,
      recommendationText = recommendationText
    )
  }

  def calculateAIRatingSingle(sortedAddresses: Seq[AddressResponseAddress] ): AddressResponseScoreSummary = {

    val maxConfidenceScore: Double = sortedAddresses.headOption.map(_.confidenceScore).getOrElse(0D)
    val secondConfidenceScore: Double = Try(sortedAddresses(1).confidenceScore).getOrElse(0D)
    val unambiguityScore: Double = BigDecimal(maxConfidenceScore - secondConfidenceScore).setScale(4, BigDecimal.RoundingMode.HALF_UP).toDouble

    makeScoreSummary(maxConfidenceScore, secondConfidenceScore, unambiguityScore)

  }


  def calculateAIRatingBulk(sortedAddresses: Seq[AddressBulkResponseAddress]): AddressResponseScoreSummary = {

    //todo: the code below will be tidied and/or refactored when output is finalised

    val maxConfidenceScore: Double = sortedAddresses.headOption.map(_.confidenceScore).getOrElse(0D)
    val secondConfidenceScore: Double = Try(sortedAddresses(1).confidenceScore).getOrElse(0D)
    val unambiguityScore: Double = BigDecimal(maxConfidenceScore - secondConfidenceScore).setScale(4, BigDecimal.RoundingMode.HALF_UP).toDouble

    makeScoreSummary(maxConfidenceScore, secondConfidenceScore, unambiguityScore)

  }

}
