package uk.gov.ons.addressIndex.demoui.model

import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.libs.json._
import play.api.data.Forms._

/**
  * Form for single address
  */

case class SingleSearchForm(
 address: String
)

object SingleSearchForm {
  val jsonFmt = Json.format[SingleSearchForm]
  val form = Form(
    mapping(
      "address" -> text
    )(SingleSearchForm.apply)(SingleSearchForm.unapply)
  )
}

