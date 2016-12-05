package uk.gov.ons.addressIndex.crfscala

import uk.gov.ons.addressIndex.crfscala.CrfScala._
import uk.gov.ons.addressIndex.crfscala.jni.{CrfScalaJni, CrfScalaJniImpl}

//TODO scaladoc
trait CrfParser {
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
            input = preprocessedTokens(0),
            next = Some(preprocessedTokens(1))
          )
        )
    } else if(onlyTwoTokens) {
      sb
        .append(
          fas.toCrfJniInput(
            input = preprocessedTokens(0),
            next = Some(preprocessedTokens(1))
          )
        )
        .append(
          fas.toCrfJniInput(
            input = preprocessedTokens(1),
            previous = Some(preprocessedTokens(0))
          )
        )
    } else if (multipleTokens) {
      for((preprocessedToken, i) <- preprocessedTokens.zipWithIndex) {
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
