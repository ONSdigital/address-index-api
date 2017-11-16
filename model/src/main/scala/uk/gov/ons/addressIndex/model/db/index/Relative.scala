package uk.gov.ons.addressIndex.model.db.index

import scala.collection.immutable.Map.Map3

/**
  * Relative DTO
  * Relatives response contains a sequence of Relative objects, one per level
  */
case class Relative(
  level: Int,
  siblings: Seq[Long],
  parents: Seq[Long]
)

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

  def fromEsMap (rel: AnyRef): Seq[Relative] = {
    var rels = Seq[Relative]()
    val relIter = rel.asInstanceOf[List[AnyRef]].iterator
    while (relIter.hasNext) {
      val filteredRel = relIter.next().asInstanceOf[Map3[String, AnyRef]].filter { case (_, value) => value != null }
      rels = rels :+ Relative(
        level = filteredRel.getOrElse(Fields.level, 0).asInstanceOf[Int],
        siblings = filteredRel.getOrElse(Fields.siblings, "[]").asInstanceOf[Seq[Long]],
        parents =  filteredRel.getOrElse(Fields.parents, "[]").asInstanceOf[Seq[Long]]
      )
    }
    collection.immutable.Seq(rels: _*)
  }
}
