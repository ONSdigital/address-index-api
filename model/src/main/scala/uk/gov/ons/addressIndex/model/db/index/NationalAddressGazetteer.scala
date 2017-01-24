package uk.gov.ons.addressIndex.model.db.index

import uk.gov.ons.addressIndex.model.server.response._

case class NationalAddressGazetteer(
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
  locality: String
) extends Formattable {

  def formatAddress: String = {
    val saoLeftRangeExists = saoStartNumber.nonEmpty || saoStartSuffix.nonEmpty
    val saoRightRangeExists = saoEndNumber.nonEmpty || saoEndSuffix.nonEmpty
    val saoHyphen = if (saoLeftRangeExists && saoRightRangeExists) "-" else ""
    val saoNumbers = Seq(saoStartNumber, saoStartSuffix, saoHyphen, saoEndNumber, saoEndSuffix)
      .map(_.trim).mkString
    val sao = if (saoText == organisation) saoNumbers else s"$saoNumbers, $saoText"

    val paoLeftRangeExists = paoStartNumber.nonEmpty || paoStartSuffix.nonEmpty
    val paoRightRangeExists = paoEndNumber.nonEmpty || paoEndSuffix.nonEmpty
    val paoHyphen = if (paoLeftRangeExists && paoRightRangeExists) "-" else ""
    val paoNumbers = Seq(paoStartNumber, paoStartSuffix, paoHyphen, paoEndNumber, paoEndSuffix)
      .map(_.trim).mkString
    val pao = if (paoText == organisation) paoNumbers else s"$paoText, $paoNumbers"

    val trimmedStreetDescriptor = streetDescriptor.trim
    val buildingNumberWithStreetDescription = s"$pao $trimmedStreetDescriptor"

    delimitByComma(organisation, sao, buildingNumberWithStreetDescription, locality,
      townName, postcodeLocator)
  }

  def toNagWithFormat: NAGWithFormat = {
    NAGWithFormat(
      formattedAddress = formatAddress,
      nag = NAG(
        uprn = uprn,
        postcodeLocator = postcodeLocator,
        addressBasePostal = addressBasePostal,
        usrn = usrn,
        lpiKey = lpiKey,
        pao = PAO(
          text = paoText,
          startNumber = paoStartNumber,
          paoStartSuffix = paoStartSuffix,
          paoEndNumber = paoEndNumber,
          paoEndSuffix = paoEndSuffix
        ),
        sao = SAO(
          text = saoText,
          startNumber = saoStartNumber,
          startSuffix = saoStartSuffix,
          endNumber = saoEndNumber,
          endSuffix = saoEndSuffix
        ),
        geo = GEO(
          latitude = latitude.toDouble,
          longitude = longitude.toDouble,
          easting = easting.toDouble,
          northing = northing.toDouble
        ),
        level = level,
        officialFlag = officialFlag,
        logicalStatus = logicalStatus,
        streetDescriptor = streetDescriptor,
        townName = townName,
        locality = locality,
        organisation = organisation,
        legalName = legalName,
        classificationCode = classificationCode
      )
    )
  }
}
