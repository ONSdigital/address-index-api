package uk.gov.ons.addressIndex.demoui.views

object MatchTypeHelper {

  def matchType(addressId: String, ids: Seq[String], matchedFormattedAddress: String): String = {
    val matchedIds = ids.count(_ == addressId)
    if(matchedIds == 1) {
      "S"
    } else if(matchedFormattedAddress.isEmpty) {
      "N"
    } else {
      "M"
    }
  }
}
