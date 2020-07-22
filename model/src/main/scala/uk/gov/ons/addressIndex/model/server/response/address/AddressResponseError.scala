package uk.gov.ons.addressIndex.model.server.response.address

import play.api.libs.json.{Format, Json}

/**
  * Contains one response error
  *
  * @param code    error code
  * @param message error description
  */
case class AddressResponseError(code: Int, message: String)

object AddressResponseError {
  implicit lazy val addressResponseErrorFormat: Format[AddressResponseError] = Json.format[AddressResponseError]
}

object EmptyQueryAddressResponseError extends AddressResponseError(
  code = 1,
  message = "No address string supplied. The string is free format and supplied using the input= query string parameter. "
)

// not currently used - check
object FormatNotSupportedAddressResponseError extends AddressResponseError(
  code = 2,
  message = "Address format is not supported"
)

object NotFoundAddressResponseError extends AddressResponseError(
  code = 3,
  message = "UPRN request didn't yield a result. The UPRN may no longer exist on the current AddressBase, or only be available with historical=true query string parameter"
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
  message = "Limit parameter is too large, maximum = *"
)

object OffsetTooLargeAddressResponseError extends AddressResponseError(
  code = 9,
  message = "Offset parameter is too large, maximum = *"
)

object FailedRequestToEsError extends AddressResponseError(
  code = 10,
  message = "Request to ElasticSearch failed (see logs)"
)

object ApiKeyMissingError extends AddressResponseError(
  code = 11,
  message = "The API key is missing from the Authorization header."
)

object ApiKeyInvalidError extends AddressResponseError(
  code = 12,
  message = "An Invalid API key was provided. A key has been found but it does not match any on the list of currently active keys."
)

object SourceMissingError extends AddressResponseError(
  code = 13,
  message = "Source key not provided. The API is now only available via the Gateway URL, enforced by this key."
)

object SourceInvalidError extends AddressResponseError(
  code = 14,
  message = "Invalid source key provided. The API is now only available via the Gateway URL, enforced by this key."
)

object FilterInvalidError extends AddressResponseError(
  code = 15,
  message = "Invalid classification filter value provided. Filters must exactly match a classification code (see /classifications) or use a pattern match such as RD*. There are also three presets residential, commercial and workplace."
)

//  this error is currently logged only, not returned
object InvalidPostcodeAddressResponseError extends AddressResponseError(
  code = 16,
  message = "Postcode supplied is not valid according to the UK addresses pattern match."
)

// can't happen when postcode is part of URI - keep in case it moves to query string
object EmptyQueryPostcodeAddressResponseError extends AddressResponseError(
  code = 17,
  message = "No postcode supplied. The postcode is supplied as part of the URL body, space optional e.g. /addresses/postcode/PO155RR"
)

object FailedRequestToEsPostcodeError extends AddressResponseError(
  code = 18,
  message = "Request to ElasticSearch failed (postcode)(see logs)"
)

// This error is currently not used, a 200 with no addresses found is returned instead
object NotFoundPostcodeResponseError extends AddressResponseError(
  code = 19,
  message = "Postcode request didn't yield a result"
)

object UprnNotNumericAddressResponseError extends AddressResponseError(
  code = 20,
  message = "UPRNs must be numeric. They can have up to 12 digits and have no leading zeroes."
)

object RangeNotNumericAddressResponseError extends AddressResponseError(
  code = 21,
  message = "Range KM parameter is not numeric"
)

object LatitudeNotNumericAddressResponseError extends AddressResponseError(
  code = 22,
  message = "Latitude parameter is not numeric. The API expects decimal degrees."
)

object LongitudeNotNumericAddressResponseError extends AddressResponseError(
  code = 23,
  message = "Longitude parameter is not numeric. The API expects decimal degrees. "
)

object LatitudeTooFarNorthAddressResponseError extends AddressResponseError(
  code = 24,
  message = "Latitude parameter must be less than 60.9"
)

object LatitudeTooFarSouthAddressResponseError extends AddressResponseError(
  code = 25,
  message = "Latitude parameter must be greater than 49.8"
)

object LongitudeTooFarEastAddressResponseError extends AddressResponseError(
  code = 26,
  message = "Longitude parameter must be less than 1.8"
)

object LongitudeTooFarWestAddressResponseError extends AddressResponseError(
  code = 27,
  message = "Longitude parameter must be greater than -8.6"
)

object ThresholdNotNumericAddressResponseError extends AddressResponseError(
  code = 28,
  message = "MatchThreshold parameter is not numeric"
)

object ThresholdNotInRangeAddressResponseError extends AddressResponseError(
  code = 29,
  message = "MatchThreshold parameter must be greater than 0 and less than or equal to 100"
)

object FailedRequestToEsPartialAddressError extends AddressResponseError(
  code = 30,
  message = "Request to ElasticSearch failed (partial address)(see logs)"
)

object FromSourceInvalidError extends AddressResponseError(
  code = 31,
  message = "Query string parameter fromsource must be all, niboost, ewboost, nionly or ewonly"
)

object ShortQueryAddressResponseError extends AddressResponseError(
  code = 33,
  message = "Partial address string too short, minimum * characters"
)

object MixedFilterError extends AddressResponseError(
  code = 34,
  message = "Invalid classification filter value provided. Filters must contain one or more full classification codes (see /classifications) or use a single pattern match such as RD*. A mixture of exact match and pattern match is not valid."
)

object FailedRequestToEsRandomError extends AddressResponseError(
  code = 35,
  message = "Request to ElasticSearch failed (random)(see logs)"
)

object EpochNotAvailableError extends AddressResponseError(
  code = 36,
  message = "Requested Epoch is not available"
)

object FailedRequestToEsUprnError extends AddressResponseError(
  code = 37,
  message = "Request to ElasticSearch failed (uprn)(see logs)"
)

object EmptyRadiusQueryAddressResponseError extends AddressResponseError(
  code = 38,
  message = "Latitude, longitude, rangekm and filter must all be provided."
)

object InvalidAddressTypeAddressResponseError extends AddressResponseError(
  code = 39,
  message = "AddressType must be of type paf, welshpaf, nag, welshnag or nisra."
)

object InvalidEQBucketError extends AddressResponseError(
  code = 40,
  message = "At least one of postcode, streetname and townname must be supplied and not wildcarded (use all three for best results)"
)