package uk.gov.ons.addressIndex.model.server.response

import play.api.http.Status
import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.db.index._
import uk.gov.ons.addressIndex.model.db.BulkAddress

import scala.util.Try


/**
  * Contains the reply for address by uprn request
  *
  * @param apiVersion version of the API used for the response
  * @param dataVersion version of the address data used for the response
  * @param response found content
  * @param status   response status / message
  * @param errors   encountered errors (or an empty list if there is no errors)
  */
case class AddressByUprnResponseContainer(
  apiVersion: String,
  dataVersion: String,
  response: AddressByUprnResponse,
  status: AddressResponseStatus,
  errors: Seq[AddressResponseError] = Seq.empty[AddressResponseError]
)

object AddressByUprnResponseContainer {
  implicit val addressByUprnResponseContainerFormat: Format[AddressByUprnResponseContainer] =
    Json.format[AddressByUprnResponseContainer]
}

/**
  * Contains relevant information to the requested address
  *
  * @param address found address
  */
case class AddressByUprnResponse(
  address: Option[AddressResponseAddress]
)

object AddressByUprnResponse {
  implicit lazy val addressByUprnResponseFormat: Format[AddressByUprnResponse] = Json.format[AddressByUprnResponse]
}



/**
  * Contains the reply for the address search request
  *
  * @param apiVersion version of the API used for the response
  * @param dataVersion version of the address data used for the response
  * @param response relevant data
  * @param status   status code / message
  * @param errors   encountred errors (or an empty list if there is no errors)
  */
case class AddressByPostcodeResponseContainer(
                                             apiVersion: String,
                                             dataVersion: String,
                                             response: AddressByPostcodeResponse,
                                             status: AddressResponseStatus,
                                             errors: Seq[AddressResponseError] = Seq.empty[AddressResponseError]
                                           )

object AddressByPostcodeResponseContainer {
  implicit lazy val addressByPostcodeResponseContainerFormat: Format[AddressByPostcodeResponseContainer] =
    Json.format[AddressByPostcodeResponseContainer]
}

/**
  * Contains relevant, to the address request, data
  *
  * @param postcode    postcode from query
  * @param addresses found addresses
  * @param limit     max number of found addresses
  * @param offset    offset of found addresses (for pagination)
  * @param total     total number of found addresses
  */
case class AddressByPostcodeResponse(
                                    postcode: String,
                                    addresses: Seq[AddressResponseAddress],
                                    filter: String,
                                    historical: Boolean,
                                    limit: Int,
                                    offset: Int,
                                    total: Long,
                                    maxScore: Double
                                  )

object AddressByPostcodeResponse {
  implicit lazy val addressByPostcodeResponseFormat: Format[AddressByPostcodeResponse] = Json.format[AddressByPostcodeResponse]
}



/**
  * Contains the reply for the address search request
  *
  * @param apiVersion version of the API used for the response
  * @param dataVersion version of the address data used for the response
  * @param response relevant data
  * @param status   status code / message
  * @param errors   encountred errors (or an empty list if there is no errors)
  */
case class AddressBySearchResponseContainer(
  apiVersion: String,
  dataVersion: String,
  response: AddressBySearchResponse,
  status: AddressResponseStatus,
  errors: Seq[AddressResponseError] = Seq.empty[AddressResponseError]
)

object AddressBySearchResponseContainer {
  implicit lazy val addressBySearchResponseContainerFormat: Format[AddressBySearchResponseContainer] =
    Json.format[AddressBySearchResponseContainer]
}

/**
  * Contains relevant, to the address request, data
  *
  * @param tokens    address decomposed into relevant parts (building number, city, street, etc.)
  * @param addresses found addresses
  * @param limit     max number of found addresses
  * @param offset    offset of found addresses (for pagination)
  * @param total     total number of found addresses
  */
