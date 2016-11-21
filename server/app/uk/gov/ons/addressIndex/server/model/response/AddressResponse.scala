package uk.gov.ons.addressIndex.server.model.response

import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.ons.addressIndex.model.db.index.PostcodeAddressFileAddress

/**
  * Contains implicit readers/writers to convert response models into json (play standard).
  * When needed, do following:
  * `import uk.gov.ons.addressIndex.server.model.response.implicits._`
  */
object Implicits {
  implicit val addressResponseErrorFormat = Json.format[AddressResponseError]
  implicit val addressResponseStatusFormat = Json.format[AddressResponseStatus]
  implicit val addressResponseGeoFormat = Json.format[AddressResponseGeo]
  implicit val addressResponseNagFormat = Json.format[AddressResponseNag]
  implicit val addressResponsePafFormat = Json.format[AddressResponsePaf]
  implicit val addressResponseAddressFormat = Json.format[AddressResponseAddress]
  implicit val addressTokensFormat = Json.format[AddressTokens]
  implicit val addressResponseFormat = Json.format[AddressResponse]
  implicit val addressBySearchResponseContainerFormat = Json.format[AddressBySearchResponseContainer]
  implicit val addressByUprnResponseContainerFormat = Json.format[AddressByUprnResponseContainer]

}

/**
  * Contains the reply for address by uprn request
  *
  * @param address found address
  * @param status  response status / message
  * @param errors  encountred errors (or an empty list if there is no errors)
  */
case class AddressByUprnResponseContainer(
  address: Option[AddressResponseAddress],
  status: AddressResponseStatus,
  errors: Seq[AddressResponseError]
)

/**
  * Contains the reply for the address search request
  *
  * @param response relevant data
  * @param status   status code / message
  * @param errors   encountred errors (or an empty list if there is no errors)
  */
case class AddressBySearchResponseContainer(
  response: AddressResponse,
  status: AddressResponseStatus,
  errors: Seq[AddressResponseError]
)

/**
  * Contains relevant, to the address request, data
  *
  * @param tokens    address decomposed into relevant parts (building number, city, street, etc.)
  * @param addresses found addresses
  * @param limit     max number of found addresses
  * @param offset    offset of found addresses (for pagination)
  * @param total     total number of found addresses
  */
case class AddressResponse(
  tokens: AddressTokens,
  addresses: Seq[AddressResponseAddress],
  limit: Int,
  offset: Int,
  total: Int
)

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

  // geo from NAG, null if address only in PAF (i.e. not found in NAG)
  geo: Option[AddressResponseGeo],

  underlyingScore: Double, // from Elastic
  underlyingMaxScore: Double // from Elastic (repeated for each address)

)

object AddressResponseAddress {
  /**
    * Transforms Paf address from elastic search into the Response address
    *
    * @param other
    * @return
    */
  def fromPafAddress(other: PostcodeAddressFileAddress): AddressResponseAddress =
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
    underlyingScore = 1,
    underlyingMaxScore = 1
  )
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

/**
  * Nag data on the address
  *
  * @param postcode postcode
  */
case class AddressResponseNag(
  postcode: String
)

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
  val ok = AddressResponseStatus(
    code = Status.OK,
    message = "Ok"
  )

  val notFound = AddressResponseStatus(
    code = Status.NOT_FOUND,
    message = "Not Found"
  )

  val badRequest = AddressResponseStatus(
    code = Status.BAD_REQUEST,
    message = "Bad request"
  )
}

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
  val emptyQuery = AddressResponseError(
    code = 1,
    message = "Empty query"
  )

  val addressFormatNotSupported = AddressResponseError(
    code = 2,
    message = "Address format is not supported"
  )

  val notFound = AddressResponseError(
    code = 3,
    message = "UPRN request didn't yield a result"
  )
}





