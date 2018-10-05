package uk.gov.ons.addressIndex.server.utils

import play.api.Logger

class AddressAPILogger(log: String) extends APILogger {

  override def logName: String = log

  // Splunk is the current system logger
  private val splunk = Logger("SPLUNK")

  def systemLog(ip: String = "", url: String = "", responseTimeMillis: String = "",
    uprn: String = "", postcode: String = "", partialAddress: String = "", input: String = "",
    offset: String = "", limit: String = "", filter: String = "", verbose: Boolean = false,
    startDate: String = "", endDate: String = "", historical: Boolean = true,
    rangekm: String = "", lat: String = "", lon: String = "", bulkSize: String = "",
    batchSize: String = "", badRequestMessage: String = "", isNotFound: Boolean = false,
    formattedOutput: String = "", numOfResults: String = "", score: String = "",
                endpoint: String = "", activity: String = "", uuid: String = "",
                networkid: String = "", organisation: String = "", clusterid: String = ""): Unit = {

    // Note we are using the info level for Splunk

      super.logMessage(splunk.info, AddressLoggerMessage(
        s" IP=$ip url=$url millis=${System.currentTimeMillis()} " +
          s"response_time_millis=$responseTimeMillis is_uprn=${!uprn.isEmpty} " +
          s"is_postcode=${!postcode.isEmpty} is_input=${!input.isEmpty} " +
          s"is_bulk=${!bulkSize.isEmpty} is_partial=${!partialAddress.isEmpty} " +
          s"uprn=$uprn postcode=$postcode input=$input " +
          s"offset=$offset limit=$limit filter=$filter " + s"verbose=$verbose " +
          s"partialAddress=$partialAddress " + s"historical=$historical " +
          s"rangekm=$rangekm lat=$lat lon=$lon " +
          s"bulk_size=$bulkSize batch_size=$batchSize " +
          s"bad_request_message=$badRequestMessage is_not_found=$isNotFound " +
          s"formattedOutput=${formattedOutput.replaceAll("""\s""", "_")} " +
          s"numOfResults=$numOfResults score=$score endpoint=$endpoint " +
          s"activity=$activity uuid=$uuid networkid=$networkid organisation=$organisation " +
          s"clusterid=$clusterid "
      )
    )
  }

}

object AddressAPILogger {

  def apply(name: String): AddressAPILogger = {
    new AddressAPILogger(name)
  }

}
