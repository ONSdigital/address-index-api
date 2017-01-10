package uk.gov.ons.addressIndex.crfscala

import uk.gov.ons.addressIndex.crfscala.CrfScala._

//TODO scaladoc
trait CrfParser {

  //TODO scaladoc
  val tagger: CrfScalaJniImpl = new CrfScalaJniImpl

  //TODO scaladoc
  def tag(input: Input, features: CrfFeatures, tokenable: CrfTokenable): Seq[CrfTokenResult] = {
    //todo optimise file
    val currentDirectory = new java.io.File(".").getCanonicalPath
    val modelPath = s"$currentDirectory/../addressCRFA.crfsuite"
    val actual = parse(input, features, tokenable)
    val augmentedActual = augmentCrfJniInput(actual)

    tagger.loadModel(modelPath)
    val resp = tagger.tag(augmentedActual)
    
    val tokenResults = resp.split(CrfScalaJni.lineEnd)
    val tokens = tokenable(input).toSeq

    tokens.zipWithIndex.map { case (token, i) =>
      val result = tokenResults(i)
      CrfTokenResult(
        value = token,
        label = result.split(": ").head
      )
    }
  }

  def augmentCrfJniInput(crfJniInput: CrfJniInput): CrfJniInput = {
    crfJniInput
      .split(CrfScalaJni.lineEnd)
      .map(
        _.split(CrfScalaJni.delimiter)
          .sorted
          .mkString(CrfScalaJni.delimiter)
      )
      .mkString(CrfScalaJni.lineEnd) + CrfScalaJni.lineEnd
  }

  //TODO scaladoc
  def parse(i: Input, features: CrfFeatures, tokenable: CrfTokenable): CrfJniInput = {
    val tokens = tokenable(i)
    val preprocessedTokens = tokenable normalise tokens
    val onlyOneToken = preprocessedTokens.length == 1
    val onlyTwoTokens = preprocessedTokens.length == 2
    val multipleTokens = preprocessedTokens.length > 2
    val sb = StringBuilder.newBuilder

    if (onlyOneToken) {
      sb
        .append(
          features.toCrfJniInput(
            input = preprocessedTokens(0)
          ).replace("\n", "") //todo remove when aggr impl done
        )
        .append("\tsingleton:1.0\n")//todo impl AggregateFeatureAnalysers
    } else if (onlyTwoTokens) {
      sb
        .append(
          features.toCrfJniInput(
            input = preprocessedTokens(0),
            next = Some(preprocessedTokens(1))
          ).replace("\n", "") //todo remove when aggr impl done
        )
        .append("\trawstring.start:1.0\tnext\\:rawstring.end:1.0\n")//todo impl AggregateFeatureAnalysers
        .append(
          features.toCrfJniInput(
            input = preprocessedTokens(1),
            previous = Some(preprocessedTokens(0))
          ).replace("\n", "") //todo remove when aggr impl done
        )
        .append("\trawstring.end:1.0\tprevious\\:rawstring.start:1.0\n")//todo impl AggregateFeatureAnalysers
    } else if (multipleTokens) {
      for((preprocessedToken, i) <- preprocessedTokens.zipWithIndex) {
        val firstToken = i == 0
        val secondToken = i == 1
        val lastToken = i == preprocessedTokens.length - 1
        val penultimateToken = i == preprocessedTokens.length - 2

        if (firstToken) {
          sb
            .append(
              features.toCrfJniInput(
                input = preprocessedTokens(i),
                next = Some(preprocessedTokens(i + 1))
              ).replace("\n", "") //todo remove when aggr impl done
            )
            .append("\trawstring.start:1.0\n")//todo impl AggregateFeatureAnalysers
        } else if (!lastToken) {
          if (penultimateToken) {
            sb
              .append(
                features.toCrfJniInput(
                  input = preprocessedTokens(i),
                  next = Some(preprocessedTokens(i + 1)),
                  previous = Some(preprocessedTokens(i - 1))
                ).replace("\n", "") //todo remove when aggr impl done
              )
              .append("\tnext\\:rawstring.end:1.0\n")//todo impl AggregateFeatureAnalysers
          } else if (secondToken) {//second needs raw string start previous
            sb
              .append(
                features.toCrfJniInput(
                  input = preprocessedTokens(i),
                  next = Some(preprocessedTokens(i + 1)),
                  previous = Some(preprocessedTokens(i - 1))
                ).replace("\n", "") //todo remove when aggr impl done
              )
              .append("\tprevious\\:rawstring.start:1.0\n") //todo impl AggregateFeatureAnalysers
          } else {
            sb
              .append(
                features.toCrfJniInput(
                  input = preprocessedTokens(i),
                  next = Some(preprocessedTokens(i + 1)),
                  previous = Some(preprocessedTokens(i - 1))
                )
              )
          }
        } else {
          sb
            .append(
              features.toCrfJniInput(
                input = preprocessedTokens(i),
                previous = Some(preprocessedTokens(i - 1))
              ).replace("\n", "") //todo remove when aggr impl done
            )
            .append("\trawstring.end:1.0\n")//todo impl AggregateFeatureAnalysers
        }
      }
    }
    sb toString: CrfJniInput
  }
}
