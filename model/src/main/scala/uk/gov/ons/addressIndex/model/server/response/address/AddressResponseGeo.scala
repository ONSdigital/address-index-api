package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json._
//import java.math.BigDecimal
import scala.math.BigDecimal
import play.api.libs.functional.syntax._
import uk.gov.ons.addressIndex.model.db.index.NationalAddressGazetteerAddress

import scala.util.Try

/**
  * Contains address geo position
  *
  * @param latitude  latitude
  * @param longitude longitude
  * @param easting   easting
  * @param northing  northing
  */
case class AddressResponseGeo(
  latitude: BigDecimal,
  longitude: BigDecimal,
  easting: Int,
  northing: Int
)

object AddressResponseGeo {
  //implicit lazy val addressResponseGeoFormat: Format[AddressResponseGeo] = Json.format[AddressResponseGeo]

  val geoReads: Reads[AddressResponseGeo] = (
      (JsPath \ "latitude").read[BigDecimal] and
      (JsPath \ "longitude").read[BigDecimal] and
      (JsPath \ "easting").read[Int] and
      (JsPath \ "northing").read[Int]
    )(AddressResponseGeo.apply _)

  val geoWrites: Writes[AddressResponseGeo] = (
      (JsPath \ "latitide").write[BigDecimal](Writes((o: BigDecimal) => JsNumber(BigDecimal(JsString(o.bigDecimal.toPlainString).value)))) and
      (JsPath \ "longitude").write[BigDecimal] (Writes((o: BigDecimal) => (JsString(o.bigDecimal.toPlainString)))) and
      (JsPath \ "easting").write[Int] and
      (JsPath \ "northing").write[Int]
    )(unlift(AddressResponseGeo.unapply))

  implicit lazy val addressResponseGeoFormat: Format[AddressResponseGeo] =
    Format(geoReads, geoWrites)

  /**
    * Creates GEO information from NAG elastic search object
    * @param other NAG elastic search
    * @return
    */
  def fromNagAddress(other: NationalAddressGazetteerAddress): Option[AddressResponseGeo] = (for {
      latitude <- Try(BigDecimal(other.latitude))
      longitude <- Try(BigDecimal(other.longitude))
      easting <- Try(other.easting.split("\\.").head.toInt)
      northing <- Try(other.northing.split("\\.").head.toInt)
    } yield AddressResponseGeo(latitude, longitude, easting, northing)).toOption

}
