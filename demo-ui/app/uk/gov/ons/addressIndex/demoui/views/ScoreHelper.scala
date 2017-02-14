package uk.gov.ons.addressIndex.demoui.views

object ScoreHelper {
  /**
    * calculates score percentage
    *
    * @param score
    * @param maxScore
    * @return
    */
  def getPercentageFromScore(score: Float, maxScore: Float): Int = {
    ((score / maxScore) * 100).toInt
  }
}