case class AddressBySearchResponse(
  tokens: Map[String, String],
  addresses: Seq[AddressResponseAddress],
  filter: String,
  historical: Boolean,
  rangekm: String,
  latitude: String,
  longitude: String,
  limit: Int,
  offset: Int,
  total: Long,
  sampleSize: Long,
  maxScore: Double,
  matchthreshold: Float
)

object AddressBySearchResponse {
  implicit lazy val addressBySearchResponseFormat: Format[AddressBySearchResponse] = Json.format[AddressBySearchResponse]
}

/**
  * Contains relevant information about the result of the bulk request
  *
  * @param apiVersion version of the API used for the response
  * @param dataVersion version of the address data used for the response
  * @param bulkAddresses found bulk addresses
  * @param status   status code / message
  * @param errors   encountred errors (or an empty list if there is no errors)
  */
case class AddressBulkResponseContainer(
  apiVersion: String,
  dataVersion: String,
  bulkAddresses: Seq[AddressBulkResponseAddress],
  status: AddressResponseStatus,
  errors: Seq[AddressResponseError] = Seq.empty[AddressResponseError]
)

object AddressBulkResponseContainer {
  implicit lazy val addressBulkResponseContainer: Format[AddressBulkResponseContainer] = Json.format[AddressBulkResponseContainer]
}

/**
  *
  * Container for relevant information on each of the address result in bulk search
  * @param id address's id provided in the input
  * @param inputAddress input address
  * @param uprn found address' uprn
  * @param matchedFormattedAddress formatted found address
  * @param matchedAddress found address
  * @param tokens tokens into which the input address was split
  * @param score resulting address score
  */
case class AddressBulkResponseAddress(
  id: String,
  inputAddress: String,
  uprn: String,
  matchedFormattedAddress: String,
  matchedAddress: Option[AddressResponseAddress],
  tokens: Map[String, String],
  confidenceScore: Double,
  score: Float,
  bespokeScore: Option[AddressResponseScore]
)

object AddressBulkResponseAddress {
  implicit lazy val addressBulkResponseAddressFormat: Format[AddressBulkResponseAddress] = Json.format[AddressBulkResponseAddress]

  def fromBulkAddress(
    bulkAddress: BulkAddress,
    addressResponseAddress: AddressResponseAddress,
    includeFullAddress: Boolean
  ): AddressBulkResponseAddress = AddressBulkResponseAddress(
    id = bulkAddress.id,
    inputAddress = bulkAddress.inputAddress,
    uprn = bulkAddress.hybridAddress.uprn,
    matchedFormattedAddress = addressResponseAddress.formattedAddressNag,
    matchedAddress = if (includeFullAddress) Some(addressResponseAddress) else None,
    tokens = bulkAddress.tokens,
    confidenceScore = addressResponseAddress.confidenceScore,
    score = bulkAddress.hybridAddress.score,
    bespokeScore = addressResponseAddress.bespokeScore
  )

}


/**
  * Contains tokens that build that the address can be splitted onto
  *
  * @param uprn           uprn
  * @param buildingNumber building number
  * @param postcode       postcode
  */
case class AddressTokens(
  uprn: String,
  buildingNumber: String,
  postcode: String
)

object AddressTokens {
  implicit lazy val addressTokensFormat: Format[AddressTokens] = Json.format[AddressTokens]
  /**
    * Empty tokens (when needed before address tokenization)
    */
  val empty = AddressTokens(
    uprn = "",
    buildingNumber = "",
    postcode = ""
  )
}

/**
  * Contains address information retrieved in ES (PAF or NAG)
  *
  * @param uprn               uprn
  * @param formattedAddress   cannonical address form
  * @param paf                optional, information from Paf index
  * @param nag                optional, information from Nag index
  * @param underlyingScore    score from elastic search
  * @param bespokeScore       custom scoring, optional so that it can be added during additional
  *                           step in the HopperScoreHelper
  */
