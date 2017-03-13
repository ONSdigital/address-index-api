package uk.gov.ons.addressIndex.model.db.index

import java.util

import play.api.libs.json.{Format, Json}

case class Relative(
  level: Int,
  siblings: Array[Long],
  parents: Array[Long]
)

object Relative {

  implicit lazy val relationFormat: Format[Relative] = Json.format[Relative]

  object Fields {

    /**
      * Document Fields
      */
    val level: String = "level"
    val siblings: String = "siblings"
    val parents: String = "parents"
  }

  def fromEsMap (rels: Map[String, Any]): Relative = {

    val siblingArray = rels.getOrElse(Fields.siblings, new util.ArrayList()).asInstanceOf[util.ArrayList[Long]]
    val parentArray = rels.getOrElse(Fields.parents, new util.ArrayList()).asInstanceOf[util.ArrayList[Long]]

    Relative (
        level = rels.getOrElse(Fields.level, 0).asInstanceOf[Int],
        siblings = rels.getOrElse(Fields.siblings, Array()).asInstanceOf[Array[Long]],
        parents = rels.getOrElse(Fields.parents, Array()).asInstanceOf[Array[Long]]
    )
  }
}
