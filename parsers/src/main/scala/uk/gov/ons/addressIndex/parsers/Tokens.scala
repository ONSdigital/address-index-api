package uk.gov.ons.addressIndex.parsers

import com.typesafe.config.ConfigFactory
import org.apache.commons.lang3.StringUtils

import scala.io.{BufferedSource, Source}
import scala.util.Try

/**
  * Hold
  */
object Tokens {

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

  val defaultPreProcessFolder = "parser.input-pre-post-processing.folder"
  val defaultMapFolder = "parser.scoring.folder"
  val defaultCodelistFolder = "parser.codelist.folder"
  val defaultDelimiter = "="

  /**
    * Does pre-tokenization treatment to the input (normalization + splitting)
    * @param input input from user
    * @return tokensm ready to be sent to the parser
    */
  def preTokenize(input: String): List[String] = normalizeInput(input).split(" ").toList

  /**
    * Normalizes input: removes counties, replaces synonyms, uppercase
    * @param input input to be normalized
    * @return normalized input
    */
  private def normalizeInput(input: String): String = {
    val upperInput = input.toUpperCase()

    val inputWithoutAccents = StringUtils.stripAccents(upperInput)

    val tokens = inputWithoutAccents
      .replaceAll("(\\d+[A-Z]?) *- *(\\d+[A-Z]?)", "$1-$2")
      .replaceAll("(\\d+)/(\\d+)", "$1-$2")
      .replaceAll("(\\d+) *TO *(\\d+)", "$1-$2")
      .replace(" IN ", " ")
      .replace(" - ", " ")
      .replace(",", " ")
      .replace("\\", " ")
      .split(" ")

    removeCounties(replaceSynonyms(tokens).filter(_.nonEmpty).mkString(" "))
  }

  private def removeCounties(input: String): String = {
    val separatedCounties = county.mkString("|")

    val countiesRegex = s"\\b($separatedCounties)\\b"

    // ONSAI-531
    val exceptRegex = s"(?<!\\bON\\s)(?<!\\bDINAS\\s)(?<!\\bUPON\\s)(?<!\\b[0-9]\\s)"

    val lookBehindRegex = s"$exceptRegex$countiesRegex"

    /**
      * A county is a county if it is not followed by following suffixes
      */
    val separatedSuffixes = List(nonCounty, company, flat, residential, road).flatten.mkString("|")

    val lookAheadRegex = s"(?!\\s($separatedSuffixes)\\b)"

    val regexp = s"$lookBehindRegex$lookAheadRegex".r

    regexp.replaceAllIn(input, " ").replaceAll("\\s+", " ").trim
  }

  private def replaceSynonyms(tokens: Array[String]): Array[String] =
    tokens.map(token => synonym.getOrElse(token, token))

  /**
    * Normalizes tokens after they were tokenized and labeled
    *
    * @param tokens labeled tokens from the parser
    * @return Map with label -> concatenated tokens ready to be sent to the ES
    */
  def postTokenize(tokens: Map[String, String]): Map[String, String] = {
    val postcodeTreatedTokens = postTokenizeTreatmentPostCode(tokens)
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
      // use lastindexOf instead of indexOf to cater for duplicate postcode parts
        val postcodeOutToken = concatenatedPostcode.substring(0, concatenatedPostcode.lastIndexOf(postcodeInToken))

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
    * @param tokens post-processed tokens
    * @return concatenated resulting string
    */
  def concatenate(tokens: Map[String, String]): String =
    Seq(organisationName, departmentName, subBuildingName, buildingName, buildingNumber,
      streetName, locality, townName, postcode).map(label => tokens.getOrElse(label, "")).filter(_.nonEmpty).mkString(" ")


  // `lazy` is needed so that if this is called from other modules, during tests, it doesn't throw exception
  lazy val directions: Seq[String] = fileToList(s"direction")

  lazy val flat: Seq[String] = fileToList(s"flat")

  lazy val company: Seq[String] = fileToList(s"company")

  lazy val road: Seq[String] = fileToList(s"road")

  lazy val residential: Seq[String] = fileToList(s"residential")

  lazy val business: Seq[String] = fileToList(s"business")

  lazy val locational: Seq[String] = fileToList(s"locational")

  lazy val ordinal: Seq[String] = fileToList(s"ordinal")

  lazy val outcodes: Seq[String] = fileToList(s"outcode")

  lazy val postTown: Seq[String] = fileToList(s"posttown")

  lazy val borough: Seq[String] = fileToList(s"borough")

  lazy val nonCounty: Seq[String] = fileToList(s"nonCounty")

  // score matrix is used by server but held in parsers for convenience
  lazy val scoreMatrix: Map[String,String] = fileToMap(s"scorematrix.txt")

  /**
    * Contains key-value map of synonyms (replace key by value)
    */
  lazy val synonym: Map[String, String] = fileToList(s"synonym")
    // The "-1" in split is to catch the trailing empty space on the lines like "ENGLAND,"
    .map(_.split(",", -1))
    .filter(_.nonEmpty)
    .map(synonymReplacement => (synonymReplacement.head, synonymReplacement.last))
    .toMap

  /**
    * List of counties to be removed from the input
    */
  lazy val county: Seq[String] = fileToList(s"county")

  /**
    * List of codelists
    */
  lazy val codeList: Seq[String] = Tokens.fileToArray(s"codelistList")

  /**
    * List of classifications
    */
  lazy val classList: Seq[String] = Tokens.fileToArray(s"classificationList")

  /**
    * List of sources
    */
  lazy val sourceList: Seq[String] = Tokens.fileToArray(s"sourceList")

  /**
    * List of logicalStatuses
    */
  lazy val logicalStatusList: Seq[String] = Tokens.fileToArray(s"logicalStatusList")

  /**
    * List of custodians
    */
  lazy val custodianList: Seq[String] = Tokens.fileToArray(s"custodianList")


  /**
    * Convert external file into list
    * @param folder
    * @param fileName
    * @return
    */
  private def fileToList(fileName: String, folder: String = defaultPreProcessFolder): Seq[String] = {
    val resource = getResource(fileName, folder)
    resource.getLines().toList
  }

  /**
    * Convert external file into array
    * @param folder
    * @param fileName
    * @return
    */
  def fileToArray(fileName: String, folder: String = defaultCodelistFolder): Seq[String] = {
    val resource = getResource(fileName, folder)
    val lines = (for (line <- resource.getLines()) yield line).toList
    resource.close
    lines
  }


  /**
    * Make external file such as score matrix file into Map
    *
    * @param fileName name of the file
    * @param folder optional, config field that holds path to the folder
    * @param delimiter optional, delimiter of values in the file, defaults to "="
    * @return Map containing key -> value from the file
    */
  def fileToMap(fileName: String, folder: String = defaultMapFolder, delimiter: String = defaultDelimiter): Map[String,String] = {
    val resource = getResource(fileName, folder)
    resource.getLines().map { l =>
      val Array(k,v1,_*) = l.split(delimiter)
      k -> v1
    }.toMap
  }

  /**
    * Fetch file stream as buffered source
    * @param folder
    * @param fileName
    * @return
    */
  def getResource(fileName: String, folder: String): BufferedSource = {
    val directory = config.getString(folder)
    val path = directory + fileName
    val currentDirectory = new java.io.File(".").getCanonicalPath
    // `Source.fromFile` needs an absolute path to the file, and current directory depends on where sbt was lauched
    // `getResource` may return null, that's why we wrap it into an `Option`
    Option(getClass.getResource(path)).map(Source.fromURL).getOrElse(Source.fromFile(currentDirectory + path))
  }

}
