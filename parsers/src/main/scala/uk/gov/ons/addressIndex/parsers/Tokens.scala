package uk.gov.ons.addressIndex.parsers

import com.typesafe.config.ConfigFactory
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenable

import scala.io.Source

/**
  * Tokenizer for the input
  */
object Tokens extends CrfTokenable {

  private val config = ConfigFactory.load()

  /**
    * Tokenizes input into tokens (also removes counties, replaces synonyms)
    * @param input the string to be tokenized
    * @return Array of tokens
    */
  override def apply(input: String): Array[String] ={
    val upperInput = input.toUpperCase()

    val inputWithoutCounties = removeCounties(upperInput)

    val tokens = inputWithoutCounties
      .replace(" - ", "-")
      .replace("- ", "-")
      .replace(" -", "-")
      .replace(",", " ")
      .replace("\\", " ")
      .split(" ")

    replaceSynonyms(tokens).filter(_.nonEmpty)
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

  val organisationName: String = "OrganisationName"
  val departmentName: String = "DepartmentName"
  val subBuildingName: String = "SubBuildingName"
  val buildingName: String = "BuildingName"
  val buildingNumber: String = "BuildingNumber"
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
    streetName,
    locality,
    townName,
    postcode
  )

  // `lazy` is needed so that if this is called from other modules, during tests, it doesn't throw exception
  lazy val directions: Seq[String] = fileToList(config.getString("parser.input-pre-post-processing.direction"))

  lazy val flat: Seq[String] = fileToList(config.getString("parser.input-pre-post-processing.flat"))

  lazy val company: Seq[String] = fileToList(config.getString("parser.input-pre-post-processing.company"))

  lazy val road: Seq[String] = fileToList(config.getString("parser.input-pre-post-processing.road"))

  lazy val residential: Seq[String] = fileToList(config.getString("parser.input-pre-post-processing.residential"))

  lazy val business: Seq[String] = fileToList(config.getString("parser.input-pre-post-processing.business"))

  lazy val locational: Seq[String] = fileToList(config.getString("parser.input-pre-post-processing.locational"))

  lazy val ordinal: Seq[String] = fileToList(config.getString("parser.input-pre-post-processing.ordinal"))

  lazy val outcodes: Seq[String] = fileToList(config.getString("parser.input-pre-post-processing.outcode"))

  lazy val postTown: Seq[String] = fileToList(config.getString("parser.input-pre-post-processing.posttown"))

  /**
    * Contains key-value map of synonyms (replace key by value)
    */
  lazy val synonym: Map[String, String] = fileToList(config.getString("parser.input-pre-post-processing.synonym"))
    // The "-1" in split is to catch the trailing empty space on the lines like "ENGLAND,"
    .map(_.split(",", -1))
    .filter(_.nonEmpty)
    .map(synonymReplacement => (synonymReplacement.head, synonymReplacement.last))
    .toMap

  /**
    * List of counties to be removed from the input
    */
  lazy val county: Seq[String] = fileToList(config.getString("parser.input-pre-post-processing.county"))

  /**
    * A county is a county if it is not followed by following suffixes
    */
  lazy val nonCountyIdentification: Seq[String] = fileToList(config.getString("parser.input-pre-post-processing.non_county_identification"))

  private def fileToList(path: String): Seq[String] = Source.fromFile(path).getLines().toList

}