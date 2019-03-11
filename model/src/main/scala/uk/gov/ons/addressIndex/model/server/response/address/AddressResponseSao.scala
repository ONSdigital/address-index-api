package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

/**
  *
  * @param saoText        sub building name
  * @param saoStartNumber sub building number
  * @param saoStartSuffix
  * @param saoEndNumber
  * @param saoEndSuffix
  */
case class AddressResponseSao(saoText: String,
                              saoStartNumber: String,
                              saoStartSuffix: String,
                              saoEndNumber: String,
                              saoEndSuffix: String)

object AddressResponseSao {
  implicit lazy val addressResponseSaoFormat: Format[AddressResponseSao] = Json.format[AddressResponseSao]
}
