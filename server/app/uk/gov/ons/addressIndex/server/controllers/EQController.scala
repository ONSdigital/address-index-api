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
                               eqPostcodeController: EQPostcodeController) extends PlayHelperController(versionProvider) {

  lazy val logger: AddressAPILogger = AddressAPILogger("address-index-server:EQController")

  def eqQuery(input: String,
              fallback: Option[String] = None,
              offset: Option[String] = None,
              limit: Option[String] = None,
              classificationfilter: Option[String] = None,
              historical: Option[String] = None,
              verbose: Option[String] = None,
              epoch: Option[String] = None,
              fromsource: Option[String] = None,
              highlight: Option[String] = None,
              favourpaf: Option[String] = None,
              favourwelsh: Option[String] = None
             ): Action[AnyContent] = Action async { implicit req =>

    if (isPostCode(input)) {
      logger.info("Input is postcode")
      eqPostcodeController.postcodeQuery(
        postcode = input,
        offset = offset,
        limit = limit,
        classificationfilter = classificationfilter,
        historical = historical,
        verbose = verbose,
        favourpaf = favourpaf,
        favourwelsh = favourwelsh,
        epoch=epoch) (req)
    } else {
      logger.info("input is partial address")
      eqPartialAddressController.partialAddressQuery(
        input = input,
        fallback = fallback,
        offset = offset,
        limit = limit,
        classificationfilter = classificationfilter,
        historical = historical,
        verbose = verbose,
        epoch = epoch,
        fromsource = fromsource,
        highlight = highlight,
        favourpaf = favourpaf,
        favourwelsh = favourwelsh) (req)
    }
  }

  /**
    * Determine if input is a postcode. Regex expression found here:
    * https://stackoverflow.com/questions/164979/regex-for-matching-uk-postcodes
    *
    * @param input the input string
    * @return
    */
  def isPostCode(input : String): Boolean = {

    val postCodePattern: Regex = "^([A-Za-z][A-Ha-hJ-Yj-y]?[0-9][A-Za-z0-9]? ?[0-9][A-Za-z]{2}|[Gg][Ii][Rr] ?0[Aa]{2})$".r

    postCodePattern.findFirstMatchIn(input) match {
      case Some(_) => true
      case None => false
    }
  }
}
