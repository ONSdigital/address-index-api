package uk.gov.ons.addressIndex.demoui.views

import uk.gov.ons.addressIndex.model.server.response.bulk.AddressBulkResponseAddress

object MatchTypeHelper {

  def matchType(addressId: String, ids: Seq[String], matchedFormattedAddress: String): String = {
    val matchedIds = ids.count(_ == addressId)

    if (matchedIds == 1 && matchedFormattedAddress.nonEmpty) "S"
    else if (matchedFormattedAddress.isEmpty) "N"
    else "M"
  }

  def countSearched(addresses: Seq[AddressBulkResponseAddress]): Int = addresses.map(_.id).toSet.size

  def countMultiples(addresses: Seq[AddressBulkResponseAddress]): Int =
    addresses.filter(_.matchedFormattedAddress.nonEmpty).groupBy(_.id).mapValues(_.size).values.count(_ > 1)

  def countSingles(addresses: Seq[AddressBulkResponseAddress]): Int =
    addresses.filter(_.matchedFormattedAddress.nonEmpty).groupBy(_.id).mapValues(_.size).values.count(_ == 1)

  def countEmpty(addresses: Seq[AddressBulkResponseAddress]): Int =
    addresses.count(_.matchedFormattedAddress.isEmpty)

}
