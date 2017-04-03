package uk.gov.ons.addressIndex.parsers

import com.typesafe.config.ConfigFactory
import uk.gov.ons.addressIndex.crfscala.CrfScala.{CrfTokenResult, CrfTokenable}

import scala.io.Source
import scala.util.Try

/**
  * Tokenizer for the input
  */
object Tokens extends CrfTokenable {


  private val config = ConfigFactory.load()


  val organisationName: String = "OrganisationName"
  val departmentName: String = "DepartmentName"
  val subBuildingName: String = "SubBuildingName"
  val buildingName: String = "BuildingName"
  val buildingNumber: String = "BuildingNumber"
  val paoStartNumber: String = "PaoStartNumber"
  val paoStartSuffix: String = "PaoStartSuffix"
  val paoEndNumber: String = "PaoEndNumber"
  val paoEndSuffix: String = "PaoEndSuffix"
  val saoStartNumber: String = "SaoStartNumber"
  val saoStartSuffix: String = "SaoStartSuffix"
  val saoEndNumber: String = "SaoEndNumber"
  val saoEndSuffix: String = "SaoEndSuffix"
  val streetName: String = "StreetName"
  val locality: String = "Locality"
  val townName: String = "TownName"
  val postcode: String = "Postcode"
  val postcodeIn: String = "PostcodeIn"
  val postcodeOut: String = "PostcodeOut"

  val all: Seq[String] = Seq(
    organisationName,
    departmentName,
    subBuildingName,
    buildingName,
    buildingNumber,
    paoStartNumber,
    paoStartSuffix,
    paoEndNumber,
    streetName,
    locality,
    townName,
    postcode
  )

  private val tokenDirectory = config.getString("parser.input-pre-post-processing.folder")

  /**
    * Tokenizes input into tokens (also removes counties, replaces synonyms)
    * @param input the string to be tokenized
    * @return Array of tokens
    */
  override def apply(input: String): Array[String] = normalizeInput(input).split(" ")

  /**
    * Normalizes input: removes counties, replaces synonyms, uppercase
    * @param input input to be normalized
    * @return normalized input
    */
  private def normalizeInput(input: String): String = {
    val upperInput = input.toUpperCase()

    val inputWithoutCounties = removeCounties(upperInput)

    val tokens = inputWithoutCounties
      .replaceAll("(\\d+[A-Z]?) *- *(\\d+[A-Z]?)", "$1-$2")
      .replaceAll("(\\d+)/(\\d+)", "$1-$2")
      .replaceAll("(\\d+) *TO *(\\d+)", "$1-$2")
      .replace(" IN ", " ")
      .replace(" CO ", " ")
      .replace(" - ", " ")
      .replace(",", " ")
      .replace("\\", " ")
      .split(" ")

    removeCounties(replaceSynonyms(tokens).filter(_.nonEmpty).mkString(" "))
  }

  private def removeCounties(input: String): String = {
    val separatedCounties = county.mkString("|")
    val countiesRegex = s"(?:\\b|\\s)($separatedCounties)(?:\\s|\\Z)"

    val separatedSuffixes = nonCountyIdentification.mkString("|")
    val suffixesRegex = s"(?!$separatedSuffixes&)"

    // The regexp is asking to take counties that don't have suffixes after them
    val regexp = s"$countiesRegex$suffixesRegex"

    input.replaceAll(regexp, " ")
  }

  private def replaceSynonyms(tokens: Array[String]): Array[String] =
    tokens.map(token => synonym.getOrElse(token, token))

  override def normalise(tokens: Array[String]): Array[String] = tokens.map(_.toUpperCase)


  /**
    * Normalizes tokens after they were tokenized and labeled
    *
    * @param tokens labeled tokens from the parser
    * @return Map with label -> concatenated tokens ready to be sent to the ES
    */
  def postTokenizeTreatment(tokens: Seq[CrfTokenResult]): Map[String, String] = {
    val groupedTokens = tokens.groupBy(_.label).map { case (label, crfTokenResults) =>
      label -> crfTokenResults.map(_.value).mkString(" ")
    }

    val postcodeTreatedTokens = postTokenizeTreatmentPostCode(groupedTokens)
    val boroughTreatedTokens = postTokenizeTreatmentBorough(postcodeTreatedTokens)
    val buildingNumberTreatedTokens = postTokenizeTreatmentBuildingNumber(boroughTreatedTokens)
    val buildingNameTreatedTokens = postTokenizeTreatmentBuildingName(buildingNumberTreatedTokens)

    buildingNameTreatedTokens
  }

