package uk.gov.ons.addressIndex.server.modules

trait ParserModule {
  /**
    * Transforms input string into labeled address modules
    * Uses JNI and a generated (by datascientists) model of CRF (conditional random fields)
    *
    * @param input string to be tokenized
    * @return List of labeled tokens
    */
  def parse(input: String): Map[String, String]
}