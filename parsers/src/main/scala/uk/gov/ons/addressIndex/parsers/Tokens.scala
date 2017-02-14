package uk.gov.ons.addressIndex.parsers

import com.typesafe.config.ConfigFactory
import uk.gov.ons.addressIndex.crfscala.CrfScala.{CrfTokenResult, CrfTokenable}

import scala.io.Source
import scala.util.matching.Regex

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
  val streetName: String = "StreetName"
  val locality: String = "Locality"
  val townName: String = "TownName"
  val postcode: String = "Postcode"

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

  private val currentDirectory = new java.io.File(".").getCanonicalPath
  private val tokenDirectory = s"$currentDirectory${config.getString("parser.input-pre-post-processing.folder")}"

  /**
    * Tokenizes input into tokens (also removes counties, replaces synonyms)
    * @param input the string to be tokenized
    * @return Array of tokens
    */
  override def apply(input: String): Array[String] ={
    val upperInput = input.toUpperCase()

    val inputWithoutCounties = removeCounties(upperInput)

    val tokens = inputWithoutCounties
      .replaceAll("(\\d+) ?- ?(\\d+)", "$1-$2")
      .replace(" - ", " ")
      .replace(",", " ")
      .replace("\\", " ")
      .split(" ")

    removeCounties(replaceSynonyms(tokens).filter(_.nonEmpty).mkString(" ")).split(" ")
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
    val groupedTokens = tokens.groupBy(_.label)
    val postcodeTreatedTokens = postTokenizeTreatmentPostCode(groupedTokens)
    val buildingNumberTreatedTokens = postTokenizeTreatmentBuildingNumber(postcodeTreatedTokens)
    val buildingNameTreatedTokens = postTokenizeTreatmentBuildingName(buildingNumberTreatedTokens)
    buildingNameTreatedTokens.map {
      case (token, values) => (token, values.map(_.value).mkString(" "))
    }
  }

  /**
    *
    * @param tokens
    * @return
    */
  def postTokenizeTreatmentPostCode(tokens: Map[String, Seq[CrfTokenResult]]): Map[String, Seq[CrfTokenResult]] = {
    val postCodeTokens = tokens.getOrElse(Tokens.postcode, Seq.empty)
    if(postCodeTokens.size == 1) {
      val token = postCodeTokens.head.value
      if(token.length >= 4) {
        val outcode = token.substring(token.length - 3, token.length)
        val incode = token.substring(0, token.indexOf(outcode))
        val regex = "[0-9][A-Z][A-Z]".r
        val optOutCode = regex.findFirstIn(outcode.toUpperCase)
        if(optOutCode.isDefined) {
          tokens.updated(
            key = Tokens.postcode,
            value = Seq(
              CrfTokenResult(
                value = s"$incode $outcode",
                label = Tokens.postcode
              )
            )
          )
        } else {
          tokens - Tokens.postcode
        }
      } else {
        tokens
      }
    } else {
      tokens
    }
  }

  /**
    * Adds paoStartNumber token if buildingNumber token is present
    * so that we can query LPI addresses
    * @param tokens tokens grouped by label
    * @return map with tokens that will also contain paoStartNumberToken if buildingNumber is present
    */
  def postTokenizeTreatmentBuildingNumber(tokens: Map[String, Seq[CrfTokenResult]]): Map[String, Seq[CrfTokenResult]]= {
    val buildingNumber: Option[String] = tokens.get(Tokens.buildingNumber).flatMap(_.headOption).map(_.value)

    buildingNumber match {
      case Some(number) =>
        val paoStartNumberToken = CrfTokenResult(
          value = number,
          label = Tokens.paoStartNumber
        )

        tokens + (Tokens.paoStartNumber -> Seq(paoStartNumberToken))

      case None => tokens
    }
  }

  /**
    * Adds paoStartSuffix and (if no buildingNumber token is present) paoStartNumber
    * tokens if buildingName token is present so that we can query LPI addresses
    * @param tokens tokens grouped by label
    * @return map with tokens that will also contain paoStartNumber and paoStartSuffix tokens if buildingName is present
    */
  def postTokenizeTreatmentBuildingName(tokens: Map[String, Seq[CrfTokenResult]]): Map[String, Seq[CrfTokenResult]]= {
    val buildingName: Option[String] = tokens.get(Tokens.buildingName).flatMap(_.headOption).map(_.value)
    // To split building name into the pao tokens, it should follow a pattern of a number followed by a letter
    val buildingNameRegexWithLetter = """(\d+)(\w)""".r
    val buildingNameRegexWithHyphen = """(\d+)-(\d+)""".r

    buildingName match {
      case Some(buildingNameRegexWithLetter(number, suffix)) =>
        val paoStartNumberToken = CrfTokenResult(number, Tokens.paoStartNumber)

        val paoStartSuffixToken = CrfTokenResult(suffix, Tokens.paoStartSuffix)

        val tokensWithPaoStartSuffix = tokens + (Tokens.paoStartSuffix -> Seq(paoStartSuffixToken))

        if (tokens.contains(Tokens.paoStartNumber)) tokensWithPaoStartSuffix
        else tokensWithPaoStartSuffix + (Tokens.paoStartNumber -> Seq(paoStartNumberToken))

      case Some(buildingNameRegexWithHyphen(firstNumber, endNumber)) =>
        val paoStartNumberToken = CrfTokenResult(firstNumber, Tokens.paoStartNumber)

        val paoEndNumberToken = CrfTokenResult(endNumber, Tokens.paoEndNumber)

        tokens + (Tokens.paoStartNumber -> Seq(paoStartNumberToken)) + (Tokens.paoEndNumber -> Seq(paoEndNumberToken))

      case Some(_) => tokens

      case None => tokens
    }
  }



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

  private def fileToList(path: String): Seq[String] = Source.fromFile(path).getLines().toList

}