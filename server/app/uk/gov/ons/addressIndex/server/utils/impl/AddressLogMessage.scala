package uk.gov.ons.addressIndex.server.utils.impl

import uk.gov.ons.addressIndex.server.utils.APILogMessage

case class AddressLogMessage(
  ip: String = "",
  url: String = "",
  responseTimeMillis: String = "",
  uprn: String = "",
  postcode: String = "",
  partialAddress: String = "",
  input: String = "",
  offset: String = "",
  limit: String = "",
  filter: String = "",
  historical: Boolean = true,
  rangekm: String = "",
  lat: String = "",
  lon: String = "",
  bulkSize: String = "",
  batchSize: String = "",
  badRequestMessage: String = "",
  isNotFound: Boolean = false,
  formattedOutput: String = "",
  numOfResults: String = "",
  score: String = "",
  uuid: String = "",
  networkid: String = ""
) extends APILogMessage
