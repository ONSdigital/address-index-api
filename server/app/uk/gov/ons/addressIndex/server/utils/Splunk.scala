package uk.gov.ons.addressIndex.server.utils

import play.api.Logger

object Splunk {

  private val logger = Logger("SPLUNK")

  def log(
    IP: String = "",
    url: String = "",
    responseTimeMillis: String = "",
    isUprn: Boolean = false,
    isInput: Boolean = false,
    isBulk: Boolean = false,
    uprn: String = "",
    input: String = "",
    offset: String = "",
    limit: String = "",
    bulkSize: String = "",
    batchSize: String = "", // bulk is splitted into smaller batches
    badRequestMessage: String = "",
    isNotFound: Boolean = false,
    formattedOutput: String = "",
    numOfResults: String = "",
    score: String = "",
    uuid: String = "",
    networkid: String = ""
  ): Unit = {
    logger.trace(
      s" IP=$IP url=$url millis=${System.currentTimeMillis()} response_time_millis=$responseTimeMillis is_uprn=$isUprn is_input=$isInput is_bulk=$isBulk " +
        s"uprn=$uprn input=$input offset=$offset limit=$limit bulk_size=$bulkSize batch_size=$batchSize " +
        s"bad_request_message=$badRequestMessage is_not_found=$isNotFound formattedOutput=${formattedOutput.replaceAll("""\s""", "_")} " +
        s"numOfResults=$numOfResults score=$score uuid=$uuid networkid=$networkid"
    )
  }

}
