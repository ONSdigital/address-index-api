package uk.gov.ons.addressIndex.model

import play.api.libs.json._

/**
  * Created by amits on 11/07/2016.
  */

case class SingleMatchResponse(totalHits: Int,
                               candidate: List[Address])

case class Address(uprn: String,
                   postcodeLocator: String,
                   primaryAddress: String,
                   secondaryAddress: String,
                   streetDescription: String,
                   town: String,
                   matchScore: Float,
                   fullAddress: String)

case class SingleSearchForm(address: Option[String],
                            street: Option[String],
                            town: Option[String],
                            postcode: Option[String])



case class Candidate(matchScore: Int, matchingAddress: Address)

case class BulkMatchResponse(matchFound: Option[Int], possibleMatches: Option[Int], noMatch: Option[Int], totalNumberOfAddresses: Option[Int])

object JSONImplicit {
  implicit val addressRead = Json.reads[Address]
  implicit val singleMatchResponseRead = Json.reads[SingleMatchResponse]
}

