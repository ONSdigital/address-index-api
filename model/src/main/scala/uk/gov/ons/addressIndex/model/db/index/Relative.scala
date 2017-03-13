package uk.gov.ons.addressIndex.model.db.index

import play.api.libs.json.{Format, Json}

case class Relative(
  level: Int,
  siblings: Seq[Long],
  parents: Seq[Long]
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

    Relative (
        level = rels.getOrElse(Fields.level, 0).asInstanceOf[Int],
        siblings = Json.parse(rels.getOrElse(Fields.siblings, "[]").toString).as[Seq[Long]],
        parents =  Json.parse(rels.getOrElse(Fields.parents, "[]").toString).as[Seq[Long]]
    )
  }
}
