package uk.gov.ons.addressIndex.model.db.index

import play.api.libs.json.{Format, Json}

case class Relation (
  level: Int,
  siblings: Array[Long],
  parents: Array[Long]
)

object Relation {

  implicit lazy val relationFormat: Format[Relation] = Json.format[Relation]

  object Fields {

    /**
      * Document Fields
      */
    val level: String = "level"
    val siblings: String = "siblings"
    val parents: String = "parents"
  }

  def fromEsMap (rels: Map[String, Any]): Relation = {

    Relation (
        level = rels.getOrElse(Fields.level, 0).asInstanceOf[Int],
        siblings = rels.getOrElse(Fields.siblings, Array()).asInstanceOf[Array[Long]],
        parents = rels.getOrElse(Fields.parents, Array()).asInstanceOf[Array[Long]]
    )
  }
}