  /**
    * Normalizes postcode address
    * @param tokens tokens grouped by label
    * @return Map with tokens that will contain normalized postcode address
    */
  def postTokenizeTreatmentPostCode(tokens: Map[String, String]): Map[String, String] = {

    // Before analyzing the postcode, we also remove whitespaces so that they don't influence the outcome
    val postcodeToken: Option[String] = tokens.get(postcode).map(_.replaceAll("\\s", ""))

    postcodeToken match {
      case Some(concatenatedPostcode) if concatenatedPostcode.length >= 5 =>
        val postcodeInToken = concatenatedPostcode.substring(concatenatedPostcode.length - 3, concatenatedPostcode.length)
        val postcodeOutToken = concatenatedPostcode.substring(0, concatenatedPostcode.indexOf(postcodeInToken))

        val tokensWithPostcodeUpdated = tokens.updated(postcode, s"$postcodeOutToken $postcodeInToken")

        tokensWithPostcodeUpdated ++ Map(
          postcodeOut -> postcodeOutToken,
          postcodeIn -> postcodeInToken
        )

      case Some(concatenatedPostcode) => tokens + (postcodeOut -> concatenatedPostcode)

      case _ => tokens
    }
  }

  /**
    * Some London street names contain boroughs that are not useful for search inside street name
    * We should extract them into locality
    * @param tokens current tokens
    * @return tokens with extracted borough
    */
  def postTokenizeTreatmentBorough(tokens: Map[String, String]): Map[String, String] ={
    val streetNameToken = tokens.get(streetName)
    val townNameToken = tokens.get(townName)

    (streetNameToken, townNameToken) match {
      case (Some(londonStreetName), Some("LONDON")) =>
        val boroughToRemove = borough.find(londonStreetName.endsWith)

        boroughToRemove match {
          case Some(londonBorough) =>
            tokens.updated(streetName, londonStreetName.replace(londonBorough, "").trim) + (locality -> londonBorough)

          case _ => tokens
        }

      case _ => tokens
    }
  }

  /**
    * Adds paoStartNumber token if buildingNumber token is present
    * so that we can query LPI addresses
    * @param tokens tokens grouped by label
    * @return map with tokens that will also contain paoStartNumberToken if buildingNumber is present
    */
  def postTokenizeTreatmentBuildingNumber(tokens: Map[String, String]): Map[String, String]= {
    // This is a failsafe in case building number is not a number
    val buildingNumberToken: Option[String] = tokens.get(buildingNumber).flatMap(token => Try(token.toShort.toString).toOption)

    buildingNumberToken match {
      case Some(number) => tokens + (paoStartNumber -> number)

      // Token is either not found or is not a number
      case None => tokens - buildingNumber
    }
  }

  /**
    * Parses buildingName / subBuildingName and fills pao/sao depending on the contents
    * (and depending if buildingNumber is present)
    * @param tokens tokens grouped by label
    * @return map with tokens that will also contain paoStartNumber and paoStartSuffix tokens if buildingName is present
    */
  def postTokenizeTreatmentBuildingName(tokens: Map[String, String]): Map[String, String]= {

    val (buildingNameToken: Option[String], subBuildingNameToken: Option[String]) = assignBuildingNames(tokens)

    val buildingNameSplit: BuildingNameSplit = splitBuildingName(buildingNameToken)
    val subBuildingNameSplit: BuildingNameSplit = splitBuildingName(subBuildingNameToken)

    // It is now safe to fill pao/sao fields because paoStartNumber filtered out buildingName in the steps before
    // but first of all we need to remove empty parsed tokens
    // What remains will be transformed into tuple and inserted into `tokens` map
    val newTokens = Seq(
      buildingNameSplit.startNumber.map(token => paoStartNumber -> token),
      buildingNameSplit.startSuffix.map(token => paoStartSuffix -> token),
      buildingNameSplit.endNumber.map(token => paoEndNumber -> token),
      buildingNameSplit.endSuffix.map(token => paoEndSuffix -> token),
      subBuildingNameSplit.startNumber.map(token => saoStartNumber -> token),
      subBuildingNameSplit.startSuffix.map(token => saoStartSuffix -> token),
      subBuildingNameSplit.endNumber.map(token => saoEndNumber -> token),
      subBuildingNameSplit.endSuffix.map(token => saoEndSuffix -> token)
    ).flatten

    tokens ++ newTokens
  }

