package uk.gov.ons.addressIndex.model.db.index

/**
  * Postcode grouping DTO
  * Captures output from ES aggregation for part postcode
  */
case class PostcodeGroup(postcode: String,
                         addressCount: Int)
