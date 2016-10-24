package uk.gov.ons.addressIndex.model.db.index

import java.util.{Date, UUID}

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.FieldType._
import com.sksamuel.elastic4s.mappings.MappingDefinition
import uk.gov.ons.addressIndex.model.db.ElasticIndex

case class PostcodeAddressFileIndex(
  guid                         : UUID = UUID.randomUUID,
  startDate                    : Option[Date],
  udprn                        : Option[Int],
  buildingNumber               : Option[Int],
  poBoxNumber                  : Option[Int],
  buildingName                 : Option[String],
  subBuildingName              : Option[String],
  organisationName             : Option[String],
  postTown                     : Option[String],
  welshPostTown                : Option[String],
  postcode                     : Option[String],
  departmentName               : Option[String],
  thoroughfare                 : Option[String],
  welshThoroughfare            : Option[String],
  dependantThoroughfare        : Option[String],
  welshDependantThoroughfare   : Option[String],
  dependentLocality            : Option[String],
  welshDependentLocality       : Option[String],
  doubleDependentLocality      : Option[String],
  welshDoubleDependentLocality : Option[String]
)

object PostcodeAddressFileIndex extends ElasticIndex[PostcodeAddressFileIndex] {

  val name = "PostcodeAddressFile"

  def mappingDefinitions() : Seq[MappingDefinition] = {
    Seq(
      mapping(name) fields (
        "guid"                         typed StringType,//<UUID>.toString
        "startDate"                    typed DateType,
        "udprn"                        typed IntegerType,
        "buildingNumber"               typed IntegerType,
        "poBoxNumber"                  typed IntegerType,
        "buildingName"                 typed StringType,
        "subBuildingName"              typed StringType,
        "organisationName"             typed StringType,
        "postTown"                     typed StringType,
        "welshPostTown"                typed StringType,
        "postcode"                     typed StringType,
        "departmentName"               typed StringType,
        "thoroughfare"                 typed StringType,
        "welshThoroughfare"            typed StringType,
        "dependantThoroughfare"        typed StringType,
        "dependentLocality"            typed StringType,
        "welshDependentLocality"       typed StringType,
        "doubleDependentLocality"      typed StringType,
        "welshDoubleDependentLocality" typed StringType
      )
    )
  }
}