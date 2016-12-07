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

  val addressStrings = xml \\ "AddressCollection" \\ "AddressString"

  val inputs = addressStrings map { addressString =>
    (addressString \\ "Input" ) text
  }
  val results = inputs.map(i => AddressParser.tag(i, FeatureAnalysers.allFeatures, Tokens))
  val x = inputs.zip(results).map { x =>
    val input = x._1
    val tokens = Tokens(input)
    val preprocessedTokens = Tokens normalise tokens
    
    val tagsWithProbs = x._2
      .split("\n")
      .map { r =>
        val tagAtZeroProbAtOne = r.split(": ")
        tagAtZeroProbAtOne(0) -> tagAtZeroProbAtOne(1)
      }
    val xmlInput = addressStrings(inputs.indexOf(input))

    val results = tagsWithProbs.zipWithIndex map { tagWithProb =>
      val tag = tagWithProb._1._1
      val prob = tagWithProb._1._2
      val index = tagWithProb._2

      val originalToken = preprocessedTokens(index)
      val originalNodes = xmlInput.filter(_ == originalToken)
      val originalNode = originalNodes.head

      val pred_label = originalNode.attribute("pred_label").get.head.text
      val pred_prob = originalNode.attribute("pred_prob").get.head.text

      Helper(
        scalaPredLabel = tag,
        scalaPredProb = prob,
        token = originalToken,
        pyPredLabel = pred_label,
        pyPredProb = pred_prob
      )
    }
    results

  }

  case class Helper(
    scalaPredLabel: String,
    scalaPredProb: String,
    token: String,
    pyPredLabel: String,
    pyPredProb: String
  )
}