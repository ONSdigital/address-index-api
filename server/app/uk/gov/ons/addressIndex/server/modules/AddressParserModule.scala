package uk.gov.ons.addressIndex.server.modules

import javax.inject.{Inject, Singleton}

import com.google.inject.ImplementedBy
import uk.gov.ons.addressIndex.crfscala.CrfScalaJniImpl
import uk.gov.ons.addressIndex.parsers.Parser

@ImplementedBy(classOf[AddressParserModule])
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

@Singleton
class AddressParserModule @Inject()(conf: ConfigModule) extends ParserModule {

  private val native = new CrfScalaJniImpl

  lazy private val parser = new Parser(native)

  private val currentDirectory = new java.io.File(".").getCanonicalPath
  private val modelPath = s"$currentDirectory/${conf.config.parserLibPath}/addressCRFA.crfsuite"

  // Singleton annotation will ensure that this is called only once
  native.loadModel(modelPath)

  def parse(input: String): Map[String, String] = parser.parse(input)
}