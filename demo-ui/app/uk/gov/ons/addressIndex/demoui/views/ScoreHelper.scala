package uk.gov.ons.addressIndex.demoui.views

import uk.gov.ons.addressIndex.model.server.response.AddressBulkResponseContainer

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
  def getRelativePercentageScore(score: Float, set: AddressBulkResponseContainer): Int = {
    ((score / set.bulkAddresses.maxBy(_.score).score) * 100).toInt
  }
}