case class AddressResponseAddress(
  uprn: String,
  parentUprn: String,
  relatives: Seq[AddressResponseRelative],
  crossRefs: Seq[AddressResponseCrossRef],
  formattedAddress: String,
  formattedAddressNag: String,
  formattedAddressPaf: String,
  welshFormattedAddressNag: String,
  welshFormattedAddressPaf: String,
  paf: Option[AddressResponsePaf],
  nag: Option[AddressResponseNag],
  geo: Option[AddressResponseGeo],
  confidenceScore: Double,
  underlyingScore: Float,
  bespokeScore: Option[AddressResponseScore]
)

object AddressResponseAddress {
  implicit lazy val addressResponseAddressFormat: Format[AddressResponseAddress] = Json.format[AddressResponseAddress]

  /**
    * Transforms hybrid object returned by ES into an Address that will be in the json response
    * @param other HybridAddress from ES
    * @return
    */
  def fromHybridAddress(other: HybridAddress): AddressResponseAddress = {

    val chosenNag: Option[NationalAddressGazetteerAddress] = chooseMostRecentNag(other.lpi, NationalAddressGazetteerAddress.Languages.english)
    val formattedAddressNag = chosenNag.map(AddressResponseNag.generateFormattedAddress).getOrElse("")

    val chosenWelshNag: Option[NationalAddressGazetteerAddress] = chooseMostRecentNag(other.lpi, NationalAddressGazetteerAddress.Languages.welsh)
    val welshFormattedAddressNag = chosenWelshNag.map(AddressResponseNag.generateFormattedAddress).getOrElse("")

    val chosenPaf: Option[PostcodeAddressFileAddress] =  other.paf.headOption
    val formattedAddressPaf = chosenPaf.map(AddressResponsePaf.generateFormattedAddress).getOrElse("")
    val welshFormattedAddressPaf = chosenPaf.map(AddressResponsePaf.generateWelshFormattedAddress).getOrElse("")

    AddressResponseAddress(
      uprn = other.uprn,
      parentUprn = other.parentUprn,
      relatives = other.relatives.map(AddressResponseRelative.fromRelative),
      crossRefs = other.crossRefs.map(AddressResponseCrossRef.fromCrossRef),
      formattedAddress = formattedAddressNag,
      formattedAddressNag = formattedAddressNag,
      formattedAddressPaf = formattedAddressPaf,
      welshFormattedAddressNag = welshFormattedAddressNag,
      welshFormattedAddressPaf = welshFormattedAddressPaf,
      paf = chosenPaf.map(AddressResponsePaf.fromPafAddress),
      nag = chosenNag.map(AddressResponseNag.fromNagAddress),
      geo = chosenNag.flatMap(AddressResponseGeo.fromNagAddress),
      confidenceScore = other.score,
      underlyingScore = other.score,
      bespokeScore = None
    )
  }

  /**
    * Gets the right (most often - the most recent) address from an array of NAG addresses
    * @param addresses list of Nag addresses
    * @return the NAG address that corresponds to the returned address
    */
  def chooseMostRecentNag(addresses: Seq[NationalAddressGazetteerAddress], language: String): Option[NationalAddressGazetteerAddress] ={
    // "if" is more readable than "getOrElse" in this case
    if (addresses.exists(address => address.lpiLogicalStatus == "1" && address.language == language ))
      addresses.find(_.lpiLogicalStatus == "1")
    else if (addresses.exists(address => address.lpiLogicalStatus == "6" && address.language == language))
      addresses.find(_.lpiLogicalStatus == "6")
    else if (addresses.exists(address => address.lpiLogicalStatus == "8" && address.language == language))
      addresses.find(_.lpiLogicalStatus == "8")
    else addresses.headOption
  }
}

/**
  * Wrapper response object for Relative (Relatives response comprises one Relative object per level)
  *
  * @param level                level number 1,2 etc. - 1 is top level
  * @param siblings             uprns of addresses at the current level
  * @param parents              uprns of addresses at the level above
  *
  */
case class AddressResponseRelative(
  level: Int,
  siblings: Seq[Long],
  parents: Seq[Long]
)

