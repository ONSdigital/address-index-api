package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

case class CountryBoosts(eboost: Double,
                         nboost: Double,
                         sboost: Double,
                         wboost: Double,
                         lboost: Double,
                         mboost: Double,
                         jboost: Double)

object CountryBoosts {
  implicit lazy val countryBoostFormat: Format[CountryBoosts] = Json.format[CountryBoosts]
}
