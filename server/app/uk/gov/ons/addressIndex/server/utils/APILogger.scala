package uk.gov.ons.addressIndex.server.utils

import play.api.Logger

trait APILogger {

  def logName: String
  private val logger = Logger(logName)

  def info(message: APILoggerMessage): Unit = logMessage(logger.info, message)
  def info(message: String): Unit = logMessage(logger.info, AddressLoggerMessage(message))

  def trace(message: APILoggerMessage): Unit = logMessage(logger.trace, message)
  def trace(message: String): Unit = logMessage(logger.trace, AddressLoggerMessage(message))

  def debug(message: APILoggerMessage): Unit = logMessage(logger.debug, message)
  def debug(message: String): Unit = logMessage(logger.debug, AddressLoggerMessage(message))

  def warn(message: APILoggerMessage): Unit = logMessage(logger.warn, message)
  def warn(message: String): Unit = logMessage(logger.warn, AddressLoggerMessage(message))

  def error(message: APILoggerMessage): Unit = logMessage(logger.error, message)
  def error(message: String): Unit = logMessage(logger.error, AddressLoggerMessage(message))

  /**
    * @param logType A function taking a 'pass-by-name' parameter of type String returning a Unit.
    * @param message the message to log
    *
    * @return Unit
    */
  protected def logMessage: ((=> String) => Unit, APILoggerMessage) => Unit = (logType, message) => {
    logType(message.message)
  }

}
