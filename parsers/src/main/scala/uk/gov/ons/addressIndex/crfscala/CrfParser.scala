package uk.gov.ons.addressIndex.crfscala

import uk.gov.ons.addressIndex.crfscala.CrfScala._

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
          ) .replace("\n", "") //todo remove when aggr impl done
          + "\trawstring.end:1.0\tnext\\:rawstring.start:1.0\n"//todo impl AggregateFeatureAnalysers
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
              ).replace("\n", "") //todo remove when aggr impl done
              + "\trawstring.end:1.0\n"//todo impl AggregateFeatureAnalysers
            )
        }
      }
    }
    sb toString : CrfJniInput
  }
}
