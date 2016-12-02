package uk.gov.ons.addressIndex

import java.io.File

import uk.gov.ons.addressIndex.crfscala.CrfScala._
import uk.gov.ons.addressIndex.parsers.{Feature, FeatureAnalysers, Features}

/*
This whole file should be removed before pushing to dev
 */

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
  val token1 = "wd24"
  val actual = FeatureAnalysers.allFeatures toCrfJniInput token1

  println(actual)

  val items : String = actual.replace("\n", "") + "\tsingleton:1.0\n"

  println(items.replace("\n", "N").replace("\t", "T"))

  val tags = new CrfScalaJniImpl().tag(modelPath, items)

  println(tags)
}
