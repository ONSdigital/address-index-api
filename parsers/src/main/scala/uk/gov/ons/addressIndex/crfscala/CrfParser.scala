package uk.gov.ons.addressIndex.crfscala

import java.io.File
import uk.gov.ons.addressIndex.crfscala.CrfScala._
import uk.gov.ons.addressIndex.parsers.{AddressParser, FeatureAnalysers, Tokens}

//TODO scaladoc
trait CrfParser {

  /**
    * @param i
    * @param fas
    * @param tokenable
    * @return
    */
  def tag(i: Input, fas: CrfFeatures, tokenable: CrfTokenable): String = {
    val libbackend = new File("parsers/src/main/resources/libbackend.so").getAbsolutePath
    System.load(libbackend)

    val currentDirectory = new java.io.File(".").getCanonicalPath
    val modelPath = s"$currentDirectory/parsers/src/main/resources/addressCRFA.crfsuite"
    val actual = AddressParser.parse(i, FeatureAnalysers.allFeatures, Tokens)

    println("ACTUAL:")
    println(actual)

    val augmentedActual = actual.split("\n").map(_.split("\t").sorted.mkString("\t")).mkString("\n") + "\n"

    println(s"address input string to CrfJniInput(IWA) AUGMENTED::: :\n$augmentedActual ")

    val tagsaug = new CrfScalaJniImpl().tag(modelPath, augmentedActual)
    println(s"aug tags produced :\n$tagsaug")
    tagsaug
  }

  //TODO scaladoc
  def parse(i: Input, fas: CrfFeatures, tokenable: CrfTokenable): CrfJniInput = {
    val tokens = tokenable(i)
    val preprocessedTokens = tokenable normalise tokens
    val onlyOneToken = preprocessedTokens.length == 1
    val onlyTwoTokens = preprocessedTokens.length == 2
    val multipleTokens = preprocessedTokens.length > 2
    val sb = StringBuilder.newBuilder

    if(onlyOneToken) {
      sb
        .append(
          fas.toCrfJniInput(
            input = preprocessedTokens(0)
          ).replace("\n", "") //todo remove when aggr impl done
        )
        .append(//todo impl AggregateFeatureAnalysers
          "\tsingleton:1.0\n"
        )
    } else if(onlyTwoTokens) {
      sb
        .append(
          fas.toCrfJniInput(
            input = preprocessedTokens(0),
            next = Some(preprocessedTokens(1))
          ).replace("\n", "") //todo remove when aggr impl done
          + "\trawstring.start:1.0\tnext\\:rawstring.end:1.0\n"//todo impl AggregateFeatureAnalysers
        )
        .append(
          fas.toCrfJniInput(
            input = preprocessedTokens(1),
            previous = Some(preprocessedTokens(0))
          ).replace("\n", "") //todo remove when aggr impl done
          + "\trawstring.end:1.0\tprevious\\:rawstring.start:1.0\n"//todo impl AggregateFeatureAnalysers
        )
    } else if (multipleTokens) {
      for((preprocessedToken, i) <- preprocessedTokens.zipWithIndex) {
        if(i == 0) {
          sb
            .append(
              fas.toCrfJniInput(
                input = preprocessedTokens(i),
                next = Some(preprocessedTokens(i + 1))
              ).replace("\n", "") //todo remove when aggr impl done
              + "\trawstring.start:1.0\n"//todo impl AggregateFeatureAnalysers
            )
        } else if(i != preprocessedTokens.length - 1) {
          if(i == preprocessedTokens.length - 2) {
            sb
              .append(
                fas.toCrfJniInput(
                  input = preprocessedTokens(i),
                  next = Some(preprocessedTokens(i + 1)),
                  previous = Some(preprocessedTokens(i - 1))
                ).replace("\n", "") //todo remove when aggr impl done
                + "\tnext\\:rawstring.end:1.0\n"//todo impl AggregateFeatureAnalysers
              )
          } else if (i == 1) {//second needs raw string start previous
            sb
              .append(
                fas.toCrfJniInput(
                  input = preprocessedTokens(i),
                  next = Some(preprocessedTokens(i + 1)),
                  previous = Some(preprocessedTokens(i - 1))
                ).replace("\n", "") //todo remove when aggr impl done
                + "\tprevious\\:rawstring.start:1.0\n"//todo impl AggregateFeatureAnalysers
              )
          } else {
            sb
              .append(
                fas.toCrfJniInput(
                  input = preprocessedTokens(i),
                  next = Some(preprocessedTokens(i + 1)),
                  previous = Some(preprocessedTokens(i - 1))
                )
              )
          }
        } else {
          //end of array
          sb
            .append(
              fas.toCrfJniInput(
                input = preprocessedTokens(i),
                previous = Some(preprocessedTokens(i - 1))
              ).replace("\n", "") //todo remove when aggr impl done
              + "\trawstring.end:1.0\n"//todo impl AggregateFeatureAnalysers
            )
        }
      }
    }
    sb toString: CrfJniInput
  }
}
