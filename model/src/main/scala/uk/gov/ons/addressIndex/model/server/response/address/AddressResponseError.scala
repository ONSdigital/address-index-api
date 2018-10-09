package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

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
  message = "Limit parameter is not numeric"
)

object OffsetNotNumericAddressResponseError extends AddressResponseError(
  code = 5,
  message = "Offset parameter is not numeric"
)

object LimitTooSmallAddressResponseError extends AddressResponseError(
  code = 6,
  message = "Limit parameter is too small, minimum = 1"
)

object OffsetTooSmallAddressResponseError extends AddressResponseError(
  code = 7,
  message = "Offset parameter is too small, minimum = 0"
)

object LimitTooLargeAddressResponseError extends AddressResponseError(
  code = 8,
  message = "Limit parameter is too large (maximum configurable)"
)

object OffsetTooLargeAddressResponseError extends AddressResponseError(
  code = 9,
  message = "Offset parameter is too large (maximum configurable)"
)

object FailedRequestToEsError extends AddressResponseError(
  code = 10,
  message = "Request to Elasticsearch failed (check the API logs)"
)

object ApiKeyMissingError extends AddressResponseError(
  code = 11,
  message = "The API key is missing"
)

object ApiKeyInvalidError extends AddressResponseError(
  code = 12,
  message = "An Invalid API key was provided"
)

object SourceMissingError extends AddressResponseError(
  code = 13,
  message = "Source key not provided (are you using the API Gateway?)"
)

object SourceInvalidError extends AddressResponseError(
  code = 14,
  message = "Invalid source key provided (are you using the API Gateway?)"
)

object FilterInvalidError extends AddressResponseError(
  code = 15,
  message = "Invalid filter value provided"
)

object OffsetNotNumericPostcodeAddressResponseError extends AddressResponseError(
  code = 16,
  message = "Offset parameter is not numeric (postcode)"
)

object LimitNotNumericPostcodeAddressResponseError extends AddressResponseError(
  code = 17,
  message = "Limit parameter is not numeric (postcode)"
)

object OffsetTooSmallPostcodeAddressResponseError extends AddressResponseError(
  code = 18,
  message = "Offset parameter is too small, minimum = 0 (postcode)"
)

object LimitTooSmallPostcodeAddressResponseError extends AddressResponseError(
  code = 19,
  message = "Limit parameter is too small, minimum = 1 (postcode)"
)

object LimitTooLargePostcodeAddressResponseError extends AddressResponseError(
  code = 20,
  message = "Limit parameter is too large (maximum configurable) (postcode)"
)

object OffsetTooLargePostcodeAddressResponseError extends AddressResponseError(
  code = 21,
  message = "Offset parameter is too large (maximum configurable) (postcode)"
)

object EmptyQueryPostcodeAddressResponseError extends AddressResponseError(
  code = 22,
  message = "Empty query (postcode)"
)

object FailedRequestToEsPostcodeError extends AddressResponseError(
  code = 23,
  message = "Request to Elasticsearch failed (postcode)(check the API logs)"
)

object NotFoundPostcodeResponseError extends AddressResponseError(
  code = 24,
  message = "Postcode request didn't yield a result"
)

object UprnNotNumericAddressResponseError extends AddressResponseError(
  code = 25,
  message = "UPRNs must be numeric"
)

object RangeNotNumericAddressResponseError extends AddressResponseError(
  code = 26,
  message = "Range KM parameter is not numeric"
)

object LatitudeNotNumericAddressResponseError extends AddressResponseError(
  code = 27,
  message = "Latitiude parameter is not numeric"
)

object LongitudeNotNumericAddressResponseError extends AddressResponseError(
  code = 28,
  message = "Longitude parameter is not numeric"
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
  message = "Matchthreshold parameter is not numeric"
)

object ThresholdNotInRangeAddressResponseError extends AddressResponseError(
  code = 34,
  message = "Matchthreshold parameter must be greater than 0 and less than or equal to 100"
)

object FilterInvalidPostcodeError extends AddressResponseError(
  code = 35,
  message = "Invalid filter value provided (postcode)"
)

object FailedRequestToEsPartialAddressError extends AddressResponseError(
  code = 36,
  message = "Request to Elasticsearch failed (partial address)(check the API logs)"
)

object StartDateInvalidResponseError extends AddressResponseError(
  code = 37,
  message = "Invalid start date"
)

object EndDateInvalidResponseError extends AddressResponseError(
  code = 38,
  message = "Invalid end date"
)

object ShortQueryAddressResponseError extends AddressResponseError(
  code = 39,
  message = "Partial address string too short, minimum 5 characters"
)
