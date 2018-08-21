package uk.gov.ons.addressIndex.server.modules

import javax.inject.{Inject, Singleton}
import uk.gov.ons.addressIndex.crfscala.CrfScalaJniImpl
import uk.gov.ons.addressIndex.parsers.Parser

@Singleton
class AddressParserModule @Inject()(conf: ConfigModule) extends ParserModule {
  lazy private val native = new CrfScalaJniImpl

  lazy private val parser = loadParser()

  private def loadParser(): Parser = {
    val currentDirectory = new java.io.File(".").getCanonicalPath
    val modelPath = s"$currentDirectory/${conf.config.parserLibPath}/addressCRFA.crfsuite"

    // Singleton annotation will ensure that this is called only once
    native.loadModel(modelPath)

    new Parser(native)
  }

  def parse(input: String): Map[String, String] = parser.parse(input)
}