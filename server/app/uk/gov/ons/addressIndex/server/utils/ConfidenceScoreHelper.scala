package uk.gov.ons.addressIndex.server.utils

import play.api.Logger
import scala.math._
import scala.util.Try

object ConfidenceScoreHelper {

  val logger = Logger("ConfidenceScoreHelper")

  /**
    * Calculate the confidence (hybrid) score for a single result
    * This is the better of the elastic and Hopper scores after scaling factors are applied
    *
    * @param tokens map containing the elastic scores of a batch of matches
    * @param struturalScore location / building score part of Hopper score
    * @param unitScore sub-building score part of Hopper score
    * @param elasticRation elastic score devided by previously calculated mean of first and second scores
    * @return confidence score as double
    */
  def calculateConfidenceScore(tokens: Map[String,String], structuralScore: Double, unitScore: Double, elasticRatio: Double): Double = {
    val unitScoreReplaced = if (unitScore == -1) 0.3D else unitScore
    val alpha = if (tokens.contains("OrganisationName") ||
      tokens.contains("SubBuildingName") ||
      tokens.contains("saoStartNumber") ||
      tokens.contains("saoStartSuffix")) 0.8D else 0.9D
    val hScore = structuralScore * (alpha + (0.99-alpha) * unitScoreReplaced)
    val hScoreScaled = pow(hScore,6)
    val elasticRatioScaled = 1 / (1 + exp(15 * (0.99 - elasticRatio)))
    BigDecimal(max(hScoreScaled, elasticRatioScaled)).setScale(4, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

  /**
    * Calculate the  elastic denominator
    * This is defined as the mean of the no.1 score and the no.2 score,
    * by position not value so for 10,10,5 it's 10 not 7.5
    *
    * @param scores map containing the elastic scores of a batch of matches
    * @return denominator as double
    */
  def calculateElasticDenominator(scores: Seq[Float]): Double = {
    def maxScore = Try(scores.max).getOrElse(1F)
    def scores2 = scores.sorted(Ordering[Float].reverse).drop(1)
    def maxScore2 = Try(scores2.max).getOrElse(maxScore)
    Try(((maxScore +  maxScore2) / 2 ).toDouble).getOrElse(1D)
  }

}
