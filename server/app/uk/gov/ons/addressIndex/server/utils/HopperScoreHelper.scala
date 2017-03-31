package uk.gov.ons.addressIndex.server.utils

import com.typesafe.config.ConfigFactory
import play.api.Logger
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.server.response.{AddressResponseAddress}
import uk.gov.ons.addressIndex.parsers.Tokens

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.util.Try

/**
  * Utility class to calculate Hopper matching scores
  * Takes the address response and adds the scores to each result
  * Uses the messages file to lookup the scoring matrices
  * @param messagesApi
  */
object HopperScoreHelper  {

  private val config = ConfigFactory.load()

  val logger = Logger("HopperScoreHelper")

  // load score matrix from external file
  private val currentDirectory = new java.io.File(".").getCanonicalPath.replace("server","")
  private val tokenDirectory = s"$currentDirectory${config.getString("addressIndex.serverResourcePath")}"
  lazy val scoreMatrix: Map[String,String] = fileToMap(s"${tokenDirectory}scorematrix.txt")

  /**
    * Creates a new immutable sequence of addresses with the scores set
    * Currently doesn't sort by any of the new scores but could do
    * @param addresses sequence of matching addresses, typically 10
    * @param tokens matching input tokens created by parser
    * @return scored addresses
    */
  def getScoresForAddresses(addresses: Seq[AddressResponseAddress], tokens: Map[String, String]): Seq[AddressResponseAddress] = {
    val startingTime = System.currentTimeMillis()
    val localityParams = new ListBuffer[Tuple2[String,String]]
    addresses.foreach { address =>
      localityParams += getLocalityParams(address, tokens)
    }
//    logger.info(localityParams.mkString)
    val scoredAddressBuffer = new ListBuffer[AddressResponseAddress]
    addresses.foreach { address =>
      scoredAddressBuffer += addScoresToAddress(address, tokens, localityParams)
    }
    val endingTime = System.currentTimeMillis()
    logger.info("Hopper Score calucation time = "+(endingTime-startingTime)+" milliseconds")
    scoredAddressBuffer.toSeq
  }

  def getLocalityParams(address: AddressResponseAddress, tokens: Map[String, String]): Tuple2[String,String] = {

    val organisationName = tokens.getOrElse(key ="OrganisationName", default = "@")
    val buildingName = tokens.getOrElse(key ="BuildingName", default = "@")
    val streetName = tokens.getOrElse(key ="StreetName", default = "@")
    val locality = tokens.getOrElse(key ="Locality", default = "@")
    val townName = tokens.getOrElse(key ="TownName", default = "@")
    val postcode = tokens.getOrElse(key ="Postcode", default = "@")
    val postcodeIn = tokens.getOrElse(key ="PostcodeIn", default = "@")
    val postcodeOut = tokens.getOrElse(key ="PostcodeOut", default = "@")

    // should it be the same param mumbers or the exact same town name, street name etc.?

    (calculateLocalityScore(
      address,
      postcode,
      postcodeIn,
      postcodeOut,
      locality,
      townName,
      streetName,
      organisationName,
      buildingName),
      getSector(Try(address.paf.get.postcode).getOrElse(address.nag.get.postcodeLocator)))
  }

  /**
    * Calculates the Hopper scores for a single address
    * @param address single address response object
    * @param tokens list of tokens handed down
    * @return new address response oobject with scores
    */
  def addScoresToAddress(address: AddressResponseAddress,
    tokens: Map[String, String],
    localityParams: ListBuffer[Tuple2[String,String]]): AddressResponseAddress = {

    val organisationName = tokens.getOrElse(key ="OrganisationName", default = "@")
    val departmentName = tokens.getOrElse(key ="DepartmentName", default = "@")
    val subBuildingName = tokens.getOrElse(key ="SubBuildingName", default = "@")
    val buildingName = tokens.getOrElse(key ="BuildingName", default = "@")
    val buildingNumber = tokens.getOrElse(key ="BuildingNumber", default = "@")
    val paoStartNumber = tokens.getOrElse(key ="PaoStartNumber", default = "@")
    val paoStartSuffix = tokens.getOrElse(key ="PaoStartSuffix", default = "@")
    val paoEndNumber = tokens.getOrElse(key ="PaoEndNumber", default = "@")
    val paoEndSuffix = tokens.getOrElse(key ="PaoEndSuffix", default = "@")
    val saoStartNumber = tokens.getOrElse(key ="SaoStartNumber", default = "@")
    val saoStartSuffix = tokens.getOrElse(key ="SaoStartSuffix", default = "@")
    val saoEndNumber = tokens.getOrElse(key ="SaoEndNumber", default = "@")
    val saoEndSuffix = tokens.getOrElse(key ="SaoEndSuffix", default = "@")
    val streetName = tokens.getOrElse(key ="StreetName", default = "@")
    val locality = tokens.getOrElse(key ="Locality", default = "@")
    val townName = tokens.getOrElse(key ="TownName", default = "@")
    val postcode = tokens.getOrElse(key ="Postcode", default = "@")
    val postcodeIn = tokens.getOrElse(key ="PostcodeIn", default = "@")
    val postcodeOut = tokens.getOrElse(key ="PostcodeOut", default = "@")

    val buildingScoreDebug = calculateBuildingScore(address,
      buildingName,
      buildingNumber,
      paoStartNumber,
      paoEndNumber,
      paoStartSuffix,
      paoEndSuffix,
      organisationName)

    val buildingScore = Try(scoreMatrix(buildingScoreDebug).toDouble).getOrElse(0d)

    val localityScoreDebug = calculateLocalityScore(
      address,
      postcode,
      postcodeIn,
      postcodeOut,
      locality,
      townName,
      streetName,
      organisationName,
      buildingName)

    val ambiguityPenalty = caluclateAmbiguityPenalty(localityScoreDebug,localityParams);
   // logger.info("penatly = "+ambiguityPenalty)
    val localityScore = Try(scoreMatrix(localityScoreDebug).toDouble).getOrElse(0d) * ambiguityPenalty

    val unitScoreDebug = calculateUnitScore(
      address,
      subBuildingName,
      departmentName,
      saoStartNumber,
      saoEndNumber,
      saoStartSuffix,
      saoEndSuffix,
      organisationName)

    val unitScore = Try(scoreMatrix(unitScoreDebug).toDouble).getOrElse(0d)

    val structuralScore = calculateStructuralScore(buildingScore, localityScore)
    val objectScore = calculateObjectScore(buildingScore,localityScore,unitScore)

    AddressResponseAddress(
      uprn = address.uprn,
      parentUprn = address.parentUprn,
      relatives = address.relatives,
      formattedAddress = address.formattedAddress,
      formattedAddressNag = address.formattedAddressNag,
      formattedAddressPaf = address.formattedAddressPaf,
      paf = address.paf,
      nag = address.nag,
      geo = address.geo,
      underlyingScore = address.underlyingScore,
      objectScore = objectScore,
      structuralScore = structuralScore,
      buildingScore = buildingScore,
      localityScore = localityScore,
      unitScore = unitScore,
      buildingScoreDebug = Try(buildingScoreDebug.substring(buildingScoreDebug.indexOf(".") + 1)).getOrElse("99"),
      localityScoreDebug = Try(localityScoreDebug.substring(localityScoreDebug.indexOf(".") + 1)).getOrElse("9999"),
      unitScoreDebug = Try(unitScoreDebug.substring(unitScoreDebug.indexOf(".") + 1)).getOrElse("9999")
    )
  }

