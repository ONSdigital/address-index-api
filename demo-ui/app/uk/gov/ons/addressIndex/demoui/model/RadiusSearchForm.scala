package uk.gov.ons.addressIndex.demoui.model

import play.api.libs.json._

/**
  * Form for single address
  */

case class RadiusSearchForm(address: String,
                            filter: String,
                            lat: String,
                            lon: String,
                            rangekm: String,
                            historical: Boolean,
                            startdate: String,
                            enddate: String)

object RadiusSearchForm {
  val jsonFmt: OFormat[RadiusSearchForm] = Json.format[RadiusSearchForm]
}