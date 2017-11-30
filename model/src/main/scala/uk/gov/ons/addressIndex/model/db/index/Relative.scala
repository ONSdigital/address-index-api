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

      // uprns sometimes come back as Integers instead of Longs from ES so need to deal with this
      val relSiblings = filteredRel.getOrElse(Fields.siblings, Seq.empty[Long]).asInstanceOf[List[Any]]
      val relParents = filteredRel.getOrElse(Fields.parents, Seq.empty[Long]).asInstanceOf[List[Any]]

      var sibs = Seq.empty[Long]

      for (sib <- relSiblings) {
        val ssib: String = sib.toString()
        val lsib: Long = ssib.toLong
        sibs = sibs :+ lsib
      }

      var pars = Seq.empty[Long]

      for (par <- relParents) {
        val spar: String = par.toString()
        val lpar: Long = spar.toLong
        pars = pars :+ lpar
      }

      rels = rels :+ Relative(
        level = filteredRel.getOrElse(Fields.level, 0).asInstanceOf[Int],
        siblings = sibs,
        parents = pars
      )
    }
    collection.immutable.Seq(rels: _*)
  }
}