  /**
    * The object cscore is the product of the structural score and the unit score
    * except where the building unambiguously identifies the object when it is set to missing (?)
    * @param buildingScore
    * @param localityScore
    * @param unitScore
    * @return score as a double rounded to 4dp
    */
  def calculateObjectScore(buildingScore: Double, localityScore: Double, unitScore: Double): Double = {
    val score = if (unitScore == -1) -1 else buildingScore * localityScore * unitScore
    BigDecimal(score).setScale(4, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

  /**
    * The structural score is the product of the locality score and the building score
    * @param buildingScore
    * @param localityScore
    * @return score as a double rounded to 4dp
    */
  def calculateStructuralScore(buildingScore: Double, localityScore: Double): Double = {
    val score = buildingScore * localityScore
    BigDecimal(score).setScale(4, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

  /**
    * The building score captures how the the building level identifiers match in source and reference
    * @param address
    * @param buildingName
    * @param buildingNumber
    * @param paoStartNumber
    * @param paoEndNumber
    * @param paoStartSuffix
    * @param paoEndSuffix
    * @param organisationName
    * @return score between 0 and 1 (during testing lookup string returned instead for diagnostic purposes)
    */
  def calculateBuildingScore(
    address: AddressResponseAddress,
    buildingName: String,
    buildingNumber: String,
    paoStartNumber: String,
    paoEndNumber: String,
    paoStartSuffix: String,
    paoEndSuffix: String,
    organisationName: String): String = {

    // each element score is the better match of paf and nag

    // match building numbers, ranges and suffixes
    val tokenBuildingLowNum = getRangeBottom(buildingName)
    val tokenBuildingHighNum = tokenBuildingLowNum.max(getRangeTop(buildingName))
    val pafBuildingLowNum = getRangeBottom(Try(address.paf.get.buildingName).getOrElse(""))
    val pafBuildingHighNum = pafBuildingLowNum.max(getRangeTop(Try(address.paf.get.buildingName).getOrElse("")))
    val pafTestBN = Try(Try(address.paf.get.buildingNumber).getOrElse("").toInt).getOrElse(-1)
    val nagBuildingLowNum = Try(Try(address.nag.get.pao.paoStartNumber).getOrElse("").toInt).getOrElse(-1)
    val nagBuildingHighNum = nagBuildingLowNum.max(Try(Try(address.nag.get.pao.paoEndNumber).getOrElse("").toInt).getOrElse(-1))
    val pafInRange = (((pafBuildingLowNum >= tokenBuildingLowNum && pafBuildingHighNum <= tokenBuildingHighNum)
      || (pafTestBN >= tokenBuildingLowNum && pafTestBN <= tokenBuildingHighNum))
      && (pafBuildingLowNum > -1 || pafTestBN > -1) && tokenBuildingLowNum > -1)
    val nagInRange = ((nagBuildingLowNum >= tokenBuildingLowNum && nagBuildingHighNum <= tokenBuildingHighNum)
    && nagBuildingLowNum > -1  && tokenBuildingLowNum > -1)
    val pafBuildingStartSuffix = getStartSuffix(Try(address.paf.get.buildingName).getOrElse(""))
    val pafBuildingEndSuffix = getEndSuffix(Try(address.paf.get.buildingName).getOrElse(""))
    val nagBuildingStartSuffix = if (Try(address.nag.get.pao.paoStartSuffix).getOrElse("") == "" ) "@"
      else Try(address.nag.get.pao.paoStartSuffix).getOrElse("")
    val nagBuildingEndSuffix = if (Try(address.nag.get.pao.paoEndSuffix).getOrElse("") == "" ) "@"
      else Try(address.nag.get.pao.paoEndSuffix).getOrElse("")
    val pafSuffixInRange = ((paoStartSuffix == pafBuildingStartSuffix && paoEndSuffix == pafBuildingEndSuffix)
    || (paoEndSuffix == "@" && paoStartSuffix >= pafBuildingStartSuffix && paoStartSuffix <= pafBuildingEndSuffix)
    || (pafBuildingEndSuffix == "@" && pafBuildingStartSuffix >= paoStartSuffix && pafBuildingStartSuffix <= paoEndSuffix ))
    val nagSuffixInRange = ((paoStartSuffix == nagBuildingStartSuffix && paoEndSuffix == nagBuildingEndSuffix)
      || (paoEndSuffix == "@" && paoStartSuffix >=nagBuildingStartSuffix && paoStartSuffix <= nagBuildingEndSuffix)
      || (nagBuildingEndSuffix == "@" && nagBuildingStartSuffix >= paoStartSuffix && nagBuildingStartSuffix <= paoEndSuffix ))

    // match building name
    val pafBuildingMatchScore = if (buildingName == "@") 4
      else matchNames(buildingName,Try(address.paf.get.buildingName).getOrElse("")).min(matchNames(Try(address.paf.get.buildingName).getOrElse(""),buildingName))
    val nagBuildingMatchScore = if (buildingName == "@") 4
      else matchNames(buildingName,Try(address.nag.get.pao.paoText).getOrElse("")).min(matchNames(Try(address.nag.get.pao.paoText).getOrElse(""),buildingName))

    // match  organisation
    val pafOrganisationMatchScore = if (organisationName == "@") 4
    else matchNames(organisationName,Try(address.paf.get.organisationName).getOrElse("")).min(matchNames(Try(address.paf.get.organisationName).getOrElse(""),organisationName))
    val nagOrganisationMatchScore = if (organisationName == "@") 4
    else matchNames(organisationName,Try(address.nag.get.organisation).getOrElse("")).min(matchNames(Try(address.nag.get.organisation).getOrElse(""),organisationName))

    // Match buildingName against buildingName and organisationName against OrganisationName (cross-ref?)
    val detailed_organisation_building_name_paf_score: Int = {
      if (buildingName == Try(address.paf.get.buildingName).getOrElse("") ||
        organisationName == Try(address.paf.get.organisationName).getOrElse("")) 1
      else if (pafOrganisationMatchScore < 2 || pafBuildingMatchScore < 2) 2
      else if (pafOrganisationMatchScore < 3 || pafBuildingMatchScore < 3) 3
      else if (buildingName == "@" && organisationName == "@" &&
        Try(address.paf.get.organisationName).getOrElse("") == "" &&
        Try(address.paf.get.buildingName).getOrElse("") == "" ) 9
      else if (!((buildingName != "@" && Try(address.paf.get.buildingName).getOrElse("") != "" ) ||
        (organisationName != "@" && Try(address.paf.get.organisationName).getOrElse("") != "" ))) 7
      else 6
    }
  //  logger.info("detailed organisation building name paf score = " + detailed_organisation_building_name_paf_score)

    // Match buildingName against paoText and organisationName against Organisation (cross-ref?)
    val detailed_organisation_building_name_nag_score: Int = {
      if (buildingName == Try(address.nag.get.pao.paoText).getOrElse("") ||
        organisationName == Try(address.nag.get.organisation).getOrElse("")) 1
      else if (nagOrganisationMatchScore < 2 || nagBuildingMatchScore < 2) 2
      else if (nagOrganisationMatchScore < 3 || nagBuildingMatchScore < 3) 3
      else if (buildingName == "@" && organisationName == "@" &&
        Try(address.nag.get.organisation).getOrElse("") == "" &&
        Try(address.nag.get.pao.paoText).getOrElse("") == "" ) 9
      else if (!((buildingName != "@" && Try(address.nag.get.pao.paoText).getOrElse("") != "" ) ||
        (organisationName != "@" && Try(address.nag.get.organisation).getOrElse("") != "" ))) 7
      else 6
    }
  //  logger.info("detailed organisation building name nag score = " + detailed_organisation_building_name_nag_score)

    // 7 Gate Reach TOKENS: buildingNumber = 7 and PaoStartNumber =7 MATCHTO: paf.BuildingNumber = 7
    // 4A-5B Gate Reach gives TOKENS: buildingName = 4A-5B (buildingNumber empty), PaoStartNumber = 4, PaoStartSuffix = A, PaoEndNumber = 5, PaoEndSuffix = B
    // MATCHTO:  paf.buildingNumber = 4 (for single num) or paf.buildingName = 4B (suffix and/or range)
    val building_number_paf_score: Int = {
      if (buildingNumber == Try(address.paf.get.buildingNumber).getOrElse("") ||
        (buildingNumber == "@" && buildingName == Try(address.paf.get.buildingName).getOrElse(""))) 1
        else if (pafSuffixInRange && (Try(address.paf.get.buildingNumber).getOrElse("") == paoStartNumber ||
        Try(address.paf.get.buildingName).getOrElse("").startsWith(paoStartNumber) ||
        Try(address.paf.get.buildingName).getOrElse("").endsWith(paoEndNumber))) 2
        else if (pafInRange) 3
        else if (Try(address.paf.get.buildingNumber).getOrElse("") == paoStartNumber ||
        Try(address.paf.get.buildingName).getOrElse("").startsWith(paoStartNumber) ||
        Try(address.paf.get.buildingName).getOrElse("").endsWith(paoEndNumber)) 4
        else if (!((tokenBuildingLowNum == -1 && buildingNumber == "@" ) ||
        (pafTestBN == -1 && Try(address.paf.get.buildingNumber).getOrElse("") == ""))) 6
        else 9
    }
   // logger.info("building number paf score = " + building_number_paf_score)

    // 7 Gate Reach TOKENS: buildingNumber = 7 and PaoStartNumber =7 MATCHTO: nag.PaoStartNumber = 7
    // 4A-5B Gate TOKENS: buildingName = 4A-5B (buildingNumber empty), PaoStartNumber = 4, PaoStartSuffix = A, PaoEndNumber = 5, PaoEndSuffix = B
    // MATCHTO: nag.Paotext = 4A-5B, nag.paoStartNumber = 4, nag.paoStartSuffix = A, nag.paoEndNumber = 5, nag.paoEndSuffix = B
    val building_number_nag_score: Int = {
      if (buildingNumber == Try(address.nag.get.pao.paoStartNumber).getOrElse("")) 1
      else if (nagSuffixInRange &&
        (paoStartNumber == Try(address.nag.get.pao.paoStartNumber).getOrElse("") ||
          paoEndNumber == Try(address.nag.get.pao.paoEndNumber).getOrElse("") )) 2
      else if (nagInRange) 3
      else if (Try(address.nag.get.pao.paoStartNumber).getOrElse("") == paoStartNumber ||
        paoEndNumber == Try(address.nag.get.pao.paoEndNumber).getOrElse("")) 4
      else if (!((tokenBuildingLowNum == -1 && buildingNumber == "@" ) || (nagBuildingLowNum == -1))) 6
      else 9
    }
  //  logger.info("building number nag score = " + building_number_nag_score)

    val detailed_organisation_building_name_param = detailed_organisation_building_name_paf_score.min(detailed_organisation_building_name_nag_score)
    val building_number_param = building_number_paf_score.min(building_number_nag_score)
    "building."+detailed_organisation_building_name_param+building_number_param
  }

  /**
    * The locality score aims to state how certain we know the locality of an address
    * @param address
    * @param postcodeIn
    * @param postcodeOut
    * @param locality
    * @param townName
    * @param streetName
    * @param organisationName
    * @param buildingName
    * @return score between 0 and 1 (during testing lookup string returned instead for diagnostic purposes)
    */
  def calculateLocalityScore(
    address: AddressResponseAddress,
    postcode: String,
    postcodeIn: String,
    postcodeOut: String,
    locality: String,
    townName: String,
    streetName: String,
    organisationName: String,
    buildingName: String): String = {

    // each element score is the better match of paf and nag

    // match buidlings
    val pafBuildingMatchScore = if (buildingName == "@") 4 else
     matchNames(buildingName,Try(address.paf.get.buildingName).getOrElse(""))
       .min(matchNames(Try(address.paf.get.buildingName).getOrElse(""),buildingName))
    val nagBuildingMatchScore = if (buildingName == "@") 4 else
      matchNames(buildingName,Try(address.nag.get.pao.paoText).getOrElse(""))
        .min(matchNames(Try(address.nag.get.pao.paoText).getOrElse(""),buildingName))
    val pafOrganisationMatchScore = if (organisationName == "@") 4 else
      matchNames(organisationName,Try(address.paf.get.organisationName).getOrElse(""))
        .min(matchNames(Try(address.paf.get.organisationName).getOrElse(""),organisationName))
    val nagOrganisationMatchScore = if (organisationName == "@") 4 else
      matchNames(organisationName,Try(address.nag.get.organisation).getOrElse(""))
        .min(matchNames(Try(address.nag.get.organisation).getOrElse(""),organisationName))

    //  match streets
    val pafThoroStreetMatchScore = matchStreets(streetName,Try(address.paf.get.thoroughfare).getOrElse(""))
     .min(matchStreets(Try(address.paf.get.thoroughfare).getOrElse(""),streetName))
    val pafDepThoroStreetMatchScore = matchStreets(streetName,Try(address.paf.get.dependentThoroughfare).getOrElse(""))
      .min(matchStreets(Try(address.paf.get.dependentThoroughfare).getOrElse(""),streetName))
    val pafWelshStreetMatchScore = matchStreets(streetName,Try(address.paf.get.welshThoroughfare).getOrElse(""))
      .min(matchStreets(Try(address.paf.get.welshThoroughfare).getOrElse(""),streetName))
    val pafDepWelshStreetMatchScore = matchStreets(streetName,Try(address.paf.get.welshDependentThoroughfare).getOrElse(""))
      .min(matchStreets(Try(address.paf.get.welshDependentThoroughfare).getOrElse(""),streetName))
    val pafStreetMatchScore = if (streetName == "@") 4 else
      min(pafThoroStreetMatchScore,pafDepThoroStreetMatchScore,pafWelshStreetMatchScore,pafDepWelshStreetMatchScore)
    val nagStreetMatchScore = if (streetName == "@") 4 else
      matchStreets(streetName,Try(address.nag.get.streetDescriptor).getOrElse(""))
        .min(matchStreets(Try(address.nag.get.streetDescriptor).getOrElse(""),streetName))

    // match town name
    val pafPostTownTownNameMatchScore = matchNames(townName,Try(address.paf.get.postTown).getOrElse(""))
      .min(matchNames(Try(address.paf.get.postTown).getOrElse(""),townName))
    val pafWelshPostTownTownNameMatchScore = matchNames(townName,Try(address.paf.get.welshPostTown).getOrElse(""))
      .min(matchNames(Try(address.paf.get.welshPostTown).getOrElse(""),townName))
    val pafDependentLocalityTownNameMatchScore = matchNames(townName,Try(address.paf.get.dependentLocality).getOrElse("")).
      min(matchNames(Try(address.paf.get.dependentLocality).getOrElse(""),townName))
    val pafWelshDependentLocalityTownNameMatchScore = matchNames(townName,Try(address.paf.get.welshDependentLocality).getOrElse(""))
      .min(matchNames(Try(address.paf.get.welshDependentLocality).getOrElse(""),townName))
    val pafDoubleDependentLocalityTownNameMatchScore = matchNames(townName,Try(address.paf.get.doubleDependentLocality).getOrElse(""))
      .min(matchNames(Try(address.paf.get.doubleDependentLocality).getOrElse(""),townName))
    val pafWelshDoubleDependentLocalityTownNameMatchScore = matchNames(townName,Try(address.paf.get.welshDoubleDependentLocality).getOrElse(""))
      .min(matchNames(Try(address.paf.get.welshDoubleDependentLocality).getOrElse(""),townName))
    val pafTownNameMatchScore = if (townName == "@") 4 else
      min(pafPostTownTownNameMatchScore,pafWelshPostTownTownNameMatchScore,pafDependentLocalityTownNameMatchScore,pafWelshDependentLocalityTownNameMatchScore,pafDoubleDependentLocalityTownNameMatchScore,pafWelshDoubleDependentLocalityTownNameMatchScore)
    val nagTownNameTownNameMatchScore = matchNames(townName,Try(address.nag.get.townName).getOrElse(""))
      .min(matchNames(Try(address.nag.get.townName).getOrElse(""),townName))
    val nagLocalityTownNameMatchScore = matchNames(townName,Try(address.nag.get.locality).getOrElse(""))
      .min(matchNames(Try(address.nag.get.locality).getOrElse(""),townName))
    val nagTownNameMatchScore = if (townName == "@") 4 else
      min(nagTownNameTownNameMatchScore,nagLocalityTownNameMatchScore)

    // match locality
    val pafPostTownlocalityMatchScore = matchNames(locality,Try(address.paf.get.postTown).getOrElse(""))
      .min(matchNames(Try(address.paf.get.postTown).getOrElse(""),locality))
    val pafWelshPostTownlocalityMatchScore = matchNames(locality,Try(address.paf.get.welshPostTown).getOrElse(""))
      .min(matchNames(Try(address.paf.get.welshPostTown).getOrElse(""),locality))
    val pafDependentLocalitylocalityMatchScore = matchNames(locality,Try(address.paf.get.dependentLocality).getOrElse(""))
      .min(matchNames(Try(address.paf.get.dependentLocality).getOrElse(""),locality))
    val pafWelshDependentLocalitylocalityMatchScore = matchNames(locality,Try(address.paf.get.welshDependentLocality).getOrElse(""))
      .min(matchNames(Try(address.paf.get.welshDependentLocality).getOrElse(""),locality))
    val pafDoubleDependentLocalitylocalityMatchScore = matchNames(locality,Try(address.paf.get.doubleDependentLocality).getOrElse(""))
      .min(matchNames(Try(address.paf.get.doubleDependentLocality).getOrElse(""),locality))
    val pafWelshDoubleDependentLocalitylocalityMatchScore = matchNames(locality,Try(address.paf.get.welshDoubleDependentLocality).getOrElse(""))
      .min(matchNames(Try(address.paf.get.welshDoubleDependentLocality).getOrElse(""),locality))
    val pafLocalityMatchScore = if (locality == "@") 4 else
      min(pafPostTownlocalityMatchScore,pafWelshPostTownlocalityMatchScore,pafDependentLocalitylocalityMatchScore,pafWelshDependentLocalitylocalityMatchScore,pafDoubleDependentLocalitylocalityMatchScore,pafWelshDoubleDependentLocalitylocalityMatchScore)
    val nagTownNamelocalityMatchScore = matchNames(locality,Try(address.nag.get.townName).getOrElse(""))
      .min(matchNames(Try(address.nag.get.townName).getOrElse(""),locality))
    val nagLocalitylocalityMatchScore = matchNames(locality,Try(address.nag.get.streetDescriptor).getOrElse(""))
      .min(matchNames(Try(address.nag.get.locality).getOrElse(""),locality))
    val nagLocalityMatchScore = if (locality == "@") 4 else
      min(nagTownNamelocalityMatchScore,nagLocalitylocalityMatchScore)

    // create test fields for postcode match
    val postcodeWithInvertedIncode = if (postcodeIn.length < 3) "@" else
      swap(postcodeIn,1,2)
    val postcodeSector = if (postcodeIn.length < 3) "@" else
      postcodeOut + " " + postcodeIn.substring(0,1)
    val postcodeArea = if (postcodeOut.length == 1) "@" else
    if (postcodeOut.length == 2) postcodeOut.substring(0,1) else postcodeOut.substring(0,2)

    // Accept a PAF match via organisation or building with edit distance of 2 or less
    val organisation_building_name_paf_score: Int = {
      if (pafOrganisationMatchScore < 3 || pafBuildingMatchScore < 3) 1
       else if ((buildingName != "@" &&
        Try(address.paf.get.buildingName).getOrElse("") != "" ) ||
        (organisationName != "@" &&
          Try(address.paf.get.organisationName).getOrElse("") != "" )) 6
       else 9
    }
    // logger.info("organisation building name paf score = " + organisation_building_name_paf_score)

    // Accept a NAG match via organisation or building with edit distance of 2 or less
    val organisation_building_name_nag_score: Int = {
      if (nagOrganisationMatchScore < 3 || nagBuildingMatchScore < 3) 1
      else if ((buildingName != "@" && Try(address.nag.get.pao.paoText).getOrElse("") != "" ) ||
        (organisationName != "@" && Try(address.nag.get.organisation).getOrElse("") != "" )) 6
       else 9
    }
    // logger.info("organisation building name nag score = " + organisation_building_name_nag_score)

    // Set the PAF Street score according to quality of match
    val street_paf_score: Int = {
      if (pafStreetMatchScore == 0) 1
      else if (pafStreetMatchScore == 1) 2
      else if (pafStreetMatchScore == 2) 5
      else if (streetName == "@") 9
      else 6
    }
  //  logger.info("street paf score = " + street_paf_score)

    // Set the NAG Street score according to quality of match
    val street_nag_score: Int = {
      if (nagStreetMatchScore == 0) 1
      else if (nagStreetMatchScore == 1) 2
      else if (nagStreetMatchScore == 2) 5
      else if (streetName == "@") 9
      else 6
    }
 //   logger.info("street nag score = " + street_nag_score)

    // Accept a PAF match via locality with an edit distance of 2 or less
    val town_locality_paf_score: Int = {
      if (pafTownNameMatchScore < 2 || pafLocalityMatchScore < 2) 1
      else if (streetName == "@") 9
      else 6
    }
   // logger.info("town locality paf score = " + town_locality_paf_score)

    // Accept a NAG match via locality with an edit distance of 2 or less
    val town_locality_nag_score: Int = {
      if (nagTownNameMatchScore < 2 || nagLocalityMatchScore < 2) 1
      else if (streetName == "@") 9
      else 6
    }
  //  logger.info("town locality nag score = " + town_locality_nag_score)

    // postcode token is formatted with space so can do exact match
    // Use helpers to match inversion, sector, outcode and area
    val postcode_paf_score: Int = {
      if (postcode == Try(address.paf.get.postcode).getOrElse("")) 1
      else if ((postcodeOut + " " + postcodeWithInvertedIncode) == Try(address.paf.get.postcode).getOrElse("")) 2
      else if (postcodeSector == getSector(Try(address.paf.get.postcode).getOrElse(""))) 3
      else if (postcodeOut == getOutcode(Try(address.paf.get.postcode).getOrElse(""))) 4
      else if (postcodeArea == Try(address.paf.get.postcode.substring(0,2)).getOrElse("")) 5
      else if (postcode == "@") 9
      else 6
    }
  //  logger.info(address.paf.get.postcode + " postcode paf score = " + postcode_paf_score)

    // postcode token is formatted with space so can do exact match
    // Use helpers to match inversion, sector, outcode and area
    val postcode_nag_score: Int = {
      if (postcode == Try(address.nag.get.postcodeLocator).getOrElse("")) 1
      else if ((postcodeOut + " " + postcodeWithInvertedIncode) == Try(address.nag.get.postcodeLocator).getOrElse("")) 2
      else if (postcodeSector == getSector(Try(address.nag.get.postcodeLocator).getOrElse(""))) 3
      else if (postcodeOut == getOutcode(Try(address.nag.get.postcodeLocator).getOrElse(""))) 4
      else if (postcodeArea == Try(address.nag.get.postcodeLocator.substring(0,2)).getOrElse("")) 5
      else if (postcode == "@") 9
      else 6
    }
 //   logger.info(address.nag.get.postcodeLocator + " postcode nag score = " + postcode_nag_score)

    val organisation_building_name_param = organisation_building_name_paf_score.min(organisation_building_name_nag_score)
    val street_param = street_paf_score.min(street_nag_score)
    val town_locality_param = town_locality_paf_score.min(town_locality_nag_score)
    val postcode_param = postcode_paf_score.min(postcode_nag_score)

    "locality."+organisation_building_name_param+street_param+town_locality_param+postcode_param
  }

  /**
    * Calculates how well the sub-building or room fields match
    * The hierarchical field is not currently available
    * If not hierarchical set to missing (?) or source sub-building name
    * or source origanisation name is unmatched
    * @param address
    * @param subBuildingName
    * @param departmentName
    * @param saoStartNumber
    * @param saoEndNumber
    * @param saoStartSuffix
    * @param saoEndSuffix
    * @param organisationName
    * @return score between 0 and 1 (during testing lookup string returned instead for diagnostic purposes)
    */
  def calculateUnitScore(
    address: AddressResponseAddress,
    subBuildingName: String,
    departmentName: String,
    saoStartNumber: String,
    saoEndNumber: String,
    saoStartSuffix: String,
    saoEndSuffix: String,
    organisationName: String): String = {

    // each element score is the better match of paf and nag

    // match oganisation
    val nagPAOOrganisationMatchScore = if (organisationName == "@") 4 else
      matchNames(organisationName,Try(address.nag.get.pao.paoText).getOrElse(""))
        .min(matchNames(Try(address.nag.get.pao.paoText).getOrElse(""),organisationName))
    val nagSAOOrganisationMatchScore = if (organisationName == "@") 4 else
      matchNames(organisationName,Try(address.nag.get.sao.saoText).getOrElse(""))
        .min(matchNames(Try(address.nag.get.sao.saoText).getOrElse(""),organisationName))
    val nagOrganisationMatchScore = if (organisationName == "@") 4 else
      matchNames(organisationName,Try(address.nag.get.organisation).getOrElse(""))
        .min(matchNames(Try(address.nag.get.organisation).getOrElse(""),organisationName))

    // match subbuilding number / suffix
    val tokenBuildingLowNum = getRangeBottom(subBuildingName)
    val tokenBuildingHighNum = tokenBuildingLowNum.max(getRangeTop(subBuildingName))
    val pafBuildingLowNum = getRangeBottom(Try(address.paf.get.subBuildingName).getOrElse(""))
    val pafBuildingHighNum = pafBuildingLowNum.max(getRangeTop(Try(address.paf.get.subBuildingName).getOrElse("")))
    val nagBuildingLowNum = Try(Try(address.nag.get.sao.saoStartNumber).getOrElse("").toInt).getOrElse(-1)
    val nagBuildingHighNum = nagBuildingLowNum.max(Try(Try(address.nag.get.sao.saoEndNumber).getOrElse("").toInt).getOrElse(-1))
    val pafInRange = (((pafBuildingLowNum >= tokenBuildingLowNum && pafBuildingHighNum <= tokenBuildingHighNum)
      && tokenBuildingLowNum > -1))
    val nagInRange = ((nagBuildingLowNum >= tokenBuildingLowNum && nagBuildingHighNum <= tokenBuildingHighNum)
      && nagBuildingLowNum > -1  && tokenBuildingLowNum > -1)
    val pafBuildingStartSuffix = getStartSuffix(Try(address.paf.get.buildingName).getOrElse(""))
    val pafBuildingEndSuffix = getEndSuffix(Try(address.paf.get.buildingName).getOrElse(""))
    val nagBuildingStartSuffix = if (Try(address.nag.get.sao.saoStartSuffix).getOrElse("") == "" ) "@"
    else Try(address.nag.get.sao.saoStartSuffix).getOrElse("")
    val nagBuildingEndSuffix = if (Try(address.nag.get.pao.paoEndSuffix).getOrElse("") == "" ) "@"
    else Try(address.nag.get.sao.saoEndSuffix).getOrElse("")
    val pafSuffixInRange = ((saoStartSuffix == pafBuildingStartSuffix && saoEndSuffix == pafBuildingEndSuffix)
      || (saoEndSuffix == "@" && saoStartSuffix >= pafBuildingStartSuffix && saoStartSuffix <= pafBuildingEndSuffix)
      || (pafBuildingEndSuffix == "@" && pafBuildingStartSuffix >= saoStartSuffix && pafBuildingStartSuffix <= saoEndSuffix ))
    val nagSuffixInRange = ((saoStartSuffix == nagBuildingStartSuffix && saoEndSuffix == nagBuildingEndSuffix)
      || (saoEndSuffix == "@" && saoStartSuffix >=nagBuildingStartSuffix && saoStartSuffix <= nagBuildingEndSuffix)
      || (nagBuildingEndSuffix == "@" && nagBuildingStartSuffix >= saoStartSuffix && nagBuildingStartSuffix <= saoEndSuffix ))

    //  match subbuilding  name
    val pafBuildingMatchScore = if (subBuildingName == "@") 4 else
      matchNames(subBuildingName,Try(address.paf.get.subBuildingName).getOrElse(""))
        .min(matchNames(Try(address.paf.get.subBuildingName).getOrElse(""),subBuildingName))
    val nagBuildingMatchScore = if (subBuildingName == "@") 4 else
      matchNames(subBuildingName,Try(address.nag.get.sao.saoText).getOrElse(""))
        .min(matchNames(Try(address.nag.get.sao.saoText).getOrElse(""),subBuildingName))

    // test for more than 1 layer
    val numRels = address.relatives.size
 //   logger.info("num rels = " + numRels)

    val ref_hierarchy_param = if (numRels > 1) 1 else 0

    // Look for organisation match agaings PAO, SAO, or Organisation (NAG only)
    val organisation_name_nag_score: Int = {
      if (nagPAOOrganisationMatchScore < 3 || nagSAOOrganisationMatchScore < 3 || nagOrganisationMatchScore < 3 ) 1
      else if (organisationName == "@" && Try(address.nag.get.organisation).getOrElse("") == "" &&
        Try(address.nag.get.pao.paoText).getOrElse("") == "" &&
        Try(address.nag.get.sao.saoText).getOrElse("") == "") 9
      else if (!((organisationName != "@" &&
        Try(address.nag.get.pao.paoText).getOrElse("") != "" )
        || (organisationName != "@" && Try(address.nag.get.sao.saoText).getOrElse("") != "" )
        || (organisationName != "@" && Try(address.paf.get.organisationName).getOrElse("") != "" )))
      {8} else {6}
    }
  //    logger.info("organisation name nag score = " + organisation_name_nag_score)

    // Match sub Building name using PAF
    val sub_building_name_paf_score: Int = {
      if (subBuildingName == Try(address.paf.get.subBuildingName).getOrElse("")) 1
      else if (pafBuildingMatchScore < 2) 2
      else if ( pafBuildingMatchScore < 3) 3
      else if (subBuildingName == "@"  && Try(address.paf.get.subBuildingName).getOrElse("") == "" ) 9
      else if (!((subBuildingName != "@" && Try(address.paf.get.subBuildingName).getOrElse("") != "" ) )) 7
      else 6
    }
 //    logger.info("sub building name paf score = " + sub_building_name_paf_score)

    // Match buildingName against saoText
    val sub_building_name_nag_score: Int = {
      if (subBuildingName == Try(address.nag.get.sao.saoText)) 1
      else if (nagBuildingMatchScore < 2) 2
      else if (nagBuildingMatchScore < 3) 3
      else if (subBuildingName == "@" && Try(address.nag.get.sao.saoText).getOrElse("") == "" ) 9
      else if (!((subBuildingName != "@" && Try(address.nag.get.sao.saoText).getOrElse("") != "" ) )) 7
      else 6
    }
   //  logger.info("sub building name nag score = " + sub_building_name_nag_score)

    // Get Sample address for SAO suffixes and ranges - currently suing same method as PAO
    val sub_building_number_paf_score: Int = {
      if (pafSuffixInRange && (Try(address.paf.get.buildingName).getOrElse("").startsWith(saoStartNumber) ||
        Try(address.paf.get.subBuildingName).getOrElse("").endsWith(saoEndNumber))) 1
      else if (pafInRange) 1
      else if (Try(address.paf.get.buildingNumber).getOrElse("") == saoStartNumber ||
        Try(address.paf.get.subBuildingName).getOrElse("").startsWith(saoStartNumber) ||
        Try(address.paf.get.subBuildingName).getOrElse("").endsWith(saoEndNumber)) 6
      else if (!((tokenBuildingLowNum == -1 && saoStartNumber == "@" ))) 8
      else 9
    }
  //     logger.info("sub building number paf score = " + sub_building_number_paf_score)

    // Get Sample address for SAO suffixes and ranges - currently suing same method as PAO
    val sub_building_number_nag_score: Int = {
      if (saoStartNumber == Try(address.nag.get.sao.saoStartNumber).getOrElse("")) 1
      else if (nagSuffixInRange &&
        (saoStartNumber == Try(address.nag.get.sao.saoStartNumber).getOrElse("") ||
          saoEndNumber == Try(address.nag.get.pao.paoEndNumber).getOrElse("") )) 1
      else if (nagInRange) 1
      else if (Try(address.nag.get.sao.saoStartNumber).getOrElse("") == saoStartNumber ||
        saoEndNumber == Try(address.nag.get.pao.paoEndNumber).getOrElse("")) 6
      else if (!((tokenBuildingLowNum == -1 && saoStartNumber == "@" ) || (nagBuildingLowNum == -1)))  8
      else 9
    }
  //  logger.info("sub building number nag score = " + sub_building_number_nag_score)

    val organisation_name_param = organisation_name_nag_score
    val sub_building_number_param = sub_building_number_paf_score.min(sub_building_number_nag_score)
    val sub_building_name_param = sub_building_name_paf_score.min(sub_building_name_nag_score)

    "unit."+ref_hierarchy_param+organisation_name_param+sub_building_name_param+sub_building_number_param
  }

  /**
    * Calculates the edit distance between two strings
    * @param str1
    * @param str2
    * @return int number of edits required
    */
  def levenshtein(str1: String, str2: String): Int = {
    val lenStr1 = str1.length
    val lenStr2 = str2.length
    val d: Array[Array[Int]] = Array.ofDim(lenStr1 + 1, lenStr2 + 1)
    for (i <- 0 to lenStr1) d(i)(0) = i
    for (j <- 0 to lenStr2) d(0)(j) = j
    for (i <- 1 to lenStr1; j <- 1 to lenStr2) {
      val cost = if (str1(i - 1) == str2(j - 1)) 0 else 1
      d(i)(j) = min(
        d(i-1)(j  ) + 1,     // deletion
        d(i  )(j-1) + 1,     // insertion
        d(i-1)(j-1) + cost   // substitution
      )
    }
    d(lenStr1)(lenStr2)
  }

  /**
    * Return the smallest number in a list
    * @param nums
    * @return
    */
  def min(nums: Int*): Int = nums.min

  /**
    * Try to get the lowest numbers in a range
    * @param range
    * @return
    */
  def getRangeBottom(range: String): Int = {
    val input = List(
      CrfTokenResult(
        value = range,
        label = Tokens.buildingName
      )
    )
    val results = Tokens.postTokenizeTreatment(input)
    results.getOrElse(key=Tokens.paoStartNumber, default="-1").toInt
  }

  /**
    * Try to get the highest number in a range
    * @param range
    * @return
    */
  def getRangeTop(range: String): Int = {
    val input = List(
      CrfTokenResult(
        value = range,
        label = Tokens.buildingName
      )
    )
    val results = Tokens.postTokenizeTreatment(input)
    results.getOrElse(key=Tokens.paoEndNumber, default="-1").toInt
  }

  /**
    * Try to get the lowest numbers in a range
    * @param range
    * @return
    */
  def getStartSuffix(range: String): String = {
    val input = List(
      CrfTokenResult(
        value = range,
        label = Tokens.buildingName
      )
    )
    val results = Tokens.postTokenizeTreatment(input)
    results.getOrElse(key=Tokens.paoStartSuffix, default="@")
  }

  /**
    * Try to get the highest number in a range
    * @param range
    * @return
    */
  def getEndSuffix(range: String): String = {
    val input = List(
      CrfTokenResult(
        value = range,
        label = Tokens.buildingName
      )
    )
    val results = Tokens.postTokenizeTreatment(input)
    results.getOrElse(key=Tokens.paoEndSuffix, default="@")
  }

  /**
    * Compare to multipart names and return an edit distance
    * @param name1
    * @param name2
    * @return edit distance number
    */
  def matchNames(name1: String, name2: String): Int = {
    val nameArray1 = name1.split(" ")
    val nameArray2 = name2.split(" ")
    val scoreBuff1 = new ListBuffer[Int]
    for (name1 <- nameArray1) {
      val scoreBuff2 = new ListBuffer[Int]
      for (name2 <- nameArray2){
        scoreBuff2 += levenshtein(name1,name2)
      }
      scoreBuff1 += scoreBuff2.min
    }
    scoreBuff1.max
  }

  /**
    * Compare to multipart street names and return an edit distance
    * For street names also try joining first two words and score ROAD v STREET as 2
    * @param name1
    * @param name2
    * @return edit distance number
    */
  def matchStreets(name1: String, name2: String): Int = {
    val nameArray1 = name1.split(" ")
    val nameArray2 = name2.split(" ")
    val name1concat = Try(nameArray1(0)).getOrElse("") + Try(nameArray1(1)).getOrElse("")
    val name2concat = Try(nameArray2(0)).getOrElse("") + Try(nameArray2(1)).getOrElse("")
    val scoreBuff1 = new ListBuffer[Int]
    for (name1 <- nameArray1) {
      val scoreBuff2 = new ListBuffer[Int]
      for (name2 <- nameArray2){
        scoreBuff2 += levenshtein(name1,name2)
        scoreBuff2 += levenshtein(name1concat,name2)
        scoreBuff2 += levenshtein(name1,name2concat)
        if (isRoadWord(name1) && isRoadWord(name2)) scoreBuff2 += 2
      }
      scoreBuff1 += scoreBuff2.min
    }
    scoreBuff1.max
  }

  /**
    * Check if word is ROAD, STREET etc.
    * @param word
    * @return
    */
  def isRoadWord(word: String): Boolean = {
    Tokens.road.contains(word)
  }

  /**
    * Function to swap two characters in a string
    * @param s
    * @param idx1
    * @param idx2
    * @return new string
    */
  def swap(s : String, idx1 : Int, idx2 : Int): String = {
    val cs = s.toCharArray
    val swp = cs(idx1)
    cs(idx1) = cs(idx2)
    cs(idx2) = swp
    new String(cs)
  }

  /**
    * Exctract sector part of postcode
    * @param pcode
    * @return
    */
  def getSector(pcode: String): String = {
    val pcArray = pcode.split(" ")
    Try(pcArray(0)).getOrElse("") + " " + Try(pcArray(1).substring(0,1)).getOrElse("")
  }

  /**
    * Extract outcode part of postcode
    * @param pcode
    * @return
    */
  def getOutcode(pcode: String): String = {
    val pcArray = pcode.split(" ")
    Try(pcArray(0)).getOrElse("")
  }

  /**
    * Takes a locality Score string and counts the number of different postcode sectors for
    * results matching that string.
    * Postcode match score must be 4 or higher.
    * Example: three results have locality score string of 9614. If they all have the same postcode sector
    * the pentaly is 1 (so no reduction). If they are all different the penalty is 3.
    * @param localityScoreDebug
    * @param address
    * @param localityParams
    * @return
    */
  def caluclateAmbiguityPenalty(localityScoreDebug: String, localityParams: ListBuffer[Tuple2[String,String]]): Double = {
    val postcodeScore = Try(Try(localityScoreDebug.substring(12,13)).getOrElse("9").toInt).getOrElse(9)
    val sectors = new ListBuffer[String]
    for (localityTuple <- localityParams){
      if (localityScoreDebug == localityTuple._1){
        sectors += localityTuple._2
      }
    }
    val penalty = if (postcodeScore < 4) 1 else sectors.distinct.size
    penalty.toDouble
  }

  /**
    * Make score matrix file into Map
    * @param path
    * @return
    */
  def fileToMap(path: String): Map[String,String] = {
    Source.fromFile(path).getLines().map { l =>
      val Array(k,v1,_*) = l.split('=')
      k -> (v1) }.toMap
  }

}
