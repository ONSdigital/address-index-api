package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.db.index.Relative

/**
  * Wrapper response object for Relative (Relatives response comprises one Relative object per level)
  *
  * @param level    level number 1,2 etc. - 1 is top level
  * @param siblings uprns of addresses at the current level
  * @param parents  uprns of addresses at the level above
  *
  */
case class AddressResponseRelative(level: Int,
                                   siblings: Seq[Long],
                                   parents: Seq[Long])

/**
  * Compainion object providing Lazy Json formatting
  */
object AddressResponseRelative {
  implicit lazy val relativeFormat: Format[AddressResponseRelative] = Json.format[AddressResponseRelative]

  def fromRelative(relative: Relative): AddressResponseRelative =
    AddressResponseRelative(relative.level, relative.siblings, relative.parents)
}