/**
  * Compainion object providing Lazy Json formatting
  */
object AddressResponseRelative {
  implicit lazy val relativeFormat: Format[AddressResponseRelative] = Json.format[AddressResponseRelative]

  def fromRelative(relative: Relative): AddressResponseRelative =
    AddressResponseRelative(relative.level, relative.siblings, relative.parents)
}



case class AddressResponseCrossRef(crossReference: String, source: String)

/**
  * Companion object providing Lazy Json formatting
  */
object AddressResponseCrossRef {
  implicit lazy val crossRefFormat: Format[AddressResponseCrossRef] = Json.format[AddressResponseCrossRef]

  def fromCrossRef(crossRef: CrossRef): AddressResponseCrossRef =
    AddressResponseCrossRef(crossRef.crossReference, crossRef.source)
}





/**
  * Paf data on the address
  *
  * @param udprn                        udprn
  * @param organisationName             organisation name
  * @param departmentName               department name
  * @param subBuildingName              sub building name
  * @param buildingName                 building name
  * @param buildingNumber               building number
  * @param dependentThoroughfare        dependent thoroughfare
  * @param thoroughfare                 thoroughfare
  * @param doubleDependentLocality      double dependent locality
  * @param dependentLocality            dependent locality
  * @param postTown                     post town
  * @param postcode                     postcode
  * @param postcodeType                 postcode type
  * @param deliveryPointSuffix          delivery point suffix
  * @param welshDependentThoroughfare   welsh dependent thoroughfare
  * @param welshThoroughfare            welsh thoroughfare
  * @param welshDoubleDependentLocality welsh double dependent locality
  * @param welshDependentLocality       welsh dependent locality
  * @param welshPostTown                welsh post town
  * @param poBoxNumber                  po box number
  * @param startDate                    start date
  * @param endDate                      end date
  */
case class AddressResponsePaf(
  udprn: String,
  organisationName: String,
  departmentName: String,
  subBuildingName: String,
  buildingName: String,
  buildingNumber: String,
  dependentThoroughfare: String,
  thoroughfare: String,
  doubleDependentLocality: String,
  dependentLocality: String,
  postTown: String,
  postcode: String,
  postcodeType: String,
  deliveryPointSuffix: String,
  welshDependentThoroughfare: String,
  welshThoroughfare: String,
  welshDoubleDependentLocality: String,
  welshDependentLocality: String,
  welshPostTown: String,
  poBoxNumber: String,
  startDate: String,
  endDate: String
)

object AddressResponsePaf {
  implicit lazy val addressResponsePafFormat: Format[AddressResponsePaf] = Json.format[AddressResponsePaf]

  def fromPafAddress(other: PostcodeAddressFileAddress): AddressResponsePaf =
    AddressResponsePaf(
      other.udprn,
      other.organisationName,
      other.departmentName,
      other.subBuildingName,
      other.buildingName,
      other.buildingNumber,
      other.dependentThoroughfare,
      other.thoroughfare,
      other.doubleDependentLocality,
      other.dependentLocality,
      other.postTown,
      other.postcode,
      other.postcodeType,
      other.deliveryPointSuffix,
      other.welshDependentThoroughfare,
      other.welshThoroughfare,
      other.welshDoubleDependentLocality,
      other.welshDependentLocality,
      other.welshPostTown,
      other.poBoxNumber,
      other.startDate,
      other.endDate
    )

  /**
    * Creates formatted address from PAF address
    * @param paf PAF address
    * @return String of formatted address
    */
  def generateFormattedAddress(paf: PostcodeAddressFileAddress): String = {

    val poBoxNumber = if (paf.poBoxNumber.isEmpty) "" else s"PO BOX ${paf.poBoxNumber}"

    val trimmedBuildingNumber = paf.buildingNumber.trim
    val trimmedDependentThoroughfare = paf.dependentThoroughfare.trim
    val trimmedThoroughfare = paf.thoroughfare.trim

    val buildingNumberWithStreetName =
      s"$trimmedBuildingNumber ${ if(trimmedDependentThoroughfare.nonEmpty) s"$trimmedDependentThoroughfare, " else "" }$trimmedThoroughfare"

    Seq(paf.departmentName, paf.organisationName, paf.subBuildingName, paf.buildingName,
      poBoxNumber, buildingNumberWithStreetName, paf.doubleDependentLocality, paf.dependentLocality,
      paf.postTown, paf.postcode).map(_.trim).filter(_.nonEmpty).mkString(", ")
  }

