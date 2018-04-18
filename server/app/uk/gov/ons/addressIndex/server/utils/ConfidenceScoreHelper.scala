package uk.gov.ons.addressIndex.server.utils

import play.api.Logger
import uk.gov.ons.addressIndex.model.server.response.{AddressResponseAddress, AddressResponseScore}
import uk.gov.ons.addressIndex.parsers.Tokens
import scala.math._
import scala.util.Try

object ConfidenceScoreHelper {

  val logger = Logger("ConfidenceScoreHelper")

  def calculateConfidenceScore(tokens: Map[String,String], structuralScore: Double, unitScore: Double, elasticRatio: Double): Double = {
    logger.info("elasticRatio="+elasticRatio)
    val unitScoreReplaced = if (unitScore == -1) 0.3D else unitScore
    logger.info("unitScoreReplaced="+unitScoreReplaced)
    val alpha = if (tokens.contains("OrganisationName") ||
      tokens.contains("SubBuildingName") ||
      tokens.contains("saoStartNumber") ||
      tokens.contains("saoStartSuffix")) 0.8D else 0.9D
    logger.info("alpha="+alpha)
    logger.info("structuralScore="+structuralScore)
    val hScore = structuralScore * (alpha + (0.99-alpha) * unitScoreReplaced)
    logger.info("hScore="+hScore)
    val hScoreScaled = pow(hScore,6)
    logger.info("hScoreScaled="+hScoreScaled)
    val elasticRatioScaled = 1 / (1 + exp(15 * (0.99 - elasticRatio)))
    logger.info("elasticRatioScaled="+elasticRatioScaled)
    max(hScoreScaled, elasticRatioScaled)
  }


  def calculateElasticDenominator(scores: Seq[Float]): Double = {
    def maxScore = Try(scores.max).getOrElse(1F)
    logger.info("maxScore="+maxScore)
    def scores2 = scores.filter(_ < maxScore)
    def maxScore2 = Try(scores2.max).getOrElse(maxScore)
    logger.info("maxScore2="+maxScore2)
    def testMean = Try(((maxScore +  maxScore2) / 2 ).toDouble).getOrElse(0D)
    if (testMean == 0) Try((scores.max).toDouble).getOrElse(1D) else testMean
  }

}
