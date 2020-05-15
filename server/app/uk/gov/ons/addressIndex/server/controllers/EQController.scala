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
                               eqPostcodeController: EQPostcodeController,
                               groupedPostcodeController: GroupedPostcodeController) extends PlayHelperController(versionProvider) {

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

    val normalizedInput: String = stripSpaces(input.toUpperCase)

    if (isPostCode(normalizedInput)) {
      logger.warn("Input is postcode")
      eqPostcodeController.postcodeQuery(
        postcode = normalizedInput,
        offset = offset,
        limit = limit,
        classificationfilter = classificationfilter,
        historical = historical,
        verbose = verbose,
        favourpaf = favourpaf,
        favourwelsh = favourwelsh,
        epoch = epoch)(req)
    } else if (isOutCodeAndSectorAndHalfUnit(normalizedInput)){
      logger.warn("Input is the outcode and most of incode parts of a postcode")
      groupedPostcodeController.groupedPostcodeQuery(
        postcode = addSpaces(normalizedInput,2),
        offset = offset,
        limit = limit,
        classificationfilter = classificationfilter,
        historical = historical,
        verbose = verbose,
 //       favourpaf = favourpaf,
 //       favourwelsh = favourwelsh,
        epoch = epoch)(req)
    } else if (isOutCodeAndSector(normalizedInput)){
      logger.warn("Input is the outcode and sector parts of a postcode")
      groupedPostcodeController.groupedPostcodeQuery(
        postcode = addSpaces(normalizedInput,1),
        offset = offset,
        limit = limit,
        classificationfilter = classificationfilter,
        historical = historical,
        verbose = verbose,
        //       favourpaf = favourpaf,
        //       favourwelsh = favourwelsh,
        epoch = epoch)(req)
    } else if (isOutCode(normalizedInput)){
      logger.warn("Input is the outcode part of a postcode")
      groupedPostcodeController.groupedPostcodeQuery(
        postcode = normalizedInput,
        offset = offset,
        limit = limit,
        classificationfilter = classificationfilter,
        historical = historical,
        verbose = verbose,
        //       favourpaf = favourpaf,
        //      favourwelsh = favourwelsh,
        epoch = epoch)(req)

    } else {
      logger.warn("input is partial address")
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

  def isOutCode(input : String): Boolean = {

    val postCodePattern: Regex = "^([A-Za-z][A-Ha-hJ-Yj-y]?[0-9][A-Za-z0-9]? ?)$".r

    postCodePattern.findFirstMatchIn(input) match {
      case Some(_) => true
      case None => false
    }
  }

  def isOutCodeAndSector(input : String): Boolean = {

    val postCodePattern: Regex = "^([A-Za-z][A-Ha-hJ-Yj-y]?[0-9][A-Za-z0-9]? ?[0-9])$".r

    postCodePattern.findFirstMatchIn(input) match {
      case Some(_) => true
      case None => false
    }
  }

  def isOutCodeAndSectorAndHalfUnit(input : String): Boolean = {

    val postCodePattern: Regex = "^([A-Za-z][A-Ha-hJ-Yj-y]?[0-9][A-Za-z0-9]? ?[0-9][A-Za-z]{1})$".r

    postCodePattern.findFirstMatchIn(input) match {
      case Some(_) => true
      case None => false
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

  /**
    * remove all whitespace characters but leave a single space at the end if there already
    *  is one so that SO1%20 and SO1 give different results
    *
    * @param str the input string
    * @return
    */
  def stripSpaces(str: String): String = {

    val possibleSpace = if (str.takeRight(1) == " ") " " else ""
    str.replaceAll("\\s", "") + possibleSpace
  }

  /**
    * To make the groupedPostcode work correctly, we need to add back in the
    *  space after the outcode.
    *
    * @param str the input string
    * @param fromRight number of character from end the space should go
    * @return
    */
  def addSpaces(str: String, fromRight: Int): String = {
    str.take(str.length - fromRight) + " " + str.takeRight(fromRight)
  }



}
