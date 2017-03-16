package uk.gov.ons.addressIndex.demoui.views

import uk.gov.ons.addressIndex.model.BulkResp

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

  /**
    * calculates relative score percentage
    *
    * @param score
    * @param set
    * @return
    */
  def getRelativePercentageScore(score: Float, set: BulkResp): Int = {
    ((score / set.resp.maxBy(_.score).score) * 100).toInt
  }

  /**
    * filter the results for a BulkResp for items
    * with a relative score weighing of less than or equal to 66
    *
    * @param set
    * @return
    */
  def filterSetByScore(set: BulkResp): BulkResp = {
    BulkResp(
      resp = set.resp.filter(i => getRelativePercentageScore(i.score, set) >= 66)
    )
  }
}
