package uk.gov.ons.addressIndex.server.utils

import play.api.Logger

object Splunk {

  private val logger = Logger("SPLUNK")

  def log(
    IP: String = "",
    url: String = "",
    responseTimeMillis: String = "",
    isUprn: Boolean = false,
    isPostcode: Boolean = false,
    isPartial: Boolean = false,
    isInput: Boolean = false,
    isBulk: Boolean = false,
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
    batchSize: String = "", // bulk is splitted into smaller batches
    badRequestMessage: String = "",
    isNotFound: Boolean = false,
    formattedOutput: String = "",
    numOfResults: String = "",
    score: String = "",
    uuid: String = "",
    networkid: String = ""
  ): Unit = {
    logger.info(
      s" IP=$IP url=$url millis=${System.currentTimeMillis()} response_time_millis=$responseTimeMillis is_uprn=$isUprn " +
        s"is_postcode=$isPostcode is_input=$isInput is_bulk=$isBulk is_partial=$isPartial " +
        s"uprn=$uprn postcode=$postcode input=$input offset=$offset limit=$limit filter=$filter partialAddress=$partialAddress " +
        s"historical=$historical" +
        s"rangekm=$rangekm lat=$lat lon=$lon " +
        s"bulk_size=$bulkSize batch_size=$batchSize " +
        s"bad_request_message=$badRequestMessage is_not_found=$isNotFound formattedOutput=${formattedOutput.replaceAll("""\s""", "_")} " +
        s"numOfResults=$numOfResults score=$score uuid=$uuid networkid=$networkid"
    )
  }

  // temporary fix - needs refacatoring
  def trace(
    IP: String = "",
    url: String = "",
    responseTimeMillis: String = "",
    isUprn: Boolean = false,
    isPostcode: Boolean = false,
    isPartial: Boolean = false,
    isInput: Boolean = false,
    isBulk: Boolean = false,
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
      s" IP=$IP url=$url millis=${System.currentTimeMillis()} response_time_millis=$responseTimeMillis " +
        s"is_uprn=$isUprn is_postcode=$isPostcode is_input=$isInput is_bulk=$isBulk is_partial=$isPartial " +
        s"uprn=$uprn postcode=$postcode input=$input offset=$offset limit=$limit filter=$filter partialAddress=$partialAddress " +
        s"historical=$historical" +
        s"rangekm=$rangekm lat=$lat lon=$lon " +
        s"bulk_size=$bulkSize batch_size=$batchSize " +
        s"bad_request_message=$badRequestMessage is_not_found=$isNotFound formattedOutput=${formattedOutput.replaceAll("""\s""", "_")} " +
        s"numOfResults=$numOfResults score=$score uuid=$uuid networkid=$networkid"
    )
  }

}
