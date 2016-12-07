package uk.gov.ons.addressIndex.parsers

import java.io.File

import org.joda.time.DateTime

import scala.xml.XML

object AddressParserMainTest extends App {
  //Before
  //Load the `fatso` c Jni lib.
  val libbackend = new File("parsers/src/main/resources/libbackend.so").getAbsolutePath
  System.load(libbackend)

  val addressInputFile = new File("parsers/src/test/resources/ParserProbabilities.xml")


  val xml = XML.loadFile(addressInputFile)

  val addressStrings = (xml \\ "AddressCollection" \\ "AddressString")

  val inputs = addressStrings map { addressString =>
    (addressString \\ "Input" ) text
  }
  println(s"created inputs")

  println("Making results start: " + DateTime.now.toString())
  val results = inputs.map(i => AddressParser.tag(i, FeatureAnalysers.allFeatures, Tokens))
  val end=  DateTime.now.toString()

  println("meshing results")
  val mesh = inputs.zip(results)

  println("Mesh")
  pprint.pprintln(mesh)

  println("Making results end: " + end)

//
//  xml.child.map { addressString =>
//    addressString.child.filter { part =>
//      part.nameToString(StringBuilder.newBuilder).toString == "Input"
//    } map { input =>
//      val address = input.text
//      val res = AddressParser.tag(address, FeatureAnalysers.allFeatures, Tokens)
//    }
//  }
}