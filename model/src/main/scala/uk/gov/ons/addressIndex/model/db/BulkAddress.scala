package uk.gov.ons.addressIndex.model.db

import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.db.index.HybridAddress

/**
  * DTO to hold the information about found address while doing bulk requests
  * @param tokens tokens that were used to do the bulk request
  * @param hybridAddress found address
  */
case class BulkAddress(
  tokens: Map[String, String],
  hybridAddress: HybridAddress
)

/**
  * If a bulk request didn't pass (failed `Future`), it will be stored in this DTO
  * @param tokens tokens that were used for the request
  * @param exception exeption that lead to the failure of the `Future`
  */
case class RejectedRequest(tokens: Seq[CrfTokenResult], exception: Throwable)

/**
  * Dto to hold the result of the internal fetch of the addresses in a bulk request
  * (will be then transformed into a response model
  * @param successfulBulkAddresses successful results of address requests
  * @param failedBulkAddresses failed results of address requests
  */
case class BulkAddresses(
  successfulBulkAddresses: Seq[BulkAddress],
  failedBulkAddresses: Seq[RejectedRequest]
)
