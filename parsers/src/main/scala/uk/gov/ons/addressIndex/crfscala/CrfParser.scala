package uk.gov.ons.addressIndex.crfscala

import uk.gov.ons.addressIndex.crfscala.CrfScala._
import uk.gov.ons.addressIndex.crfscala.jni.{CrfScalaJni, CrfScalaJniImpl}

//TODO scaladoc
trait CrfParser {
  //TODO scaladoc
  def parse(i: Input, fas: CrfFeatures, tokenable: CrfTokenable): CrfJniInput = {
    val tokens                      = tokenable(i)
    val preprocessedTokens          = tokenable normalise tokens
    val preprocessedTokensWithIndex = preprocessedTokens.zipWithIndex
    val onlyOneToken                = preprocessedTokensWithIndex.length == 1
    val onlyTwoTokens               = preprocessedTokensWithIndex.length == 2
    val multipleTokens              = preprocessedTokensWithIndex.length > 2
    val sb                          = StringBuilder.newBuilder

    if(onlyOneToken) {
      println("only 1")
      sb
        .append(
          fas.toCrfJniInput(
            input = preprocessedTokens(0),
            next = Some(preprocessedTokens(1))
          )
        )
    } else if(onlyTwoTokens) {
      println("only 2")

      val one = fas.toCrfJniInput(
        input = preprocessedTokens(0),
        next = Some(preprocessedTokens(1))
      )
      println(one)

      val two = fas.toCrfJniInput(
        input = preprocessedTokens(0),
        next = Some(preprocessedTokens(1))
      )
      println(two)

      sb
        .append(
          one
        )
        .append(
          two
        )
    } else if (multipleTokens) {
      println("multiple")
      for((preprocessedToken, i) <- preprocessedTokensWithIndex) {
        if(i == 0) {
          sb
            .append(
              fas.toCrfJniInput(
                input = preprocessedTokens(i),
                next = Some(preprocessedTokens(i + 1))
              )
            )
        } else if(i != preprocessedTokens.length - 1) {
          sb
            .append(
              fas.toCrfJniInput(
                input = preprocessedTokens(i),
                next = Some(preprocessedTokens(i + 1)),
                previous = Some(preprocessedTokens(i - 1))
              )
            )
        } else {
          //end of array
          sb
            .append(
              fas.toCrfJniInput(
                input = preprocessedTokens(i),
                previous = Some(preprocessedTokens(i - 1))
              )
            )
        }
      }
    }
    sb toString : CrfJniInput
  }
}
