package uk.gov.ons.addressIndex.model.db.index

/**
 * Data structure containing addresses with the maximum address
 * @param addresses fetched addresses
 * @param maxScore maximum score
 */
case class NationalAddressGazetteerAddresses(
  addresses: Seq[NationalAddressGazetteerAddress],
  maxScore: Float
)

/**
  * NAG Address DTO
  */
case class NationalAddressGazetteerAddress(
  uprn: String,
  postcodeLocator: String,
  addressBasePostal: String,
  latitude: String,
  longitude: String,
  easting: String,
  northing: String,
  organisation: String,
  legalName: String,
  classificationCode: String,
  usrn: String,
  lpiKey: String,
  paoText: String,
  paoStartNumber: String,
  paoStartSuffix: String,
  paoEndNumber: String,
  paoEndSuffix: String,
  saoText: String,
  saoStartNumber: String,
  saoStartSuffix: String,
  saoEndNumber: String,
  saoEndSuffix: String,
  level: String,
  officialFlag: String,
  logicalStatus: String,
  streetDescriptor: String,
  townName: String,
  locality: String,
  score: Float
) extends AddressFormattable {
  def generateFormattedAddress(nag: NationalAddressGazetteerAddress): String = {

    val saoLeftRangeExists = nag.saoStartNumber.nonEmpty || nag.saoStartSuffix.nonEmpty
    val saoRightRangeExists = nag.saoEndNumber.nonEmpty || nag.saoEndSuffix.nonEmpty
    val saoHyphen = if (saoLeftRangeExists && saoRightRangeExists) "-" else ""
    val saoNumbers = Seq(nag.saoStartNumber, nag.saoStartSuffix, saoHyphen, nag.saoEndNumber, nag.saoEndSuffix)
      .map(_.trim).mkString
    val sao = if (nag.saoText == nag.organisation) saoNumbers else s"$saoNumbers, ${nag.saoText}"

    val paoLeftRangeExists = nag.paoStartNumber.nonEmpty || nag.paoStartSuffix.nonEmpty
    val paoRightRangeExists = nag.paoEndNumber.nonEmpty || nag.paoEndSuffix.nonEmpty
    val paoHyphen = if (paoLeftRangeExists && paoRightRangeExists) "-" else ""
    val paoNumbers = Seq(nag.paoStartNumber, nag.paoStartSuffix, paoHyphen, nag.paoEndNumber, nag.paoEndSuffix)
      .map(_.trim).mkString
    val pao = if (nag.paoText == nag.organisation) paoNumbers else s"${nag.paoText}, $paoNumbers"

    val trimmedStreetDescriptor = nag.streetDescriptor.trim
    val buildingNumberWithStreetDescription = s"$pao $trimmedStreetDescriptor"

    delimitByComma(nag.organisation, sao, buildingNumberWithStreetDescription, nag.locality,
      nag.townName, nag.postcodeLocator)
  }
}
