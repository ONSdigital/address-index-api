package uk.gov.ons.addressIndex.server.controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.ons.addressIndex.server.modules.VersionModule
import uk.gov.ons.addressIndex.server.utils.AddressAPILogger
import uk.gov.ons.addressIndex.server.utils.GroupOptions

import scala.util.Try
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
              favourwelsh: Option[String] = None,
              eboost: Option[String] = None,
              nboost: Option[String] = None,
              sboost: Option[String] = None,
              wboost: Option[String] = None,
              groupfullpostcodes: Option[String] = None
             ): Action[AnyContent] = Action async { implicit req =>

    val groupFullPostcodesVal: String = groupfullpostcodes.getOrElse(GroupOptions.NO.toString)

    // we want to test the string with spaces as supplied by the user first
  // if no match try again with spaces removed.
    val normalizedInput: String = stripSpaces(input.toUpperCase)
    if (isPostCode(normalizedInput)) {
      if (groupFullPostcodesVal.equals(GroupOptions.YES.toString)) {
        val postcodeFormatted: String = if (!input.contains(" ")) {
          val (postcodeStart, postcodeEnd) = input.splitAt(input.length() - 3)
          (postcodeStart + " " + postcodeEnd).toUpperCase
        } else input.toUpperCase
        logger.info("Input is a postcode but grouping is requested")
        groupedPostcodeController.groupedPostcodeQuery(
          postcode = postcodeFormatted,
          offset = offset,
          limit = limit,
          classificationfilter = classificationfilter,
          historical = historical,
          verbose = verbose,
          epoch = epoch)(req)
      } else {
        logger.info("Input is postcode, groupfullpostcode setting = "  + groupFullPostcodesVal)
        eqPostcodeController.postcodeQuery(
          postcode = normalizedInput,
          offset = offset,
          limit = limit,
          classificationfilter = classificationfilter,
          historical = historical,
          verbose = verbose,
          favourpaf = favourpaf,
          favourwelsh = favourwelsh,
          epoch = epoch,
          groupfullpostcodes = Some(groupFullPostcodesVal)
        )(req)
      }

    } else if (isOutCodeAndSectorAndHalfUnitWithSpace(input)){
      logger.info("Input is the outcode and most of incode parts of a postcode")
      groupedPostcodeController.groupedPostcodeQuery(
        postcode = if (input.contains(" ")) input else addSpaces(input,2),
        offset = offset,
        limit = limit,
        classificationfilter = classificationfilter,
        historical = historical,
        verbose = verbose,
        epoch = epoch)(req)
    } else if (isOutCodeAndSectorWithSpace(input)){
      logger.info("Input is the outcode and sector parts of a postcode")
      groupedPostcodeController.groupedPostcodeQuery(
        postcode = if (input.contains(" ")) input else addSpaces(input,1),
        offset = offset,
        limit = limit,
        classificationfilter = classificationfilter,
        historical = historical,
        verbose = verbose,
        epoch = epoch)(req)
    } else if (isOutCode(input)){
      logger.info("Input is the outcode part of a postcode")
      groupedPostcodeController.groupedPostcodeQuery(
        postcode = input,
        offset = offset,
        limit = limit,
        classificationfilter = classificationfilter,
        historical = historical,
        verbose = verbose,
        epoch = epoch)(req)
    } else if (isOutCodeAndSectorAndHalfUnit(normalizedInput)){
      logger.info("Input is the outcode and most of incode parts of a postcode")
      groupedPostcodeController.groupedPostcodeQuery(
        postcode = addSpaces(normalizedInput,2),
        offset = offset,
        limit = limit,
        classificationfilter = classificationfilter,
        historical = historical,
        verbose = verbose,
        epoch = epoch)(req)
    } else if (isOutCodeAndSector(normalizedInput)){
      logger.info("Input is the outcode and sector parts of a postcode")
      groupedPostcodeController.groupedPostcodeQuery(
        postcode = addSpaces(normalizedInput,1),
        offset = offset,
        limit = limit,
        classificationfilter = classificationfilter,
        historical = historical,
        verbose = verbose,
        epoch = epoch)(req)
    } else if (isOutCode(normalizedInput)){
      logger.info("Input is the outcode part of a postcode")
      groupedPostcodeController.groupedPostcodeQuery(
        postcode = normalizedInput,
        offset = offset,
        limit = limit,
        classificationfilter = classificationfilter,
        historical = historical,
        verbose = verbose,
        epoch = epoch)(req)
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
        favourwelsh = favourwelsh,
        eboost = eboost,
        nboost = nboost,
        sboost = sboost,
        wboost = wboost) (req)
    }
  }

  /**
    * Determine if input is an outcode
    * trailing space optional
    *
    * @param input the input string
    * @return
    */
  def isOutCode(input : String): Boolean = {

    val postCodePattern: Regex = "^([A-Za-z][A-Ha-hJ-Yj-y]?[0-9][A-Za-z0-9]? ?)$".r

    postCodePattern.findFirstMatchIn(input) match {
      case Some(_) => true
      case None => false
    }
  }

  /**
    * Determine if input is an outcode and a sector
    * space optional
    *
    * @param input the input string
    * @return
    */
  def isOutCodeAndSector(input : String): Boolean = {

    val postCodePattern: Regex = "^([A-Za-z][A-Ha-hJ-Yj-y]?[0-9][A-Za-z0-9]? ?[0-9])$".r

    postCodePattern.findFirstMatchIn(input) match {
      case Some(_) => true
      case None => false
    }
  }

  /**
    * Determine if input is an outcode and a sector
    * space mandatory
    *
    * @param input the input string
    * @return
    */
  def isOutCodeAndSectorWithSpace(input : String): Boolean = {

    val postCodePattern: Regex = "^([A-Za-z][A-Ha-hJ-Yj-y]?[0-9][A-Za-z0-9]? [0-9])$".r

    postCodePattern.findFirstMatchIn(input) match {
      case Some(_) => true
      case None => false
    }
  }

  /**
    * Determine if input is a postcode apart from final character of incode
    * space optional
    *
    * @param input the input string
    * @return
    */
  def isOutCodeAndSectorAndHalfUnit(input : String): Boolean = {

    val postCodePattern: Regex = "^([A-Za-z][A-Ha-hJ-Yj-y]?[0-9][A-Za-z0-9]? ?[0-9][A-Za-z])$".r

    postCodePattern.findFirstMatchIn(input) match {
      case Some(_) => true
      case None => false
    }

  }

  /**
    * Determine if input is a postcode apart from final character of incode
    * space mandatory
    *
    * @param input the input string
    * @return
    */
  def isOutCodeAndSectorAndHalfUnitWithSpace(input : String): Boolean = {

    val postCodePattern: Regex = "^([A-Za-z][A-Ha-hJ-Yj-y]?[0-9][A-Za-z0-9]? [0-9][A-Za-z])$".r

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
