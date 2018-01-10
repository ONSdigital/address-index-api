package uk.gov.ons.addressIndex.model.db.index

/**
  * CrossRef DTO
  * CrossRefs response contains a sequence of CrossRef objects
  */
case class CrossRef (crossReference: String, source: String)

/**
  * CrossRef DTO companion object includes method to cast from elastic response.
  * CrossRef response contains a sequence of CrossRef objects.
  */
object CrossRef {

  object Fields {
    /**
      * Document Fields
      */
    val crossReference: String = "crossReference"
    val source: String = "source"
  }

  def fromEsMap (crossRefs: Map[String, Any]): CrossRef = {
    CrossRef (
      crossRefs.getOrElse(Fields.crossReference, "").asInstanceOf[String],
      crossRefs.getOrElse(Fields.source, "").asInstanceOf[String]
    )
  }
}