package uk.gov.ons.addressIndex.model.db.index

/**
  * PAF Address DTO
  */
case class PostcodeAddressFileAddress(
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
  pafAll: String
)

/**
  * PAF Address DTO companion object that also contains implicits needed for Elastic4s
  */
object PostcodeAddressFileAddress {

  object Fields {

    /**
      * Document Fields
      */
    val postcode: String = "postcode"
    val recordIdentifier: String = "recordIdentifier"
    val changeType: String = "changeType"
    val postTown: String = "postTown"
    val proOrder: String = "proOrder"
    val uprn: String = "uprn"
    val udprn: String = "udprn"
    val organizationName: String = "organizationName"
    val departmentName: String = "departmentName"
    val subBuildingName: String = "subBuildingName"
    val buildingName: String = "buildingName"
    val buildingNumber: String = "buildingNumber"
    val dependentThoroughfare: String = "dependentThoroughfare"
    val thoroughfare: String = "thoroughfare"
    val doubleDependentLocality: String = "doubleDependentLocality"
    val dependentLocality: String = "dependentLocality"
    val postcodeType: String = "postcodeType"
    val deliveryPointSuffix: String = "deliveryPointSuffix"
    val welshDependentThoroughfare: String = "welshDependentThoroughfare"
    val welshThoroughfare: String = "welshThoroughfare"
    val welshDoubleDependentLocality: String = "welshDoubleDependentLocality"
    val welshDependentLocality: String = "welshDependentLocality"
    val welshPostTown: String = "welshPostTown"
    val poBoxNumber: String = "poBoxNumber"
    val processDate: String = "processDate"
    val startDate: String = "startDate"
    val endDate: String = "endDate"
    val lastUpdateDate: String = "lastUpdateDate"
    val entryDate: String = "entryDate"
    val pafAll: String = "pafAll"
  }

  def fromEsMap(paf: Map[String, AnyRef]): PostcodeAddressFileAddress = {
    val filteredPaf = paf.filter { case (_, value) => value != null }

    PostcodeAddressFileAddress(
      recordIdentifier = filteredPaf.getOrElse(Fields.recordIdentifier, "").toString,
      changeType = filteredPaf.getOrElse(Fields.changeType, "").toString,
      proOrder = filteredPaf.getOrElse(Fields.proOrder, "").toString,
      uprn = filteredPaf.getOrElse(Fields.uprn, "").toString,
      udprn = filteredPaf.getOrElse(Fields.udprn, "").toString,
      organizationName = filteredPaf.getOrElse(Fields.organizationName, "").toString,
      departmentName = filteredPaf.getOrElse(Fields.departmentName, "").toString,
      subBuildingName = filteredPaf.getOrElse(Fields.subBuildingName, "").toString,
      buildingName = filteredPaf.getOrElse(Fields.buildingName, "").toString,
      buildingNumber = filteredPaf.getOrElse(Fields.buildingNumber, "").toString,
      dependentThoroughfare = filteredPaf.getOrElse(Fields.dependentThoroughfare, "").toString,
      thoroughfare = filteredPaf.getOrElse(Fields.thoroughfare, "").toString,
      doubleDependentLocality = filteredPaf.getOrElse(Fields.doubleDependentLocality, "").toString,
      dependentLocality = filteredPaf.getOrElse(Fields.dependentLocality, "").toString,
      postTown = filteredPaf.getOrElse(Fields.postTown, "").toString,
      postcode = filteredPaf.getOrElse(Fields.postcode, "").toString,
      postcodeType = filteredPaf.getOrElse(Fields.postcodeType, "").toString,
      deliveryPointSuffix = filteredPaf.getOrElse(Fields.deliveryPointSuffix, "").toString,
      welshDependentThoroughfare = filteredPaf.getOrElse(Fields.welshDependentThoroughfare, "").toString,
      welshThoroughfare = filteredPaf.getOrElse(Fields.welshThoroughfare, "").toString,
      welshDoubleDependentLocality = filteredPaf.getOrElse(Fields.welshDoubleDependentLocality, "").toString,
      welshDependentLocality = filteredPaf.getOrElse(Fields.welshDependentLocality, "").toString,
      welshPostTown = filteredPaf.getOrElse(Fields.welshPostTown, "").toString,
      poBoxNumber = filteredPaf.getOrElse(Fields.poBoxNumber, "").toString,
      processDate = filteredPaf.getOrElse(Fields.processDate, "").toString,
      startDate = filteredPaf.getOrElse(Fields.startDate, "").toString,
      endDate = filteredPaf.getOrElse(Fields.endDate, "").toString,
      lastUpdateDate = filteredPaf.getOrElse(Fields.lastUpdateDate, "").toString,
      entryDate = filteredPaf.getOrElse(Fields.entryDate, "").toString,
      pafAll = filteredPaf.getOrElse(Fields.entryDate, "").toString
    )
  }
}