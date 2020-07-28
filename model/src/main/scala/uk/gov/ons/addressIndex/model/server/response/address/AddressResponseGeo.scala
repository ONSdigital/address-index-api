package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.db.index.{AuxiliaryAddress, NationalAddressGazetteerAddress, NisraAddress}

import scala.util.Try

/**
  * Contains address geo position
  *
  * @param latitude  latitude
  * @param longitude longitude
  * @param easting   easting
  * @param northing  northing
  */
case class AddressResponseGeo(latitude: BigDecimal, longitude: BigDecimal, easting: Int, northing: Int)

object AddressResponseGeo {
  implicit lazy val addressResponseGeoFormat: Format[AddressResponseGeo] = Json.format[AddressResponseGeo]

  /**
    * Creates GEO information from NAG elastic search object
    *
    * @param other NAG elastic search
    * @return
    */
  def fromNagAddress(other: NationalAddressGazetteerAddress): Option[AddressResponseGeo] = (for {
      latitude <- Try(BigDecimal(other.latitude))
      longitude <- Try(BigDecimal(other.longitude))
      easting <- Try(other.easting.split("\\.").headOption.map(_.toInt).get)
      northing <- Try(other.northing.split("\\.").headOption.map(_.toInt).get)
    } yield AddressResponseGeo(latitude, longitude, easting, northing)).toOption

  /**
    * Creates GEO information from NISRA elastic search object
    * @param other NISRA elastic search
    * @return
    */
  def fromNisraAddress(other: NisraAddress): Option[AddressResponseGeo] = (for {
    latitude <- Try(BigDecimal(other.latitude))
    longitude <- Try(BigDecimal(other.longitude))
    easting <- Try(other.easting.split("\\.").headOption.map(_.toInt).get)
    northing <- Try(other.northing.split("\\.").headOption.map(_.toInt).get)
  } yield AddressResponseGeo(latitude, longitude, easting, northing)).toOption

  /**
   * Creates GEO information from AUXILIARY elastic search object
   * @param other AUXILIARY elastic search
   * @return
   */
  def fromAuxiliaryAddress(other: AuxiliaryAddress): Option[AddressResponseGeo] = (for {
    latitude <- Try(BigDecimal(other.location.lat))
    longitude <- Try(BigDecimal(other.location.lon))
  } yield AddressResponseGeo(latitude, longitude, 0, 0)).toOption
}
