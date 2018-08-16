package uk.gov.ons.addressIndex.demoui.views

import uk.gov.ons.addressIndex.model.server.response.bulk.AddressBulkResponseContainer

object ScoreHelper {
  /**
    * calculates score percentage
    *
    * @param score
    * @param maxScore
    * @return
    */
  def getPercentageFromScore(score: Double, maxScore: Double): Int = {
    ((score / maxScore) * 100).toInt
  }

  /**
    * calculates relative score percentage
    *
    * @param score
    * @param set
    * @return
    */
  def getRelativePercentageScore(score: Double, set: AddressBulkResponseContainer): Int = {
    ((score / set.bulkAddresses.maxBy(_.underlyingScore).underlyingScore) * 100).toInt
  }

  /**
    * Returns rank of the address among the addresses with the same id
    * @param index the index of the address in the response container
    * @param set response container
    * @return the rank of the address
    */
  def getRank(index: Int, set: AddressBulkResponseContainer): Int = {
    val addresses = set.bulkAddresses
    val id = addresses(index).id
    val prefixAddressesDifferentId = addresses.takeWhile(_.id != id)

    index - prefixAddressesDifferentId.size + 1
  }
}
