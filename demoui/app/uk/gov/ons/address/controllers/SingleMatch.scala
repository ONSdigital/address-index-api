package uk.gov.ons.address.controllers

import javax.inject.{Inject, Singleton}

import org.slf4j.LoggerFactory
import play.api.Logger
import play.api.data.Forms._
import play.api.data._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Controller, _}
import uk.gov.ons.address.client.AddressApiClient
import uk.gov.ons.address.conf.OnsFrontendConfiguration
import uk.gov.ons.address.model._
import uk.gov.ons.address.views

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SingleMatch @Inject()(
    addressApiClient: AddressApiClient,
    onsFrontendConfiguration: OnsFrontendConfiguration,
    val messagesApi: MessagesApi)(implicit exec: ExecutionContext)
    extends Controller
    with I18nSupport {
  val logger = Logger("app-log")
  def showSingleMatchPage = Action {
    Ok(
        views.html.singleMatch(None,
                               SingleMatch.singleSearchForm,
                               None,
                               onsFrontendConfiguration))
  }

  def doSingleMatch() = Action.async { implicit request =>
    val address  = request.body.asFormUrlEncoded.get("address").mkString
 //   val street   = request.body.asFormUrlEncoded.get("street").mkString
 //   val town     = request.body.asFormUrlEncoded.get("town").mkString
 //   val postCode = request.body.asFormUrlEncoded.get("postcode").mkString
  //  val inputAddress = address + " " + street + " " + town + " " + postCode
   val inputAddress = address
    if (inputAddress.trim.isEmpty) {
      logger.debug("Empty input address")
      Future.successful(Ok(
        views.html.singleMatch(
          None,
          SingleMatch.singleSearchForm,
          Some("Please provide an address for matching!"),
          onsFrontendConfiguration
        )
      )
      )
    } else {
      logger.debug("Calling Single match api")
      val result = addressApiClient.singleMatch(inputAddress)
      result.map(a => Ok(
        views.html.singleMatch(
          Some(a),
          SingleMatch.singleSearchForm,
          None,
          onsFrontendConfiguration
        )
      )
      )
    }
  }
}

object SingleMatch {
  val singleSearchForm = Form(
      mapping(
          "address" -> optional(text),
          "street" -> optional(text),
          "town" -> optional(text),
          "postcode" -> optional(text)
      )(SingleSearchForm.apply)(SingleSearchForm.unapply)
  )
}