  /**
    * Creates Welsh formatted address from PAF address
    * @param paf PAF address
    * @return String of Welsh formatted address
    */
  def generateWelshFormattedAddress(paf: PostcodeAddressFileAddress): String = {

    val poBoxNumber = if (paf.poBoxNumber.isEmpty) "" else s"PO BOX ${paf.poBoxNumber}"

    val trimmedBuildingNumber = paf.buildingNumber.trim
    val trimmedDependentThoroughfare = paf.welshDependentThoroughfare.trim
    val trimmedThoroughfare = paf.welshThoroughfare.trim

    val buildingNumberWithStreetName =
      s"$trimmedBuildingNumber ${ if(trimmedDependentThoroughfare.nonEmpty) s"$trimmedDependentThoroughfare, " else "" }$trimmedThoroughfare"

    Seq(paf.departmentName, paf.organisationName, paf.subBuildingName, paf.buildingName,
      poBoxNumber, buildingNumberWithStreetName, paf.welshDoubleDependentLocality, paf.welshDependentLocality,
      paf.welshPostTown, paf.postcode).map(_.trim).filter(_.nonEmpty).mkString(", ")
  }

}

/**
  * @param uprn uprn
  * @param postcodeLocator postcode
  * @param addressBasePostal
  * @param usrn ursn
  * @param lpiKey lpi key
  * @param pao
  * @param sao
  * @param level ground and first floor
  * @param officialFlag
  * @param logicalStatus
  * @param streetDescriptor
  * @param townName
  * @param locality
  * @param organisation
  * @param legalName
  * @param classificationCode
  */
case class AddressResponseNag(
  uprn: String,
  postcodeLocator: String,
  addressBasePostal: String,
  usrn: String,
  lpiKey: String,
  pao: AddressResponsePao,
  sao: AddressResponseSao,
  level: String,
  officialFlag: String,
  logicalStatus: String,
  streetDescriptor: String,
  townName: String,
  locality: String,
  organisation: String,
  legalName: String,
  classificationCode: String,
  localCustodianCode: String,
  localCustodianName: String,
  localCustodianGeogCode: String,
  lpiEndDate: String
)

object AddressResponseNag {
  implicit lazy val addressResponseNagFormat: Format[AddressResponseNag] = Json.format[AddressResponseNag]

  def fromNagAddress(other: NationalAddressGazetteerAddress): AddressResponseNag = {
    AddressResponseNag(
        other.uprn,
        other.postcodeLocator,
        other.addressBasePostal,
        other.usrn,
        other.lpiKey,
        pao = AddressResponsePao(
          other.paoText,
          other.paoStartNumber,
          other.paoStartSuffix,
          other.paoEndNumber,
          other.paoEndSuffix
        ),
        sao = AddressResponseSao(
          other.saoText,
          other.saoStartNumber,
          other.saoStartSuffix,
          other.saoEndNumber,
          other.saoEndSuffix
        ),
        other.level,
        other.officialFlag,
        other.lpiLogicalStatus,
        other.streetDescriptor,
        other.townName,
        other.locality,
        other.organisation,
        other.legalName,
        other.classificationCode,
        other.localCustodianCode,
        other.localCustodianName,
        other.localCustodianGeogCode,
        other.lpiEndDate
      )
  }

