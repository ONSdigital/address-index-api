package uk.gov.ons.addressIndex.server.model.response

import play.api.http.Status
import play.api.libs.json.{Json, OFormat}
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
  implicit val addressByUprnResponseContainerFormat: OFormat[AddressByUprnResponseContainer] =
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
  implicit lazy val addressByUprnResponseFormat: OFormat[AddressByUprnResponse] = Json.format[AddressByUprnResponse]
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
  implicit lazy val addressBySearchResponseContainerFormat: OFormat[AddressBySearchResponseContainer] =
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
  implicit lazy val addressBySearchResponseFormat: OFormat[AddressBySearchResponse] = Json.format[AddressBySearchResponse]
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
  implicit lazy val addressTokensFormat: OFormat[AddressTokens] = Json.format[AddressTokens]
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
  implicit lazy val addressResponseAddressFormat: OFormat[AddressResponseAddress] = Json.format[AddressResponseAddress]

  /**
    * Transforms Paf address from elastic search into the Response address
    *
    * @param maxScore elastic's response maximum score
    * @param other
    * @return
    */
  def fromPafAddress(maxScore: Float)(other: PostcodeAddressFileAddress): AddressResponseAddress =
    AddressResponseAddress(
      uprn = other.uprn,
      formattedAddress = "",
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
    } yield AddressResponseGeo(latitude, longitude, 0, 0)

    AddressResponseAddress(
      uprn = other.uprn,
      formattedAddress = "",
      paf = None,
      nag = Some(AddressResponseNag(
        other.uprn,
        other.postcodeLocator,
        other.addressBasePostal,
        other.ursn,
        other.lpiKey,
        other.paoText,
        other.paoStartNumber,
        other.paoStartSuffix,
        other.paoEndNumber,
        other.paoEndSuffix,
        other.saoText,
        other.saoStartNumber,
        other.saoStartSuffix,
        other.saoEndNumber,
        other.saoEndSuffix,
        other.level,
        other.logicalStatus,
        other.streetDescriptor,
        other.townName,
        other.locality,
        other.organisation,
        other.legalName
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
  implicit lazy val addressResponsePafFormat: OFormat[AddressResponsePaf] = Json.format[AddressResponsePaf]
}

/**
  * @param uprn uprn
  * @param postcodeLocator postcode
  * @param addressBasePostal
  * @param ursn ursn
  * @param lpiKey lpi key
  * @param paoText building name
  * @param paoStartNumber building number
  * @param paoStartSuffix
  * @param paoEndNumber
  * @param paoEndSuffix
  * @param saoText sub building name
  * @param saoStartNumber sub building number
  * @param saoStartSuffix
  * @param saoEndNumber
  * @param saoEndSuffix
  * @param level ground and first floor
  * // The following one is removed until further notice
  * @param officialFlag
  * @param logicalStatus
  * @param streetDescriptor
  * @param townName
  * @param locality
  * @param organisation
  * @param legalName
  */
case class AddressResponseNag(
  uprn: String,
  postcodeLocator: String,
  addressBasePostal: String,
  ursn: String,
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
  logicalStatus: String,
  streetDescriptor: String,
  townName: String,
  locality: String,
  organisation: String,
  legalName: String
)

object AddressResponseNag {
  implicit lazy val addressResponseNagFormat: OFormat[AddressResponseNag] = Json.format[AddressResponseNag]
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
  implicit lazy val addressResponseGeoFormat: OFormat[AddressResponseGeo] = Json.format[AddressResponseGeo]
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
  implicit lazy val addressResponseStatusFormat: OFormat[AddressResponseStatus] = Json.format[AddressResponseStatus]
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
  implicit lazy val addressResponseErrorFormat: OFormat[AddressResponseError] = Json.format[AddressResponseError]
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





