package uk.gov.ons.addressIndex.demoui.model

import play.api.libs.json._

/**
  * Form for single address
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

case class SingleSearchForm(
 address: String,
 format: String = "paf"
)
object SingleSearchForm {
  implicit val jsonFmt = Json.format[SingleSearchForm]
}

object JSONImplicits {
  implicit val addressRead = Json.reads[Address]
  implicit val singleMatchResponseRead = Json.reads[SingleMatchResponse]
}