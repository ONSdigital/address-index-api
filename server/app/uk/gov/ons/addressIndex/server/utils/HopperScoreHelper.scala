package uk.gov.ons.addressIndex.server.utils

import uk.gov.ons.addressIndex.model.db.BulkAddress
import uk.gov.ons.addressIndex.model.server.response.address.AddressResponseAddress
import uk.gov.ons.addressIndex.parsers.Tokens

import scala.util.Try

/**
  * Utility class to calculate Hopper matching scores
  * Takes the address response and adds the scores to each result
  * Uses an external file to lookup the scoring matrices
  */
object HopperScoreHelper  {

  val logger = GenericLogger("HopperScoreHelper")
  val empty = "@"
  val defaultBuildingScore = "99"
  val defaultLocalityScore = "9999"
  val defaultUnitScore = "9999"

  // load score matrix from external file in parsers
  lazy val scoreMatrix: Map[String,String] = Tokens.fileToMap(s"scorematrix.txt")

  /**
    * Creates a new immutable sequence of addresses with the scores set
    * Currently doesn't sort by any of the new scores but could do
    * @param addresses sequence of matching addresses, typically 10
    * @param tokens matching input tokens created by parser
    * @return scored addresses
    */
  def getScoresForAddresses(addresses: Seq[AddressResponseAddress], tokens: Map[String, String], elasticDenominator: Double): Seq[AddressResponseAddress] = {
    val startingTime = System.currentTimeMillis()
    val localityParams = addresses.map(address => getLocalityParams(address,tokens))
    val scoredAddresses = addresses.zipWithIndex.map{case (address, index) => addScoresToAddress(index, address, tokens, localityParams, elasticDenominator)}
    val endingTime = System.currentTimeMillis()
    logger.trace("Hopper Score calucation time = "+(endingTime-startingTime)+" milliseconds")
    scoredAddresses
  }

  def getScoresForBulks(addresses: Seq[BulkAddress], tokens: Map[String, String], elasticDenominator: Double): Seq[AddressResponseAddress] = {
    val startingTime = System.currentTimeMillis()
    val localityParams = addresses.map(address => getLocalityParams(AddressResponseAddress.fromHybridAddress(address.hybridAddress, true),tokens))
    val scoredAddresses = addresses.zipWithIndex.map{case (address, index) => addScoresToAddress(index, AddressResponseAddress.fromHybridAddress(address.hybridAddress, true), tokens, localityParams, elasticDenominator)}
    val endingTime = System.currentTimeMillis()
    logger.trace("Hopper Score calucation time = "+(endingTime-startingTime)+" milliseconds")
    scoredAddresses
  }

