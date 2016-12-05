package uk.gov.ons.addressIndex

import java.io.File
import uk.gov.ons.addressIndex.parsers._

class CrfScalaJniImpl {
  // @native def modelOpen(location : String) : Boolean
  @native def tag(modelPath: String, items : String) : String
  // @native def modelLabels() : Array[String]
}

object Main extends App {
  val libbackend = new File("parsers/src/main/resources/libbackend.so").getAbsolutePath
  System.load(libbackend)

  val currentDirectory = new java.io.File(".").getCanonicalPath

//  val inputPath = s"$currentDirectory/parsers/src/main/resources/testInput.txt"
  val modelPath = s"$currentDirectory/parsers/src/main/resources/addressCRFA.crfsuite"

//  val items = Source.fromFile(inputPath).mkString
  val input = "14 Acacia Avenue, Surbiton, SU567AU"
  println(s"address input string :\n$input")

  val actual = AddressParser.parse(input, FeatureAnalysers.allFeatures, Tokens)
  println(s"address input string to CrfJniInput(IWA) :\n$actual ")

  val tags = new CrfScalaJniImpl().tag(modelPath, actual)
  println(s"tags produced :\n$tags")
}
