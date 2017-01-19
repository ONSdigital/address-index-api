package uk.gov.ons.addressIndex.model.db.index

import uk.gov.ons.addressIndex.model.server.response.{PAF, PAFWithFormat}

trait Formattable {
  def delimitByComma(parts: String*) = parts.map(_.trim).filter(_.nonEmpty).mkString(", ")
}

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
) extends Formattable {

  def formatAddress: String = {
    val newPoboxNumber = if (poBoxNumber.isEmpty) "" else s"PO BOX $poBoxNumber"
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

  def toPAFWithFormat: PAFWithFormat = {
    PAFWithFormat(
      formattedAddress = formatAddress,
      paf = PAF(
        udprn = udprn,
        organisationName = organizationName,
        departmentName = departmentName,
        subBuildingName = subBuildingName,
        buildingName = buildingName,
        buildingNumber = buildingNumber,
        dependentThoroughfare = dependentThoroughfare,
        thoroughfare = thoroughfare,
        doubleDependentLocality = doubleDependentLocality,
        dependentLocality = dependentLocality,
        postTown = postTown,
        postcode = postcode,
        postcodeType = postcodeType,
        deliveryPointSuffix = deliveryPointSuffix,
        welshDependentThoroughfare = welshDependentThoroughfare,
        welshThoroughfare = welshThoroughfare,
        welshDoubleDependentLocality = welshDoubleDependentLocality,
        welshDependentLocality = welshDependentLocality,
        welshPostTown = welshPostTown,
        poBoxNumber = poBoxNumber,
        startDate = startDate,
        endDate = endDate
      )
    )
  }
}