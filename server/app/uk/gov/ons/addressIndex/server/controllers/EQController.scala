package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.ons.addressIndex.server.modules.VersionModule
import uk.gov.ons.addressIndex.server.utils.AddressAPILogger

import scala.util.matching.Regex

@Singleton
class EQController @Inject () (val controllerComponents: ControllerComponents,
                               eqPartialAddressController: EQPartialAddressController,
                               versionProvider: VersionModule,
                               postcodeController: PostcodeController) extends PlayHelperController(versionProvider) {

  lazy val logger: AddressAPILogger = AddressAPILogger("address-index-server:EQController")
  // Decide which endpoint
  //
  def eqQuery(input: String,
              fallback: Option[String] = None,
              offset: Option[String] = None,
              limit: Option[String] = None,
              classificationfilter: Option[String] = None,
              historical: Option[String] = None,
              verbose: Option[String] = None,
              epoch: Option[String] = None,
              fromsource: Option[String] = None,
              highverbose: Option[String] = None,
              favourpaf: Option[String] = None,
              favourwelsh: Option[String] = None
             ): Action[AnyContent] = Action async { implicit req =>

    if (isPostCode(input)) {
      postcodeController.postcodeQuery(input, offset, limit, classificationfilter,historical, verbose, epoch) (req)
    } else {
      eqPartialAddressController.partialAddressQuery(input,
        fallback, offset, limit, classificationfilter, historical,
        verbose, epoch, fromsource)(req)
    }
  }

  def isPostCode(input : String): Boolean = {

    //https://stackoverflow.com/questions/164979/regex-for-matching-uk-postcodes
    val postCodePattern: Regex = "^([A-Za-z][A-Ha-hJ-Yj-y]?[0-9][A-Za-z0-9]? ?[0-9][A-Za-z]{2}|[Gg][Ii][Rr] ?0[Aa]{2})$".r

    postCodePattern.findFirstMatchIn(input) match {
      case Some(_) => true
      case None => false
    }
  }
}
