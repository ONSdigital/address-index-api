package uk.gov.ons.addressIndex.model.db.index

import play.api.libs.json.{Format, Json}

/**
  * Postcode grouping DTO
  * Captures output from ES aggregation for part postcode
  */
case class PostcodeGroup(postcode: String,
                         addressCount: Int)

object PostcodeGroup {
  implicit lazy val postcodeGroupFormat: Format[PostcodeGroup] = Json.format[PostcodeGroup]
}

