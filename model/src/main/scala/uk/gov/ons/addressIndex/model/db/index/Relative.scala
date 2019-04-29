package uk.gov.ons.addressIndex.model.db.index

/**
  * Relative DTO
  * Relatives response contains a sequence of Relative objects, one per level
  */
case class Relative(level: Int,
                    siblings: Seq[Long],
                    parents: Seq[Long])

/**
  * Relative DTO companion object includes method to cast from elastic response
  * Relatives response contains a sequence of Relative objects, one per level
  * If there is only one sibling it is the same as the main uprn
  */
object Relative {
  object Fields {
    /**
      * Document Fields
      */
    val level: String = "level"
    val siblings: String = "siblings"
    val parents: String = "parents"
  }

  def fromEsMap(rels: Map[String, Any]): Relative = {
    Relative(
      rels.getOrElse(Fields.level, 0).asInstanceOf[Int],
      // uprns sometimes come back as Integers instead of Longs from ES so need to deal with this
      rels.getOrElse(Fields.siblings, Seq.empty).asInstanceOf[Seq[Any]].map(_.toString.toLong),
      rels.getOrElse(Fields.parents, Seq.empty).asInstanceOf[Seq[Any]].map(_.toString.toLong)
    )
  }
}