  /**
    * Similar to a table (present/not present) for three values: BuildingNumber, BuildingName, SubBuildingName,
    * analyses every possible permutation
    * @param tokens parsed tokens
    * @return Tuple with BuildingName and SubBuildingName
    */
  private def assignBuildingNames(tokens: Map[String, String]): (Option[String], Option[String]) =
    (tokens.get(buildingNumber), tokens.get(buildingName), tokens.get(subBuildingName)) match {
      case (None, None, None) => (None, None)
      case (Some(_), None, None) => (None, None)

      case (None, Some(_), None) =>
        // this is a special case where we can have 2 ranges in a building name
        val buildingNameToken = tokens.getOrElse(buildingName, "")
        val buildingNameNumbers = buildingNameToken.split(" ").filter(isLikeBuildingName)

        if (buildingNameNumbers.length == 2) (Some(buildingNameNumbers(1)),Some(buildingNameNumbers(0)))
        else (tokens.get(buildingName), None)

      case (None, None, Some(_)) =>
        // this is a special case where we can have 2 ranges in a sub building name
        val subBuildingNameToken = tokens.getOrElse(subBuildingName, "")
        val subBuildingNameNumbers = subBuildingNameToken.split(" ").filter(isLikeBuildingName)

        if (subBuildingNameNumbers.length == 2) (Some(subBuildingNameNumbers(1)), Some(subBuildingNameNumbers(0)))
        else (None, tokens.get(subBuildingName))

      case (Some(_), Some(_), None) => (None, tokens.get(buildingName))
      case (None, Some(_), Some(_)) => (tokens.get(buildingName), tokens.get(subBuildingName))
      case (Some(_), None, Some(_)) => (None, tokens.get(subBuildingName))
      case (Some(_), Some(_), Some(_)) => (None, tokens.get(subBuildingName))
    }


  private def isLikeBuildingName(token: String): Boolean = token.matches("""^\d+[A-Z]?$|^\d+[A-Z]?-\d+[A-Z]?$""")

  /**
    * Small case class that is mainly used to replace a Tuple4 and to improve readability
    * @param startNumber pao/sao start number
    * @param startSuffix pao/sao start suffix
    * @param endNumber pao/sao end number
    * @param endSuffix pao/sao end suffix
    */
  private case class BuildingNameSplit(
    startNumber: Option[String] = None,
    startSuffix: Option[String] = None,
    endNumber: Option[String] = None,
    endSuffix: Option[String] = None
  )

