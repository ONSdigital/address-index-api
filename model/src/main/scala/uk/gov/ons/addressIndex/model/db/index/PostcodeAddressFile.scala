package uk.gov.ons.addressIndex.model.db.index

import com.sksamuel.elastic4s.{HitAs, RichSearchHit}
import uk.gov.ons.addressIndex.model.db.ElasticIndex



trait AddressFormattable {
  def delimitByComma(parts: String*) = parts.map(_.trim).filter(_.nonEmpty).mkString(", ")
}

/**
 * Data structure containing addresses with the maximum address
 * @param addresses fetched addresses
 * @param maxScore maximum score
 */
case class PostcodeAddressFileAddresses(
 addresses: Seq[PostcodeAddressFile],
 maxScore: Float
)

/**
  * PAF Address DTO
  */
case class PostcodeAddressFile(
  recordIdentifier: String,
  changeType: String,
  proOrder: String,
  uprn: String,
  udprn: String,
  organizationName: String,
  departmentName: String,
  subBuildingName: String,
  buildingName: String,
  buildingNumber: String,
  dependentThoroughfare: String,
  thoroughfare: String,
  doubleDependentLocality: String,
  dependentLocality: String,
  postTown: String,
  postcode: String,
  postcodeType: String,
  deliveryPointSuffix: String,
  welshDependentThoroughfare: String,
  welshThoroughfare: String,
  welshDoubleDependentLocality: String,
  welshDependentLocality: String,
  welshPostTown: String,
  poBoxNumber: String,
  processDate: String,
  startDate: String,
  endDate: String,
  lastUpdateDate: String,
  entryDate: String,
  score: Float
) extends AddressFormattable {
  def formatAddress: String = {
    val newPoboxNumber = if (poBoxNumber.isEmpty) "" else s"PO BOX ${poBoxNumber}"
    val trimmedBuildingNumber = buildingNumber.trim
    val trimmedDependentThoroughfare = dependentThoroughfare.trim
    val trimmedThoroughfare = thoroughfare.trim

    val buildingNumberWithStreetName = s"$trimmedBuildingNumber ${
      if(trimmedDependentThoroughfare.nonEmpty)
        s"$trimmedDependentThoroughfare, "
      else
        ""
    }$trimmedThoroughfare"

    delimitByComma(departmentName, organizationName, subBuildingName, buildingName,
      newPoboxNumber, buildingNumberWithStreetName, doubleDependentLocality, dependentLocality,
      postTown, postcode)
  }
}