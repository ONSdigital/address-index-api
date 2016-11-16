package uk.gov.ons.addressIndex.server.model.response

import play.api.libs.json.Json
import uk.gov.ons.addressIndex.model.db.index.PostcodeAddressFileAddress

case class PostcodeAddressFileReplyUnit(
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
  poBoxNumber: String,
  startDate: String,
  lastUpdateDate: String
)

/**
  * The initial data object contains more than 22 fields, hence it cannot be translated into json.
  * That's why we need this one
  */
object PostcodeAddressFileReplyUnit {

  implicit val pafReplyUnitWrites = Json.format[PostcodeAddressFileReplyUnit]

  def fromPostcodeAddressFileAddress(other : PostcodeAddressFileAddress) : PostcodeAddressFileReplyUnit = {
    PostcodeAddressFileReplyUnit(
      other.recordIdentifier,
      other.changeType,
      other.proOrder,
      other.uprn,
      other.udprn,
      other.organizationName,
      other.departmentName,
      other.subBuildingName,
      other.buildingName,
      other.buildingNumber,
      other.dependentThoroughfare,
      other.thoroughfare,
      other.doubleDependentLocality,
      other.dependentLocality,
      other.postTown,
      other.postcode,
      other.postcodeType,
      other.deliveryPointSuffix,
      other.poBoxNumber,
      other.startDate,
      other.lastUpdateDate
    )
  }
}