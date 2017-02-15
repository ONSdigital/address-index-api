package uk.gov.ons.addressIndex.model.db

import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.model.db.index.HybridAddress

/**
  * DTO to hold the information about found address while doing bulk requests
  * @param maxPossibleScore the max elastic score possible
  * @param inputAddress the original input address
  * @param id
  * @param tokens tokens that were used to do the bulk request
  * @param hybridAddress found address
  */
case class BulkAddress(
  maxPossibleScore: Float,
  id: String,
  inputAddress: String,
  tokens: Map[String, String],
  hybridAddress: HybridAddress,
  matchedFormattedAddress: String
)

/**
  * All the information needed to make a request in a bulk search will be stored here
  * @param id the id supplied in the file
  * @param inputAddress the input string that we will use for search
  * @param tokens tokenized input string
  */
case class BulkAddressRequestData(
  id: String,
  inputAddress: String,
  tokens: Seq[CrfTokenResult]
)

/**
  * Dto to hold the result of the internal fetch of the addresses in a bulk request
  * will be then transformed into a response model
  * @param successfulBulkAddresses successful results of address requests
  * @param failedRequests stream containing the data used to create failed requests
  */
case class BulkAddresses(
  successfulBulkAddresses: Seq[BulkAddress],
  failedRequests: Stream[BulkAddressRequestData]
)
