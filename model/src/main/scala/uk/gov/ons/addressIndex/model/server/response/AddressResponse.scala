package uk.gov.ons.addressIndex.model.server.response

import play.api.http.Status
import play.api.libs.json.{Format, Json}
import uk.gov.ons.addressIndex.model.db.index.{NationalAddressGazetteerAddress, PostcodeAddressFileAddress}

import scala.util.Try

/**
  * Contains the reply for address by uprn request
  *
  * @param response found content
  * @param status   response status / message
  * @param errors   encountered errors (or an empty list if there is no errors)
  */
case class AddressByUprnResponseContainer(
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
  * @param response relevant data
  * @param status   status code / message
  * @param errors   encountred errors (or an empty list if there is no errors)
  */
case class AddressBySearchResponseContainer(
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
  tokens: AddressTokens,
  addresses: Seq[AddressResponseAddress],
  limit: Int,
  offset: Int,
  total: Int
)

object AddressBySearchResponse {
  implicit lazy val addressBySearchResponseFormat: Format[AddressBySearchResponse] = Json.format[AddressBySearchResponse]
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
  * @param geo                optional, geo information
  * @param underlyingScore    score from elastic search
  * @param underlyingMaxScore maximum score in the elastic result
  */
case class AddressResponseAddress(
  uprn: String,
  formattedAddress: String,
  paf: Option[AddressResponsePaf],
  nag: Option[AddressResponseNag],
  geo: Option[AddressResponseGeo],
  underlyingScore: Float,
  underlyingMaxScore: Float
)

object AddressResponseAddress {
  implicit lazy val addressResponseAddressFormat: Format[AddressResponseAddress] = Json.format[AddressResponseAddress]

  /**
    * Transforms Paf address from elastic search into the Response address
    *
    * @param maxScore elastic's response maximum score
    * @param other
    * @return
    */
  def fromPafAddress(maxScore: Float)(other: PostcodeAddressFileAddress): AddressResponseAddress = {

    val poBoxNumber = if (other.poBoxNumber.isEmpty) "" else s"PO BOX ${other.poBoxNumber}"

    val formattedAddress = Seq(other.departmentName, other.organizationName, other.subBuildingName,
      other.buildingName, other.buildingNumber, poBoxNumber, other.dependentThoroughfare,
      other.thoroughfare, other.doubleDependentLocality, other.dependentLocality, other.postTown,
      other.postcode).filter(_.nonEmpty).mkString(" ")

    AddressResponseAddress(
      uprn = other.uprn,
      formattedAddress = formattedAddress,
      paf = Some(AddressResponsePaf(
        other.udprn,
        other.organizationName,
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
      )),
      nag = None,
      geo = None,
      underlyingScore = other.score,
      underlyingMaxScore = maxScore
    )
  }

  /**
    *
    * @param other address in elastic's response form
    * @return
    */
  def fromPafAddress(other: PostcodeAddressFileAddress): AddressResponseAddress = fromPafAddress(1.0f)(other)

  /**
    * Transforms Paf address from elastic search into the Response address
    *
    * @param maxScore elastic's response maximum score
    * @param other
    * @return
    */
  def fromNagAddress(maxScore: Float)(other: NationalAddressGazetteerAddress): AddressResponseAddress = {

    val geo: Try[AddressResponseGeo] = for {
      latitude <- Try(other.latitude.toDouble)
      longitude <- Try(other.longitude.toDouble)
      easting <- Try(other.easting.toInt)
      northing <- Try(other.northing.toInt)
    } yield AddressResponseGeo(latitude, longitude, easting, northing)

    val saoLeftRangeExists = other.saoStartNumber != "" || other.saoStartSuffix != ""
    val saoRightRangeExists = other.saoEndNumber != "" || other.saoEndSuffix != ""
    val saoHyphen = if (saoLeftRangeExists && saoRightRangeExists) "-" else ""
    val saoNumbers = Seq(other.saoStartNumber, other.saoStartSuffix, saoHyphen, other.saoEndNumber, other.saoEndSuffix).mkString("")
    val sao = if (other.saoText == other.organisation) saoNumbers else s"$saoNumbers ${other.saoText}"

    val paoLeftRangeExists = other.paoStartNumber != "" || other.paoStartSuffix != ""
    val paoRightRangeExists = other.paoEndNumber != "" || other.paoEndSuffix != ""
    val paoHyphen = if (paoLeftRangeExists && paoRightRangeExists) "-" else ""
    val paoNumbers = Seq(other.paoStartNumber, other.paoStartSuffix, paoHyphen, other.paoEndNumber, other.paoEndSuffix).mkString("")
    val pao = if (other.paoText == other.organisation) paoNumbers else s"${other.paoText} $paoNumbers"

    val formattedAddress = Seq(other.organisation, sao, pao, other.streetDescriptor, other.locality,
      other.townName, other.postcodeLocator).filter(_.nonEmpty).mkString(" ")

    AddressResponseAddress(
      uprn = other.uprn,
      formattedAddress = formattedAddress,
      paf = None,
      nag = Some(AddressResponseNag(
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
        other.logicalStatus,
        other.streetDescriptor,
        other.townName,
        other.locality,
        other.organisation,
        other.legalName,
        other.classificationCode
      )),
      geo = geo.toOption,
      underlyingScore = other.score,
      underlyingMaxScore = maxScore
    )
  }

  def fromNagAddress(other: NationalAddressGazetteerAddress): AddressResponseAddress = fromNagAddress(1.0f)(other)

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
}

/**
  * @param uprn uprn
  * @param postcodeLocator postcode
  * @param addressBasePostal
  * @param usrn ursn
  * @param lpiKey lpi key
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
  officialFlag:String,
  logicalStatus: String,
  streetDescriptor: String,
  townName: String,
  locality: String,
  organisation: String,
  legalName: String,
  classificationCode: String
)

object AddressResponseNag {
  implicit lazy val addressResponseNagFormat: Format[AddressResponseNag] = Json.format[AddressResponseNag]
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
}

/**
  * Contains response status
  *
  * @param code    http code
  * @param message response description
  */
case class AddressResponseStatus(
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

object NotFoundAddressResponseStatus extends AddressResponseStatus(
  code = Status.NOT_FOUND,
  message = "Not Found"
)

object BadRequestAddressResponseStatus extends AddressResponseStatus(
  code = Status.BAD_REQUEST,
  message = "Bad request"
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





