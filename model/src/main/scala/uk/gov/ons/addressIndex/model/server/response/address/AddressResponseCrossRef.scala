package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.db.index.CrossRef

/**
  * Companion object providing Lazy Json formatting
  */
object AddressResponseCrossRef {
  implicit lazy val crossRefFormat: Format[AddressResponseCrossRef] = Json.format[AddressResponseCrossRef]

  def fromCrossRef(crossRef: CrossRef): AddressResponseCrossRef =
    AddressResponseCrossRef(crossRef.crossReference, crossRef.source)
}

case class AddressResponseCrossRef(crossReference: String, source: String)
