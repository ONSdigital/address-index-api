package uk.gov.ons.addressIndex.server.utils

/**
  * A generic logger
  *
  * @param logger the name of the logger
  */
class GenericLogger(logger: String) extends APILogger {
  override def logName: String = logger
}

object GenericLogger {

  def apply(name: String): GenericLogger = {
    new GenericLogger(name)
  }

}
