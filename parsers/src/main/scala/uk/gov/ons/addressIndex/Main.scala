package uk.gov.ons.addressIndex

import java.io.File

import scala.io.Source

/*
This whole file should be removed before pushing to dev
 */

class CrfScalaJniImpl {
  // @native def modelOpen(location : String) : Boolean
  @native def tag(modelPath: String, items : String) : String
  // @native def modelLabels() : Array[String]
}

object Main extends App{


  val libbackend = new File("parsers/src/main/resources/libbackend.so").getAbsolutePath

  System.load(libbackend)

  val currentDirectory = new java.io.File(".").getCanonicalPath

  val inputPath = s"$currentDirectory/parsers/src/main/resources/testInput.txt"
  val modelPath = s"$currentDirectory/parsers/src/main/resources/addressCRFA.crfsuite"

  val items = Source.fromFile(inputPath).mkString

  val tags = new CrfScalaJniImpl().tag(modelPath, items)

  println(tags)
}
