package uk.gov.ons.addressIndex.server.modules

import javax.inject.Singleton

import com.google.inject.ImplementedBy
import uk.gov.ons.addressIndex.crfscala.CrfScala.CrfTokenResult
import uk.gov.ons.addressIndex.parsers.{AddressParser, Tokens}

@ImplementedBy(classOf[AddressParserModule])
trait ParserModule {
  /**
    * Transforms input string into labeled address modules
    * Uses JNI and a generated (by datascientists) model of CRF (conditional random fields)
    * @param input string to be tokenized
    * @return List of labeled tokens
    */
  def tag(input: String): Seq[CrfTokenResult]

  /**
    * Normalizes input: removes counties, replaces synonyms, uppercase
    * @param input input to be normalized
    * @return normalized input
    */
  def normalizeInput(input: String): String
}

@Singleton
class AddressParserModule extends ParserModule{

  def tag(input: String): Seq[CrfTokenResult] = {
    AddressParser.tag(input)
  }

  def normalizeInput(input: String): String = Tokens.normalizeInput(input)
}