  /**
    * Formatted address should contain commas between all fields except after digits
    * The actual logic is pretty complex and should be treated on example-to-example level
    * (with unit tests)
    * @param nag NAG address
    * @return String of formatted address
    */
  def generateFormattedAddress(nag: NationalAddressGazetteerAddress): String = {

    val saoLeftRangeExists = nag.saoStartNumber.nonEmpty || nag.saoStartSuffix.nonEmpty
    val saoRightRangeExists = nag.saoEndNumber.nonEmpty || nag.saoEndSuffix.nonEmpty
    val saoHyphen = if (saoLeftRangeExists && saoRightRangeExists) "-" else ""
    val saoNumbers = Seq(nag.saoStartNumber, nag.saoStartSuffix, saoHyphen, nag.saoEndNumber, nag.saoEndSuffix)
      .map(_.trim).mkString
    val sao =
      if (nag.saoText == nag.organisation || nag.saoText.isEmpty) saoNumbers
      else if (saoNumbers.isEmpty) s"${nag.saoText},"
      else s"$saoNumbers, ${nag.saoText},"

    val paoLeftRangeExists = nag.paoStartNumber.nonEmpty || nag.paoStartSuffix.nonEmpty
    val paoRightRangeExists = nag.paoEndNumber.nonEmpty || nag.paoEndSuffix.nonEmpty
    val paoHyphen = if (paoLeftRangeExists && paoRightRangeExists) "-" else ""
    val paoNumbers = Seq(nag.paoStartNumber, nag.paoStartSuffix, paoHyphen, nag.paoEndNumber, nag.paoEndSuffix)
      .map(_.trim).mkString
    val pao =
      if (nag.paoText == nag.organisation || nag.paoText.isEmpty) paoNumbers
      else if (paoNumbers.isEmpty) s"${nag.paoText},"
      else s"${nag.paoText}, $paoNumbers"

    val trimmedStreetDescriptor = nag.streetDescriptor.trim
    val buildingNumberWithStreetDescription =
      if (pao.isEmpty) s"$sao $trimmedStreetDescriptor"
      else if (sao.isEmpty) s"$pao $trimmedStreetDescriptor"
      else if (pao.isEmpty && sao.isEmpty) trimmedStreetDescriptor
      else s"$sao $pao $trimmedStreetDescriptor"

    Seq(nag.organisation, buildingNumberWithStreetDescription, nag.locality,
    nag.townName, nag.postcodeLocator).map(_.trim).filter(_.nonEmpty).mkString(", ")
  }

}

/**
  *
  * @param paoText building name
  * @param paoStartNumber building number
  * @param paoStartSuffix
  * @param paoEndNumber
  * @param paoEndSuffix
  */
case class AddressResponsePao(
  paoText: String,
  paoStartNumber: String,
  paoStartSuffix: String,
  paoEndNumber: String,
  paoEndSuffix: String
)

object AddressResponsePao {
  implicit lazy val addressResponsePaoFormat: Format[AddressResponsePao] = Json.format[AddressResponsePao]
}


/**
  *
  * @param saoText sub building name
  * @param saoStartNumber sub building number
  * @param saoStartSuffix
  * @param saoEndNumber
  * @param saoEndSuffix
  */
case class AddressResponseSao(
  saoText: String,
  saoStartNumber: String,
  saoStartSuffix: String,
  saoEndNumber: String,
  saoEndSuffix: String
)

object AddressResponseSao {
  implicit lazy val addressResponseSaoFormat: Format[AddressResponseSao] = Json.format[AddressResponseSao]
}

/**
  * Contains address geo position
  *
  * @param latitude  latitude
  * @param longitude longitude
  * @param easting   easting
  * @param northing  northing
  */
case class AddressResponseGeo(
  latitude: Double,
  longitude: Double,
  easting: Int,
  northing: Int
)

object AddressResponseGeo {
  implicit lazy val addressResponseGeoFormat: Format[AddressResponseGeo] = Json.format[AddressResponseGeo]

