package uk.gov.ons.addressIndex.server.utils

import play.api.Logger

object Splunk {

  private val logger = Logger("SPLUNK")

  def log(
    IP: String = "",
    url: String = "",
    responseTimeMillis: Long = 0,
    isUprn: Boolean = false,
    isInput: Boolean = false,
    isBulk: Boolean = false,
    addressUprn: String = "",
    addressInput: String = "",
    addressOffset: String = "",
    addressLimit: String = "",
    bulkSize: Int = 0,
    batchSize: Int = 0,
    badRequestMessage: String = "",
    isNotFound: Boolean = false
  ): Unit = {
    logger.info(
      s" IP=$IP url=$url millis=${System.currentTimeMillis()} response_time_millis=$responseTimeMillis is_uprn=$isUprn is_input=$isInput is_bulk=$isBulk " +
        s"uprn=$addressUprn input=$addressInput offset=$addressOffset limit=$addressLimit bulk_size=$bulkSize batch_size=$batchSize " +
        s"bad_request_message=$badRequestMessage is_not_found=$isNotFound"
    )
  }

}