  /**
    * Parses buildingName and extracts its parts
    * All numbers should be `Short`
    * @param buildingName building name to be parsed
    * @return extracted parts in a form of a `BuildingNameSplit` class
    */
  private def splitBuildingName(buildingName: Option[String]): BuildingNameSplit = {

    val buildingNameNumber = """.*?(\d+).*?""".r
    val buildingNameLetter = """.*?(\d+)([A-Z]).*?""".r
    val buildingNameRange = """.*?(\d+)-(\d+).*?""".r
    val buildingNameRangeStartSuffix = """.*?(\d+)([A-Z])-(\d+).*?""".r
    val buildingNameRangeEndSuffix = """.*?(\d+)-(\d+)([A-Z]).*?""".r
    val buildingNameRangeStartSuffixEndSuffix = """.*?(\d+)([A-Z])-(\d+)([A-Z]).*?""".r

    // order is important
    buildingName match {
      case Some(buildingNameRangeStartSuffixEndSuffix(startNumber, startSuffix, endNumber, endSuffix)) =>
        BuildingNameSplit(
          startNumber = Try(startNumber.toShort.toString).toOption,
          startSuffix = Some(startSuffix),
          endNumber = Try(endNumber.toShort.toString).toOption,
          endSuffix = Some(endSuffix)
        )

      case Some(buildingNameRangeEndSuffix(startNumber, endNumber, endSuffix)) =>
        BuildingNameSplit(
          startNumber = Try(startNumber.toShort.toString).toOption,
          endNumber = Try(endNumber.toShort.toString).toOption,
          endSuffix = Some(endSuffix)
        )

      case Some(buildingNameRangeStartSuffix(startNumber, startSuffix, endNumber)) =>
        BuildingNameSplit(
          startNumber = Try(startNumber.toShort.toString).toOption,
          startSuffix = Some(startSuffix),
          endNumber = Try(endNumber.toShort.toString).toOption
        )

      case Some(buildingNameRange(startNumber, endNumber)) =>
        BuildingNameSplit(
          startNumber = Try(startNumber.toShort.toString).toOption,
          endNumber = Try(endNumber.toShort.toString).toOption
        )

      case Some(buildingNameLetter(startNumber, startSuffix)) =>
        BuildingNameSplit(
          startNumber = Try(startNumber.toShort.toString).toOption,
          startSuffix = Some(startSuffix)
        )

      case Some(buildingNameNumber(number)) =>
        BuildingNameSplit(startNumber = Try(number.toShort.toString).toOption)

      case _ => BuildingNameSplit()
    }
  }

  /**
    * Concatenates post-processed tokens so that we could use it against special xAll fields
    * IMPORTANT! Locality is not included due to it screwing up the fallback query
    * @param tokens post-processed tokens
    * @return concatenated resulting string
    */
  def concatenate(tokens: Map[String, String]): String =
    Seq(organisationName, departmentName, subBuildingName, buildingName, buildingNumber,
      streetName, townName, postcode).map(label => tokens.getOrElse(label, "")).filter(_.nonEmpty).mkString(" ")


  // `lazy` is needed so that if this is called from other modules, during tests, it doesn't throw exception
  lazy val directions: Seq[String] = fileToList(s"${tokenDirectory}direction")

  lazy val flat: Seq[String] = fileToList(s"${tokenDirectory}flat")

  lazy val company: Seq[String] = fileToList(s"${tokenDirectory}company")

  lazy val road: Seq[String] = fileToList(s"${tokenDirectory}road")

  lazy val residential: Seq[String] = fileToList(s"${tokenDirectory}residential")

  lazy val business: Seq[String] = fileToList(s"${tokenDirectory}business")

  lazy val locational: Seq[String] = fileToList(s"${tokenDirectory}locational")

  lazy val ordinal: Seq[String] = fileToList(s"${tokenDirectory}ordinal")

  lazy val outcodes: Seq[String] = fileToList(s"${tokenDirectory}outcode")

  lazy val postTown: Seq[String] = fileToList(s"${tokenDirectory}posttown")

  lazy val borough: Seq[String] = fileToList(s"${tokenDirectory}borough")

  /**
    * Contains key-value map of synonyms (replace key by value)
    */
  lazy val synonym: Map[String, String] = fileToList(s"${tokenDirectory}synonym")
    // The "-1" in split is to catch the trailing empty space on the lines like "ENGLAND,"
    .map(_.split(",", -1))
    .filter(_.nonEmpty)
    .map(synonymReplacement => (synonymReplacement.head, synonymReplacement.last))
    .toMap

  /**
    * List of counties to be removed from the input
    */
  lazy val county: Seq[String] = fileToList(s"${tokenDirectory}county")

  /**
    * A county is a county if it is not followed by following suffixes
    */
  lazy val nonCountyIdentification: Seq[String] = fileToList(s"${tokenDirectory}non_county_identification")

  private def fileToList(path: String): Seq[String] = Source.fromURL(getClass.getResource(path)).getLines().toList

}
