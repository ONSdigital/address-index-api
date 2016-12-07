package uk.gov.ons.addressIndex.parsers

import java.io.File
import uk.gov.ons.addressIndex.crfscala.CrfScalaJni
import scala.util.Try
import scala.xml.XML

//Forgive me father for I have sinned.
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
  val results = inputs.map { i =>
    val result = AddressParser.tag(i, FeatureAnalysers.allFeatures, Tokens)
    result
  }

  val x = inputs.zip(results).map { x =>
    val input = x._1

    println(s"input: $input")

    def augment(actual: String): String = {
      actual
        .split(CrfScalaJni.lineEnd)
        .map(
          _.split(CrfScalaJni.delimiter)
            .sorted
            .mkString(CrfScalaJni.delimiter)
        )
        .mkString(CrfScalaJni.lineEnd) + CrfScalaJni.lineEnd
    }
    println(augment(AddressParser.parse(input, FeatureAnalysers.allFeatures, Tokens)))


    val tokens = Tokens(input)
    val preprocessedTokens = Tokens normalise tokens

    println(s"preprocessedTokens:")
    pprint.pprintln(preprocessedTokens)

    val tagsWithProbs = x._2
      .split("\n")
      .map { r =>
        val tagAtZeroProbAtOne = r.split(": ")
        tagAtZeroProbAtOne(0) -> tagAtZeroProbAtOne(1)
      }

    val xmlInput = addressStrings(inputs.indexOf(input))

    println("test input xml")
    pprint.pprintln(xmlInput)

    println("scala results:")
    pprint.pprintln(tagsWithProbs)

    val results = tagsWithProbs.zipWithIndex map { tagWithProb =>

      val tag = tagWithProb._1._1
      println(s"looking at tag: Scala: $tag")
      val prob = tagWithProb._1._2
      println(s"looking at prob: Scala: $prob")
      val index = tagWithProb._2

      val originalToken = preprocessedTokens(index)
      println(s"looking orginal token: Scala: $originalToken")

      val originalNodes = xmlInput.child.filter(_.text == originalToken)
      pprint.pprintln(originalNodes)

      originalNodes.map { originalNode =>
        Try[String] {

          //      val originalNode = originalNodes.head
          pprint.pprintln(originalNode)

          val pred_label = originalNode.attribute("pred_label").get.head.text
          println(s"pred_label py : ${pred_label}")

          val pred_prob = originalNode.attribute("pred_prob").get.head.text
          println(s"pred_prob py : ${pred_prob}")

          val h = Helper(
            scalaPredLabel = tag,
            scalaPredProb = prob,
            token = originalToken,
            pyPredLabel = pred_label,
            pyPredProb = pred_prob
          )
          assert(h.scalaPredLabel == h.pyPredLabel)

          val augPy = augDoubleStringForCompare(h.pyPredProb)
          val augSc = augDoubleStringForCompare(h.pyPredProb)

          println(s"asserting: ${augSc} to ${augPy}")

          assert(augPy == augSc)

          println
          ""
        }
      }
    }

    pprint.pprintln(results)

    results
  }



  def augDoubleStringForCompare(i: String): String = (i + "000000").substring(0, 6)

  case class Helper(
    scalaPredLabel: String,
    scalaPredProb: String,
    token: String,
    pyPredLabel: String,
    pyPredProb: String
  )
}