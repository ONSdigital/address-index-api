package uk.gov.ons.addressIndex.model.db

import uk.gov.ons.addressIndex.model.db.index.HybridAddress
import uk.gov.ons.addressIndex.model.server.response.bulk.AddressBulkResponseAddress

/**
  * DTO to hold the information about found address while doing bulk requests
  *
  * @param inputAddress  the original input address
  * @param id
  * @param tokens        tokens that were used to do the bulk request
  * @param hybridAddress found address
  */
case class BulkAddress(id: String,
                       inputAddress: String,
                       tokens: Map[String, String],
                       hybridAddress: HybridAddress)

object BulkAddress {
  /**
    * If we didn't find any result for a particular input, we still need to
    * return something in the response to point out that there is no addresses
    * found
    *
    * @param requestData data that was used to search for addresses (albeit yielding an empty result)
    * @return BulkAddress indicating that there is no associated addresses found
    */
  def empty(requestData: BulkAddressRequestData) =
    BulkAddress(requestData.id, requestData.inputAddress, requestData.tokens, HybridAddress.empty)

  /**
    * Transforms HybridAddress (with request data) into a BulkAddress
    *
    * @param hybridAddress found HybridAddress
    * @param requestData   request data used to find this hybrid address
    * @return bulk address DTO
    */
  def fromHybridAddress(hybridAddress: HybridAddress, requestData: BulkAddressRequestData): BulkAddress =
    BulkAddress(requestData.id, requestData.inputAddress, requestData.tokens, hybridAddress)
}

/**
  * All the information needed to make a request in a bulk search will be stored here
  *
  * @param id                       the id supplied in the file
  * @param inputAddress             the input string that we will use for search
  * @param tokens                   map with label -> value of the tokenized input
  * @param lastFailExceptionMessage if the request has failed, this field will contain the message from
  *                                 the exception
  */
case class BulkAddressRequestData(id: String,
                                  inputAddress: String,
                                  tokens: Map[String, String],
                                  lastFailExceptionMessage: String = "")

/**
  * Dto to hold the result of the internal fetch of the addresses in a bulk request
  * will be then transformed into a response model
  *
  * @param successfulBulkAddresses successful results of address requests
  * @param failedRequests          stream containing the data used to create failed requests
  */
case class BulkAddresses(successfulBulkAddresses: Stream[Seq[AddressBulkResponseAddress]],
                         failedRequests: Stream[BulkAddressRequestData])