  /**
    * Creates GEO information from NAG elastic search object
    * @param other NAG elastic search
    * @return
    */
  def fromNagAddress(other: NationalAddressGazetteerAddress): Option[AddressResponseGeo] = (for {
      latitude <- Try(other.latitude.toDouble)
      longitude <- Try(other.longitude.toDouble)
      easting <- Try(other.easting.split("\\.").head.toInt)
      northing <- Try(other.northing.split("\\.").head.toInt)
    } yield AddressResponseGeo(latitude, longitude, easting, northing)).toOption

}

/**
  * Hopper Score - this class contains debug fields that may not be in final product
  * @param objectScore
  * @param structuralScore
  * @param buildingScore
  * @param localityScore
  * @param unitScore
  * @param buildingScoreDebug
  * @param localityScoreDebug
  * @param unitScoreDebug
  */
case class AddressResponseScore (
  objectScore: Double,
  structuralScore: Double,
  buildingScore: Double,
  localityScore: Double,
  unitScore: Double,
  buildingScoreDebug: String,
  localityScoreDebug: String,
  unitScoreDebug: String,
  ambiguityPenalty: Double
)

object AddressResponseScore {
  implicit lazy val addressResponseScoreFormat: Format[AddressResponseScore] = Json.format[AddressResponseScore]
}
/**
  * Contains response status
  *
  * @param code    http code
  * @param message response description
  */
case class AddressResponseStatus (
  code: Int,
  message: String
)

object AddressResponseStatus {
  implicit lazy val addressResponseStatusFormat: Format[AddressResponseStatus] = Json.format[AddressResponseStatus]
}

object OkAddressResponseStatus extends AddressResponseStatus(
  code = Status.OK,
  message = "Ok"
)

/**
  * Container for version info
  * @param apiVersion
  * @param dataVersion
  */
case class AddressResponseVersion(
  apiVersion: String,
  dataVersion: String
)

object AddressResponseVersion {
  implicit lazy val addressResponseVersionFormat: Format[AddressResponseVersion] = Json.format[AddressResponseVersion]
}

object NotFoundAddressResponseStatus extends AddressResponseStatus(
  code = Status.NOT_FOUND,
  message = "Not Found"
)

object BadRequestAddressResponseStatus extends AddressResponseStatus(
  code = Status.BAD_REQUEST,
  message = "Bad request"
)

object UnauthorizedRequestAddressResponseStatus extends AddressResponseStatus(
  code = Status.UNAUTHORIZED,
  message = "Unauthorized"
)

object InternalServerErrorAddressResponseStatus extends AddressResponseStatus(
  code = Status.INTERNAL_SERVER_ERROR,
  message = "Internal server error"
)


/**
  * Contains one response error
  *
  * @param code    error code
  * @param message error description
  */
case class AddressResponseError(
  code: Int,
  message: String
)

object AddressResponseError {
  implicit lazy val addressResponseErrorFormat: Format[AddressResponseError] = Json.format[AddressResponseError]
}

object EmptyQueryAddressResponseError extends AddressResponseError(
  code = 1,
  message = "Empty query"
)

object FormatNotSupportedAddressResponseError extends AddressResponseError(
  code = 2,
  message = "Address format is not supported"
)

object NotFoundAddressResponseError extends AddressResponseError(
  code = 3,
  message = "UPRN request didn't yield a result"
)

object LimitNotNumericAddressResponseError extends AddressResponseError(
  code = 4,
  message = "Limit parameter not numeric"
)

object OffsetNotNumericAddressResponseError extends AddressResponseError(
  code = 5,
  message = "Offset parameter not numeric"
)

object LimitTooSmallAddressResponseError extends AddressResponseError(
  code = 6,
  message = "Limit parameter too small, minimum = 1"
)

object OffsetTooSmallAddressResponseError extends AddressResponseError(
  code = 7,
  message = "Offset parameter too small, minimum = 0"
)

object LimitTooLargeAddressResponseError extends AddressResponseError(
  code = 8,
  message = "Limit parameter too large (maximum configurable)"
)

