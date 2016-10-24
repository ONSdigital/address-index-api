package uk.gov.ons.addressIndex.model.db.index

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.FieldType._
import com.sksamuel.elastic4s.mappings.MappingDefinition
import uk.gov.ons.addressIndex.model.db.ElasticIndex

case class PostcodeIndex(
  inCode  : String,
  outCode : String
)

object PostcodeIndex extends ElasticIndex[PostcodeIndex] {

  val name = "Postcode"

  def mappingDefinitions() : Seq[MappingDefinition] = {
    Seq(
      mapping(name) fields (
        "inCode"  typed StringType,
        "outCode" typed StringType
      )
    )
  }
}
