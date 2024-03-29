package uk.gov.ons.addressIndex.demoui.model

import play.api.libs.json._

/**
  * Form for single address
  */

case class PostcodeSearchForm(address: String,
                              filter: String,
                              historical: Boolean,
                              epoch: String,
                              startdate: String,
                              enddate: String)

object PostcodeSearchForm {
  val jsonFmt: OFormat[PostcodeSearchForm] = Json.format[PostcodeSearchForm]
}