object OffsetTooLargeAddressResponseError extends AddressResponseError(
  code = 9,
  message = "Offset parameter too large (maximum configurable)"
)

object FailedRequestToEsError extends AddressResponseError(
  code = 10,
  message = "Failed request to the Elastic Search (check api logs)"
)

object ApiKeyMissingError extends AddressResponseError(
  code = 11,
  message = "Api key not supplied"
)

object ApiKeyInvalidError extends AddressResponseError(
  code = 12,
  message = "Invalid Api key supplied"
)

object SourceMissingError extends AddressResponseError(
  code = 13,
  message = "Source key not supplied (check that using Gateway)"
)

object SourceInvalidError extends AddressResponseError(
  code = 14,
  message = "Invalid source key supplied (check that using Gateway)"
)

object FilterInvalidError extends AddressResponseError(
  code = 15,
  message = "Invalid filter value supplied"
)

object OffsetNotNumericPostcodeAddressResponseError extends AddressResponseError(
  code = 16,
  message = "Offset parameter not numeric (postcode)"
)

object LimitNotNumericPostcodeAddressResponseError extends AddressResponseError(
  code = 17,
  message = "Limit parameter not numeric (postcode)"
)

object OffsetTooSmallPostcodeAddressResponseError extends AddressResponseError(
  code = 18,
  message = "Offset parameter too small, minimum = 0 (postcode)"
)

object LimitTooSmallPostcodeAddressResponseError extends AddressResponseError(
  code = 19,
  message = "Limit parameter too small, minimum = 1 (postcode)"
)

object LimitTooLargePostcodeAddressResponseError extends AddressResponseError(
  code = 20,
  message = "Limit parameter too large (maximum configurable) (postcode)"
)

object OffsetTooLargePostcodeAddressResponseError extends AddressResponseError(
  code = 21,
  message = "Offset parameter too large (maximum configurable) (postcode)"
)

object EmptyQueryPostcodeAddressResponseError extends AddressResponseError(
  code = 22,
  message = "Empty query (postcode)"
)

object FailedRequestToEsPostcodeError extends AddressResponseError(
  code = 23,
  message = "Failed request to the Elastic Search (postcode)(check api logs)"
)

object NotFoundPostcodeResponseError extends AddressResponseError(
  code = 24,
  message = "Postcode request didn't yield a result"
)

object UprnNotNumericAddressResponseError extends AddressResponseError(
  code = 25,
  message = "UPRNs nust be numeric"
)

object RangeNotNumericAddressResponseError extends AddressResponseError(
  code = 26,
  message = "Range km parameter not numeric"
)

object LatitudeNotNumericAddressResponseError extends AddressResponseError(
  code = 27,
  message = "Latitiude parameter not numeric"
)

object LongitudeNotNumericAddressResponseError extends AddressResponseError(
  code = 28,
  message = "Longitude parameter not numeric"
)

object LatitudeTooFarNorthAddressResponseError extends AddressResponseError(
  code = 29,
  message = "Latitiude parameter must be less than 60.9"
)

object LatitudeTooFarSouthAddressResponseError extends AddressResponseError(
  code = 30,
  message = "Latitude parameter must be greater than 49.8"
)

object LongitudeTooFarEastAddressResponseError extends AddressResponseError(
  code = 31,
  message = "Latitiude parameter must be less than 1.8"
)

object LongitudeTooFarWestAddressResponseError extends AddressResponseError(
  code = 32,
  message = "Longitude parameter must be greater than -8.6"
)

object ThresholdNotNumericAddressResponseError extends AddressResponseError(
  code = 33,
  message = "Matchthreshold parameter not numeric"
)

object ThresholdNotInRangeAddressResponseError extends AddressResponseError(
  code = 34,
  message = "Matchthreshold parameter must be greater than 0 and less than or equal to 100"
)

object FilterInvalidPostcodeError extends AddressResponseError(
  code = 35,
  message = "Invalid filter value supplied (postcode)"
)