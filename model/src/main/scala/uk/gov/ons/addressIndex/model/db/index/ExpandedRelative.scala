package uk.gov.ons.addressIndex.model.db.index

/**
  * Relative Expander DTO
  * Relatives response contains a sequence of Relative objects, one per level
  * Expanded vesion has siblings with formattedAdresses for the UI
  */
case class ExpandedRelative(
  level: Int,
  siblings: Seq[ExpandedSibling]
)

case class ExpandedSibling(
  uprn: Long,
  formattedAddress: String
)