  def getLocalityParams(address: AddressResponseAddress, tokens: Map[String, String]): (String,String) = {

    val organisationName = tokens.getOrElse(Tokens.organisationName, empty)
    val buildingName = tokens.getOrElse(Tokens.buildingName, empty)
    val streetName = tokens.getOrElse(Tokens.streetName, empty)
    val locality = tokens.getOrElse(Tokens.locality, empty)
    val townName = tokens.getOrElse(Tokens.townName, empty)
    val postcode = tokens.getOrElse(Tokens.postcode, empty)
    val postcodeIn = tokens.getOrElse(Tokens.postcodeIn, empty)
    val postcodeOut = tokens.getOrElse(Tokens.postcodeOut, empty)

    val pafPostcode = address.paf.map(_.postcode).getOrElse("")
    val nagPostcode = address.nag.map(_.postcodeLocator).getOrElse("")
    val postcodeToUse = if (pafPostcode != "") pafPostcode else nagPostcode

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
      getSector(postcodeToUse))
  }

  /**
    * Calculates the Hopper scores for a single address
    * @param address single address response object
    * @param tokens list of tokens handed down
    * @return new address response oobject with scores
    */
  def addScoresToAddress(addNo: Int,  address: AddressResponseAddress,
    tokens: Map[String, String],
    localityParams: Seq[(String,String)],elasticDenominator: Double): AddressResponseAddress = {

    val organisationName = tokens.getOrElse(Tokens.organisationName, empty)
    val subBuildingName = tokens.getOrElse(Tokens.subBuildingName, empty)
    val buildingName = tokens.getOrElse(Tokens.buildingName, empty)
    val buildingNumber = tokens.getOrElse(Tokens.buildingNumber, empty)
    val paoStartNumber = tokens.getOrElse(Tokens.paoStartNumber, empty)
    val paoStartSuffix = tokens.getOrElse(Tokens.paoStartSuffix, empty)
    val paoEndNumber = tokens.getOrElse(Tokens.paoEndNumber, empty)
    val paoEndSuffix = tokens.getOrElse(Tokens.paoEndSuffix, empty)
    val saoStartNumber = tokens.getOrElse(Tokens.saoStartNumber, empty)
    val saoStartSuffix = tokens.getOrElse(Tokens.saoStartSuffix, empty)
    val saoEndNumber = tokens.getOrElse(Tokens.saoEndNumber, empty)
    val saoEndSuffix = tokens.getOrElse(Tokens.saoEndSuffix, empty)

    val buildingScoreDebug = calculateBuildingScore(address,
      buildingName,
      buildingNumber,
      paoStartNumber,
      paoEndNumber,
      paoStartSuffix,
      paoEndSuffix,
      organisationName)

    val buildingScore = Try(scoreMatrix(buildingScoreDebug).toDouble).getOrElse(0d)

    //  extract locality score from values previously calculated for ambiguity calculation
    val localityScoreDebug = localityParams.lift(addNo).getOrElse(("",""))._1

    val ambiguityPenalty = calculateAmbiguityPenalty(localityScoreDebug,localityParams)

    val localityScore = Try(scoreMatrix(localityScoreDebug).toDouble).getOrElse(0d) / ambiguityPenalty

    val unitScoreDebug = calculateUnitScore(
      address,
      subBuildingName,
      saoStartNumber,
      saoEndNumber,
      saoStartSuffix,
      saoEndSuffix,
      organisationName)

    val unitScore = Try(scoreMatrix(unitScoreDebug).toDouble).getOrElse(0d)

    val structuralScore = calculateStructuralScore(buildingScore, localityScore)
    val objectScore = calculateObjectScore(buildingScore,localityScore,unitScore)
    val respBuildingScoreDebug = Try(buildingScoreDebug.substring(buildingScoreDebug.indexOf(".") + 1)).getOrElse("99")
    val respLocalityScoreDebug = Try(localityScoreDebug.substring(localityScoreDebug.indexOf(".") + 1)).getOrElse("9999")
    val respUnitScoreDebug = Try(unitScoreDebug.substring(unitScoreDebug.indexOf(".") + 1)).getOrElse("9999")

    val safeDenominator = if (elasticDenominator == 0) 1 else elasticDenominator
    val elasticRatio = if (elasticDenominator == -1D) 1.2D else Try(address.underlyingScore).getOrElse(1F) / safeDenominator
    val confidenceScore = ConfidenceScoreHelper.calculateConfidenceScore(tokens, structuralScore, unitScore, elasticRatio)

    address.copy(confidenceScore=confidenceScore)
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

    // get paf values
    val pafBuildingName = address.paf.map(_.buildingName).getOrElse("")
    val pafBuildingNumber = address.paf.map(_.buildingNumber).getOrElse("")
    val pafOrganisationName = address.paf.map(_.organisationName).getOrElse("")

    //get nag values
    val nagPaoStartNumber = address.nag.map(_.pao).map(_.paoStartNumber).getOrElse("")
    val nagPaoEndNumber = address.nag.map(_.pao).map(_.paoEndNumber).getOrElse("")
    val nagPaoStartSuffix = address.nag.map(_.pao).map(_.paoStartSuffix).getOrElse("")
    val nagPaoEndSuffix = address.nag.map(_.pao).map(_.paoEndSuffix).getOrElse("")
    val nagPaoText = address.nag.map(_.pao).map(_.paoText).getOrElse("")
    val nagSaoText = address.nag.map(_.sao).map(_.saoText).getOrElse("")
    val nagOrganisationName = address.nag.map(_.organisation).getOrElse("")

    // each element score is the better match of paf and nag

    val detailedOrganisationBuildingNamePafScore = calculateDetailedOrganisationBuildingNamePafScore (
      atSignForEmpty(getNonNumberPartsFromName(buildingName)),
      getNonNumberPartsFromName(pafBuildingName),
      organisationName,
      pafOrganisationName)

    val detailedOrganisationBuildingNameNagScore = calculateDetailedOrganisationBuildingNameNagScore (
      atSignForEmpty(getNonNumberPartsFromName(buildingName)),
      getNonNumberPartsFromName(nagPaoText),
      getNonNumberPartsFromName(nagSaoText),
      organisationName,
      nagOrganisationName)

    val detailedOrganisationBuildingNameParam = detailedOrganisationBuildingNamePafScore.min(detailedOrganisationBuildingNameNagScore)

    val buildingNumberPafScore = calculateBuildingNumPafScore (
      atSignForEmpty(getNumberPartsFromName(buildingName)),
      getNumberPartsFromName(pafBuildingName),
      pafBuildingNumber,
      paoStartSuffix,
      paoEndSuffix,
      buildingNumber,
      paoStartNumber,
      paoEndNumber)

    val buildingNumberNagScore = calculateBuildingNumNagScore (
      atSignForEmpty(getNumberPartsFromName(buildingName)),
      nagPaoStartNumber,
      nagPaoEndNumber,
      nagPaoStartSuffix,
      nagPaoEndSuffix,
      paoEndSuffix,
      paoStartSuffix,
      buildingNumber,
      paoStartNumber,
      paoEndNumber)

    val buildingNumberParam = buildingNumberPafScore.min(buildingNumberNagScore)

    "building." + detailedOrganisationBuildingNameParam + buildingNumberParam
  }

  /**
    * Detailed match of origaisation and building name using PAF
    * @param buildingName
    * @param pafBuildingName
    * @param organisationName
    * @param pafOrganisationName
    * @return
    */
  def calculateDetailedOrganisationBuildingNamePafScore (
    buildingName: String,
    pafBuildingName:  String,
    organisationName: String,
    pafOrganisationName: String) : Int = {

    // match building name
    val pafBuildingMatchScore = if (buildingName == empty) 4
    else matchNames(buildingName,pafBuildingName).min(matchNames(pafBuildingName,buildingName))

    // match  organisation
    val pafOrganisationMatchScore = if (organisationName == empty) 4
    else matchNames(organisationName,pafOrganisationName).min(matchNames(pafOrganisationName,organisationName))

    // cross reference
    val pafXrefMatchScore = if (organisationName == empty || buildingName == empty) 4
    else matchNames(organisationName,pafBuildingName).min(matchNames(pafOrganisationName,buildingName))

    // Match buildingName against buildingName and organisationName against OrganisationName (and cross-ref)
    if (buildingName == pafBuildingName || organisationName == pafOrganisationName || buildingName == pafOrganisationName || organisationName == pafBuildingName ) 1
      else if (pafOrganisationMatchScore < 2 || pafBuildingMatchScore < 2 || pafXrefMatchScore < 2) 2
      else if (pafOrganisationMatchScore < 3 || pafBuildingMatchScore < 3 || pafXrefMatchScore < 3) 3
      else if (buildingName == empty && organisationName == empty &&
        pafOrganisationName == "" && pafBuildingName == "" ) 9
      else if (!((buildingName != empty && pafBuildingName != "" ) ||
        (organisationName != empty && pafOrganisationName != "" ))) 7
      else 6
   }

  /**
    * Detailed match of origaisation and building name using NAG
    * @param buildingName
    * @param nagPaoText
    * @param organisationName
    * @param nagOrganisationName
    * @return
    */
  def calculateDetailedOrganisationBuildingNameNagScore (
    buildingName: String,
    nagPaoText: String,
    nagSaoText: String,
    organisationName: String,
    nagOrganisationName: String) : Int = {

    // match building name against Pao text, Sao text and organisation name
    val nagBuildingMatchScore = if (buildingName == empty) 4
    else min(
      matchNames(buildingName,nagPaoText),
      matchNames(nagPaoText,buildingName),
      matchNames(buildingName,nagSaoText),
      matchNames(nagSaoText,buildingName),
      matchNames(buildingName,nagOrganisationName),
      matchNames(nagOrganisationName,buildingName)
    )

    // match organisationa against Pao text, Sao text and organisation name
    val nagOrganisationMatchScore = if (organisationName == empty) 4
    else min(
      matchNames(organisationName,nagPaoText),
      matchNames(nagPaoText,organisationName),
      matchNames(organisationName,nagSaoText),
      matchNames(nagSaoText,organisationName),
      matchNames(organisationName,nagOrganisationName),
      matchNames(nagOrganisationName,organisationName)
    )

    // Take the best match of organisation and building
    if (buildingName == nagPaoText || buildingName == nagSaoText || buildingName == nagOrganisationName ||
      organisationName == nagPaoText || organisationName == nagSaoText || organisationName == nagOrganisationName) 1
      else if (nagOrganisationMatchScore < 2 || nagBuildingMatchScore < 2) 2
      else if (nagOrganisationMatchScore < 3 || nagBuildingMatchScore < 3) 3
      else if (buildingName == empty && organisationName == empty
        && nagOrganisationName == "" && nagPaoText == "" ) 9
      else if (!((buildingName != empty && nagPaoText != "" ) ||
        (organisationName != empty && nagOrganisationName != "" ))) 7
      else 6
  }

  def calculateBuildingNumPafScore (
    buildingName: String,
    pafBuildingName: String,
    pafBuildingNumber: String,
    paoStartSuffix: String,
    paoEndSuffix: String,
    buildingNumber: String,
    paoStartNumber: String,
    paoEndNumber: String) : Int = {

    // match building numbers, ranges and suffixes
    val tokenBuildingLowNum = getRangeBottom(buildingName)
    val tokenBuildingHighNum = tokenBuildingLowNum.max(getRangeTop(buildingName))
    val pafBuildingLowNum = getRangeBottom(pafBuildingName)
    val pafBuildingHighNum = pafBuildingLowNum.max(getRangeTop(pafBuildingName))
    val pafTestBN = Try(pafBuildingNumber.toInt).getOrElse(-1)
    val pafInRange = (((pafBuildingLowNum >= tokenBuildingLowNum && pafBuildingHighNum <= tokenBuildingHighNum)
      || (pafTestBN >= tokenBuildingLowNum && pafTestBN <= tokenBuildingHighNum))
      && (pafBuildingLowNum > -1 || pafTestBN > -1) && tokenBuildingLowNum > -1)
    val pafBuildingStartSuffix = getStartSuffix(pafBuildingName)
    val pafBuildingEndSuffix = getEndSuffix(pafBuildingName)
    val pafSuffixInRange = ((paoStartSuffix == pafBuildingStartSuffix && paoEndSuffix == pafBuildingEndSuffix)
      || (paoEndSuffix == empty && paoStartSuffix >= pafBuildingStartSuffix && paoStartSuffix <= pafBuildingEndSuffix)
      || (pafBuildingEndSuffix == empty && pafBuildingStartSuffix >= paoStartSuffix && pafBuildingStartSuffix <= paoEndSuffix ))

    // 7 Gate Reach TOKENS: buildingNumber = 7 and PaoStartNumber =7 MATCHTO: paf.BuildingNumber = 7
    // 4A-5B Gate Reach gives TOKENS: buildingName = 4A-5B (buildingNumber empty), PaoStartNumber = 4, PaoStartSuffix = A, PaoEndNumber = 5, PaoEndSuffix = B
    // MATCHTO:  paf.buildingNumber = 4 (for single num) or paf.buildingName = 4B (suffix and/or range)
    if (buildingNumber == pafBuildingNumber || (buildingNumber == empty && buildingName == pafBuildingName)) 1
    else if (pafSuffixInRange && (pafBuildingNumber == paoStartNumber ||
      pafBuildingLowNum.toString() == paoStartNumber || pafBuildingHighNum.toString() == paoEndNumber)) 2
    else if (pafInRange && pafSuffixInRange) 3
    else if (pafBuildingNumber == paoStartNumber ||
      pafBuildingLowNum.toString() == paoStartNumber || pafBuildingHighNum.toString() == paoEndNumber) 4
    else if ((tokenBuildingLowNum != -1 || buildingNumber != empty ) &&
      (pafBuildingLowNum != -1 || pafBuildingNumber != "" )) 6
    else 9
  }

  def calculateBuildingNumNagScore (
    buildingName: String,
    nagPaoStartNumber: String,
    nagPaoEndNumber: String,
    nagPaoStartSuffix: String,
    nagPaoEndSuffix: String,
    paoEndSuffix: String,
    paoStartSuffix: String,
    buildingNumber: String,
    paoStartNumber: String,
    paoEndNumber: String) : Int = {

    // match building numbers, ranges and suffixes
    val tokenBuildingLowNum = getRangeBottom(buildingName)
    val tokenBuildingHighNum = tokenBuildingLowNum.max(getRangeTop(buildingName))
    val nagBuildingLowNum = Try(nagPaoStartNumber.toInt).getOrElse(-1)
    val nagBuildingHighNum = nagBuildingLowNum.max(Try(nagPaoEndNumber.toInt).getOrElse(-1))
    val nagInRange = ((nagBuildingLowNum >= tokenBuildingLowNum && nagBuildingHighNum <= tokenBuildingHighNum)
      && nagBuildingLowNum > -1  && tokenBuildingLowNum > -1)
    val nagBuildingStartSuffix = if (nagPaoStartSuffix == "" ) empty else nagPaoStartSuffix
    val nagBuildingEndSuffix = if (nagPaoEndSuffix == "" ) empty else nagPaoEndSuffix
    val nagSuffixInRange = ((paoStartSuffix == nagBuildingStartSuffix && paoEndSuffix == nagBuildingEndSuffix)
      || (paoEndSuffix == empty && paoStartSuffix >=nagBuildingStartSuffix && paoStartSuffix <= nagBuildingEndSuffix)
      || (nagBuildingEndSuffix == empty && nagBuildingStartSuffix >= paoStartSuffix && nagBuildingStartSuffix <= paoEndSuffix ))

    // 7 Gate Reach TOKENS: buildingNumber = 7 and PaoStartNumber =7 MATCHTO: nag.PaoStartNumber = 7
    // 4A-5B Gate TOKENS: buildingName = 4A-5B (buildingNumber empty), PaoStartNumber = 4, PaoStartSuffix = A, PaoEndNumber = 5, PaoEndSuffix = B
    // MATCHTO: nag.Paotext = 4A-5B, nag.paoStartNumber = 4, nag.paoStartSuffix = A, nag.paoEndNumber = 5, nag.paoEndSuffix = B

      if (((buildingNumber == nagPaoStartNumber && atSignForEmpty(nagPaoEndNumber) == empty) ||
        (paoStartNumber == atSignForEmpty(nagPaoStartNumber) && paoEndNumber == atSignForEmpty(nagPaoEndNumber)))
        && (atSignForEmpty(nagPaoStartSuffix) == paoStartSuffix) && (atSignForEmpty(nagPaoEndSuffix) == paoEndSuffix)) 1
      else if (nagSuffixInRange &&
        (paoStartNumber == nagPaoStartNumber || paoEndNumber == nagPaoEndNumber )) 2
      else if (nagInRange && nagSuffixInRange) 3
      else if (nagPaoStartNumber == paoStartNumber || paoEndNumber == nagPaoEndNumber) 4
      else if (!((tokenBuildingLowNum == -1 && buildingNumber == empty ) || (nagBuildingLowNum == -1))) 6
      else 9
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

    // get paf values
    val pafBuildingName = address.paf.map(_.buildingName).getOrElse("")
    val pafOrganisationName = address.paf.map(_.organisationName).getOrElse("")
    val pafThoroughfare = address.paf.map(_.thoroughfare).getOrElse("")
    val pafDependentThoroughfare = address.paf.map(_.dependentThoroughfare).getOrElse("")
    val pafWelshThoroughfare = address.paf.map(_.welshThoroughfare).getOrElse("")
    val pafWelshDependentThoroughfare = address.paf.map(_.welshDependentThoroughfare).getOrElse("")
    val pafPostTown = address.paf.map(_.postTown).getOrElse("")
    val pafWelshPostTown = address.paf.map(_.welshPostTown).getOrElse("")
    val pafDependentLocality = address.paf.map(_.dependentLocality).getOrElse("")
    val pafWelshDependentLocality = address.paf.map(_.welshDependentLocality).getOrElse("")
    val pafDoubleDependentLocality = address.paf.map(_.doubleDependentLocality).getOrElse("")
    val pafWelshDoubleDependentLocality = address.paf.map(_.welshDoubleDependentLocality).getOrElse("")
    val pafPostcode = address.paf.map(_.postcode).getOrElse("")

    //get nag values
    val nagPaoText = address.nag.map(_.pao).map(_.paoText).getOrElse("")
    val nagOrganisationName = address.nag.map(_.organisation).getOrElse("")
    val nagStreetDescriptor = address.nag.map(_.streetDescriptor).getOrElse("")
    val nagTownName = address.nag.map(_.townName).getOrElse("")
    val nagLocality = address.nag.map(_.locality).getOrElse("")
    val nagPostcode = address.nag.map(_.postcodeLocator).getOrElse("")

    // create test fields for postcode match
    val postcodeWithInvertedIncode = if (postcodeIn.length < 3) empty else swap(postcodeIn,1,2)
    val postcodeSector = if (postcodeIn.length < 3) empty else postcodeOut + " " + postcodeIn.substring(0,1)
    val postcodeArea = if (postcodeOut.length <= 1) empty else
      if (postcodeOut.length == 2) postcodeOut.substring(0,1) else postcodeOut.substring(0,2)

    // each element score is the better match of paf and nag

    val OrganisationBuildingNamePafScore = calculateOrganisationBuildingNamePafScore (
      atSignForEmpty(getNonNumberPartsFromName(buildingName)),
      getNonNumberPartsFromName(pafBuildingName),
      organisationName,
      pafOrganisationName)

    val OrganisationBuildingNameNagScore = calculateOrganisationBuildingNameNagScore (
      atSignForEmpty(getNonNumberPartsFromName(buildingName)),
      getNonNumberPartsFromName(nagPaoText),
      organisationName,
      nagOrganisationName)

    val organisationBuildingNameParam = OrganisationBuildingNamePafScore.min(OrganisationBuildingNameNagScore)

    val streetPafScore = calculateStreetPafScore (
      streetName,
      pafThoroughfare,
      pafDependentThoroughfare,
      pafWelshThoroughfare,
      pafWelshDependentThoroughfare)

    val streetNagScore = calculateStreetNagScore(streetName, nagStreetDescriptor)

    val streetParam = streetPafScore.min(streetNagScore)

    val townLocalityPafScore = calculateTownLocalityPafScore (
      townName,
      locality,
      pafPostTown,
      pafWelshPostTown,
      pafDependentLocality,
      pafWelshDependentLocality,
      pafDoubleDependentLocality,
      pafWelshDoubleDependentLocality,
      streetName)

    val townLocalityNagScore = calculateTownLocalityNagScore (
      townName,
      nagTownName,
      locality,
      nagLocality,
      streetName)

    val townLocalityParam = townLocalityPafScore.min(townLocalityNagScore)

    val postcodePafScore = calculatePostcodePafScore (
      postcode,
      pafPostcode,
      postcodeOut,
      postcodeWithInvertedIncode,
      postcodeSector,
      postcodeArea)

    val postcodeNagScore = calculatePostcodeNagScore (
      postcode,
      nagPostcode,
      postcodeOut,
      postcodeWithInvertedIncode,
      postcodeSector,
      postcodeArea)

    val postcodeParam = postcodePafScore.min(postcodeNagScore)

    "locality." + organisationBuildingNameParam + streetParam + townLocalityParam + postcodeParam
  }


  /**
    * Match building and organisation using PAF
    * @param buildingName
    * @param pafBuildingName
    * @param organisationName
    * @param pafOrganisationName
    * @return
    */
  def calculateOrganisationBuildingNamePafScore (
    buildingName: String,
    pafBuildingName: String,
    organisationName: String,
    pafOrganisationName: String) : Int = {

    // building with paf building only
    val pafBuildingMatchScore = if (buildingName == empty) 4
    else min(
      matchNames(buildingName,pafBuildingName),
      matchNames(pafBuildingName,buildingName)
    )

   // organisation can match with paf organisation or building
    val pafOrganisationMatchScore = if (organisationName == empty) 4
    else min(
      matchNames(organisationName,pafOrganisationName),
      matchNames(pafOrganisationName,organisationName),
      matchNames(organisationName,pafBuildingName),
      matchNames(pafBuildingName,organisationName)
    )

    // Accept a PAF match via organisation or building with edit distance of 2 or less
    if (pafOrganisationMatchScore < 3 || pafBuildingMatchScore < 3) 1
      else if ((buildingName != empty && pafBuildingName != "" ) ||
        (organisationName != empty && pafOrganisationName != "" )) 6
      else 9
  }

  /**
    * Match organisation and building name using NAG
    * @param buildingName
    * @param nagPaoText
    * @param organisationName
    * @param nagOrganisationName
    * @return
    */
  def calculateOrganisationBuildingNameNagScore (
    buildingName: String,
    nagPaoText: String,
    organisationName: String,
    nagOrganisationName: String) : Int = {

    val nagBuildingMatchScore = if (buildingName == empty) 4
    else matchNames(buildingName,nagPaoText).min(matchNames(nagPaoText,buildingName))

    val nagOrganisationMatchScore = if (organisationName == empty) 4
    else min(
      matchNames(organisationName,nagOrganisationName),
      matchNames(nagOrganisationName,organisationName),
      matchNames(organisationName,nagPaoText),
      matchNames(nagPaoText,organisationName)
    )

    // Accept a NAG match via organisation or building with edit distance of 2 or less
    if (nagOrganisationMatchScore < 3 || nagBuildingMatchScore < 3) 1
      else if ((buildingName != empty && nagPaoText != "" ) ||
        (organisationName != empty && nagOrganisationName != "" )) 6
      else 9
  }

  /**
    * Match Street using PAF
    * @param streetName
    * @param pafThoroughfare
    * @param pafDependentThoroughfare
    * @param pafWelshThoroughfare
    * @param pafWelshDependentThoroughfare
    * @return
    */
  def calculateStreetPafScore (
    streetName: String,
    pafThoroughfare: String,
    pafDependentThoroughfare: String,
    pafWelshThoroughfare: String,
    pafWelshDependentThoroughfare: String) : Int = {

    val pafThoroStreetMatchScore = matchStreets(streetName,pafThoroughfare).min(matchStreets(pafThoroughfare,streetName))
    val pafDepThoroStreetMatchScore = matchStreets(streetName,pafDependentThoroughfare)
      .min(matchStreets(pafDependentThoroughfare,streetName))
    val pafWelshStreetMatchScore = matchStreets(streetName,pafWelshThoroughfare)
      .min(matchStreets(pafWelshThoroughfare,streetName))
    val pafDepWelshStreetMatchScore = matchStreets(streetName,pafWelshDependentThoroughfare)
      .min(matchStreets(pafWelshDependentThoroughfare,streetName))
    val pafStreetMatchScore = if (streetName == empty) 4
    else min(pafThoroStreetMatchScore,pafDepThoroStreetMatchScore,pafWelshStreetMatchScore,pafDepWelshStreetMatchScore)

    if (pafStreetMatchScore == 0) 1
      else if (pafStreetMatchScore == 1) 2
      else if (pafStreetMatchScore == 2) 5
      else if (streetName == empty) 9
      else 6
  }

  /**
    * Match Street using NAG
    * @param streetName
    * @param nagStreetDescriptor
    * @return
    */
  def calculateStreetNagScore(streetName: String, nagStreetDescriptor: String) : Int = {

    val nagStreetMatchScore = if (streetName == empty) 4
    else matchStreets(streetName,nagStreetDescriptor).min(matchStreets(nagStreetDescriptor,streetName))

    if (nagStreetMatchScore == 0) 1
      else if (nagStreetMatchScore == 1) 2
      else if (nagStreetMatchScore == 2) 5
      else if (streetName == empty) 9
      else 6
  }

  /**
    * Attempt to  match town and locality using PAF
    * @param townName
    * @param locality
    * @param pafPostTown
    * @param pafWelshPostTown
    * @param pafDependentLocality
    * @param pafWelshDependentLocality
    * @param pafDoubleDependentLocality
    * @param pafWelshDoubleDependentLocality
    * @param streetName
    * @return
    */
  def calculateTownLocalityPafScore (
    townName: String,
    locality: String,
    pafPostTown: String,
    pafWelshPostTown: String,
    pafDependentLocality: String,
    pafWelshDependentLocality: String,
    pafDoubleDependentLocality: String,
    pafWelshDoubleDependentLocality: String,
    streetName: String) : Int = {

    // match town name
    val pafPostTownTownNameMatchScore = matchNames(townName,pafPostTown).min(matchNames(pafPostTown,townName))
    val pafWelshPostTownTownNameMatchScore = matchNames(townName,pafWelshPostTown)
      .min(matchNames(pafWelshPostTown,townName))
    val pafDependentLocalityTownNameMatchScore = matchNames(townName,pafDependentLocality)
      .min(matchNames(pafDependentLocality,townName))
    val pafWelshDependentLocalityTownNameMatchScore = matchNames(townName,pafWelshDependentLocality)
      .min(matchNames(pafWelshDependentLocality,townName))
    val pafDoubleDependentLocalityTownNameMatchScore = matchNames(townName,pafDoubleDependentLocality)
      .min(matchNames(pafDoubleDependentLocality,townName))
    val pafWelshDoubleDependentLocalityTownNameMatchScore = matchNames(townName,pafWelshDoubleDependentLocality)
      .min(matchNames(pafWelshDoubleDependentLocality,townName))
    val pafTownNameMatchScore = if (townName == empty) 4
    else min(pafPostTownTownNameMatchScore,
      pafWelshPostTownTownNameMatchScore,
      pafDependentLocalityTownNameMatchScore,
      pafWelshDependentLocalityTownNameMatchScore,
      pafDoubleDependentLocalityTownNameMatchScore,
      pafWelshDoubleDependentLocalityTownNameMatchScore)

    // match locality
    val pafPostTownlocalityMatchScore = matchNames(locality,pafPostTown).min(matchNames(pafPostTown,locality))
    val pafWelshPostTownlocalityMatchScore = matchNames(locality,pafWelshPostTown)
      .min(matchNames(pafWelshPostTown,locality))
    val pafDependentLocalitylocalityMatchScore = matchNames(locality,pafDependentLocality)
      .min(matchNames(pafDependentLocality,locality))
    val pafWelshDependentLocalitylocalityMatchScore = matchNames(locality,pafWelshDependentLocality)
      .min(matchNames(pafWelshDependentLocality,locality))
    val pafDoubleDependentLocalitylocalityMatchScore = matchNames(locality,pafDoubleDependentLocality)
      .min(matchNames(pafDoubleDependentLocality,locality))
    val pafWelshDoubleDependentLocalitylocalityMatchScore = matchNames(locality,pafWelshDoubleDependentLocality)
      .min(matchNames(pafWelshDoubleDependentLocality,locality))
    val pafLocalityMatchScore = if (locality == empty) 4
    else min(pafPostTownlocalityMatchScore,
      pafWelshPostTownlocalityMatchScore,
      pafDependentLocalitylocalityMatchScore,
      pafWelshDependentLocalitylocalityMatchScore,
      pafDoubleDependentLocalitylocalityMatchScore,
      pafWelshDoubleDependentLocalitylocalityMatchScore)

    // Accept a PAF match via locality with an edit distance of 2 or less
    if (pafTownNameMatchScore < 2 || pafLocalityMatchScore < 2) 1
      else if (streetName == empty) 9
      else 6
  }

  /**
    * Attempt to match town and locality using NAG
    * @param townName
    * @param nagTownName
    * @param locality
    * @param nagLocality
    * @param streetName
    * @return
    */
  def calculateTownLocalityNagScore (
    townName: String,
    nagTownName: String,
    locality: String,
    nagLocality: String,
    streetName: String) : Int = {

    // town name
    val nagTownNameTownNameMatchScore = matchNames(townName,nagTownName).min(matchNames(nagTownName,townName))
    val nagLocalityTownNameMatchScore = matchNames(townName,nagLocality).min(matchNames(nagLocality,townName))
    val nagTownNameMatchScore = if (townName == empty) 4
    else min(nagTownNameTownNameMatchScore,nagLocalityTownNameMatchScore)

    // locality
    val nagTownNamelocalityMatchScore = matchNames(locality,nagTownName).min(matchNames(nagTownName,locality))
    val nagLocalitylocalityMatchScore = matchNames(locality,nagLocality).min(matchNames(nagLocality,locality))
    val nagLocalityMatchScore = if (locality == empty) 4
    else min(nagTownNamelocalityMatchScore,nagLocalitylocalityMatchScore)

    // Accept a NAG match via locality with an edit distance of 2 or less
      if (nagTownNameMatchScore < 2 || nagLocalityMatchScore < 2) 1
      else if (streetName == empty) 9
      else 6
  }

  /**
    * Match PAF postocde
    * Postcode token is formatted with space so can do exact match
    * Use helpers to match inversion, sector, outcode and area
    * @param postcode
    * @param pafPostcode
    * @param postcodeOut
    * @param postcodeWithInvertedIncode
    * @param postcodeSector
    * @param postcodeArea
    * @return
    */
  def calculatePostcodePafScore (
    postcode: String,
    pafPostcode: String,
    postcodeOut: String,
    postcodeWithInvertedIncode: String,
    postcodeSector: String,
    postcodeArea: String) : Int = {

    if (postcode == pafPostcode) 1
      else if ((postcodeOut + " " + postcodeWithInvertedIncode) == pafPostcode) 2
      else if (postcodeSector == getSector(pafPostcode)) 3
      else if (postcodeOut == getOutcode(pafPostcode)) 4
      else if (postcodeArea == Try(pafPostcode.substring(0,2)).getOrElse("")) 5
      else if (postcode == empty) 9
      else 6
  }

  /**
    * Match NAG postcode
    * Postcode token is formatted with space so can do exact match
    * Use helpers to match inversion, sector, outcode and area
    * @param postcode
    * @param nagPostcode
    * @param postcodeOut
    * @param postcodeWithInvertedIncode
    * @param postcodeSector
    * @param postcodeArea
    * @return
    */
  def calculatePostcodeNagScore (
    postcode: String,
    nagPostcode: String,
    postcodeOut: String,
    postcodeWithInvertedIncode: String,
    postcodeSector: String,
    postcodeArea: String) : Int = {

      if (postcode == nagPostcode) 1
      else if ((postcodeOut + " " + postcodeWithInvertedIncode) == nagPostcode) 2
      else if (postcodeSector == getSector(nagPostcode)) 3
      else if (postcodeOut == getOutcode(nagPostcode)) 4
      else if (postcodeArea == Try(nagPostcode.substring(0,2)).getOrElse("")) 5
      else if (postcode == empty) 9
      else 6
  }

  /**
    * Calculates how well the sub-building or room fields match
    * The hierarchical field is not currently available
    * If not hierarchical set to missing (?) or source sub-building name
    * or source origanisation name is unmatched
    * @param address
    * @param subBuildingName
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
    saoStartNumber: String,
    saoEndNumber: String,
    saoStartSuffix: String,
    saoEndSuffix: String,
    organisationName: String): String = {

     // get paf values
    val pafBuildingName = address.paf.map(_.buildingName).getOrElse("")
    val pafBuildingNumber = address.paf.map(_.buildingNumber).getOrElse("")
    val pafSubBuildingName = address.paf.map(_.subBuildingName).getOrElse("")
    val pafOrganisationName = address.paf.map(_.organisationName).getOrElse("")

    //get nag values
    val nagPaoText = address.nag.map(_.pao).map(_.paoText).getOrElse("")
    val nagSaoText = address.nag.map(_.sao).map(_.saoText).getOrElse("")
    val nagOrganisationName = address.nag.map(_.organisation).getOrElse("")
    val nagSaoStartNumber = address.nag.map(_.sao).map(_.saoStartNumber).getOrElse("")
    val nagSaoEndNumber = address.nag.map(_.sao).map(_.saoEndNumber).getOrElse("")
    val nagSaoStartSuffix = address.nag.map(_.sao).map(_.saoStartSuffix).getOrElse("")
    val nagSaoEndSuffix = address.nag.map(_.sao).map(_.saoEndSuffix).getOrElse("")

    // test for more than 1 layer - may need to expand this into separate method with more logic
    val parentUPRN = address.parentUprn
    val numRels = address.relatives.size
    // if the address is the top level score it as a non-hierarchical
    // could give different number e.g 2 to allow these to be scored differently
    val refHierarchyParam = if (numRels > 1){
    if (parentUPRN == "0") 0 else 1
    }  else 0

    // each element score is the better match of paf and nag

    val orgainisationNameNagScore =
      calculateOrganisationNameNagScore(organisationName,nagPaoText,nagSaoText,nagOrganisationName,pafOrganisationName)
    // no PAF value
    val organisationNameParam = orgainisationNameNagScore

    val subBuildingNamePafScore = calculateSubBuildingNamePafScore(
      atSignForEmpty(getNonNumberPartsFromName(subBuildingName)),
      getNonNumberPartsFromName(pafSubBuildingName),atSignForEmpty(getNonNumberPartsFromName(organisationName)))
    val subBuildingNameNagScore = calculateSubBuildingNameNagScore(
      atSignForEmpty(getNonNumberPartsFromName(subBuildingName)),
      getNonNumberPartsFromName(nagSaoText))
    val subBuildingNameParam = subBuildingNamePafScore.min(subBuildingNameNagScore)

    val subBuildingNumberPafScore = calculateSubBuildingNumberPafScore (
      atSignForEmpty(getNumberPartsFromName(subBuildingName)),
      getNumberPartsFromName(pafSubBuildingName),
      getNumberPartsFromName(pafBuildingName),
      saoStartSuffix,
      saoEndSuffix,
      saoStartNumber,
      saoEndNumber,
      pafBuildingNumber)

    val subBuildingNumberNagScore = calculateSubBuildingNumberNagScore (
      atSignForEmpty(getNumberPartsFromName(subBuildingName)),
      nagSaoText,
      nagSaoStartNumber,
      nagSaoEndNumber,
      nagSaoStartSuffix,
      nagSaoEndSuffix,
      saoStartSuffix,
      saoEndSuffix,
      saoStartNumber,
      saoEndNumber)

    val subBuildingNumberParam = subBuildingNumberPafScore.min(subBuildingNumberNagScore)

    "unit." + refHierarchyParam + organisationNameParam + subBuildingNameParam + subBuildingNumberParam
  }

  def calculateOrganisationNameNagScore (
    organisationName: String,
    nagPaoText: String,
    nagSaoText: String,
    nagOrganisationName: String,
    pafOrganisationName: String) : Int = {

    // match oganisation
    val nagPAOOrganisationMatchScore = if (organisationName == empty) 4
      else matchNames(organisationName,nagPaoText).min(matchNames(nagPaoText,organisationName))
    val nagSAOOrganisationMatchScore = if (organisationName == empty) 4
      else matchNames(organisationName,nagSaoText).min(matchNames(nagSaoText,organisationName))
    val nagOrganisationMatchScore = if (organisationName == empty) 4
      else matchNames(organisationName,nagOrganisationName).min(matchNames(nagOrganisationName,organisationName))

    // Look for organisation match agaings PAO, SAO, or Organisation (NAG only)
    if (nagPAOOrganisationMatchScore < 3 || nagSAOOrganisationMatchScore < 3 || nagOrganisationMatchScore < 3 ) 1
    else if (organisationName == empty && nagOrganisationName == "" && ((nagPaoText == "" && nagSaoText == "") || (pafOrganisationName == ""))) 9
    else if (!((organisationName != empty && nagPaoText != "" )
      || (organisationName != empty && nagSaoText != "" )
      || (organisationName != empty && nagOrganisationName != "" ))) 8
    else 6
  }

  /**
    * Match subbuildingname using PAF
    * @param subBuildingName
    * @param pafSubBuildingName
    * @return
    */
  def calculateSubBuildingNamePafScore (subBuildingName: String, pafSubBuildingName: String, organisationName: String) : Int = {
    val pafBuildingMatchScore = if (subBuildingName == empty) 4
    else matchNames(subBuildingName,pafSubBuildingName).min(matchNames(pafSubBuildingName,subBuildingName))
      if (subBuildingName == pafSubBuildingName || organisationName == pafSubBuildingName) 1
      else if (pafBuildingMatchScore < 2) 2
      else if ( pafBuildingMatchScore < 3) 3
      else if (subBuildingName == empty  && pafSubBuildingName == "" ) 9
      else if (!((subBuildingName != empty && pafSubBuildingName != "" ) )) 8
      else 6
  }

  /**
    * Match buildingName against saoText
    * @param subBuildingName
    * @param nagSaoText
    * @return
    */
  def calculateSubBuildingNameNagScore (subBuildingName: String, nagSaoText: String) : Int = {
    val nagBuildingMatchScore = if (subBuildingName == empty) 4 else
      matchNames(subBuildingName,nagSaoText).min(matchNames(nagSaoText,subBuildingName))
      if (subBuildingName == nagSaoText) 1
      else if (nagBuildingMatchScore < 2) 2
      else if (nagBuildingMatchScore < 3) 3
      else if (subBuildingName == empty && nagSaoText == "" ) 9
      else if (!((subBuildingName != empty && nagSaoText != "" ) )) 8
      else 6
  }

  /**
    * Match subbuilding number / suffix using PAF
    * @param subBuildingName
    * @param pafSubBuildingName
    * @param pafBuildingName
    * @param saoStartSuffix
    * @param saoEndSuffix
    * @param saoStartNumber
    * @param saoEndNumber
    * @param pafBuildingNumber
    * @return
    */
  def calculateSubBuildingNumberPafScore (
    subBuildingName: String,
    pafSubBuildingName: String,
    pafBuildingName: String,
    saoStartSuffix: String,
    saoEndSuffix: String,
    saoStartNumber: String,
    saoEndNumber: String,
    pafBuildingNumber: String) : Int = {

    val tokenBuildingLowNum = getRangeBottom(subBuildingName)
    val tokenBuildingHighNum = tokenBuildingLowNum.max(getRangeTop(subBuildingName))
    val pafBuildingLowNum = getRangeBottom(pafSubBuildingName)
    val pafBuildingHighNum = pafBuildingLowNum.max(getRangeTop(pafSubBuildingName))
    val pafInRange = (((pafBuildingLowNum >= tokenBuildingLowNum && pafBuildingHighNum <= tokenBuildingHighNum)
      && tokenBuildingLowNum > -1))
    val pafBuildingStartSuffix = getStartSuffix(pafBuildingName)
    val pafBuildingEndSuffix = getEndSuffix(pafBuildingName)
    val pafSuffixInRange = ((saoStartSuffix == pafBuildingStartSuffix && saoEndSuffix == pafBuildingEndSuffix)
      || (saoEndSuffix == empty && saoStartSuffix >= pafBuildingStartSuffix && saoStartSuffix <= pafBuildingEndSuffix)
      || (pafBuildingEndSuffix == empty && pafBuildingStartSuffix >= saoStartSuffix && pafBuildingStartSuffix <= saoEndSuffix ))

    if (pafSuffixInRange && (pafBuildingLowNum.toString() == saoStartNumber ||
      pafBuildingHighNum.toString() == saoEndNumber)) 1
    else if (pafSuffixInRange && pafInRange) 1
    else if (pafBuildingNumber == saoStartNumber ||
      pafBuildingLowNum.toString() == saoStartNumber ||
      pafBuildingHighNum.toString() == saoEndNumber) 6
    else if (!((tokenBuildingLowNum == -1 && saoStartNumber == empty ))) 8
    else 9
  }

  /**
    * Match subbuilding number / suffix
    * @param subBuildingName
    * @param nagSaoStartNumber
    * @param nagSaoEndNumber
    * @param nagSaoStartSuffix
    * @param nagSaoEndSuffix
    * @param saoStartSuffix
    * @param saoEndSuffix
    * @param saoStartNumber
    * @param saoEndNumber
    * @return
    */
  def calculateSubBuildingNumberNagScore (
    subBuildingName: String,
    nagSaoText: String,
    nagSaoStartNumber: String,
    nagSaoEndNumber: String,
    nagSaoStartSuffix: String,
    nagSaoEndSuffix: String,
    saoStartSuffix: String,
    saoEndSuffix: String,
    saoStartNumber: String,
    saoEndNumber: String) : Int = {

    val tokenBuildingLowNum = getRangeBottom(subBuildingName)
    val tokenBuildingHighNum = tokenBuildingLowNum.max(getRangeTop(subBuildingName))
    val numBuildingLowNum = Try(nagSaoStartNumber.toInt).getOrElse(-1)
    val numBuildingHighNum = numBuildingLowNum.max(Try(nagSaoEndNumber.toInt).getOrElse(-1))
    val saoBuildingLowNum = getRangeBottom(getNumberPartsFromName(nagSaoText))
    val saoBuildingHighNum = saoBuildingLowNum.max(getRangeTop(getNumberPartsFromName(nagSaoText)))
    val nagBuildingLowNum = if (numBuildingLowNum == -1) saoBuildingLowNum else numBuildingLowNum
    val nagBuildingHighNum = if (numBuildingHighNum == -1) saoBuildingHighNum else numBuildingHighNum
    val nagInRange = ((nagBuildingLowNum >= tokenBuildingLowNum && nagBuildingHighNum <= tokenBuildingHighNum)
      && nagBuildingLowNum > -1  && tokenBuildingLowNum > -1)
    val nagBuildingStartSuffix = if (nagSaoStartSuffix == "" ) empty else nagSaoStartSuffix
    val nagBuildingEndSuffix = if (nagSaoEndSuffix == "" ) empty else nagSaoEndSuffix
    val nagSuffixInRange = ((saoStartSuffix == nagBuildingStartSuffix && saoEndSuffix == nagBuildingEndSuffix)
      || (saoEndSuffix == empty && saoStartSuffix >=nagBuildingStartSuffix && saoStartSuffix <= nagBuildingEndSuffix)
      || (nagBuildingEndSuffix == empty && nagBuildingStartSuffix >= saoStartSuffix && nagBuildingStartSuffix <= saoEndSuffix ))
    if (nagSuffixInRange &&
        (saoStartNumber == nagSaoStartNumber || saoEndNumber == nagSaoEndNumber )) 1
    else if (nagInRange && nagSuffixInRange) 1
    else if (nagSaoStartNumber == saoStartNumber || saoEndNumber == nagSaoEndNumber) 6
    else if (!((tokenBuildingLowNum == -1 && saoStartNumber == empty ) || (nagBuildingLowNum == -1)))  8
    else 9
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
    val reg = """\d+""".r
    val numlist = reg.findAllIn(range).toList
    if (numlist.isEmpty) -1 else Try(numlist.min.toInt).getOrElse(-1)
  }

  /**
    * Try to get the highest number in a range
    * @param range
    * @return
    */
  def getRangeTop(range: String): Int = {
    val reg = """\d+""".r
    val numlist = reg.findAllIn(range).toList
    if (numlist.isEmpty) -1 else Try(numlist.max.toInt).getOrElse(-1)
  }

  /**
    * Try to get the first letter that follows a number
    * @param range
    * @return
    */
  def getStartSuffix(range: String): String = {
    val reg = """.*?\d+([A-Z])+.*?""".r
    range match {
      case reg(suffix) => suffix
      case _ => empty
    }
  }

  /**
    * Try to get the last letter that follows a number
    * @param range
    * @return
    */
  def getEndSuffix(range: String): String = {
    val reg = """.*?\d+[A-Z]?-\d+([A-Z]).*?""".r
    range match {
      case reg(suffix) => suffix
      case _ => empty
    }
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
    nameArray1.map {name1 =>
      val levenshteins = nameArray2.map {name2 => levenshtein(name1,name2)}
      levenshteins.min
    }.max
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
    nameArray1.map {name1 =>
      val levenshteins = nameArray2.map {name2 =>
       min(levenshtein(name1,name2),
         levenshtein(name1concat,name2),
         levenshtein(name1,name2concat),
         if (isRoadWord(name1) && isRoadWord(name2)) 2 else 4)
      }
      levenshteins.min
    }.max
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
    * @param localityParams
    * @return
    */
  def calculateAmbiguityPenalty(localityScoreDebug: String, localityParams: Seq[(String,String)]): Double = {
    val postcodeScore = Try(localityScoreDebug.substring(12,13).toInt).getOrElse(9)
    val sectors = localityParams.collect {case (locality, sector) if locality == localityScoreDebug => sector }
    val penalty = if (postcodeScore < 4) 1 else sectors.distinct.size
    penalty.toDouble
  }

  /**
    * Method 1 to separate the number and name parts e.g 6A HEDGEHOG HOUSE
    * Return just the number bit, discard the rest
    * @param name
    * @return String containing just e.g 6A
    * */
  def getNumberPartsFromName(name: String): String = {
    val parts = name.split(" ")
    val numberParts = for {part <- parts if containsNumber(part)} yield part
    numberParts.mkString(" ")
}
  /**
    * Method 2 to separate the number and name parts e.g 6A HEDGEHOG HOUSE
    * Remove the number part and return the rest
    * @param name
    * @return String containing just e.g HEDEGEHOG HOUSE
    * */
  def getNonNumberPartsFromName(name: String): String = {
    val parts = name.split(" ")
    val stringParts = for {part <- parts if !containsNumber(part)} yield part
    stringParts.mkString(" ")
  }

  /**
    * Test there is at least one number in a String
    * @param namepart
    * @return
    */
  def containsNumber(namepart: String): Boolean = {
    val numPattern = "[0-9]+".r
    !numPattern.findAllIn(namepart).toArray.isEmpty
  }

  /**
    * If token becomes empty treat it as missing by setting it to the atsign character
    * @param tokenString
    * @return
    */
  def atSignForEmpty (tokenString: String): String = {
    if  (tokenString == "") empty else tokenString
  }

}
