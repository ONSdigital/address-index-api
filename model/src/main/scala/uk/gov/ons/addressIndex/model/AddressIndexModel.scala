package uk.gov.ons.addressIndex.model

import java.util.UUID

case class AddressIndexUPRNRequest(
  uprn: BigInt,
  id: UUID
)

case class AddressIndexSearchRequest(
  input: String,
  limit: String,
  offset: String,
  id: UUID
)
