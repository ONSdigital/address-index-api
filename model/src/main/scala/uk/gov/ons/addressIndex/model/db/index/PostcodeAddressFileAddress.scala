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
  addresses: Seq[PostcodeAddressFileAddress],
  maxScore: Float
)

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
  score: Float
) extends AddressFormattable {
  def formatAddress(paf: PostcodeAddressFileAddress): String = {
    val poBoxNumber = if (paf.poBoxNumber.isEmpty) "" else s"PO BOX ${paf.poBoxNumber}"

    val trimmedBuildingNumber = paf.buildingNumber.trim
    val trimmedDependentThoroughfare = paf.dependentThoroughfare.trim
    val trimmedThoroughfare = paf.thoroughfare.trim

    val buildingNumberWithStreetName =
      s"$trimmedBuildingNumber ${ if(trimmedDependentThoroughfare.nonEmpty) s"$trimmedDependentThoroughfare, " else "" }$trimmedThoroughfare"

    delimitByComma(paf.departmentName, paf.organizationName, paf.subBuildingName, paf.buildingName,
      poBoxNumber, buildingNumberWithStreetName, paf.doubleDependentLocality, paf.dependentLocality,
      paf.postTown, paf.postcode)
  }
}


/**
  * PAF Address DTO companion object that also contains implicits needed for Elastic4s
  */
object PostcodeAddressFileAddress extends ElasticIndex[PostcodeAddressFileAddress] {

  val name: String = "PostcodeAddressFile"

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
  }


  /**
    * This is needed to directly transform a collection of objects returned by Elastic
    * request into a collection of PAF addresses
    */
  implicit object PostcodeAddressFileAddressHitAs extends HitAs[PostcodeAddressFileAddress] {
    import Fields._

    override def as(hit: RichSearchHit): PostcodeAddressFileAddress = {
      val map = hit.sourceAsMap
      PostcodeAddressFileAddress(
        map(recordIdentifier).toString,
        map(changeType).toString,
        map(proOrder).toString,
        map(uprn).toString,
        map(udprn).toString,
        map(organizationName).toString,
        map(departmentName).toString,
        map(subBuildingName).toString,
        map(buildingName).toString,
        map(buildingNumber).toString,
        map(dependentThoroughfare).toString,
        map(thoroughfare).toString,
        map(doubleDependentLocality).toString,
        map(dependentLocality).toString,
        map(postTown).toString,
        map(postcode).toString,
        map(postcodeType).toString,
        map(deliveryPointSuffix).toString,
        map(welshDependentThoroughfare).toString,
        map(welshThoroughfare).toString,
        map(welshDoubleDependentLocality).toString,
        map(welshDependentLocality).toString,
        map(welshPostTown).toString,
        map(poBoxNumber).toString,
        map(processDate).toString,
        map(startDate).toString,
        map(endDate).toString,
        map(lastUpdateDate).toString,
        map(entryDate).toString,
        hit.score
      )
    }
  }
}