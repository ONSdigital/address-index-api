package uk.gov.ons.addressIndex.server.utils.impl

import play.api.Logger
import uk.gov.ons.addressIndex.server.utils.APILogging

/**
  * This is a temporary implementation until we move to zipkin integration
  * using AspectJ AOP
  */
object AddressLogging extends APILogging[AddressLogMessage] {

  private val logger = Logger("SPLUNK")

  override def log(message: AddressLogMessage): Unit = logMessage(logger.info, message)
  override def trace(message: AddressLogMessage): Unit = logMessage(logger.trace, message)
  override def debug(message: AddressLogMessage): Unit = logMessage(logger.debug, message)

  private def logMessage: ((=> String) => Unit, AddressLogMessage) => Unit = (logType, message) => {

    logType(s" IP=$message.IP url=$message.url millis=${System.currentTimeMillis()} " +
      s"response_time_millis=$message.responseTimeMillis is_uprn=${!message.uprn.isEmpty} " +
      s"is_postcode=${!message.postcode.isEmpty} is_input=${!message.input.isEmpty} " +
      s"is_bulk=${!message.bulkSize.isEmpty} is_partial=${!message.partialAddress.isEmpty} " +
      s"uprn=${message.uprn} postcode=${message.postcode} input=${message.input} " +
      s"offset=${message.offset} limit=${message.limit} filter=${message.filter} " +
      s"partialAddress=${message.partialAddress} " + s"historical=${message.historical} " +
      s"rangekm=${message.rangekm} lat=${message.lat} lon=${message.lon} " +
      s"bulk_size=${message.bulkSize} batch_size=${message.batchSize} " +
      s"bad_request_message=${message.badRequestMessage} is_not_found=${message.isNotFound} " +
      s"formattedOutput=${message.formattedOutput.replaceAll("""\s""", "_")}" +
      s"numOfResults=${message.numOfResults} score=${message.score} " +
      s"uuid=${message.uuid} networkid=${message.networkid}")
  }

}
